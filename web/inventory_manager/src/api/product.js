import request from './request'

/** 分页查询商品（添加库存时选品用） */
export function getProductPage(params = {}) {
  return request.get('/product/manager/page', { params: { page: 1, pageSize: 100, ...params } })
}
