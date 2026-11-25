import { createRouter, createWebHistory } from 'vue-router'
//引入页面组件
import Login from '../views/Login.vue'
import MomentList from '../views/MomentList.vue'

// 路由表
const routes = [
  {
    path: '/login',    
    name: 'Login',
    component: Login  
  },
  {
    path: '/moment',
    name: 'MomentList',
    component: MomentList
  },

  // 默认访问根路径
  {
    path: '/',
    redirect: '/login'
  }
]

const router = createRouter({
  history: createWebHistory(), // 用history模式（地址栏没有#）
  routes
})


// router.beforeEach((to, from, next) => {
 
//   const token = localStorage.getItem('admin-token')

//   if (to.path === '/moment' && !token) {
//     next('/login')
//   } else {
//     next() // 放行
//   }
// })

export default router
