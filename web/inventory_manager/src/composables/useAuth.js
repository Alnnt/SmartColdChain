import { ref, computed, onMounted } from 'vue'
import { getUserInfo } from '../api/auth'

const user = ref(null)
const currentWarehouseId = ref(null)

export function useAuth() {
  const isSuperAdmin = computed(() => {
    const r = user.value?.roles
    return Array.isArray(r) && r.includes('ROLE_ADMIN')
  })

  const boundWarehouseId = computed(() => {
    const w = user.value?.warehouseId
    return w != null && w !== '' ? String(w) : null
  })

  /** 当前选中的仓库ID（超管切换用；仓管为绑定仓） */
  const effectiveWarehouseId = computed(() => {
    if (boundWarehouseId.value) return boundWarehouseId.value
    return currentWarehouseId.value
  })

  function setCurrentWarehouseId(id) {
    currentWarehouseId.value = id != null && id !== '' ? String(id) : null
  }

  async function loadUser() {
    const token = localStorage.getItem('accessToken')
    if (!token) {
      user.value = null
      return
    }
    try {
      const res = await getUserInfo()
      const data = res?.data ?? null
      if (data) {
        user.value = {
          userId: data.userId,
          username: data.username,
          nickname: data.nickname,
          avatar: data.avatar,
          roles: data.roles || [],
          warehouseId: data.warehouseId != null ? String(data.warehouseId) : null,
        }
      } else {
        user.value = null
      }
    } catch {
      user.value = null
    }
  }

  function setUser(data) {
    user.value = data
    if (data) {
      if (data.warehouseId != null) data.warehouseId = String(data.warehouseId)
      localStorage.setItem('user', JSON.stringify(data))
    } else {
      localStorage.removeItem('user')
    }
  }

  function logout() {
    localStorage.removeItem('accessToken')
    localStorage.removeItem('user')
    user.value = null
    currentWarehouseId.value = null
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

  return {
    user,
    isSuperAdmin,
    boundWarehouseId,
    effectiveWarehouseId,
    currentWarehouseId,
    setCurrentWarehouseId,
    loadUser,
    setUser,
    logout,
  }
}
