import request from './request'

// ============ 用户（/api/system）============

// 分页用户列表
export function pageUsers(params) {
  return request({ url: '/system/users', method: 'get', params })
}

// 用户详情
export function getUser(id) {
  return request({ url: `/system/users/${id}`, method: 'get' })
}

// 新增用户
export function createUser(data) {
  return request({ url: '/system/users', method: 'post', data })
}

// 修改用户
export function updateUser(data) {
  return request({ url: '/system/users', method: 'put', data })
}

// 删除用户
export function deleteUser(id) {
  return request({ url: `/system/users/${id}`, method: 'delete' })
}

// ============ 字典（/api/system）============

// 按类型取字典项，type 可空表示全部
export function listDictItems(type) {
  return request({ url: '/system/dict/items', method: 'get', params: { type } })
}

// 分页字典类型
export function pageDictTypes(params) {
  return request({ url: '/system/dict/types', method: 'get', params })
}

// 新增字典项
export function createDictItem(data) {
  return request({ url: '/system/dict/items', method: 'post', data })
}

// 修改字典项
export function updateDictItem(data) {
  return request({ url: '/system/dict/items', method: 'put', data })
}

// 删除字典项
export function deleteDictItem(id) {
  return request({ url: `/system/dict/items/${id}`, method: 'delete' })
}
