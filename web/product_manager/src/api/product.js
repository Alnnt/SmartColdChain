import request from './request'

/** 管理端：分页查询商品 */
export function getProductPage(params = {}) {
  return request.get('/product/manager/page', { params: { page: 1, pageSize: 20, ...params } })
}

/** 管理端：根据ID查询商品（id 传字符串避免大数溢出） */
export function getProductById(id) {
  return request.get(`/product/manager/${String(id)}`)
}

/** 管理端：新增商品 */
export function createProduct(data) {
  return request.post('/product/manager', data)
}

/** 管理端：更新商品（id 传字符串避免大数溢出） */
export function updateProduct(id, data) {
  return request.put(`/product/manager/${String(id)}`, data)
}

/** 管理端：删除商品（id 传字符串避免大数溢出） */
export function deleteProduct(id) {
  return request.delete(`/product/manager/${String(id)}`)
}
