// 微信保存到本机文件目录；H5 使用 IndexedDB 保存 Blob，刷新后重建对象 URL。
const cache = new Map()
let database
function db() {
  if (!database) database = new Promise((resolve, reject) => {
    const req = indexedDB.open('peekmoment-media-v1', 1)
    req.onupgradeneeded = () => req.result.createObjectStore('files')
    req.onsuccess = () => resolve(req.result)
    req.onerror = () => reject(req.error)
  }).catch(e => { database = null; throw e })
  return database
}
async function access(mode, action) {
  const database = await db()
  return new Promise((resolve, reject) => {
    const tx = database.transaction('files', mode)
    const req = action(tx.objectStore('files'))
    tx.oncomplete = () => resolve(req.result)
    tx.onabort = tx.onerror = () => reject(tx.error || new Error('媒体存储失败'))
  })
}
export async function persist(src) {
  // #ifdef H5
  const res = await fetch(src)
  if (!res.ok) throw new Error('无法读取所选媒体')
  const blob = await res.blob()
  const key = 'idb:' + Date.now() + '-' + Math.random().toString(36).slice(2)
  await access('readwrite', store => store.put(blob, key))
  return key
  // #endif
  // #ifdef MP-WEIXIN
  return new Promise((resolve,reject) => {
    wx.getFileSystemManager().saveFile({tempFilePath: src, success: r => resolve(r.savedFilePath), fail: reject})
  })
  // #endif
}
export async function resolveMedia(src) {
  if (!src.startsWith('idb:')) return src
  if (cache.has(src)) return cache.get(src)
  const blob = await access('readonly', store => store.get(src))
  if (!blob) throw new Error('本地媒体不存在')
  if (!cache.has(src)) cache.set(src, URL.createObjectURL(blob))
  return cache.get(src)
}
export async function discard(src) {
  if (!src || src.startsWith('/static/')) return
  // #ifdef H5
  if (src.startsWith('idb:')) {
    await access('readwrite', store => store.delete(src))
    if (cache.has(src)) URL.revokeObjectURL(cache.get(src))
    cache.delete(src)
  }
  // #endif
  // #ifdef MP-WEIXIN
  await new Promise((resolve, reject) => wx.getFileSystemManager().removeSavedFile({filePath:src,success:resolve,fail:reject}))
  // #endif
}
export const cleanup = paths => Promise.all(paths.map(src => discard(src).catch(() => {})))
