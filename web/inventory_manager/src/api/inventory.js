import request from './request'

/** 管理端：仓库列表（可选 warehouseId 过滤） */
export function getWarehouses(warehouseId) {
  const params = warehouseId != null ? { warehouseId } : {}
  return request.get('/inventory/manager/warehouses', { params })
}

/** 管理端：单个仓库 */
export function getWarehouse(id) {
  return request.get(`/inventory/manager/warehouse/${id}`)
}

/** 管理端：创建仓库 */
export function createWarehouse(data) {
  return request.post('/inventory/manager/warehouse', data)
}

/** 管理端：更新仓库 */
export function updateWarehouse(id, data) {
  return request.put(`/inventory/manager/warehouse/${id}`, data)
}

/** 管理端：删除仓库 */
export function deleteWarehouse(id) {
  return request.delete(`/inventory/manager/warehouse/${id}`)
}

/** 管理端：库存列表（可选 warehouseId 过滤） */
export function getInventoryItems(warehouseId) {
  const params = warehouseId != null ? { warehouseId } : {}
  return request.get('/inventory/manager/items', { params })
}

/** 管理端：调整库存 delta 正数增加、负数减少（inventoryId 传字符串避免大数溢出） */
export function adjustStock(inventoryId, delta) {
  return request.post('/inventory/manager/adjust', { inventoryId: String(inventoryId), delta })
}

/** 管理端：添加库存商品（warehouseId 可选，仓管时用绑定仓） */
export function addInventoryItem(payload) {
  return request.post('/inventory/manager/items', {
    warehouseId: payload.warehouseId != null ? String(payload.warehouseId) : undefined,
    productId: String(payload.productId),
    quantity: payload.quantity,
  })
}
