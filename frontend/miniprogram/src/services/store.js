import { seedPosts, demoUser } from './seed.js'
const KEY = 'peekmoment.local.v1'
const clone = value => JSON.parse(JSON.stringify(value))

function read() {
  const state = uni.getStorageSync(KEY)
  if (state === '' || state == null) return { user: null, posts: clone(seedPosts) }
  if (!Array.isArray(state.posts)) throw new Error('本地数据无法读取')
  return state
}
function update(fn) {
  const state = read()
  const result = fn(state)
  uni.setStorageSync(KEY, state)
  return result
}
const uid = () => Date.now().toString(36) + '-' + Math.random().toString(36).slice(2, 10)
export const user = () => read().user
export const login = () => update(s => { s.user = { ...demoUser } })
export const logout = () => update(s => { s.user = null })
function member(s) {
  if (!s.user) throw new Error('请先登录')
  return s.user
}
function find(s, id) {
  const p = s.posts.find(item => item.id === id)
  if (!p) throw new Error('这条动态已被删除')
  return p
}
export const post = id => read().posts.find(p => p.id === id)
export function localDate(value) {
  const d = new Date(value)
  return [d.getFullYear(), String(d.getMonth()+1).padStart(2,'0'), String(d.getDate()).padStart(2,'0')].join('-')
}
export function dateLabel(value) {
  const d = new Date(value)
  return (d.getMonth()+1) + '月' + d.getDate() + '日'
}
export function clock(value) {
  const d = new Date(value)
  return String(d.getHours()).padStart(2,'0') + ':' + String(d.getMinutes()).padStart(2,'0')
}
export function rating(p) {
  const values = Object.values(p.ratings)
  return values.length ? (values.reduce((a,b) => a+b,0) / values.length).toFixed(1) : '—'
}
export function filterPosts(posts, {topic = '', query = '', day = ''} = {}) {
  const needle = query.trim().toLowerCase()
  return posts.filter(p => (!topic || p.tags.includes(topic)) &&
    (!day || localDate(p.createdAt) === day) &&
    (!needle || [p.title, p.body, p.author, ...p.tags].some(v => v.toLowerCase().includes(needle)))
  ).sort((a,b) => b.createdAt - a.createdAt)
}
export function list({mine = false, ...filters} = {}) {
  const state = read()
  const posts = mine ? state.posts.filter(p => p.userId === state.user?.id) : state.posts
  return filterPosts(posts, filters)
}
export function validate({ title, body, tags, media }) {
  if (!title.trim() && !body.trim() && !media.length) throw new Error('写点内容，或选一张照片、一段视频')
  if (title.trim().length > 40 || body.trim().length > 1000) throw new Error('标题或正文过长')
  if (tags.length > 5 || tags.some(t => !t.trim() || t.length > 12)) throw new Error('最多 5 个标签，每个不超过 12 字')
  if (media.some(m => !['image','video'].includes(m.kind) || !m.src) || media.length > 9 ||
      (media.some(m => m.kind === 'video') && media.length !== 1)) throw new Error('可选 9 张图片或 1 段视频')
}
export function savePost(id, draft) {
  validate(draft)
  return update(s => {
    const u = member(s)
    const p = id ? find(s, id) : { id: uid(), userId: u.id, author: u.name,
      createdAt: Date.now(), ratings: {}, comments: [] }
    if (p.userId !== u.id) throw new Error('只能编辑自己的动态')
    Object.assign(p, {title: draft.title.trim(), body: draft.body.trim(), tags: [...new Set(draft.tags)], media: clone(draft.media)})
    if (!id) s.posts.unshift(p)
    return p.id
  })
}
export function removePost(id) {
  return update(s => {
    const u = member(s), p = find(s, id)
    if (p.userId !== u.id) throw new Error('只能删除自己的动态')
    s.posts = s.posts.filter(item => item.id !== id)
    return p.media
  })
}
export function rate(id, value) {
  if (!Number.isInteger(value) || value < 1 || value > 5) throw new Error('请选择 1 至 5 星')
  update(s => { const u = member(s); find(s,id).ratings[u.id] = value })
}
export function comment(id, body) {
  const text = body.trim()
  if (!text || text.length > 200) throw new Error('评论需为 1 至 200 字')
  update(s => { const u = member(s); find(s,id).comments.push({id: uid(), userId: u.id, author: u.name, body: text, at: Date.now()}) })
}
