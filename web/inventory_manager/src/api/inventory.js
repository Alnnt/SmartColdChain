import request from './request'

/** 管理端：仓库列表 */
export function getWarehouses() {
  return request.get('/inventory/manager/warehouses')
}

/** 管理端：库存列表 */
export function getInventoryItems() {
  return request.get('/inventory/manager/items')
}

/** 管理端：调整库存 delta 正数增加、负数减少（inventoryId 传字符串避免大数溢出） */
export function adjustStock(inventoryId, delta) {
  return request.post('/inventory/manager/adjust', { inventoryId: String(inventoryId), delta })
}
