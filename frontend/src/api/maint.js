import request from './request'

// ============ 卫生检查计划（/api/maint/check-plans）============

export function pageCheckPlans(params) {
  return request({ url: '/maint/check-plans', method: 'get', params })
}

export function getCheckPlan(id) {
  return request({ url: `/maint/check-plans/${id}`, method: 'get' })
}

// 新增/修改计划（itemTemplate: JSON 字符串，检查项模板）
export function saveCheckPlan(data) {
  return request({ url: '/maint/check-plans', method: data.id ? 'put' : 'post', data })
}

export function deleteCheckPlan(id) {
  return request({ url: `/maint/check-plans/${id}`, method: 'delete' })
}

// ============ 卫生检查记录（/api/maint/check-records）============

// 分页记录（可按 roomId / planId / period 过滤）
export function pageCheckRecords(params) {
  return request({ url: '/maint/check-records', method: 'get', params })
}

// 检查员打分提交（planId, roomId, score, detail: JSON 各检查项得分）
export function submitCheckRecord(data) {
  return request({ url: '/maint/check-records', method: 'post', data })
}

// 宿舍历史检查（查分）
export function roomCheckRecords(roomId) {
  return request({ url: `/maint/check-records/room/${roomId}`, method: 'get' })
}

// ============ 评优（/api/maint/evaluation）============

// 规则列表
export function listEvaluationRules(params) {
  return request({ url: '/maint/evaluation/rules', method: 'get', params })
}

export function saveEvaluationRule(data) {
  return request({ url: '/maint/evaluation/rules', method: data.id ? 'put' : 'post', data })
}

export function deleteEvaluationRule(id) {
  return request({ url: `/maint/evaluation/rules/${id}`, method: 'delete' })
}

// 触发计算（period 如 2026-10，按当前 active 规则算分并生成星级）
export function calculateEvaluation(data) {
  return request({ url: '/maint/evaluation/calculate', method: 'post', data })
}

// 分页评优得分（period 过滤）
export function pageEvaluationScores(params) {
  return request({ url: '/maint/evaluation/scores', method: 'get', params })
}

// 我的评优结果（学生查分）
export function myEvaluationScores(params) {
  return request({ url: '/maint/evaluation/scores/mine', method: 'get', params })
}

// 评优结果/称号（公示 + 初审/终审）
export function pageEvaluationAwards(params) {
  return request({ url: '/maint/evaluation/awards', method: 'get', params })
}

// 初审（宿管：awardId, final_status: INITIAL/REJECT, note）
export function initialReviewAward(data) {
  return request({ url: '/maint/evaluation/awards/initial', method: 'post', data })
}

// 终审（辅导员/管理员）
export function finalReviewAward(data) {
  return request({ url: '/maint/evaluation/awards/final', method: 'post', data })
}
