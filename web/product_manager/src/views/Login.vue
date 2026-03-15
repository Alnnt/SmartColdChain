<template>
  <div class="login-page">
    <div class="card form-card">
      <h1 class="title">商品管理</h1>
      <p class="subtitle">冷链云仓 · 管理员登录</p>
      <form @submit.prevent="submit">
        <div class="field">
          <label class="label">用户名</label>
          <input v-model="form.username" class="input" type="text" required placeholder="请输入用户名" />
        </div>
        <div class="field">
          <label class="label">密码</label>
          <input v-model="form.password" class="input" type="password" required placeholder="请输入密码" />
        </div>
        <p v-if="error" class="error-msg">{{ error }}</p>
        <button type="submit" class="btn btn-primary btn-block" :disabled="loading">
          {{ loading ? '登录中…' : '登录' }}
        </button>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuth } from '../composables/useAuth'
import { login } from '../api/auth'

const router = useRouter()
const route = useRoute()
const { setUser } = useAuth()

const form = reactive({ username: '', password: '' })
const error = ref('')
const loading = ref(false)

async function submit() {
  error.value = ''
  loading.value = true
  try {
    const res = await login(form)
    const data = res.data
    if (data?.accessToken) {
      localStorage.setItem('accessToken', data.accessToken)
      setUser({
        userId: data.userId,
        username: data.username,
        nickname: data.nickname,
        avatar: data.avatar,
      })
      const redirect = route.query.redirect || '/'
      router.push(redirect)
    }
  } catch (e) {
    error.value = e.message || '登录失败'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 1rem;
}

.form-card {
  width: 100%;
  max-width: 360px;
}

.title {
  font-size: 1.5rem;
  font-weight: 700;
  margin-bottom: 0.25rem;
}

.subtitle {
  color: var(--text-muted);
  font-size: 0.9rem;
  margin-bottom: 1.5rem;
}

.field {
  margin-bottom: 1rem;
}

.btn-block {
  width: 100%;
  margin-top: 0.5rem;
}
</style>
