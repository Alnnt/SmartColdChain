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
      { path: '', name: 'Dashboard', redirect: '/inventory' },
      { path: 'inventory', name: 'Inventory', component: () => import('../views/InventoryList.vue'), meta: { title: '库存列表' } },
      { path: 'warehouses', name: 'Warehouses', component: () => import('../views/WarehouseList.vue'), meta: { title: '仓库管理' } },
    ],
  },
]

const router = createRouter({
  history: createWebHashHistory(),
  routes,
})

router.beforeEach((to, _from, next) => {
  document.title = to.meta.title ? `${to.meta.title} - 库存管理` : '库存管理 - 冷链云仓'
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
