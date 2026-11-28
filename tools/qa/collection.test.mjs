import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import { test } from 'node:test'
import { runInNewContext } from 'node:vm'
import { startTestMock } from '../mock/test-http.mjs'

const collection = JSON.parse(readFileSync(new URL('./peekmoment-week1.postman_collection.json', import.meta.url), 'utf8'))

test('Apifox collection executes in order against a fresh mock', async t => {
  const send = await startTestMock(t)
  const environment = new Map([
    ['baseUrl', 'http://localhost'],
    ['adminUsername', 'mock-admin'],
    ['adminPassword', 'mock-password'],
    ['demoToken', 'mock-publisher']
  ])

  for (const item of collection.item) {
    await t.test(item.name, async () => {
      const requestVars = new Map()
      const pm = {
        environment: {
          set: (key, value) => environment.set(key, value),
          unset: key => environment.delete(key)
        },
        variables: {
          get: key => requestVars.has(key) ? requestVars.get(key) : environment.get(key),
          set: (key, value) => requestVars.set(key, value)
        },
        test: (name, check) => assert.doesNotThrow(check, name)
      }
      function scripts(phase) {
        for (const event of item.event.filter(event => event.listen === phase)) {
          runInNewContext(event.script.exec.join('\n'), { pm }, { timeout: 2000 })
        }
      }
      function substitute(text) {
        return text.replace(/\{\{(\w+)\}\}/g, (_, key) => {
          assert.notEqual(pm.variables.get(key), undefined, 'Missing collection variable ' + key)
          return pm.variables.get(key)
        })
      }
      scripts('prerequest')
      const request = item.request
      const url = new URL(substitute(request.url.raw))
      const response = await send({
        path: url.pathname + url.search,
        method: request.method,
        headers: Object.fromEntries(request.header.map(({ key, value }) => [key, substitute(value)])),
        body: request.body && substitute(request.body.raw)
      })
      pm.response = { code: response.status, json: () => response.json }
      scripts('test')
    })
  }
})
