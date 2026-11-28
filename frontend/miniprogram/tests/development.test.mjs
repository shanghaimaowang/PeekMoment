import { test } from 'node:test'
import assert from 'node:assert/strict'

process.env.NODE_ENV = 'development'
const { apiOrigin, listMoments, publishMoment, setPublisherToken, canPublish } =
  await import('../src/services/development.js')

test('development timeline uses server pagination and resolves relative media references', async () => {
  globalThis.uni = { request(options) {
    assert.equal(options.url, apiOrigin + '/api/moments?page=2&pageSize=10')
    assert.equal(options.method, 'GET')
    assert.deepEqual(options.header, {})
    options.success({statusCode:200, data:{code:0, message:'ok', data:{
      page:2, pageSize:10, total:11,
      items:[{id:'11', media:[{kind:'image', src:'/uploads/test.jpg'}]}]
    }}})
  }}
  const page = await listMoments(2)
  assert.equal(page.items[0].media[0].src, apiOrigin + '/uploads/test.jpg')
  assert.equal(page.total, 11)
})

test('development publish sends the current in-memory credential and surfaces server messages', async () => {
  const draft = { title:'', body:'联调', tags:['校园'], media:[] }
  let allowed = false
  globalThis.uni = { request(options) {
    assert.equal(options.url, apiOrigin + '/api/moments')
    assert.equal(options.method, 'POST')
    assert.equal(options.header.Authorization, 'Bearer development-token')
    assert.deepEqual(options.data, draft)
    if (allowed) options.success({statusCode:201, data:{code:0, message:'ok', data:{id:'abc'}}})
    else options.success({statusCode:401, data:{code:401, message:'开发令牌无效', data:null}})
  }}
  setPublisherToken(' development-token ')
  try {
    await assert.rejects(publishMoment(draft), /开发令牌无效/)
    allowed = true
    assert.equal((await publishMoment(draft)).id, 'abc')
  } finally { setPublisherToken() }
  assert.equal(canPublish(), false)
  assert.throws(() => publishMoment(draft), /令牌/)
})
