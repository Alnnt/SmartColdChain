import { ref } from 'vue'
import { getUserInfo } from '../api/auth'

const user = ref(null)

export function useAuth() {
  function setUser(u) {
    user.value = u
  }

  function loadUser() {
    const token = localStorage.getItem('accessToken')
    const saved = localStorage.getItem('user')
    if (saved) {
      try {
        user.value = JSON.parse(saved)
      } catch (_) {}
    }
    if (!token) {
      user.value = null
      return Promise.resolve(null)
    }
    return getUserInfo()
      .then((res) => {
        const u = res.data
        if (u) {
          user.value = u
          localStorage.setItem('user', JSON.stringify(u))
        }
        return u
      })
      .catch(() => {
        user.value = null
        localStorage.removeItem('user')
        localStorage.removeItem('accessToken')
        return null
      })
  }

  function logout() {
    user.value = null
    localStorage.removeItem('accessToken')
    localStorage.removeItem('user')
  }

  return { user, setUser, loadUser, logout }
}
