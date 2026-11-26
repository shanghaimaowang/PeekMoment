const time = (day, hour, minute) => new Date(2026, 10, day, hour, minute).getTime()
export const demoUser = { id: 'demo', name: '小满' }
export const topics = ['校园', '随拍', '生活', '旅行']
export const seedPosts = [
  {
    id: 'roof', userId: 'yuan', author: '阿原',
    title: '', body: '爬了半天台阶，到这儿一回头，才看见后面还有个湖。',
    tags: ['旅行', '随拍'], media: [{kind: 'image', src: '/static/china.jpg'}],
    createdAt: time(26, 14, 32), ratings: {lin: 5, tang: 4, jiu: 5},
    comments: [{id: 'c1', userId: 'lin', author: '林栀', body: '这个角度能看到湖诶', at: time(26,14,40)}]
  },
  {
    id: 'library', userId: 'demo', author: '小满',
    title: '', body: '有人在致理楼捡到一把深蓝色的伞吗？下午下课的时候才发现没拿。伞柄上挂了只小鸭子。',
    tags: ['校园'], media: [], createdAt: time(26, 12, 10), ratings: {}, comments: []
  },
  {
    id: 'bloom', userId: 'lin', author: '林栀',
    title: '', body: '路边这朵花，放大看才发现花瓣排得这么整齐。',
    tags: ['随拍', '生活'], media: [{kind: 'image', src: '/static/flower.jpg'}],
    createdAt: time(25, 17, 8), ratings: {yuan: 5, jiu: 5}, comments: []
  }
]
