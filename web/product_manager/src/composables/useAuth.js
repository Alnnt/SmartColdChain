import { ref, onMounted } from 'vue'
import { getUserInfo } from '../api/auth'

const user = ref(null)

export function useAuth() {
  async function loadUser() {
    const token = localStorage.getItem('accessToken')
    if (!token) {
      user.value = null
      return
    }
    try {
      const res = await getUserInfo()
      user.value = res?.data ?? null
    } catch {
      user.value = null
    }
  }

  function setUser(data) {
    user.value = data
    if (data) localStorage.setItem('user', JSON.stringify(data))
    else localStorage.removeItem('user')
  }

  function logout() {
    localStorage.removeItem('accessToken')
    localStorage.removeItem('user')
    user.value = null
  }

  onMounted(() => {
    const saved = localStorage.getItem('user')
    if (saved) {
      try {
        user.value = JSON.parse(saved)
      } catch {
        user.value = null
      }
    }
  })

  return { user, loadUser, setUser, logout }
}
