import { randomUUID } from 'node:crypto'
import { once } from 'node:events'
import { request } from 'node:http'
import { tmpdir } from 'node:os'
import { join } from 'node:path'
import { createMockServer } from './server.mjs'

export async function startTestMock(t) {
  const name = 'peekmoment-test-' + randomUUID()
  const socketPath = process.platform === 'win32' ? '\\\\.\\pipe\\' + name : join(tmpdir(), name + '.sock')
  const server = createMockServer().listen(socketPath)
  await once(server, 'listening')
  t.after(() => new Promise(resolve => {
    server.close(resolve)
    server.closeAllConnections()
  }))
  return async function send({ method, path, headers, body }) {
    const res = await new Promise((resolve, reject) => {
      const req = request({ socketPath, method, path, headers, signal: AbortSignal.timeout(5000) }, resolve)
      req.on('error', reject)
      req.end(body)
    })
    const chunks = []
    for await (const chunk of res) chunks.push(chunk)
    return { status: res.statusCode, json: JSON.parse(Buffer.concat(chunks).toString('utf8')) }
  }
}
