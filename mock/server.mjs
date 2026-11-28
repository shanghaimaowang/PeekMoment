import { createServer } from 'node:http'
import { randomUUID } from 'node:crypto'
import { readFileSync } from 'node:fs'
import { pathToFileURL } from 'node:url'
import { validate } from '../frontend/miniprogram/services/store.js'

const contract = JSON.parse(readFileSync(new URL('../docs/openapi-week1.json', import.meta.url), 'utf8'))
const example = (path, verb, status = 200) =>
  contract.paths[path][verb].responses[status].content['application/json'].example.data

function fail(status, message) {
  throw Object.assign(new Error(message), { status })
}

async function jsonBody(request) {
  let size = 0
  const chunks = []
  for await (const part of request) {
    size += part.length
    if (size > 64 * 1024) fail(413, '请求体过大')
    chunks.push(part)
  }
  let value
  try { value = JSON.parse(Buffer.concat(chunks).toString('utf8')) }
  catch { fail(400, '请求体须为 JSON 对象') }
  if (!value || typeof value !== 'object' || Array.isArray(value)) fail(400, '请求体须为 JSON 对象')
  return value
}

function paginate(rows, query) {
  function positive(key, fallback, max) {
    const values = query.getAll(key)
    if (!values.length) return fallback
    if (values.length !== 1 || !/^[1-9]\d{0,9}$/.test(values[0]) || Number(values[0]) > max) {
      fail(400, '分页参数无效')
    }
    return Number(values[0])
  }
  const page = positive('page', 1, 2147483647)
  const pageSize = positive('pageSize', 10, 100)
  const start = (page - 1) * pageSize
  return { items: rows.slice(start, start + pageSize), page, pageSize, total: rows.length }
}

function normalizeAttachment(item) {
  const kind = typeof item?.kind === 'string' ? item.kind.trim() : ''
  const src = typeof item?.src === 'string' ? item.src.trim() : ''
  if (!src || src.length > 2048 || /[\s\\]/.test(src) || /%(?![a-f\d]{2})/i.test(src)) fail(400, '附件地址无效')
  if (src.startsWith('/uploads/')) {
    let path
    try { path = decodeURIComponent(src.split(/[?#]/, 1)[0]) } catch { fail(400, '附件路径无效') }
    if (path === '/uploads/' || path.includes('\\') || path.split('/').some(p => p === '.' || p === '..')) {
      fail(400, '附件路径无效')
    }
  } else {
    let url
    try { url = new URL(src) } catch { fail(400, '附件地址无效') }
    if (!/^https?:\/\/[^/]/i.test(src) || !url.hostname || url.username || url.password) fail(400, '附件地址无效')
  }
  return { kind, src }
}

function parseDraft(input) {
  if (Object.keys(input).some(key => !['title','body','tags','media'].includes(key))) fail(400, '动态字段无效')
  const { title = '', body = '', tags = [], media = [] } = input
  if (typeof title !== 'string' || typeof body !== 'string' || !Array.isArray(tags) ||
      tags.some(tag => typeof tag !== 'string') || !Array.isArray(media)) fail(400, '动态字段无效')
  const draft = { title: title.trim(), body: body.trim(), tags: tags.map(tag => tag.trim()), media: media.map(normalizeAttachment) }
  try { validate(draft) } catch (error) { fail(400, error.message) }
  draft.tags = [...new Set(draft.tags)]
  return draft
}

export function createMockServer() {
  const user = example('/api/admin/users', 'get').items[0]
  const moments = [structuredClone(example('/api/moments', 'post', 201))]
  const sessions = new Map()
  return createServer(async (request, response) => {
    // Allow a locally served H5 preview to use the loopback mock.
    const origin = request.headers.origin || ''
    if (/^https?:\/\/(localhost|127\.0\.0\.1)(:\d+)?$/.test(origin)) {
      response.setHeader('Access-Control-Allow-Origin', origin)
      response.setHeader('Vary', 'Origin')
      response.setHeader('Access-Control-Allow-Headers', 'Content-Type, Authorization')
      response.setHeader('Access-Control-Allow-Methods', 'GET, POST, OPTIONS')
    }
    if (request.method === 'OPTIONS') { response.writeHead(204).end(); return }
    const send = (status, data, message = 'ok') => {
      response.writeHead(status, {'Content-Type': 'application/json; charset=utf-8', 'Cache-Control': 'no-store'})
      response.end(JSON.stringify({ code: status < 400 ? 0 : status, message, data }))
    }
    try {
      const { pathname, searchParams } = new URL(request.url, 'http://localhost')
      const method = request.method.toLowerCase()
      const route = contract.paths[pathname]
      if (!route) fail(404, '接口不存在')
      if (!route[method]) fail(405, '请求方法不支持')
      const token = /^Bearer ([^\s]+)$/i.exec(request.headers.authorization || '')?.[1]
      const guard = route[method].security?.[0]
      if (guard?.AdminSession && !(sessions.get(token) > Date.now())) fail(401, '管理员会话无效')
      if (guard?.DemoPublisher && token !== 'mock-publisher') fail(401, '发布身份无效')
      const input = method === 'post' ? await jsonBody(request) : null
      switch (route[method].operationId) {
        case 'getHealth': return send(200, example(pathname, method))
        case 'adminLogin': {
          if (!input.username || !input.password) fail(400, '请输入管理员账号和密码')
          if (input.username !== 'mock-admin' || input.password !== 'mock-password') fail(401, '账号或密码错误')
          const token = randomUUID(), expiresAt = Date.now() + 7200000
          sessions.set(token, expiresAt)
          return send(200, { token, expiresAt, username: input.username })
        }
        case 'publishMoment': {
          const draft = parseDraft(input)
          const saved = { ...example(pathname, method, 201), ...draft, id: String(moments.length + 1), createdAt: Date.now() }
          moments.unshift(saved)
          return send(201, saved)
        }
        case 'listMoments': return send(200, paginate(moments.filter(m => m.status === 'published'), searchParams))
        case 'listAdminUsers':
        case 'listAdminMoments': {
          const keyword = (searchParams.get('keyword') || '').trim().toLowerCase()
          const users = pathname.endsWith('/users')
          const fields = users ? ['id', 'nickname'] : ['id', 'title', 'body', 'author']
          const matches = (users ? [user] : moments).filter(row =>
            fields.some(field => row[field].toLowerCase().includes(keyword)))
          return send(200, paginate(matches, searchParams))
        }
        default: fail(501, 'Mock 尚未实现该操作')
      }
    } catch (error) {
      send(error.status || 500, null, error.status ? error.message : 'Mock 处理失败')
    }
  })
}

if (process.argv[1] && import.meta.url === pathToFileURL(process.argv[1]).href) {
  const port = Number(process.env.PORT || 3100)
  createMockServer().listen(port, '127.0.0.1', () => console.log('PeekMoment Mock    http://127.0.0.1:' + port + '/api/health'))
}
