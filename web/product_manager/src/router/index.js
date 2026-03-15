import { createRouter, createWebHashHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { title: '登录', guest: true },
  },
  {
    path: '/',
    component: () => import('../views/Layout.vue'),
    meta: { auth: true },
    children: [
      { path: '', name: 'ProductList', redirect: '/products' },
      { path: 'products', name: 'Products', component: () => import('../views/ProductList.vue'), meta: { title: '商品列表' } },
    ],
  },
]

const router = createRouter({
  history: createWebHashHistory(),
  routes,
})

router.beforeEach((to, _from, next) => {
  document.title = to.meta.title ? `${to.meta.title} - 商品管理` : '商品管理 - 冷链云仓'
  const token = localStorage.getItem('accessToken')
  const hasUser = !!token
  if (to.meta.auth && !hasUser) {
    next({ name: 'Login', query: { redirect: to.fullPath } })
    return
  }
  if (to.meta.guest && hasUser) {
    next({ path: '/' })
    return
  }
  next()
})

export default router
