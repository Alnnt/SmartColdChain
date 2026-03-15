<template>
  <div class="page">
    <header class="page-header">
      <h1>订单列表</h1>
      <button type="button" class="btn btn-primary" @click="loadOrders" :disabled="loading">
        {{ loading ? '刷新中…' : '刷新' }}
      </button>
    </header>
    <div class="card table-card">
      <p v-if="error" class="error-msg">{{ error }}</p>
      <div v-else-if="loading && !orders.length" class="empty">加载中…</div>
      <div v-else-if="!orders.length" class="empty">暂无订单</div>
      <table v-else class="data-table">
        <thead>
          <tr>
            <th>订单编号</th>
            <th>商品</th>
            <th>数量</th>
            <th>金额</th>
            <th>状态</th>
            <th>创建时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="o in orders" :key="o.id">
            <td>{{ o.orderNo }}</td>
            <td>{{ o.productName || `商品ID: ${o.productId}` }}</td>
            <td>{{ o.count }}</td>
            <td>¥{{ (o.amount != null ? Number(o.amount) : 0).toFixed(2) }}</td>
            <td>{{ o.statusDesc ?? statusText(o.status) }}</td>
            <td>{{ o.createTime }}</td>
            <td>
              <button
                v-if="o.status === 1"
                type="button"
                class="btn btn-primary btn-sm"
                :disabled="shippingId === o.id"
                @click="ship(o)"
              >
                {{ shippingId === o.id ? '发货中…' : '发货' }}
              </button>
              <span v-else class="muted">{{ o.status === 2 ? '已发货' : '' }}</span>
            </td>
          </tr>
        </tbody>
      </table>
      <div v-if="total > pageSize" class="pagination">
        <button type="button" class="btn btn-secondary btn-sm" :disabled="page <= 1" @click="goPage(page - 1)">
          上一页
        </button>
        <span class="page-info">第 {{ page }} / {{ totalPages }} 页，共 {{ total }} 条</span>
        <button type="button" class="btn btn-secondary btn-sm" :disabled="page >= totalPages" @click="goPage(page + 1)">
          下一页
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { getManagerOrders, shipOrder } from '../api/order'
import { useAuth } from '../composables/useAuth'

const { effectiveWarehouseId } = useAuth()

const orders = ref([])
const loading = ref(false)
const error = ref('')
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const shippingId = ref(null)

const warehouseIdParam = computed(() => effectiveWarehouseId.value || undefined)
const totalPages = computed(() => Math.max(1, Math.ceil(total.value / pageSize.value)))

function statusText(s) {
  const map = { 0: '待支付', 1: '已支付', 2: '已发货', 3: '已完成', 4: '已取消' }
  return map[s] ?? ''
}

async function loadOrders() {
  loading.value = true
  error.value = ''
  try {
    const res = await getManagerOrders(warehouseIdParam.value, page.value, pageSize.value)
    const data = res?.data
    orders.value = data?.records ?? []
    total.value = data?.total ?? 0
  } catch (e) {
    error.value = e.message || '加载失败'
    orders.value = []
  } finally {
    loading.value = false
  }
}

function goPage(p) {
  if (p < 1 || p > totalPages.value) return
  page.value = p
  loadOrders()
}

async function ship(o) {
  shippingId.value = o.id
  try {
    await shipOrder(o.id)
    await loadOrders()
  } catch (e) {
    error.value = e.message || '发货失败'
  } finally {
    shippingId.value = null
  }
}

watch(effectiveWarehouseId, () => {
  page.value = 1
  loadOrders()
})

onMounted(() => {
  loadOrders()
})
</script>

<style scoped>
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 1.25rem;
}

.page-header h1 {
  font-size: 1.25rem;
  font-weight: 600;
}

.table-card {
  overflow-x: auto;
}

.empty {
  padding: 2rem;
  text-align: center;
  color: var(--text-muted);
}

.muted {
  color: var(--text-muted);
  font-size: 0.875rem;
}

.pagination {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-top: 1rem;
  padding-top: 1rem;
  border-top: 1px solid var(--border);
}

.page-info {
  font-size: 0.875rem;
  color: var(--text-muted);
}
</style>
