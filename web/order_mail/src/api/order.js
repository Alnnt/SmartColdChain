import request from './request'

export function createOrder(data) {
  return request.post('/order/create', data)
}

export function getMyOrders(params = {}) {
  return request.get('/order/my', { params: { page: 1, pageSize: 20, ...params } })
}

export function getOrderById(orderId) {
  return request.get(`/order/${orderId}`)
}

export function getOrderByNo(orderNo) {
  return request.get(`/order/no/${orderNo}`)
}

export function cancelOrder(orderId) {
  return request.put(`/order/${orderId}/cancel`)
}
