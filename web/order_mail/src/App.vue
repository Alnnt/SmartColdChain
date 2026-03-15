<template>
  <div class="app">
    <header class="header">
      <router-link to="/" class="logo">冷链商城</router-link>
      <nav class="nav">
        <template v-if="user">
          <span class="user-name">{{ user.nickname || user.username }}</span>
          <router-link to="/orders">我的订单</router-link>
          <router-link to="/create">去下单</router-link>
          <router-link to="/profile">个人中心</router-link>
          <button class="btn-link" type="button" @click="logout">退出</button>
        </template>
        <template v-else>
          <router-link to="/login">登录</router-link>
          <router-link to="/register">注册</router-link>
        </template>
      </nav>
    </header>
    <main class="main">
      <router-view />
    </main>
    <footer class="footer">
      <p>冷链商城 · 订单与物流追踪</p>
    </footer>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuth } from './composables/useAuth'

const router = useRouter()
const { user, loadUser, logout: doLogout } = useAuth()

onMounted(() => {
  loadUser()
})

function logout() {
  doLogout()
  router.push('/')
}
</script>

<style>
:root {
  --bg: #0f1419;
  --card: #1a2332;
  --border: #2d3a4f;
  --text: #e6edf3;
  --text-muted: #8b949e;
  --accent: #58a6ff;
  --accent-hover: #79b8ff;
  --success: #3fb950;
  --warning: #d29922;
  --danger: #f85149;
  --radius: 10px;
  --font: 'Noto Sans SC', -apple-system, sans-serif;
}

* {
  box-sizing: border-box;
  margin: 0;
  padding: 0;
}

body {
  font-family: var(--font);
  background: var(--bg);
  color: var(--text);
  min-height: 100vh;
  line-height: 1.5;
}

.app {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1rem 2rem;
  background: var(--card);
  border-bottom: 1px solid var(--border);
}

.logo {
  font-size: 1.25rem;
  font-weight: 700;
  color: var(--accent);
  text-decoration: none;
}

.nav {
  display: flex;
  align-items: center;
  gap: 1.5rem;
}

.nav a {
  color: var(--text-muted);
  text-decoration: none;
  font-weight: 500;
}

.nav a:hover,
.nav a.router-link-active {
  color: var(--accent);
}

.user-name {
  color: var(--text);
  font-size: 0.9rem;
}

.btn-link {
  background: none;
  border: none;
  color: var(--text-muted);
  cursor: pointer;
  font: inherit;
  font-weight: 500;
}

.btn-link:hover {
  color: var(--danger);
}

.main {
  flex: 1;
  padding: 2rem;
  max-width: 960px;
  margin: 0 auto;
  width: 100%;
}

.footer {
  padding: 1rem 2rem;
  text-align: center;
  color: var(--text-muted);
  font-size: 0.875rem;
  border-top: 1px solid var(--border);
}

.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 0.6rem 1.2rem;
  border-radius: var(--radius);
  font: inherit;
  font-weight: 500;
  cursor: pointer;
  border: none;
  transition: background 0.2s, color 0.2s;
}

.btn-primary {
  background: var(--accent);
  color: #fff;
}

.btn-primary:hover {
  background: var(--accent-hover);
}

.btn-secondary {
  background: var(--border);
  color: var(--text);
}

.btn-secondary:hover {
  background: #3d4d66;
}

.input {
  width: 100%;
  padding: 0.65rem 0.9rem;
  border: 1px solid var(--border);
  border-radius: var(--radius);
  background: var(--bg);
  color: var(--text);
  font: inherit;
}

.input:focus {
  outline: none;
  border-color: var(--accent);
}

.label {
  display: block;
  margin-bottom: 0.35rem;
  font-size: 0.9rem;
  color: var(--text-muted);
}

.card {
  background: var(--card);
  border: 1px solid var(--border);
  border-radius: var(--radius);
  padding: 1.5rem;
}

.error-msg {
  color: var(--danger);
  font-size: 0.875rem;
  margin-top: 0.25rem;
}

.success-msg {
  color: var(--success);
  font-size: 0.875rem;
  margin-top: 0.25rem;
}
</style>
