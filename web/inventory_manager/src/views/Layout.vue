<template>
  <div class="layout">
    <aside class="sidebar">
      <div class="sidebar-header">
        <router-link to="/" class="logo">库存管理</router-link>
      </div>
      <div v-if="isSuperAdmin" class="warehouse-switch">
        <label class="switch-label">当前仓库</label>
        <select v-model="selectedWarehouseId" class="select" @change="onWarehouseChange">
          <option value="">全部</option>
          <option v-for="w in warehouseOptions" :key="w.id" :value="String(w.id)">{{ w.name }}</option>
        </select>
      </div>
      <nav class="nav">
        <router-link to="/inventory" class="nav-item" active-class="active">
          <span class="nav-icon">📦</span>
          <span>库存列表</span>
        </router-link>
        <router-link v-if="isSuperAdmin" to="/warehouses" class="nav-item" active-class="active">
          <span class="nav-icon">🏭</span>
          <span>仓库管理</span>
        </router-link>
        <router-link to="/orders" class="nav-item" active-class="active">
          <span class="nav-icon">📋</span>
          <span>订单列表</span>
        </router-link>
      </nav>
      <div class="sidebar-footer">
        <div class="user-block">
          <span class="user-label">当前用户</span>
          <span class="user-name">{{ user?.nickname || user?.username || '—' }}</span>
        </div>
        <button type="button" class="btn btn-secondary btn-sm" @click="logout">退出登录</button>
      </div>
    </aside>
    <main class="main">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useAuth } from '../composables/useAuth'
import { getWarehouses } from '../api/inventory'

const router = useRouter()
const { user, isSuperAdmin, boundWarehouseId, setCurrentWarehouseId, loadUser, logout: doLogout } = useAuth()

const warehouseOptions = ref([])
const selectedWarehouseId = ref('')

onMounted(async () => {
  await loadUser()
  if (isSuperAdmin.value) loadWarehouses()
})

watch(boundWarehouseId, (id) => {
  if (id) selectedWarehouseId.value = id
}, { immediate: true })

async function loadWarehouses() {
  try {
    const res = await getWarehouses()
    warehouseOptions.value = res?.data ?? []
  } catch {
    warehouseOptions.value = []
  }
}

function onWarehouseChange() {
  setCurrentWarehouseId(selectedWarehouseId.value || null)
}

function logout() {
  doLogout()
  router.push('/login')
}
</script>

<style scoped>
.layout {
  display: flex;
  min-height: 100vh;
}

.sidebar {
  position: fixed;
  top: 0;
  left: 0;
  width: 220px;
  height: 100vh;
  background: var(--card);
  border-right: 1px solid var(--border);
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
}

.sidebar-header {
  padding: 1.25rem;
  border-bottom: 1px solid var(--border);
}

.warehouse-switch {
  padding: 0.75rem 1.25rem;
  border-bottom: 1px solid var(--border);
}

.switch-label {
  display: block;
  font-size: 0.75rem;
  color: var(--text-muted);
  margin-bottom: 0.35rem;
}

.select {
  width: 100%;
  padding: 0.4rem 0.5rem;
  font-size: 0.875rem;
  border: 1px solid var(--border);
  border-radius: 4px;
  background: var(--card);
  color: var(--text);
}

.logo {
  font-size: 1.1rem;
  font-weight: 700;
  color: var(--accent);
  text-decoration: none;
}

.nav {
  flex: 1;
  padding: 0.75rem 0;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.6rem 1.25rem;
  color: var(--text-muted);
  text-decoration: none;
  font-weight: 500;
  transition: color 0.15s, background 0.15s;
}

.nav-item:hover {
  color: var(--text);
  background: rgba(35, 134, 54, 0.08);
}

.nav-item.active {
  color: var(--accent);
  background: rgba(35, 134, 54, 0.12);
}

.nav-icon {
  font-size: 1.1rem;
}

.sidebar-footer {
  padding: 1rem 1.25rem;
  border-top: 1px solid var(--border);
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.user-block {
  display: flex;
  flex-direction: column;
  gap: 0.2rem;
  min-width: 0;
}

.user-label {
  font-size: 0.7rem;
  color: var(--text-muted);
  text-transform: uppercase;
  letter-spacing: 0.02em;
}

.user-name {
  font-size: 0.875rem;
  font-weight: 500;
  color: var(--text);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.sidebar-footer .btn-sm {
  align-self: flex-start;
}

.btn-sm {
  padding: 0.35rem 0.65rem;
  font-size: 0.8125rem;
}

.main {
  flex: 1;
  margin-left: 220px;
  min-width: 0;
  padding: 1.5rem 2rem;
  overflow: auto;
}
</style>
