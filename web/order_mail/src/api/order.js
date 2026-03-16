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

/**
 * 支付订单（模拟支付完成后调用，会执行实际扣减库存与创建运单）
 * @param {{ orderNo: string, paidAmount: number, paymentTime?: number }} data
 */
export function payOrder(data) {
  return request.post('/order/payment/callback', {
    orderNo: data.orderNo,
    paidAmount: data.paidAmount,
    paymentTime: data.paymentTime ?? Date.now(),
  })
}
