import assert from 'node:assert/strict'
import test from 'node:test'
import { startTestMock } from './test-http.mjs'

test('小程序联调客户端通过 HTTP Mock 发布并重新分页读取动态', async t => {
  const send = await startTestMock(t)
  const previousMode = process.env.NODE_ENV
  process.env.NODE_ENV = 'development'
  let client
  try {
    client = await import('../frontend/miniprogram/services/development.js')
  } finally {
    if (previousMode === undefined) delete process.env.NODE_ENV
    else process.env.NODE_ENV = previousMode
  }
  const previousUni = globalThis.uni
  t.after(() => {
    client.setPublisherToken()
    if (previousUni === undefined) delete globalThis.uni
    else globalThis.uni = previousUni
  })
  // Only replace the device's HTTP primitive. Keep the actual client, headers,
  // request body, routes, envelope handling and Mock server in the round trip.
  globalThis.uni = { request(options) {
    const url = new URL(options.url)
    send({
      method: options.method, path: url.pathname + url.search,
      headers: { 'Content-Type': 'application/json', ...options.header },
      body: options.data === undefined ? undefined : JSON.stringify(options.data)
    }).then(res => options.success({ statusCode: res.status, data: res.json }), options.fail)
  }}
  const before = await client.listMoments(1)
  const draft = {title:'联调', body:'从小程序发布', tags:['校园'], media:[]}
  client.setPublisherToken('invalid')
  await assert.rejects(client.publishMoment(draft), error => error.statusCode === 401)
  client.setPublisherToken('mock-publisher')
  const posted = await client.publishMoment(draft)
  client.setPublisherToken()
  const after = await client.listMoments(1)
  assert.equal(after.pageSize, 10)
  assert.equal(after.total, before.total + 1)
  assert.deepEqual(after.items[0], posted)
  assert.equal(posted.body, draft.body)
  assert.equal(posted.status, 'published')
})

test('联调用户发布的动态能从公开列表和管理员查询取回', async t => {
  const send = await startTestMock(t)
  async function call(path, { body, token, status = 200 } = {}) {
    const res = await send({
      path: '/api' + path,
      method: body ? 'POST' : 'GET',
      headers: { 'Content-Type': 'application/json', ...(token ? {Authorization: 'Bearer ' + token} : {}) },
      body: body ? JSON.stringify(body) : undefined
    })
    const json = res.json
    assert.equal(res.status, status, JSON.stringify(json))
    assert.equal(json.code, status < 400 ? 0 : status)
    return json.data
  }
  assert.equal((await call('/health')).status, 'ok')
  await call('/admin/users', { status: 401 })
  await call('/admin/login', { body: {username:'mock-admin', password:'wrong'}, status:401 })
  const { token } = await call('/admin/login', { body: { username: 'mock-admin', password: 'mock-password' } })
  assert.equal((await send({ path:'/api/admin/users', method:'GET', headers:{ Authorization:token } })).status,401)
  const body = { body: '联调用例', tags: ['校园','校园'] }
  await call('/moments', {body, token, status:401})
  const saved = await call('/moments', {body, token:'mock-publisher', status:201})
  assert.deepEqual(saved.tags, ['校园'])
  assert.deepEqual((await call('/moments?pageSize=1')).items, [saved])
  assert.deepEqual((await call('/admin/moments?keyword='+encodeURIComponent('联调'),{token})).items, [saved])
  assert.equal((await call('/admin/users?keyword=xiaoman',{token})).total,1)
  await call('/moments?pageSize=0',{status:400})
  await call('/moments',{body:{userId:'someone'},token:'mock-publisher',status:400})
  for (const src of ['blob:camera', '/uploads/../draft.jpg', 'https://user@example.org/photo.jpg']) {
    await call('/moments', {body:{media:[{kind:'image', src}]}, token:'mock-publisher', status:400})
  }
})
