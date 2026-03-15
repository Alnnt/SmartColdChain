import request from './request'

/** 管理员登录（商品管理员、超级管理员使用） */
export function login(data) {
  return request.post('/auth/login', data)
}

export function getUserInfo() {
  return request.get('/auth/info')
}

export function logout() {
  return request.post('/auth/logout')
}
