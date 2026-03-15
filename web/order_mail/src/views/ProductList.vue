<template>
  <div class="product-list-page">
    <h2 class="page-title">浏览商品</h2>
    <p class="page-desc">多选商品加入购物车，左侧购物车中可编辑数量或删除，然后去结算</p>

    <!-- 有商品时左侧固定购物车（不遮挡顶部导航） -->
    <aside v-if="cartItems.length > 0" class="cart-sidebar-fixed">
      <div class="cart-sidebar card">
        <div class="cart-sidebar-header">
          <h3>购物车</h3>
        </div>
        <div class="cart-sidebar-body">
          <ul class="cart-item-list">
            <li v-for="(it, index) in cartItems" :key="String(it.product?.id) + '-' + index" class="cart-item">
              <div class="cart-item-info">
                <span class="cart-item-name" :title="it.product?.name">{{ it.product?.name }}</span>
                <span class="cart-item-price">¥{{ (Number(it.product?.price) || 0).toFixed(2) }} × {{ it.quantity }}</span>
              </div>
              <div class="cart-item-actions">
                <input
                  v-model.number="cartQuantities[it.product?.id]"
                  type="number"
                  min="1"
                  class="cart-qty-input"
                  @change="onCartQtyChange(it.product?.id)"
                />
                <button type="button" class="cart-item-remove btn btn-secondary btn-sm" @click="removeFromCart(it.product?.id)">删除</button>
              </div>
              <div class="cart-item-subtotal">¥{{ ((Number(it.product?.price) || 0) * (it.quantity || 0)).toFixed(2) }}</div>
            </li>
          </ul>
        </div>
        <div class="cart-sidebar-footer">
          <div class="cart-total">合计：¥{{ cartTotal.toFixed(2) }}</div>
          <router-link to="/create" class="btn btn-primary btn-block">去结算</router-link>
        </div>
      </div>
    </aside>

    <!-- 有购物车时给主内容留出左侧空间 -->
    <div class="product-list-content" :class="{ 'has-cart': cartItems.length > 0 }">
    <!-- 多选操作栏 -->
    <div v-if="selected.size > 0" class="selection-bar card">
      <span>已选 {{ selected.size }} 件商品</span>
      <div class="selection-actions">
        <label class="qty-label">统一数量：</label>
        <input v-model.number="batchQty" type="number" min="1" class="qty-input" />
        <button type="button" class="btn btn-primary" @click="addSelectedToCart">
          加入购物车
        </button>
        <button type="button" class="btn btn-secondary" @click="clearSelection">取消选择</button>
      </div>
    </div>

    <!-- 瀑布流商品网格（滚动到底部自动加载更多） -->
    <div class="waterfall-grid">
        <div
          v-for="p in products"
          :key="p.id"
          class="product-card card"
          :class="{ selected: selected.has(String(p.id)) }"
        >
          <div class="card-img-wrap">
            <img v-if="p.img" :src="p.img" :alt="p.name" class="card-img" />
            <div v-else class="card-img-placeholder">暂无图片</div>
          </div>
          <div class="card-body">
            <label class="checkbox-wrap">
              <input
                type="checkbox"
                :checked="selected.has(String(p.id))"
                @change="toggleSelect(p)"
              />
              <span class="card-title">{{ p.name }}</span>
            </label>
            <p class="card-price">¥{{ (Number(p.price) || 0).toFixed(2) }}</p>
            <div class="card-actions">
              <input
                v-model.number="quantities[p.id]"
                type="number"
                min="1"
                class="qty-input small"
                @click.stop
              />
              <button type="button" class="btn btn-primary btn-sm" @click="addOneToCart(p)">
                加入购物车
              </button>
            </div>
          </div>
        </div>
      <div v-if="loading" class="loading-more">加载中…</div>
      <div v-else-if="noMore && products.length > 0" class="no-more">没有更多了</div>
      <div v-else-if="!loading && products.length === 0" class="empty">暂无商品</div>
    </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, watch } from 'vue'
import { getProductPage } from '../api/product'
import { useCart } from '../composables/useCart'

const products = ref([])
const page = ref(1)
const pageSize = 12
const total = ref(0)
const loading = ref(false)
const noMore = ref(false)
const selected = ref(new Set())
const batchQty = ref(1)
const quantities = reactive({})
const {
  items: cartItems,
  totalAmount: cartTotal,
  add: addToCart,
  setQuantity: setCartQuantity,
  remove: removeFromCart,
} = useCart()

// 侧栏内数量输入与 cart 同步（便于 v-model 编辑）
const cartQuantities = reactive({})
function syncCartQuantities() {
  cartItems.value.forEach((it) => {
    const id = it.product?.id != null ? String(it.product.id) : null
    if (id) cartQuantities[id] = it.quantity ?? 1
  })
}
function onCartQtyChange(productId) {
  if (productId == null) return
  const q = Math.max(1, parseInt(cartQuantities[productId], 10) || 1)
  cartQuantities[productId] = q
  setCartQuantity(productId, q)
}

watch(cartItems, () => syncCartQuantities(), { deep: true })

function toggleSelect(p) {
  const id = String(p.id)
  const next = new Set(selected.value)
  if (next.has(id)) next.delete(id)
  else next.add(id)
  selected.value = next
  if (!quantities[p.id]) quantities[p.id] = 1
}

function clearSelection() {
  selected.value = new Set()
}

function addOneToCart(p) {
  const qty = Math.max(1, quantities[p.id] || 1)
  addToCart(p, qty)
}

function addSelectedToCart() {
  const qty = Math.max(1, batchQty.value)
  products.value.forEach((p) => {
    if (selected.value.has(String(p.id))) addToCart(p, qty)
  })
  clearSelection()
}

async function loadMore() {
  if (loading.value || noMore.value) return
  loading.value = true
  try {
    const res = await getProductPage({ page: page.value, pageSize })
    const data = res?.data
    const records = data?.records ?? []
    products.value.push(...records)
    records.forEach((p) => {
      if (quantities[p.id] == null) quantities[p.id] = 1
    })
    total.value = data?.total ?? 0
    if (records.length < pageSize || products.value.length >= total.value) noMore.value = true
    else page.value += 1
  } catch (_) {
    noMore.value = true
  } finally {
    loading.value = false
  }
}

function onScroll() {
  const { scrollTop, scrollHeight, clientHeight } = document.documentElement
  if (scrollHeight - scrollTop - clientHeight < 300) loadMore()
}

onMounted(() => {
  loadMore()
  window.addEventListener('scroll', onScroll, { passive: true })
  syncCartQuantities()
})

onUnmounted(() => {
  window.removeEventListener('scroll', onScroll)
})
</script>

<style scoped>
.product-list-page {
  width: 100%;
}

.page-title {
  font-size: 1.5rem;
  margin-bottom: 0.25rem;
}

.page-desc {
  color: var(--text-muted);
  font-size: 0.9rem;
  margin-bottom: 1.5rem;
}

.selection-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.selection-actions {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.qty-label {
  font-size: 0.9rem;
  color: var(--text-muted);
}

.qty-input {
  width: 4rem;
  padding: 0.35rem 0.5rem;
  border: 1px solid var(--border);
  border-radius: 6px;
  background: var(--bg);
  color: var(--text);
  font: inherit;
}

.qty-input.small {
  width: 3rem;
}

.waterfall-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 1.25rem;
}

.product-card {
  padding: 0;
  overflow: hidden;
  transition: box-shadow 0.2s, border-color 0.2s;
}

.product-card.selected {
  border-color: var(--accent);
  box-shadow: 0 0 0 2px rgba(88, 166, 255, 0.3);
}

.card-img-wrap {
  aspect-ratio: 1;
  background: var(--bg);
  display: flex;
  align-items: center;
  justify-content: center;
}

.card-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.card-img-placeholder {
  color: var(--text-muted);
  font-size: 0.875rem;
}

.card-body {
  padding: 0.75rem 1rem;
}

.checkbox-wrap {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  cursor: pointer;
  margin-bottom: 0.35rem;
}

.checkbox-wrap input {
  flex-shrink: 0;
}

.card-title {
  font-weight: 500;
  font-size: 0.95rem;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-price {
  color: var(--accent);
  font-weight: 600;
  font-size: 1rem;
  margin-bottom: 0.5rem;
}

.card-actions {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.btn-sm {
  padding: 0.4rem 0.75rem;
  font-size: 0.85rem;
}

.loading-more,
.no-more,
.empty {
  grid-column: 1 / -1;
  text-align: center;
  padding: 1.5rem;
  color: var(--text-muted);
  font-size: 0.9rem;
}

/* 购物车左侧固定侧栏（不遮挡顶部导航，从 header 下方开始） */
.cart-sidebar-fixed {
  position: fixed;
  left: 0;
  top: var(--header-height, 56px);
  bottom: 0;
  width: 320px;
  z-index: 50;
  overflow: hidden;
}

.cart-sidebar-fixed .cart-sidebar {
  width: 100%;
  height: 100%;
  margin: 0;
  border-radius: 0;
  border-left: none;
  border-right: 1px solid var(--border);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.product-list-content.has-cart {
  margin-left: 320px;
}

.cart-sidebar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1rem 1.25rem;
  border-bottom: 1px solid var(--border);
  flex-shrink: 0;
}

.cart-sidebar-header h3 {
  font-size: 1.1rem;
  font-weight: 600;
  margin: 0;
}

.cart-sidebar-body {
  flex: 1;
  overflow-y: auto;
  padding: 0.75rem;
}

.cart-item-list {
  list-style: none;
  margin: 0;
  padding: 0;
}

.cart-item {
  padding: 0.75rem 0;
  border-bottom: 1px solid var(--border);
  display: grid;
  gap: 0.5rem;
}

.cart-item:last-child {
  border-bottom: none;
}

.cart-item-info {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.cart-item-name {
  font-weight: 500;
  font-size: 0.9rem;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.cart-item-price {
  font-size: 0.8rem;
  color: var(--text-muted);
}

.cart-item-actions {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.cart-qty-input {
  width: 4rem;
  padding: 0.35rem 0.5rem;
  border: 1px solid var(--border);
  border-radius: 6px;
  background: var(--bg);
  color: var(--text);
  font: inherit;
  font-size: 0.9rem;
}

.cart-item-subtotal {
  font-weight: 600;
  color: var(--accent);
  font-size: 0.95rem;
}

.cart-sidebar-footer {
  padding: 1rem 1.25rem;
  border-top: 1px solid var(--border);
  flex-shrink: 0;
}

.cart-total {
  font-size: 1.1rem;
  font-weight: 600;
  margin-bottom: 0.75rem;
}

.cart-sidebar-footer .btn-block {
  width: 100%;
}
</style>
