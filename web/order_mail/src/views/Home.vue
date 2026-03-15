<template>
  <div class="home">
    <h1 class="title">冷链商城</h1>
    <p class="subtitle">登录后即可下单，并随时追踪订单状态</p>
    <div v-if="!user" class="actions">
      <router-link to="/login" class="btn btn-primary">登录</router-link>
      <router-link to="/register" class="btn btn-secondary">注册</router-link>
    </div>
    <div v-else class="welcome">
      <p>欢迎，{{ user.nickname || user.username }}！</p>
      <router-link to="/create" class="btn btn-primary">去下单</router-link>
      <router-link to="/orders" class="btn btn-secondary">我的订单</router-link>
    </div>
  </div>
</template>

<script setup>
import { useAuth } from '../composables/useAuth'

const { user } = useAuth()
</script>

<style scoped>
.home {
  text-align: center;
  padding: 3rem 0;
}

.title {
  font-size: 2rem;
  margin-bottom: 0.5rem;
}

.subtitle {
  color: var(--text-muted);
  margin-bottom: 2rem;
}

.actions,
.welcome {
  display: flex;
  gap: 1rem;
  justify-content: center;
  flex-wrap: wrap;
}

.welcome p {
  width: 100%;
  margin-bottom: 0.5rem;
}
</style>
