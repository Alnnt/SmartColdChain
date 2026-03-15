<template>
  <div class="page">
    <header class="page-header">
      <h1>商品列表（分页）</h1>
      <button type="button" class="btn btn-primary" @click="openEdit(null)">
        新增商品
      </button>
    </header>
    <div class="card table-card">
      <p v-if="error" class="error-msg">{{ error }}</p>
      <div v-else-if="loading && !list.length" class="empty">加载中…</div>
      <div v-else-if="!list.length" class="empty">暂无商品，点击「新增商品」添加</div>
      <table v-else class="data-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>预览图</th>
            <th>名称</th>
            <th>价格</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="row in list" :key="row.id">
            <td>{{ row.id }}</td>
            <td>
              <img v-if="row.img" :src="row.img" alt="" class="thumb" />
              <span v-else class="no-img">—</span>
            </td>
            <td>{{ row.name }}</td>
            <td>¥{{ (row.price != null ? Number(row.price) : 0).toFixed(2) }}</td>
            <td>
              <button type="button" class="btn btn-secondary btn-sm" @click="openEdit(row)">编辑</button>
              <button type="button" class="btn btn-danger btn-sm" @click="confirmDelete(row)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
      <div v-if="list.length > 0" class="pagination">
        <button type="button" class="btn btn-secondary btn-sm" :disabled="page <= 1" @click="goPage(page - 1)">
          上一页
        </button>
        <span class="page-info">第 {{ page }} / {{ totalPages }} 页，共 {{ total }} 条</span>
        <button type="button" class="btn btn-secondary btn-sm" :disabled="page >= totalPages" @click="goPage(page + 1)">
          下一页
        </button>
      </div>
    </div>

    <!-- 新增/编辑弹窗 -->
    <div v-if="showEdit" class="modal-overlay" @click.self="showEdit = false">
      <div class="modal card">
        <h3>{{ editRow ? '编辑商品' : '新增商品' }}</h3>
        <form @submit.prevent="submitEdit" class="form-edit">
          <div class="field">
            <label class="label">商品名称</label>
            <input v-model="form.name" class="input" type="text" required placeholder="请输入商品名称" />
          </div>
          <div class="field">
            <label class="label">价格</label>
            <input v-model.number="form.price" class="input" type="number" step="0.01" min="0" required placeholder="0.00" />
          </div>
          <div class="field">
            <label class="label">预览图 URL</label>
            <input v-model="form.img" class="input" type="url" placeholder="https://..." />
            <p class="hint">请输入完整的图片链接地址</p>
          </div>
          <p v-if="editError" class="error-msg">{{ editError }}</p>
          <p v-if="editSuccess" class="success-msg">{{ editSuccess }}</p>
          <div class="modal-actions">
            <button type="button" class="btn btn-secondary" @click="showEdit = false">取消</button>
            <button type="submit" class="btn btn-primary" :disabled="submitting">保存</button>
          </div>
        </form>
      </div>
    </div>

    <!-- 删除确认 -->
    <div v-if="showDelete" class="modal-overlay" @click.self="showDelete = false">
      <div class="modal card">
        <h3>确认删除</h3>
        <p>确定要删除商品「{{ deleteRow?.name }}」吗？</p>
        <div class="modal-actions">
          <button type="button" class="btn btn-secondary" @click="showDelete = false">取消</button>
          <button type="button" class="btn btn-danger" :disabled="deleting" @click="doDelete">删除</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { getProductPage, createProduct, updateProduct, deleteProduct } from '../api/product'

const list = ref([])
const loading = ref(false)
const error = ref('')
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const totalPages = computed(() => Math.max(1, Math.ceil(total.value / pageSize.value)))

const showEdit = ref(false)
const editRow = ref(null)
const form = reactive({ name: '', price: '', img: '' })
const editError = ref('')
const editSuccess = ref('')
const submitting = ref(false)

const showDelete = ref(false)
const deleteRow = ref(null)
const deleting = ref(false)

async function load() {
  loading.value = true
  error.value = ''
  try {
    const res = await getProductPage({ page: page.value, pageSize: pageSize.value })
    const data = res?.data
    list.value = data?.records ?? []
    total.value = data?.total ?? 0
  } catch (e) {
    error.value = e.message || '加载失败'
    list.value = []
  } finally {
    loading.value = false
  }
}

function openEdit(row) {
  editRow.value = row
  form.name = row ? row.name : ''
  form.price = row != null && row.price != null ? Number(row.price) : ''
  form.img = row?.img ?? ''
  editError.value = ''
  editSuccess.value = ''
  showEdit.value = true
}

async function submitEdit() {
  editError.value = ''
  editSuccess.value = ''
  if (!form.name || form.price === '' || form.price == null) {
    editError.value = '请填写名称和价格'
    return
  }
  submitting.value = true
  try {
    if (editRow.value) {
      await updateProduct(editRow.value.id, {
        name: form.name,
        price: Number(form.price),
        img: form.img || undefined,
      })
      editSuccess.value = '更新成功'
    } else {
      await createProduct({
        name: form.name,
        price: Number(form.price),
        img: form.img || undefined,
      })
      editSuccess.value = '创建成功'
    }
    await load()
    setTimeout(() => {
      showEdit.value = false
    }, 800)
  } catch (e) {
    editError.value = e.message || '保存失败'
  } finally {
    submitting.value = false
  }
}

function confirmDelete(row) {
  deleteRow.value = row
  showDelete.value = true
}

async function doDelete() {
  if (!deleteRow.value) return
  deleting.value = true
  try {
    await deleteProduct(deleteRow.value.id)
    await load()
    showDelete.value = false
    deleteRow.value = null
  } catch (e) {
    error.value = e.message || '删除失败'
  } finally {
    deleting.value = false
  }
}

function goPage(p) {
  const next = Math.max(1, Math.min(totalPages.value, p))
  page.value = next
  load()
}

onMounted(() => {
  load()
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

.thumb {
  width: 48px;
  height: 48px;
  object-fit: cover;
  border-radius: 4px;
}

.no-img {
  color: var(--text-muted);
  font-size: 0.875rem;
}

.empty {
  padding: 2rem;
  text-align: center;
  color: var(--text-muted);
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
  max-width: 420px;
}

.modal h3 {
  margin-bottom: 1rem;
  font-size: 1.1rem;
}

.form-edit .field {
  margin-bottom: 1rem;
}

.hint {
  font-size: 0.75rem;
  color: var(--text-muted);
  margin-top: 0.25rem;
}

.modal-actions {
  display: flex;
  gap: 0.75rem;
  justify-content: flex-end;
  margin-top: 1rem;
}

.btn-sm {
  margin-right: 0.5rem;
}
</style>
