import { ref, computed } from 'vue'

/** 购物车项：{ product, quantity }，product 含 id, name, price, img */
const items = ref([])
/** 购物车侧边栏是否展开（商品页内悬浮栏） */
const cartSidebarOpen = ref(false)

const CART_KEY = 'cold_chain_cart'

function loadFromStorage() {
  try {
    const raw = localStorage.getItem(CART_KEY)
    if (raw) items.value = JSON.parse(raw)
    else items.value = []
  } catch {
    items.value = []
  }
}

function saveToStorage() {
  try {
    localStorage.setItem(CART_KEY, JSON.stringify(items.value))
  } catch (_) {}
}

export function useCart() {
  if (items.value.length === 0) loadFromStorage()

  const totalCount = computed(() => items.value.reduce((n, it) => n + (it.quantity || 0), 0))
  const totalAmount = computed(() =>
    items.value.reduce((sum, it) => sum + (Number(it.product?.price) || 0) * (it.quantity || 0), 0)
  )

  function add(product, quantity = 1) {
    const id = product?.id != null ? String(product.id) : null
    if (!id) return
    const existing = items.value.find((it) => String(it.product?.id) === id)
    if (existing) {
      existing.quantity = (existing.quantity || 0) + quantity
    } else {
      // 统一用字符串存 id，避免前端大数溢出
      items.value.push({ product: { ...product, id }, quantity })
    }
    saveToStorage()
  }

  function setQuantity(productId, quantity) {
    const id = String(productId)
    const idx = items.value.findIndex((it) => String(it.product?.id) === id)
    if (idx === -1) return
    if (quantity <= 0) {
      items.value.splice(idx, 1)
    } else {
      items.value[idx].quantity = quantity
    }
    saveToStorage()
  }

  function remove(productId) {
    setQuantity(productId, 0)
  }

  function clear() {
    items.value = []
    saveToStorage()
  }

  function openCartSidebar() {
    cartSidebarOpen.value = true
  }

  function closeCartSidebar() {
    cartSidebarOpen.value = false
  }

  function toggleCartSidebar() {
    cartSidebarOpen.value = !cartSidebarOpen.value
  }

  return {
    items,
    totalCount,
    totalAmount,
    add,
    setQuantity,
    remove,
    clear,
    loadFromStorage,
    cartSidebarOpen,
    openCartSidebar,
    closeCartSidebar,
    toggleCartSidebar,
  }
}
