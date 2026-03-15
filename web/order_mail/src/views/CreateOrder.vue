<template>
  <div class="create-order">
    <h2>创建订单</h2>
    <div class="card">
      <form @submit.prevent="submit">
        <div class="field">
          <label class="label">选择商品</label>
          <select v-model="form.productId" class="input" required>
            <option :value="null" disabled>请选择商品</option>
            <option v-for="p in products" :key="p.id" :value="String(p.id)">
              {{ p.name }} - ¥{{ p.price.toFixed(2) }}
            </option>
          </select>
        </div>
        <div class="field">
          <label class="label">数量</label>
          <input v-model.number="form.productCount" class="input" type="number" min="1" required />
        </div>
        <div class="field">
          <label class="label">收货地址</label>
          <select v-model="form.addressId" class="input" required>
            <option :value="null" disabled>请选择地址</option>
            <option v-for="a in addresses" :key="a.id" :value="a.id">
              {{ a.contactName }} {{ a.contactPhone }} {{ a.fullAddress || [a.province, a.city, a.district, a.detail].filter(Boolean).join(' ') }}
            </option>
          </select>
          <p v-if="addresses.length === 0 && !loadingAddr" class="error-msg">请先在个人中心添加收货地址</p>
        </div>
        <div class="field total">
          <span class="label">订单金额</span>
          <span class="amount">¥{{ total.toFixed(2) }}</span>
        </div>
        <p v-if="error" class="error-msg">{{ error }}</p>
        <p v-if="success" class="success-msg">{{ success }}</p>
        <button type="submit" class="btn btn-primary" :disabled="loading || addresses.length === 0">
          {{ loading ? '提交中…' : '提交订单' }}
        </button>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { createOrder } from '../api/order'
import { getAddressList } from '../api/address'
import { MOCK_PRODUCTS } from '../data/products'

const router = useRouter()
const products = MOCK_PRODUCTS
const addresses = ref([])
const loadingAddr = ref(true)
const form = reactive({
  productId: null,
  productCount: 1,
  addressId: null,
})
const error = ref('')
const success = ref('')
const loading = ref(false)

const total = computed(() => {
  const p = products.find((x) => String(x.id) === form.productId)
  if (!p) return 0
  return p.price * (form.productCount || 0)
})

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
  error.value = ''
  success.value = ''
  const p = products.find((x) => String(x.id) === form.productId)
  if (!p || !form.addressId) return
  loading.value = true
  try {
    const res = await createOrder({
      productId: String(form.productId),
      productCount: form.productCount,
      amount: total.value,
      addressId: String(form.addressId),
    })
    const order = res.data
    success.value = `订单创建成功，订单号：${order?.orderNo || ''}`
    setTimeout(() => {
      router.push('/orders')
    }, 1500)
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

.field {
  margin-bottom: 1rem;
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
