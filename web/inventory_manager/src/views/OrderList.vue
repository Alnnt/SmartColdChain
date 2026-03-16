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
            <th>商品明细</th>
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
            <td class="items-cell" @click="o.items?.length && openDetail(o)">
              <template v-if="o.items?.length">
                <div v-for="(item, idx) in o.items" :key="item.id || idx" class="item-line">
                  {{ item.productName || '—' }} {{ item.count ?? 0 }}
                </div>
                <span class="detail-hint">点击查看详情</span>
              </template>
              <span v-else class="muted">—</span>
            </td>
            <td>
              <template v-if="o.items?.length">
                {{ o.items.reduce((s, it) => s + (it.count || 0), 0) }}
              </template>
              <span v-else>—</span>
            </td>
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

    <!-- 商品明细弹窗 -->
    <div v-if="detailOrder" class="modal-overlay" @click.self="detailOrder = null">
      <div class="modal-box">
        <div class="modal-header">
          <h3>订单 {{ detailOrder.orderNo }} 商品明细</h3>
          <button type="button" class="modal-close" aria-label="关闭" @click="detailOrder = null">×</button>
        </div>
        <div class="modal-body">
          <table class="detail-table">
            <thead>
              <tr>
                <th>商品名</th>
                <th>数量</th>
                <th>商品ID</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(item, idx) in detailOrder.items" :key="item.id || idx">
                <td>{{ item.productName || '—' }}</td>
                <td>{{ item.count ?? 0 }}</td>
                <td>{{ item.productId ?? '—' }}</td>
              </tr>
            </tbody>
          </table>
        </div>
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
const detailOrder = ref(null)

function openDetail(order) {
  detailOrder.value = order
}

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

.items-cell {
  max-width: 260px;
  cursor: pointer;
  user-select: none;
}

.items-cell:hover .detail-hint {
  opacity: 1;
}

.item-line {
  font-size: 0.875rem;
  padding: 0.2rem 0;
  line-height: 1.4;
}

.item-line + .item-line {
  border-top: 1px solid var(--border, #eee);
}

.detail-hint {
  font-size: 0.75rem;
  color: var(--text-muted);
  opacity: 0.7;
  margin-top: 0.25rem;
  display: inline-block;
}

/* 弹窗 */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-box {
  background: var(--bg, #fff);
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  min-width: 360px;
  max-width: 90vw;
  max-height: 80vh;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1rem 1.25rem;
  border-bottom: 1px solid var(--border, #eee);
}

.modal-header h3 {
  margin: 0;
  font-size: 1rem;
  font-weight: 600;
}

.modal-close {
  background: none;
  border: none;
  font-size: 1.5rem;
  line-height: 1;
  cursor: pointer;
  color: var(--text-muted);
  padding: 0 0.25rem;
}

.modal-close:hover {
  color: var(--text, #333);
}

.modal-body {
  padding: 1rem 1.25rem;
  overflow: auto;
}

.detail-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 0.875rem;
}

.detail-table th,
.detail-table td {
  padding: 0.5rem 0.75rem;
  text-align: left;
  border-bottom: 1px solid var(--border, #eee);
}

.detail-table th {
  font-weight: 600;
  color: var(--text-muted);
}
</style>
