import axios from 'axios'
import { ElMessage } from 'element-plus'

/**
 * Axios 实例 + 统一拦截器。
 * - 请求：自动注入 Authorization: Bearer <token>
 * - 响应：解包 Result<T>；code===401 清 token 跳登录；code!==200 弹错并 reject
 */
const service = axios.create({
  baseURL: '/api',
  timeout: 30000
})

// token 从 localStorage 读取（与 Pinia user store 共享同一个 key）
export const TOKEN_KEY = 'dormity_token'

service.interceptors.request.use((config) => {
  const token = localStorage.getItem(TOKEN_KEY)
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

service.interceptors.response.use(
  (response) => {
    const res = response.data
    // 后端 Result 约定：code === 200 成功
    if (res && typeof res === 'object' && 'code' in res) {
      if (res.code === 401 || res.code === 403) {
        handleUnauthorized(res.code === 403 ? '无权访问' : '登录已过期')
        return Promise.reject(new Error(res.message || '未授权'))
      }
      if (res.code !== 200) {
        ElMessage.error(res.message || '请求失败')
        return Promise.reject(new Error(res.message || `错误码 ${res.code}`))
      }
      return res.data
    }
    return res
  },
  (error) => {
    const status = error.response?.status
    if (status === 401 || status === 403) {
      handleUnauthorized(status === 403 ? '无权访问' : '登录已过期')
    } else {
      ElMessage.error(error.response?.data?.message || error.message || '网络错误')
    }
    return Promise.reject(error)
  }
)

function handleUnauthorized(msg) {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem('dormity_user')
  ElMessage.warning(msg)
  if (!window.location.pathname.startsWith('/login')) {
    window.location.href = '/login'
  }
}

export default service
