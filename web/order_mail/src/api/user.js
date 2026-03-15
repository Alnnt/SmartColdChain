import request from './request'

export function getProfile() {
  return request.get('/user/info')
}

export function updateProfile(data) {
  return request.put('/user/info', null, {
    params: { nickname: data.nickname, avatar: data.avatar },
  })
}

export function changePassword(oldPassword, newPassword) {
  return request.put('/user/password', null, {
    params: { oldPassword, newPassword },
  })
}
