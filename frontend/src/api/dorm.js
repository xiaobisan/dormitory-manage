import request from './request'

// ============ 楼栋（/api/dorm/buildings）============

export function pageBuildings(params) {
  return request({ url: '/dorm/buildings', method: 'get', params })
}

export function getBuilding(id) {
  return request({ url: `/dorm/buildings/${id}`, method: 'get' })
}

export function createBuilding(data) {
  return request({ url: '/dorm/buildings', method: 'post', data })
}

export function updateBuilding(data) {
  return request({ url: '/dorm/buildings', method: 'put', data })
}

export function deleteBuilding(id) {
  return request({ url: `/dorm/buildings/${id}`, method: 'delete' })
}

// ============ 宿舍（/api/dorm/rooms）============

export function pageRooms(params) {
  return request({ url: '/dorm/rooms', method: 'get', params })
}

export function createRoom(data) {
  return request({ url: '/dorm/rooms', method: 'post', data })
}

export function updateRoom(data) {
  return request({ url: '/dorm/rooms', method: 'put', data })
}

export function deleteRoom(id) {
  return request({ url: `/dorm/rooms/${id}`, method: 'delete' })
}

// 宿舍下床位
export function listBedsByRoom(roomId) {
  return request({ url: `/dorm/rooms/${roomId}/beds`, method: 'get' })
}

// ============ 床位（/api/dorm/beds）============

export function pageBeds(params) {
  return request({ url: '/dorm/beds', method: 'get', params })
}

// 某楼层/楼栋床位地图数据：按楼栋返回 rooms+beds+入住学生
export function buildingBedMap(buildingId) {
  return request({ url: `/dorm/buildings/${buildingId}/bed-map`, method: 'get' })
}

// ============ 入住/退宿（/api/dorm/enrollments）============

export function pageEnrollments(params) {
  return request({ url: '/dorm/enrollments', method: 'get', params })
}

// 入住办理
export function checkIn(data) {
  return request({ url: '/dorm/enrollments/check-in', method: 'post', data })
}

// 退宿办理
export function checkOut(data) {
  return request({ url: '/dorm/enrollments/check-out', method: 'post', data })
}

// 我的入住信息
export function myEnrollment() {
  return request({ url: '/dorm/enrollments/mine', method: 'get' })
}

// ============ 水电（/api/dorm/usages、/api/dorm/bills、/api/dorm/payments）============

// 分页用量
export function pageUsages(params) {
  return request({ url: '/dorm/usages', method: 'get', params })
}

// 录入用量
export function createUsage(data) {
  return request({ url: '/dorm/usages', method: 'post', data })
}

// 按周期生成账单（入参 BillGenerateDTO: period/buildingId?）
export function generateBills(data) {
  return request({ url: '/dorm/bills/generate', method: 'post', data })
}

// 分页账单
export function pageBills(params) {
  return request({ url: '/dorm/bills', method: 'get', params })
}

// 我的账单
export function myBills(params) {
  return request({ url: '/dorm/bills/mine', method: 'get', params })
}

// 缴费
export function payBill(data) {
  return request({ url: '/dorm/payments', method: 'post', data })
}

// 缴费记录
export function pagePayments(params) {
  return request({ url: '/dorm/payments', method: 'get', params })
}

// ============ 报修（/api/dorm/repairs）============

// 分页工单（维修工/宿管）
export function pageRepairs(params) {
  return request({ url: '/dorm/repairs', method: 'get', params })
}

// 我的报修（学生）
export function myRepairs(params) {
  return request({ url: '/dorm/repairs/mine', method: 'get', params })
}

export function getRepair(id) {
  return request({ url: `/dorm/repairs/${id}`, method: 'get' })
}

// 学生提交报修（RepairSubmitDTO）
export function submitRepair(data) {
  return request({ url: '/dorm/repairs', method: 'post', data })
}

// 宿管派单（RepairAssignDTO: repairId, assigneeId）
export function assignRepair(data) {
  return request({ url: '/dorm/repairs/assign', method: 'post', data })
}

// 维修工接单/状态反馈（RepairHandleDTO: repairId, status, note）
export function handleRepair(data) {
  return request({ url: '/dorm/repairs/handle', method: 'post', data })
}

// 学生验收评价（repairId, rating, ratingNote）
export function rateRepair(data) {
  return request({ url: '/dorm/repairs/rate', method: 'post', data })
}

// ============ 访客（/api/dorm/visitors）============

export function pageVisitors(params) {
  return request({ url: '/dorm/visitors', method: 'get', params })
}

// 学生登记访客
export function createVisitor(data) {
  return request({ url: '/dorm/visitors', method: 'post', data })
}

// 我的访客记录
export function myVisitors(params) {
  return request({ url: '/dorm/visitors/mine', method: 'get', params })
}

// 宿管审批（visitorId, status APPROVED/REJECTED）
export function auditVisitor(data) {
  return request({ url: '/dorm/visitors/audit', method: 'post', data })
}
