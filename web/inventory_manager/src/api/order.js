import request from './request'

/** 管理端：按仓库分页查询订单 */
export function getManagerOrders(warehouseId, page = 1, pageSize = 10) {
  const params = { page, pageSize }
  if (warehouseId != null && warehouseId !== '') params.warehouseId = warehouseId
  return request.get('/order/manager/orders', { params })
}

/** 发货 */
export function shipOrder(orderId) {
  return request.put(`/order/${orderId}/ship`)
}
