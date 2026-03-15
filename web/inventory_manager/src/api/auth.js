import request from './request'

/** 管理员登录（库存管理端：超级管理员、仓库管理员使用） */
export function login(data) {
  return request.post('/auth/login', data)
}

export function getUserInfo() {
  return request.get('/auth/info')
}

export function logout() {
  return request.post('/auth/logout')
}
