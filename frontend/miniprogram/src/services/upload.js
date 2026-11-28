// The picker supplies temporary paths. The transfer adapter returns a reference
// suitable for saving in a post (a local file key in the week-one demo).
import { persist } from './media.js'

const original = src => ({ src, release() {} })
const wait = ms => new Promise(resolve => setTimeout(resolve, ms))

export function createFileTransfer(url, readToken) {
  return (src, kind) => new Promise((resolve, reject) => {
    const token = readToken()
    uni.uploadFile({
      url, filePath: src, name: 'file', formData: { kind },
      header: token ? { Authorization: 'Bearer ' + token } : {}, timeout: 30000,
      success: result => {
        let envelope
        try { envelope = typeof result.data === 'string' ? JSON.parse(result.data) : result.data }
        catch { /* Status and fallback message cover invalid responses. */ }
        const reference = envelope?.data?.url
        if (result.statusCode >= 200 && result.statusCode < 300 && envelope?.code === 0 &&
            typeof reference === 'string' && /^(https?:\/\/|\/uploads\/)/i.test(reference)) {
          resolve(reference)
        } else {
          const error = new Error(envelope?.message || '上传失败（' + result.statusCode + '）')
          error.retryable = result.statusCode === 408 || result.statusCode === 429 || result.statusCode >= 500
          reject(error)
        }
      },
      fail: reject
    })
  })
}

export function chooseImages(count) {
  return new Promise((resolve, reject) => uni.chooseImage({
    count, sizeType: ['original'], sourceType: ['album', 'camera'],
    success: result => resolve(result.tempFilePaths.slice(0, count)),
    fail: reject
  }))
}

export async function compressPhoto(src) {
  // #ifdef H5
  const response = await fetch(src)
  if (!response.ok) throw new Error('无法读取所选照片')
  const blob = await response.blob()
  // Preserve animated GIFs and formats which the browser cannot re-encode.
  if (!['image/jpeg', 'image/png', 'image/webp'].includes(blob.type)) return original(src)
  const image = new Image()
  await new Promise((resolve, reject) => {
    image.onload = resolve
    image.onerror = () => reject(new Error('无法读取照片尺寸'))
    image.src = src
  })
  const scale = Math.min(1, 1600 / Math.max(image.naturalWidth, image.naturalHeight))
  const canvas = document.createElement('canvas')
  canvas.width = Math.max(1, Math.round(image.naturalWidth * scale))
  canvas.height = Math.max(1, Math.round(image.naturalHeight * scale))
  const context = canvas.getContext('2d')
  if (!context) return original(src)
  context.drawImage(image, 0, 0, canvas.width, canvas.height)
  const smaller = await new Promise(resolve => canvas.toBlob(resolve, blob.type, 0.78))
  if (!smaller || smaller.size >= blob.size) return original(src)
  const url = URL.createObjectURL(smaller)
  return { src: url, release: () => URL.revokeObjectURL(url) }
  // #endif
  // #ifdef MP-WEIXIN
  return new Promise((resolve, reject) => uni.compressImage({
    src, quality: 78,
    success: result => resolve(original(result.tempFilePath)),
    fail: reject
  }))
  // #endif
}

export async function prepareAttachments(items, {
  compress = compressPhoto, transfer = persist, onState = () => {},
  onPrepared = () => {}, active = () => true, delay = wait
} = {}) {
  const files = new Array(items.length)
  const failures = []
  let next = 0

  async function worker() {
    while (active() && next < items.length) {
      const index = next++
      const item = items[index]
      if (!item.fresh || item.prepared) {
        files[index] = { kind: item.kind, src: item.prepared || item.src }
        continue
      }
      let source
      try {
        onState(index, '准备中')
        source = item.kind === 'image' ? await compress(item.src) : original(item.src)
        for (let attempt = 0; attempt < 3 && active(); attempt++) {
          try {
            onState(index, attempt ? '重试中 ' + attempt + '/2' : '传输中')
            const saved = await transfer(source.src, item.kind)
            onPrepared(index, saved)
            files[index] = { kind: item.kind, src: saved }
            onState(index, '已就绪')
            break
          } catch (error) {
            if (attempt === 2 || error?.retryable === false) throw error
            await delay(250 * (attempt + 1))
          }
        }
      } catch (error) {
        failures.push({ index, error })
        onState(index, '待重试')
      } finally {
        source?.release()
      }
    }
  }

  await Promise.all(Array.from({length: Math.min(3, items.length)}, worker))
  if (!active()) throw new Error('已离开编辑器')
  if (failures.length) {
    const { index, error } = failures.sort((a,b) => a.index - b.index)[0]
    throw new Error('第 ' + (index + 1) + ' 个附件处理失败，可再次提交。' + (error?.message || error?.errMsg || ''))
  }
  return files
}
