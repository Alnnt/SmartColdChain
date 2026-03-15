import request from './request'

export function login(data) {
  return request.post('/auth/user/login', data)
}

export function getUserInfo() {
  return request.get('/auth/info')
}

export function logout() {
  return request.post('/auth/logout')
}
