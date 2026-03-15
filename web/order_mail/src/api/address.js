import request from './request'

export function getAddressList() {
  return request.get('/user/address/list')
}

export function getAddressById(addressId) {
  return request.get(`/user/address/${addressId}`)
}

export function createAddress(data) {
  return request.post('/user/address', data)
}

export function updateAddress(data) {
  return request.put('/user/address', data)
}

export function deleteAddress(addressId) {
  return request.delete(`/user/address/${addressId}`)
}

export function setDefaultAddress(addressId) {
  return request.put(`/user/address/${addressId}/default`)
}
