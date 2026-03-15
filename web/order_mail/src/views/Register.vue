<template>
  <div class="form-page">
    <div class="card form-card">
      <h2>注册</h2>
      <form @submit.prevent="submit">
        <div class="field">
          <label class="label">用户名（4-20 位）</label>
          <input v-model="form.username" class="input" type="text" required minlength="4" maxlength="20" placeholder="请输入用户名" />
        </div>
        <div class="field">
          <label class="label">密码（6-20 位）</label>
          <input v-model="form.password" class="input" type="password" required minlength="6" maxlength="20" placeholder="请输入密码" />
        </div>
        <div class="field">
          <label class="label">确认密码</label>
          <input v-model="form.confirmPassword" class="input" type="password" required placeholder="请再次输入密码" />
        </div>
        <div class="field">
          <label class="label">手机号</label>
          <input v-model="form.phone" class="input" type="tel" required placeholder="11 位手机号" />
        </div>
        <div class="field">
          <label class="label">昵称（选填）</label>
          <input v-model="form.nickname" class="input" type="text" placeholder="昵称" />
        </div>
        <p v-if="error" class="error-msg">{{ error }}</p>
        <button type="submit" class="btn btn-primary" :disabled="loading">注册</button>
      </form>
      <p class="link-line">
        已有账号？<router-link to="/login">去登录</router-link>
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useAuth } from '../composables/useAuth'
import { register } from '../api/auth'

const router = useRouter()
const { setUser } = useAuth()

const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  phone: '',
  nickname: '',
})
const error = ref('')
const loading = ref(false)

async function submit() {
  error.value = ''
  if (form.password !== form.confirmPassword) {
    error.value = '两次密码不一致'
    return
  }
  if (!/^1[3-9]\d{9}$/.test(form.phone)) {
    error.value = '手机号格式不正确'
    return
  }
  loading.value = true
  try {
    const res = await register(form)
    const data = res.data
    if (data?.accessToken) {
      localStorage.setItem('accessToken', data.accessToken)
      setUser({
        userId: data.userId,
        username: data.username,
        nickname: data.nickname,
        avatar: data.avatar,
      })
      router.push('/')
    }
  } catch (e) {
    error.value = e.message || '注册失败'
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
