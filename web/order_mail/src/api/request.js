import axios from 'axios'

const request = axios.create({
  baseURL: '/api',
  timeout: 15000,
  headers: { 'Content-Type': 'application/json' },
})

request.interceptors.request.use((config) => {
  const token = localStorage.getItem('accessToken')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

request.interceptors.response.use(
  (res) => {
    const { code, data, message } = res.data ?? {}
    if (code !== 200 && code !== undefined) {
      return Promise.reject(new Error(message || '请求失败'))
    }
    return res.data
  },
  (err) => {
    const msg = err.response?.data?.message || err.message || '网络错误'
    if (err.response?.status === 401) {
      localStorage.removeItem('accessToken')
      localStorage.removeItem('user')
      window.location.hash = '#/login'
    }
    return Promise.reject(new Error(msg))
  }
)

export default request
