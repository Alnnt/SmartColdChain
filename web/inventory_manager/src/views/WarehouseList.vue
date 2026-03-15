<template>
  <div class="page">
    <header class="page-header">
      <h1>仓库管理</h1>
      <button type="button" class="btn btn-primary" @click="loadWarehouses" :disabled="loading">
        {{ loading ? '刷新中…' : '刷新' }}
      </button>
    </header>
    <div class="card table-card">
      <p v-if="error" class="error-msg">{{ error }}</p>
      <div v-else-if="loading && !warehouses.length" class="empty">加载中…</div>
      <div v-else-if="!warehouses.length" class="empty">暂无仓库</div>
      <table v-else class="data-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>仓库名称</th>
            <th>地址</th>
            <th>纬度</th>
            <th>经度</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="w in warehouses" :key="w.id">
            <td>{{ w.id }}</td>
            <td>{{ w.name }}</td>
            <td>{{ w.address || '—' }}</td>
            <td>{{ w.latitude ?? '—' }}</td>
            <td>{{ w.longitude ?? '—' }}</td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getWarehouses } from '../api/inventory'

const warehouses = ref([])
const loading = ref(false)
const error = ref('')

async function loadWarehouses() {
  loading.value = true
  error.value = ''
  try {
    const res = await getWarehouses()
    warehouses.value = res?.data ?? []
  } catch (e) {
    error.value = e.message || '加载失败'
    warehouses.value = []
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadWarehouses()
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
</style>
