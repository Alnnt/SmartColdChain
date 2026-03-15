import request from './request'

/** 分页查询商品（商城浏览，支持瀑布流） */
export function getProductPage(params = {}) {
  return request.get('/product/page', { params: { page: 1, pageSize: 12, ...params } })
}

/** 根据ID查询商品详情（id 传字符串避免大数溢出） */
export function getProductById(id) {
  return request.get(`/product/${String(id)}`)
}
