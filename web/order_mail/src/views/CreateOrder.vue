<template>
  <div class="create-order">
    <h2>确认订单</h2>

    <!-- 购物车为空 -->
    <div v-if="cartItems.length === 0" class="card empty-cart">
      <p>购物车为空，请先浏览商品并加入购物车。</p>
      <router-link to="/products" class="btn btn-primary">去选商品</router-link>
    </div>

    <!-- 购物车有商品：展示列表并选择地址下单 -->
    <div v-else class="card">
      <div class="cart-items">
        <div v-for="(it, index) in cartItems" :key="index" class="cart-row">
          <span class="cart-name">{{ it.product?.name }}</span>
          <span class="cart-qty">× {{ it.quantity }}</span>
          <span class="cart-subtotal">¥{{ ((Number(it.product?.price) || 0) * (it.quantity || 0)).toFixed(2) }}</span>
        </div>
      </div>
      <div class="field">
        <label class="label">收货地址</label>
        <select v-model="addressId" class="input" required>
          <option :value="null" disabled>请选择地址</option>
          <option v-for="a in addresses" :key="String(a.id)" :value="String(a.id)">
            {{ a.contactName }} {{ a.contactPhone }} {{ a.fullAddress || [a.province, a.city, a.district, a.detail].filter(Boolean).join(' ') }}
          </option>
        </select>
        <p v-if="addresses.length === 0 && !loadingAddr" class="error-msg">请先在个人中心添加收货地址</p>
      </div>
      <div class="field total">
        <span class="label">订单总金额</span>
        <span class="amount">¥{{ cartTotal.toFixed(2) }}</span>
      </div>
      <p v-if="error" class="error-msg">{{ error }}</p>
      <p v-if="success" class="success-msg">{{ success }}</p>
      <button type="button" class="btn btn-primary" :disabled="loading || addresses.length === 0" @click="submit">
        {{ loading ? '提交中…' : '提交订单' }}
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { createOrder } from '../api/order'
import { getAddressList } from '../api/address'
import { useCart } from '../composables/useCart'

const router = useRouter()
const { items: cartItems, totalAmount: cartTotal, clear: clearCart } = useCart()
const addresses = ref([])
const loadingAddr = ref(true)
const addressId = ref(null)
const error = ref('')
const success = ref('')
const loading = ref(false)

onMounted(async () => {
  try {
    const res = await getAddressList()
    addresses.value = res.data || []
  } catch (_) {
    addresses.value = []
  } finally {
    loadingAddr.value = false
  }
})

async function submit() {
  if (!addressId.value || cartItems.value.length === 0) return
  error.value = ''
  success.value = ''
  loading.value = true
  try {
    const orderNos = []
    for (const it of cartItems.value) {
      const product = it.product
      const qty = it.quantity || 1
      const amount = (Number(product?.price) || 0) * qty
      const res = await createOrder({
        productId: String(product?.id),
        productName: product?.name ?? '',
        productCount: qty,
        amount,
        addressId: String(addressId.value),
      })
      const order = res?.data
      if (order?.orderNo) orderNos.push(order.orderNo)
    }
    clearCart()
    success.value = orderNos.length > 0 ? `已成功创建 ${orderNos.length} 笔订单` : '订单已提交'
    setTimeout(() => router.push('/orders'), 1500)
  } catch (e) {
    error.value = e.message || '下单失败'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.create-order h2 {
  margin-bottom: 1rem;
}

.empty-cart {
  text-align: center;
  padding: 2rem;
}

.empty-cart p {
  margin-bottom: 1rem;
  color: var(--text-muted);
}

.field {
  margin-bottom: 1rem;
}

.cart-items {
  margin-bottom: 1rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid var(--border);
}

.cart-row {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 0.5rem 0;
}

.cart-name {
  flex: 1;
  font-weight: 500;
}

.cart-qty {
  color: var(--text-muted);
}

.cart-subtotal {
  font-weight: 600;
  color: var(--accent);
}

.total {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.amount {
  font-size: 1.25rem;
  font-weight: 600;
  color: var(--accent);
}

.btn {
  margin-top: 0.5rem;
}
</style>
