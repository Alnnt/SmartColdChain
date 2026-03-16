<template>
  <div class="order-detail">
    <router-link to="/orders" class="back">← 返回订单列表</router-link>
    <div v-if="loading" class="loading">加载中…</div>
    <div v-else-if="!order" class="empty">订单不存在</div>
    <div v-else class="card detail-card">
      <h2>订单详情</h2>
      <div class="detail-row">
        <span class="label">订单号</span>
        <span>{{ order.orderNo }}</span>
      </div>
      <div class="detail-row">
        <span class="label">订单状态</span>
        <span class="status" :class="statusClass(order.status)">{{ order.statusDesc || statusText(order.status) }}</span>
      </div>
      <div class="detail-row" v-if="order.items?.length">
        <span class="label">商品明细</span>
        <span></span>
      </div>
      <div v-if="order.items?.length" class="item-list">
        <div v-for="(item, idx) in order.items" :key="item.id || idx" class="item-row">
          <span>{{ item.productName || '—' }}</span>
          <span>× {{ item.count || 0 }}</span>
          <span>¥{{ (item.amount || 0).toFixed(2) }}</span>
        </div>
      </div>
      <div class="detail-row">
        <span class="label">订单金额</span>
        <span>¥{{ (order.amount || 0).toFixed(2) }}</span>
      </div>
      <div class="detail-row">
        <span class="label">收货地址</span>
        <span class="address-text">{{ addressText }}</span>
      </div>
      <div class="detail-row" v-if="order.waybillId">
        <span class="label">运单 ID</span>
        <span>{{ order.waybillId }}</span>
      </div>
      <div class="detail-row">
        <span class="label">创建时间</span>
        <span>{{ order.createTime }}</span>
      </div>
      <div v-if="order.status === 0" class="actions">
        <button class="btn btn-secondary" @click="cancel">取消订单</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getOrderById, cancelOrder } from '../api/order'
import { getAddressById } from '../api/address'

const route = useRoute()
const router = useRouter()
const order = ref(null)
const address = ref(null)
const addressLoadFailed = ref(false)
const loading = ref(true)

// 使用字符串 ID，避免大数溢出
const id = computed(() => String(route.params.id ?? ''))

const addressText = computed(() => {
  if (address.value) {
    const a = address.value
    const part = a.fullAddress || [a.province, a.city, a.district, a.detail].filter(Boolean).join(' ')
    return [a.contactName, a.contactPhone, part].filter(Boolean).join(' ')
  }
  if (order.value?.addressId && addressLoadFailed.value) return `地址ID: ${order.value.addressId}`
  if (order.value?.addressId) return '加载中…'
  return '—'
})

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

async function loadAddress(addressId) {
  if (!addressId) {
    address.value = null
    addressLoadFailed.value = false
    return
  }
  addressLoadFailed.value = false
  try {
    const res = await getAddressById(addressId)
    address.value = res?.data ?? null
    if (!address.value) addressLoadFailed.value = true
  } catch (_) {
    address.value = null
    addressLoadFailed.value = true
  }
}

onMounted(async () => {
  try {
    const res = await getOrderById(id.value)
    order.value = res.data
    if (order.value?.addressId) {
      await loadAddress(order.value.addressId)
    }
  } catch (_) {
    order.value = null
  } finally {
    loading.value = false
  }
})

async function cancel() {
  if (!confirm('确定取消该订单？')) return
  try {
    await cancelOrder(id.value)
    order.value = { ...order.value, status: 4, statusDesc: '已取消' }
  } catch (e) {
    alert(e.message || '取消失败')
  }
}
</script>

<style scoped>
.order-detail h2 {
  margin-bottom: 1rem;
}

.back {
  display: inline-block;
  margin-bottom: 1rem;
  color: var(--accent);
  text-decoration: none;
}

.back:hover {
  text-decoration: underline;
}

.loading,
.empty {
  color: var(--text-muted);
  padding: 2rem;
}

.detail-card {
  max-width: 480px;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  padding: 0.5rem 0;
  border-bottom: 1px solid var(--border);
}

.detail-row .label {
  color: var(--text-muted);
}

.address-text {
  max-width: 280px;
  word-break: break-all;
  text-align: right;
}

.item-list {
  margin: 0.5rem 0;
  padding-left: 0.5rem;
  border-left: 2px solid var(--border);
}

.item-row {
  display: flex;
  justify-content: space-between;
  padding: 0.25rem 0;
  font-size: 0.9rem;
}

.status.pending {
  color: var(--warning);
}

.status.paid,
.status.shipped {
  color: var(--accent);
}

.status.done {
  color: var(--success);
}

.status.cancelled {
  color: var(--text-muted);
}

.actions {
  margin-top: 1rem;
}
</style>
