import assert from 'node:assert/strict'
import { spawnSync } from 'node:child_process'
import { cpSync, mkdirSync, mkdtempSync, readFileSync, rmSync, statSync, writeFileSync } from 'node:fs'
import { tmpdir } from 'node:os'
import { join } from 'node:path'
import { fileURLToPath } from 'node:url'

// Official stable Vue 3 Vite preset for the WeChat target.
// https://github.com/dcloudio/uni-preset-vue/tree/189dfe2e7319b01122efade86f4a0e56b1f718dc
const uniVersion = '3.0.0-4080420251103001'
const buildProject = {
  name: 'peekmoment-weixin-compile-check',
  private: true,
  scripts: { build: 'uni build -p mp-weixin' },
  dependencies: {
    '@dcloudio/uni-app': uniVersion,
    '@dcloudio/uni-components': uniVersion,
    '@dcloudio/uni-h5': uniVersion,
    '@dcloudio/uni-mp-weixin': uniVersion,
    '@dcloudio/uni-cli-shared': uniVersion,
    '@dcloudio/vite-plugin-uni': uniVersion,
    '@vue/runtime-core': '3.4.21',
    vue: '3.4.21',
    'vue-i18n': '9.14.5',
    vite: '5.2.8'
  }
}

const source = fileURLToPath(new URL('../../frontend/miniprogram/', import.meta.url))
const directory = mkdtempSync(join(tmpdir(), 'peekmoment-weixin-'))
function npm(args) {
  // Windows installs npm.cmd. Only fixed arguments below are passed to cmd.
  const result = spawnSync('npm', args, {
    cwd: directory, stdio: 'inherit', windowsHide: true, shell: process.platform === 'win32'
  })
  if (result.error) throw result.error
  if (result.status !== 0) throw new Error('npm ' + args[0] + ' failed (' + (result.status ?? result.signal) + ')')
}

try {
  writeFileSync(join(directory, 'package.json'), JSON.stringify(buildProject))
  writeFileSync(join(directory, 'vite.config.js'), [
    "import { defineConfig } from 'vite'",
    "import uni from '@dcloudio/vite-plugin-uni'",
    'export default defineConfig({ plugins: [uni()] })'
  ].join('\n'))
  const src = join(directory, 'src')
  const appSource = join(source, 'src')
  mkdirSync(src)
  cpSync(join(source, 'index.html'), join(directory, 'index.html'))
  for (const entry of ['App.vue','main.js','manifest.json','pages.json','uni.scss','components','pages','services','static']) {
    cpSync(join(appSource, entry), join(src, entry), {recursive: true})
  }
  // Resolve nested semver ranges against the same publication cutoff on each run.
  npm(['install','--before=2025-12-01T00:00:00Z','--no-audit','--no-fund'])
  npm(['run','build'])

  const output = join(directory, 'dist/build/mp-weixin')
  const routes = JSON.parse(readFileSync(join(src, 'pages.json'), 'utf8')).pages.map(page => page.path)
  const manifest = JSON.parse(readFileSync(join(output, 'app.json'), 'utf8'))
  assert.deepEqual(manifest.pages, routes)
  for (const file of ['app.js', ...routes.flatMap(route => [route + '.wxml', route + '.js'])]) {
    assert.ok(statSync(join(output, file)).size > 0, file + ' is empty')
  }
  console.log('Verified compiled WeChat app manifest, scripts and templates for ' + routes.length + ' pages.')
} finally {
  rmSync(directory, { recursive: true, force: true, maxRetries: 3 })
}
