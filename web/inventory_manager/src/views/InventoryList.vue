<template>
  <div class="page">
    <header class="page-header">
      <h1>库存列表</h1>
      <div class="header-actions">
        <button type="button" class="btn btn-primary" @click="openAdd" :disabled="loading">添加库存商品</button>
        <button type="button" class="btn btn-secondary" @click="loadItems" :disabled="loading">
          {{ loading ? '刷新中…' : '刷新' }}
        </button>
      </div>
    </header>
    <div class="card table-card">
      <p v-if="error" class="error-msg">{{ error }}</p>
      <div v-else-if="loading && !items.length" class="empty">加载中…</div>
      <div v-else-if="!items.length" class="empty">暂无库存记录</div>
      <table v-else class="data-table">
        <thead>
          <tr>
            <th>库存ID</th>
            <th>商品ID</th>
            <th>商品名称</th>
            <th>仓库</th>
            <th>总库存</th>
            <th>冻结</th>
            <th>可用</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="row in items" :key="row.id">
            <td>{{ row.id }}</td>
            <td>{{ row.productId }}</td>
            <td>{{ productNameMap[row.productId] ?? '—' }}</td>
            <td>{{ row.warehouseName }}</td>
            <td>{{ row.totalStock }}</td>
            <td>{{ row.frozenStock }}</td>
            <td>{{ row.availableStock }}</td>
            <td>
              <button type="button" class="btn btn-secondary btn-sm" @click="openAdjust(row)">
                调整
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 调整库存弹窗 -->
    <div v-if="showAdjust" class="modal-overlay" @click.self="showAdjust = false">
      <div class="modal card">
        <h3>调整库存</h3>
        <p class="muted">仓库：{{ adjustRow?.warehouseName }} · 商品：{{ productNameMap[adjustRow?.productId] || ('ID:' + adjustRow?.productId) }}</p>
        <form @submit.prevent="submitAdjust" class="form-inline">
          <div class="field">
            <label class="label">调整数量（正数增加，负数减少）</label>
            <input v-model.number="adjustDelta" type="number" class="input" required />
          </div>
          <p v-if="adjustError" class="error-msg">{{ adjustError }}</p>
          <p v-if="adjustSuccess" class="success-msg">{{ adjustSuccess }}</p>
          <div class="modal-actions">
            <button type="button" class="btn btn-secondary" @click="closeAdjust">取消</button>
            <button type="submit" class="btn btn-primary" :disabled="adjusting">确定</button>
          </div>
        </form>
      </div>
    </div>

    <!-- 添加库存商品弹窗 -->
    <div v-if="showAdd" class="modal-overlay" @click.self="closeAdd">
      <div class="modal card modal-wide">
        <h3>添加库存商品</h3>
        <form @submit.prevent="submitAdd" class="form">
          <div class="field">
            <label class="label">选择商品</label>
            <select v-model="addProductId" class="input" required>
              <option value="">请选择</option>
              <option v-for="p in productOptions" :key="p.id" :value="String(p.id)">{{ p.name }} (ID: {{ p.id }})</option>
            </select>
          </div>
          <div class="field">
            <label class="label">数量</label>
            <input v-model.number="addQuantity" type="number" min="1" class="input" required />
          </div>
          <p v-if="addError" class="error-msg">{{ addError }}</p>
          <div class="modal-actions">
            <button type="button" class="btn btn-secondary" @click="closeAdd">取消</button>
            <button type="submit" class="btn btn-primary" :disabled="adding">确定</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { getInventoryItems, adjustStock, addInventoryItem } from '../api/inventory'
import { getProductPage } from '../api/product'
import { useAuth } from '../composables/useAuth'

const { effectiveWarehouseId } = useAuth()

const items = ref([])
const loading = ref(false)
const error = ref('')

const showAdjust = ref(false)
const adjustRow = ref(null)
const adjustDelta = ref(0)
const adjustError = ref('')
const adjustSuccess = ref('')
const adjusting = ref(false)

const showAdd = ref(false)
const productOptions = ref([])
const addProductId = ref('')
const addQuantity = ref(1)
const addError = ref('')
const adding = ref(false)

const warehouseIdParam = computed(() => effectiveWarehouseId.value || undefined)
const productNameMap = ref({})

async function loadItems() {
  loading.value = true
  error.value = ''
  try {
    const res = await getInventoryItems(warehouseIdParam.value)
    items.value = res?.data ?? []
    await loadProductNameMap()
  } catch (e) {
    error.value = e.message || '加载失败'
    items.value = []
  } finally {
    loading.value = false
  }
}

async function loadProductNameMap() {
  const map = {}
  try {
    const res = await getProductPage({ pageSize: 500 })
    const data = res?.data
    const list = data?.records ?? data?.list ?? []
    list.forEach((p) => {
      if (p.id != null) map[p.id] = p.name ?? ''
    })
  } catch {
    // ignore
  }
  productNameMap.value = map
}

async function openAdd() {
  addError.value = ''
  addProductId.value = ''
  addQuantity.value = 1
  try {
    const res = await getProductPage()
    const data = res?.data
    productOptions.value = data?.records ?? data?.list ?? []
  } catch {
    productOptions.value = []
  }
  showAdd.value = true
}

function closeAdd() {
  showAdd.value = false
}

async function submitAdd() {
  if (!addProductId.value || addQuantity.value < 1) return
  adding.value = true
  addError.value = ''
  try {
    await addInventoryItem({
      warehouseId: warehouseIdParam.value,
      productId: addProductId.value,
      quantity: addQuantity.value,
    })
    closeAdd()
    await loadItems()
  } catch (e) {
    addError.value = e.message || '添加失败'
  } finally {
    adding.value = false
  }
}

function openAdjust(row) {
  adjustRow.value = row
  adjustDelta.value = 0
  adjustError.value = ''
  adjustSuccess.value = ''
  showAdjust.value = true
}

function closeAdjust() {
  showAdjust.value = false
  adjustRow.value = null
}

async function submitAdjust() {
  if (adjustDelta.value === 0) {
    adjustError.value = '请输入非零的调整数量'
    return
  }
  adjusting.value = true
  adjustError.value = ''
  adjustSuccess.value = ''
  try {
    await adjustStock(adjustRow.value.id, adjustDelta.value)
    adjustSuccess.value = '调整成功'
    await loadItems()
    setTimeout(() => {
      closeAdjust()
    }, 800)
  } catch (e) {
    adjustError.value = e.message || '调整失败'
  } finally {
    adjusting.value = false
  }
}

onMounted(() => {
  loadItems()
})
</script>

<style scoped>
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 1.25rem;
}

.header-actions {
  display: flex;
  gap: 0.5rem;
}

.modal-wide {
  max-width: 480px;
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
  margin-bottom: 1rem;
}

.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
  padding: 1rem;
}

.modal {
  width: 100%;
  max-width: 400px;
}

.modal h3 {
  margin-bottom: 0.5rem;
  font-size: 1.1rem;
}

.form-inline .field {
  margin-bottom: 1rem;
}

.modal-actions {
  display: flex;
  gap: 0.75rem;
  justify-content: flex-end;
  margin-top: 1rem;
}
</style>
