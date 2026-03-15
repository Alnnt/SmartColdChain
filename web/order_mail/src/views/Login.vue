<template>
  <div class="form-page">
    <div class="card form-card">
      <h2>登录</h2>
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
        <button type="submit" class="btn btn-primary" :disabled="loading">登录</button>
      </form>
      <p class="link-line">
        还没有账号？<router-link to="/register">立即注册</router-link>
      </p>
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
.form-page {
  max-width: 400px;
  margin: 0 auto;
}

.form-card h2 {
  margin-bottom: 1.5rem;
}

.field {
  margin-bottom: 1rem;
}

.form-card .btn {
  width: 100%;
  margin-top: 0.5rem;
}

.link-line {
  margin-top: 1rem;
  color: var(--text-muted);
  font-size: 0.9rem;
}

.link-line a {
  color: var(--accent);
}
</style>
