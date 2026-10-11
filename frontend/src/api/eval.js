import request from './request'

// ============ 违纪（/api/eval/violations）============

// 违纪类型（字典性质）
export function listViolationTypes(params) {
  return request({ url: '/eval/violation-types', method: 'get', params })
}

export function saveViolationType(data) {
  return request({ url: '/eval/violation-types', method: data.id ? 'put' : 'post', data })
}

// 分页违纪记录（辅导员查本系；学生查自己的 /mine）
export function pageViolations(params) {
  return request({ url: '/eval/violations', method: 'get', params })
}

// 我的违纪（学生）
export function myViolations(params) {
  return request({ url: '/eval/violations/mine', method: 'get', params })
}

// 录入违纪
export function createViolation(data) {
  return request({ url: '/eval/violations', method: 'post', data })
}

// 处理违纪（辅导员）
export function handleViolation(data) {
  return request({ url: '/eval/violations/handle', method: 'post', data })
}

// 违纪统计（辅导员看板：本系按类型分布）
export function violationStats(params) {
  return request({ url: '/eval/violations/stats', method: 'get', params })
}

// ============ 看板（/api/eval/dashboard）============

// 全局看板（管理员：入住率/收缴率/报修时效/违纪趋势/卫生Top）
export function globalDashboard(params) {
  return request({ url: '/eval/dashboard/global', method: 'get', params })
}

// 辅导员看板（本系）
export function counselorDashboard(params) {
  return request({ url: '/eval/dashboard/counselor', method: 'get', params })
}

// 学生工作台聚合数据（我的宿舍/床位/本月水电/报修/违纪/评优）
export function studentWorkbench() {
  return request({ url: '/eval/dashboard/workbench', method: 'get' })
}
