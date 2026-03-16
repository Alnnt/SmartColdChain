import { createRouter, createWebHashHistory } from 'vue-router'

const routes = [
  { path: '/', name: 'Home', component: () => import('../views/Home.vue'), meta: { title: '首页' } },
  { path: '/products', name: 'ProductList', component: () => import('../views/ProductList.vue'), meta: { title: '浏览商品', auth: true } },
  { path: '/login', name: 'Login', component: () => import('../views/Login.vue'), meta: { title: '登录', guest: true } },
  { path: '/register', name: 'Register', component: () => import('../views/Register.vue'), meta: { title: '注册', guest: true } },
  { path: '/create', name: 'CreateOrder', component: () => import('../views/CreateOrder.vue'), meta: { title: '下单', auth: true } },
  { path: '/pay', name: 'PayConfirm', component: () => import('../views/PayConfirm.vue'), meta: { title: '确认支付', auth: true } },
  { path: '/orders', name: 'MyOrders', component: () => import('../views/MyOrders.vue'), meta: { title: '我的订单', auth: true } },
  { path: '/orders/:id', name: 'OrderDetail', component: () => import('../views/OrderDetail.vue'), meta: { title: '订单详情', auth: true } },
  { path: '/profile', name: 'Profile', component: () => import('../views/Profile.vue'), meta: { title: '个人中心', auth: true } },
]

const router = createRouter({
  history: createWebHashHistory(),
  routes,
})

router.beforeEach((to, _from, next) => {
  document.title = to.meta.title ? `${to.meta.title} - 冷链商城` : '冷链商城'
  const token = localStorage.getItem('accessToken')
  const hasUser = !!token
  if (to.meta.auth && !hasUser) {
    next({ name: 'Login', query: { redirect: to.fullPath } })
    return
  }
  if (to.meta.guest && hasUser) {
    next({ name: 'Home' })
    return
  }
  next()
})

export default router
