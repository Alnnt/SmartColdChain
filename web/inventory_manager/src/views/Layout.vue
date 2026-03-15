<template>
  <div class="layout">
    <aside class="sidebar">
      <div class="sidebar-header">
        <router-link to="/" class="logo">库存管理</router-link>
      </div>
      <nav class="nav">
        <router-link to="/inventory" class="nav-item" active-class="active">
          <span class="nav-icon">📦</span>
          <span>库存列表</span>
        </router-link>
        <router-link to="/warehouses" class="nav-item" active-class="active">
          <span class="nav-icon">🏭</span>
          <span>仓库管理</span>
        </router-link>
      </nav>
      <div class="sidebar-footer">
        <span class="user-name">{{ user?.nickname || user?.username || '用户' }}</span>
        <button type="button" class="btn btn-secondary btn-sm" @click="logout">退出</button>
      </div>
    </aside>
    <main class="main">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuth } from '../composables/useAuth'

const router = useRouter()
const { user, loadUser, logout: doLogout } = useAuth()

onMounted(() => {
  loadUser()
})

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
  width: 220px;
  background: var(--card);
  border-right: 1px solid var(--border);
  display: flex;
  flex-direction: column;
}

.sidebar-header {
  padding: 1.25rem;
  border-bottom: 1px solid var(--border);
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
  align-items: center;
  justify-content: space-between;
  gap: 0.5rem;
}

.user-name {
  font-size: 0.875rem;
  color: var(--text-muted);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.btn-sm {
  padding: 0.35rem 0.65rem;
  font-size: 0.8125rem;
}

.main {
  flex: 1;
  padding: 1.5rem 2rem;
  overflow: auto;
}
</style>
