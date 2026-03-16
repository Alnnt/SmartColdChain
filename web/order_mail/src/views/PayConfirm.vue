<template>
  <div class="pay-confirm">
    <h2>确认支付</h2>

    <div v-if="!orders.length" class="card empty">
      <p>暂无待支付订单，请从购物车提交订单。</p>
      <router-link to="/create" class="btn btn-primary">去下单</router-link>
    </div>

    <div v-else class="card">
      <div class="order-list">
        <div v-for="(o, i) in orders" :key="o.orderNo || i" class="order-row">
          <span class="order-no">{{ o.orderNo }}</span>
          <span class="order-desc">{{ o.productName ? `${o.productName} × ${o.count || 0}` : '订单' }}</span>
          <span class="order-amount">¥{{ (o.amount || 0).toFixed(2) }}</span>
        </div>
      </div>
      <div class="total-row">
        <span class="label">合计</span>
        <span class="amount">¥{{ totalAmount.toFixed(2) }}</span>
      </div>
      <p v-if="error" class="error-msg">{{ error }}</p>
      <p v-if="success" class="success-msg">{{ success }}</p>
      <div class="actions">
        <button
          type="button"
          class="btn btn-secondary"
          :disabled="loading"
          @click="goBack"
        >
          返回修改
        </button>
        <button
          type="button"
          class="btn btn-primary"
          :disabled="loading"
          @click="submitPay"
        >
          {{ loading ? '支付中…' : '模拟支付' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { payOrder } from '../api/order'

const router = useRouter()
const route = useRoute()

// 从路由 state 或 query 恢复待支付订单（state 由 CreateOrder 跳转时传入）
const orders = ref([])
const loading = ref(false)
const error = ref('')
const success = ref('')

const totalAmount = computed(() => {
  return orders.value.reduce((sum, o) => sum + (Number(o.amount) || 0), 0)
})

onMounted(() => {
  const state = history.state
  if (state?.orders && Array.isArray(state.orders) && state.orders.length > 0) {
    orders.value = state.orders
    return
  }
  // 若直接访问 /pay 无 state，可考虑从 sessionStorage 读取（CreateOrder 也可写入）
  const saved = sessionStorage.getItem('pendingPayOrders')
  if (saved) {
    try {
      const list = JSON.parse(saved)
      if (Array.isArray(list) && list.length > 0) {
        orders.value = list
      }
    } catch (_) {
      orders.value = []
    }
  }
})

function goBack() {
  router.push('/create')
}

async function submitPay() {
  if (!orders.value.length) return
  error.value = ''
  success.value = ''
  loading.value = true
  try {
    for (const o of orders.value) {
      await payOrder({
        orderNo: o.orderNo,
        paidAmount: Number(o.amount) || 0,
        paymentTime: Date.now(),
      })
    }
    sessionStorage.removeItem('pendingPayOrders')
    success.value = '支付成功'
    setTimeout(() => router.push('/orders'), 1200)
  } catch (e) {
    error.value = e.message || '支付失败'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.pay-confirm h2 {
  margin-bottom: 1rem;
}

.empty {
  text-align: center;
  padding: 2rem;
}

.empty p {
  margin-bottom: 1rem;
  color: var(--text-muted);
}

.order-list {
  margin-bottom: 1rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid var(--border);
}

.order-row {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.5rem 0;
}

.order-no {
  flex: 0 0 180px;
  font-family: monospace;
  font-size: 0.9rem;
}

.order-desc {
  flex: 1;
  color: var(--text-muted);
}

.order-amount {
  font-weight: 600;
  color: var(--accent);
}

.total-row {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 0.5rem;
  margin-bottom: 1rem;
}

.total-row .label {
  color: var(--text-muted);
}

.total-row .amount {
  font-size: 1.25rem;
  font-weight: 600;
  color: var(--accent);
}

.actions {
  display: flex;
  gap: 0.75rem;
  justify-content: flex-end;
}

.error-msg {
  color: var(--danger, #c00);
  margin-bottom: 0.5rem;
}

.success-msg {
  color: var(--success, #0a0);
  margin-bottom: 0.5rem;
}
</style>
