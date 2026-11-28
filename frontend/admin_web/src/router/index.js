import { createRouter, createWebHistory } from 'vue-router'
import AdminLayout from '../layouts/AdminLayout.vue'

// 还没开发的页面统一用占位组件
const Placeholder = () => import('../views/Placeholder.vue')

const routes = [
  // 登录页：不在布局内
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { public: true, title: '登录' },
  },

  // 后台：布局 + 所有菜单页
  {
    path: '/',
    component: AdminLayout,
    redirect: '/home',
    children: [
      {
        path: '/home',
        name: 'Home',
        component: () => import('../views/Home.vue'),
        meta: { title: '首页' },
      },
      {
        path: '/dashboard',
        name: 'Dashboard',
        component: Placeholder,
        meta: { title: '数据看板' },
      },

      // 内容管理
      {
        path: '/content/moment',
        name: 'ContentMoment',
        component: Placeholder,
        meta: { title: '动态管理' },
      },
      {
        path: '/content/comment',
        name: 'ContentComment',
        component: Placeholder,
        meta: { title: '评论管理' },
      },
      {
        path: '/content/tags',
        name: 'ContentTags',
        component: Placeholder,
        meta: { title: '标签话题' },
      },

      // 审核中心
      {
        path: '/moderation/queue',
        name: 'ModerationQueue',
        component: Placeholder,
        meta: { title: '待审核队列' },
      },
      {
        path: '/moderation/reports',
        name: 'ModerationReports',
        component: Placeholder,
        meta: { title: '举报处理' },
      },
      {
        path: '/moderation/sensitive-words',
        name: 'ModerationSensitiveWords',
        component: Placeholder,
        meta: { title: '敏感词库' },
      },

      // 用户管理
      {
        path: '/users',
        name: 'UserList',
        component: Placeholder,
        meta: { title: '用户列表' },
      },
      {
        path: '/users/friends',
        name: 'UserFriends',
        component: Placeholder,
        meta: { title: '好友关系' },
      },
    ],
  },

  // 访问不存在的地址回首页
  { path: '/:pathMatch(.*)*', redirect: '/home' },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

const TOKEN_KEY = 'admin_token'

// 路由守卫：没登录 → 登录页；已登录去登录页 → 首页
router.beforeEach((to) => {
  const token = localStorage.getItem(TOKEN_KEY)

  if (to.meta.public) {
    return token ? '/home' : true
  }

  if (!token) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }

  return true
})

// 浏览器标签标题
router.afterEach((to) => {
  document.title = to.meta.title
    ? `${to.meta.title} - PeekMoment 后台`
    : 'PeekMoment 后台'
})

export default router
