<template>
  <div class="my-orders">
    <h2>我的订单</h2>
    <div v-if="loading" class="loading">加载中…</div>
    <div v-else-if="!list.length" class="empty">暂无订单</div>
    <div v-else class="order-list">
      <div v-for="order in list" :key="order.id" class="card order-card">
        <div class="order-head">
          <span class="order-no">订单号：{{ order.orderNo }}</span>
          <span class="status" :class="statusClass(order.status)">{{ order.statusDesc || statusText(order.status) }}</span>
        </div>
        <div class="order-body">
          <p>商品 ID：{{ order.productId }} · 数量：{{ order.count }} · 金额：¥{{ (order.amount || 0).toFixed(2) }}</p>
          <p class="time">{{ order.createTime }}</p>
        </div>
        <div class="order-actions">
          <router-link :to="`/orders/${order.id}`" class="btn btn-secondary">查看详情</router-link>
          <button
            v-if="order.status === 0"
            class="btn btn-secondary"
            @click="cancel(order.id)"
          >
            取消订单
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getMyOrders, cancelOrder } from '../api/order'

const list = ref([])
const loading = ref(true)

const STATUS_MAP = {
  0: '待支付',
  1: '已支付',
  2: '已发货',
  3: '已完成',
  4: '已取消',
}

function statusText(code) {
  return STATUS_MAP[code] ?? '未知'
}

function statusClass(code) {
  const map = { 0: 'pending', 1: 'paid', 2: 'shipped', 3: 'done', 4: 'cancelled' }
  return map[code] ?? ''
}

onMounted(async () => {
  try {
    const res = await getMyOrders()
    const data = res.data
    list.value = data?.records ?? data ?? []
  } catch (_) {
    list.value = []
  } finally {
    loading.value = false
  }
})

async function cancel(id) {
  if (!confirm('确定取消该订单？')) return
  try {
    await cancelOrder(id)
    const idx = list.value.findIndex((o) => o.id === id)
    if (idx >= 0) list.value[idx].status = 4
    if (list.value[idx]) list.value[idx].statusDesc = '已取消'
  } catch (e) {
    alert(e.message || '取消失败')
  }
}
</script>

<style scoped>
.my-orders h2 {
  margin-bottom: 1rem;
}

.loading,
.empty {
  color: var(--text-muted);
  padding: 2rem;
  text-align: center;
}

.order-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.order-card {
  padding: 1rem;
}

.order-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.75rem;
  padding-bottom: 0.5rem;
  border-bottom: 1px solid var(--border);
}

.order-no {
  font-weight: 500;
}

.status {
  font-size: 0.9rem;
  padding: 0.2rem 0.5rem;
  border-radius: 4px;
}

.status.pending {
  color: var(--warning);
  background: rgba(210, 153, 34, 0.15);
}

.status.paid,
.status.shipped {
  color: var(--accent);
  background: rgba(88, 166, 255, 0.15);
}

.status.done {
  color: var(--success);
  background: rgba(63, 185, 80, 0.15);
}

.status.cancelled {
  color: var(--text-muted);
}

.order-body p {
  margin-bottom: 0.25rem;
  font-size: 0.9rem;
}

.time {
  color: var(--text-muted);
  font-size: 0.85rem;
}

.order-actions {
  margin-top: 0.75rem;
  display: flex;
  gap: 0.5rem;
}
</style>
