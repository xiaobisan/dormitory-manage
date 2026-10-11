import request from './request'

// 登录 -> {token, user}
export function login(data) {
  return request({ url: '/auth/login', method: 'post', data })
}

// 注册
export function register(data) {
  return request({ url: '/auth/register', method: 'post', data })
}

// 当前用户信息
export function getMe() {
  return request({ url: '/auth/me', method: 'get' })
}
