import { createRequest } from './request.js'
import { createFileTransfer } from './upload.js'

// For device debugging, replace loopback with the address of your development API.
export const apiOrigin = process.env.NODE_ENV === 'development' ? 'http://127.0.0.1:8080' : ''
let publisherToken = ''
const request = createRequest(apiOrigin, () => publisherToken)
// Set a same-origin multipart route once the upload service is available.
const uploadPath = ''
export const transferToServer = apiOrigin && uploadPath.startsWith('/') && !uploadPath.startsWith('//')
  ? createFileTransfer(apiOrigin + uploadPath, () => publisherToken) : null

export function setPublisherToken(value = '') { publisherToken = value.trim() }
export function canPublish() { return Boolean(apiOrigin && publisherToken) }

async function call(options) {
  if (!apiOrigin) throw new Error('请从开发构建打开联调广场')
  const response = await request(options)
  if (response?.code !== 0) throw new Error(response?.message || '接口响应格式错误')
  return response.data
}

export async function listMoments(page) {
  const result = await call({ path: '/api/moments?page=' + page + '&pageSize=10' })
  return {
    ...result,
    items: result.items.map(moment => ({
      ...moment,
      media: moment.media.map(file => ({
        ...file, src: file.src.startsWith('/uploads/') ? apiOrigin + file.src : file.src
      }))
    }))
  }
}

export function publishMoment(draft) {
  if (!canPublish()) throw new Error('请先填写联调发布令牌')
  return call({ path: '/api/moments', method: 'POST', data: draft })
}
