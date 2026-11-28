import test from 'node:test'
import assert from 'node:assert/strict'
import { createFileTransfer, prepareAttachments } from '../src/services/upload.js'

const fresh = (src, kind = 'image') => ({ kind, src, fresh: true })
const intercept = () => {
  const compressed = [], released = []
  return {
    compressed, released,
    compress: async src => {
      compressed.push(src)
      return { src: 'compressed:' + src, release: () => released.push(src) }
    }
  }
}
const sleep = ms => new Promise(resolve => setTimeout(resolve, ms))

test('multipart transport sends a file, classifies HTTP failures and accepts durable media references', async () => {
  const responses = [
    {statusCode:503,data:'unavailable'},
    {statusCode:413,data:JSON.stringify({message:'文件过大'})},
    {statusCode:200,data:JSON.stringify({code:0,data:{url:'file:///temp/photo'}})},
    {statusCode:201,data:JSON.stringify({code:0,data:{url:'/uploads/photo.jpg'}})}
  ]
  const transfer = createFileTransfer('https://example.test/upload', () => 'token')
  globalThis.uni = { uploadFile(options) {
    assert.equal(options.url, 'https://example.test/upload')
    assert.equal(options.filePath, '/tmp/photo')
    assert.equal(options.name, 'file')
    assert.equal(options.header.Authorization, 'Bearer token')
    assert.deepEqual(options.formData, {kind:'image'})
    options.success(responses.shift())
  }}
  await assert.rejects(transfer('/tmp/photo','image'), e => e.retryable === true)
  await assert.rejects(transfer('/tmp/photo','image'), e => !e.retryable && e.message === '文件过大')
  await assert.rejects(transfer('/tmp/photo','image'), e => !e.retryable)
  assert.equal(await transfer('/tmp/photo','image'), '/uploads/photo.jpg')
})

test('runs up to three transfers and returns the original image order', async () => {
  const pictures = ['a','b','c','d','e','f','g','h','i'].map(src => fresh(src))
  const codec = intercept()
  let running = 0, peak = 0
  const result = await prepareAttachments(pictures, {
    compress: codec.compress,
    transfer: async src => {
      peak = Math.max(peak, ++running)
      await sleep(src.endsWith('a') ? 15 : 1)
      running--
      return 'saved:' + src
    }
  })
  assert.equal(peak, 3)
  assert.deepEqual(codec.compressed, pictures.map(item => item.src))
  assert.deepEqual(codec.released.sort(), pictures.map(item => item.src))
  assert.deepEqual(result.map(item => item.src), pictures.map(item => 'saved:compressed:' + item.src))
})

test('retries a transient transfer twice and skips image compression for video', async () => {
  const codec = intercept(), delays = []
  let calls = 0
  const result = await prepareAttachments([fresh('clip', 'video')], {
    compress: codec.compress, delay: async ms => { delays.push(ms) },
    transfer: async () => {
      if (++calls < 3) throw new Error('暂时失败')
      return 'saved-video'
    }
  })
  assert.deepEqual(result, [{kind:'video', src:'saved-video'}])
  assert.deepEqual(codec.compressed, [])
  assert.deepEqual(delays, [250,500])
})

test('retains prepared and preexisting files when manually retrying an exhausted item', async () => {
  const files = [{ kind: 'image', src: 'existing' }, fresh('a'), fresh('b')]
  const calls = [], codec = intercept()
  let fail = true
  const options = {
    compress: codec.compress, delay: async () => {},
    onPrepared: (i, src) => { files[i].prepared = src },
    transfer: async src => {
      calls.push(src)
      if (src.endsWith('b') && fail) throw new Error('暂时失败')
      return 'saved:' + src
    }
  }
  await assert.rejects(prepareAttachments(files, options), /第 3 个附件/)
  fail = false
  const result = await prepareAttachments(files, options)
  assert.equal(calls.filter(src => src.endsWith('a')).length, 1)
  assert.equal(calls.filter(src => src.endsWith('b')).length, 4)
  assert.deepEqual(result.map(item => item.src), ['existing','saved:compressed:a','saved:compressed:b'])
})

test('a permanent transfer failure releases the compressed source without retrying', async () => {
  const codec = intercept()
  let calls = 0
  await assert.rejects(prepareAttachments([fresh('a')], {
    compress: codec.compress,
    transfer: async () => { calls++; throw Object.assign(new Error('文件超限'), {retryable: false}) }
  }), /文件超限/)
  assert.equal(calls, 1)
  assert.deepEqual(codec.released, ['a'])
})

test('leaving the editor stops dispatching files while accounting for in-flight completions', async () => {
  let active = true
  const prepared = []
  await assert.rejects(prepareAttachments(['a','b','c','d'].map(src => fresh(src)), {
    compress: intercept().compress, active: () => active,
    transfer: async src => { await sleep(1); active = false; return src },
    onPrepared: (i, src) => prepared.push(src)
  }), /离开编辑器/)
  assert.deepEqual(prepared.sort(), ['compressed:a','compressed:b','compressed:c'])
})
