<template>
  <div class="page">
    <header class="page-header">
      <h1>仓库管理</h1>
      <button type="button" class="btn btn-primary" @click="openEdit(null)" :disabled="loading">
        新增仓库
      </button>
    </header>

    <div class="card map-card">
      <div ref="mapRef" class="map-container"></div>
    </div>

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
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="w in warehouses" :key="w.id">
            <td>{{ w.id }}</td>
            <td>{{ w.name }}</td>
            <td>{{ w.address || '—' }}</td>
            <td>{{ w.latitude ?? '—' }}</td>
            <td>{{ w.longitude ?? '—' }}</td>
            <td>
              <button type="button" class="btn btn-secondary btn-sm" @click="openEdit(w)">编辑</button>
              <button type="button" class="btn btn-danger btn-sm" @click="confirmDelete(w)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-if="showModal" class="modal-overlay" @click.self="showModal = false">
      <div class="modal card">
        <h3>{{ editingId ? '编辑仓库' : '新增仓库' }}</h3>
        <form @submit.prevent="submitForm" class="form">
          <div class="field">
            <label class="label">仓库名称</label>
            <input v-model="form.name" class="input" type="text" required placeholder="请输入仓库名称" />
          </div>
          <div class="field">
            <label class="label">地址</label>
            <input v-model="form.address" class="input" type="text" required placeholder="请输入地址" />
          </div>
          <div class="field row">
            <div class="half">
              <label class="label">纬度</label>
              <input v-model.number="form.latitude" class="input" type="number" step="any" required placeholder="纬度" />
            </div>
            <div class="half">
              <label class="label">经度</label>
              <input v-model.number="form.longitude" class="input" type="number" step="any" required placeholder="经度" />
            </div>
          </div>
          <p v-if="modalError" class="error-msg">{{ modalError }}</p>
          <div class="modal-actions">
            <button type="button" class="btn btn-secondary" @click="showModal = false">取消</button>
            <button type="submit" class="btn btn-primary" :disabled="saving">保存</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'
import { getWarehouses, createWarehouse, updateWarehouse, deleteWarehouse } from '../api/inventory'

const mapRef = ref(null)
let map = null
let markersLayer = null

const warehouses = ref([])
const loading = ref(false)
const error = ref('')
const showModal = ref(false)
const editingId = ref(null)
const form = ref({ name: '', address: '', latitude: 39.9, longitude: 116.4 })
const modalError = ref('')
const saving = ref(false)

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

function initMap() {
  if (!mapRef.value || map) return
  map = L.map(mapRef.value).setView([39.9, 116.4], 4)
  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '&copy; OpenStreetMap',
  }).addTo(map)
  markersLayer = L.layerGroup().addTo(map)
}

function updateMarkers() {
  if (!markersLayer || !map) return
  markersLayer.clearLayers()
  const list = warehouses.value.filter((w) => w.latitude != null && w.longitude != null)
  const icon = L.divIcon({ className: 'wh-marker', html: '<span></span>', iconSize: [20, 20], iconAnchor: [10, 10] })
  list.forEach((w) => {
    const marker = L.marker([w.latitude, w.longitude], { icon })
      .bindPopup(`<b>${(w.name || '').replace(/</g, '&lt;')}</b><br/>${(w.address || '').replace(/</g, '&lt;')}`)
      .on('click', () => openEdit(w))
    markersLayer.addLayer(marker)
  })
  if (list.length === 1) {
    map.setView([list[0].latitude, list[0].longitude], 12)
  } else if (list.length > 1) {
    const bounds = L.latLngBounds(list.map((w) => [w.latitude, w.longitude]))
    map.fitBounds(bounds, { padding: [30, 30] })
  }
}

watch(warehouses, updateMarkers, { deep: true })

function openEdit(w) {
  editingId.value = w ? String(w.id) : null
  form.value = w
    ? { name: w.name, address: w.address || '', latitude: w.latitude ?? 39.9, longitude: w.longitude ?? 116.4 }
    : { name: '', address: '', latitude: 39.9, longitude: 116.4 }
  modalError.value = ''
  showModal.value = true
}

async function submitForm() {
  saving.value = true
  modalError.value = ''
  try {
    if (editingId.value) {
      await updateWarehouse(editingId.value, form.value)
    } else {
      await createWarehouse(form.value)
    }
    showModal.value = false
    await loadWarehouses()
  } catch (e) {
    modalError.value = e.message || '保存失败'
  } finally {
    saving.value = false
  }
}

function confirmDelete(w) {
  if (!confirm(`确定删除仓库「${w.name}」吗？`)) return
  deleteWarehouse(String(w.id))
    .then(() => loadWarehouses())
    .catch((e) => (error.value = e.message || '删除失败'))
}

onMounted(() => {
  initMap()
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

.map-card {
  margin-bottom: 1.25rem;
  padding: 0;
  overflow: hidden;
}

.map-container {
  height: 320px;
  width: 100%;
}

.table-card {
  overflow-x: auto;
}

.empty {
  padding: 2rem;
  text-align: center;
  color: var(--text-muted);
}

.btn-sm {
  margin-right: 0.5rem;
}

.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
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

.form .field {
  margin-bottom: 1rem;
}

.form .row {
  display: flex;
  gap: 1rem;
}

.form .half {
  flex: 1;
}

.modal-actions {
  display: flex;
  gap: 0.75rem;
  justify-content: flex-end;
  margin-top: 1.25rem;
}

:deep(.wh-marker) {
  background: var(--accent, #238636);
  border: 2px solid #fff;
  border-radius: 50%;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.3);
}
</style>
