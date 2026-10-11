/** 状态/字典 中文映射（本地兜底；优先用后端字典数据） */

export const ROLE_MAP = {
  STUDENT: '学生',
  DORM_MANAGER: '宿管',
  REPAIR_WORKER: '维修工',
  COUNSELOR: '辅导员',
  ADMIN: '管理员'
}

export const BED_STATUS = {
  EMPTY: { label: '空闲', color: '#67c23a' },
  OCCUPIED: { label: '已占用', color: '#409eff' },
  REPAIR: { label: '维修', color: '#e6a23c' },
  LOCKED: { label: '锁定', color: '#909399' }
}

export const ROOM_STATUS = {
  EMPTY: { label: '空', color: '#67c23a' },
  IN_USE: { label: '使用中', color: '#409eff' },
  REPAIR: { label: '维修', color: '#e6a23c' }
}

export const BILL_STATUS = {
  UNPAID: { label: '待缴', color: '#e6a23c' },
  PAID: { label: '已缴', color: '#67c23a' },
  OVERDUE: { label: '逾期', color: '#f56c6c' }
}

export const REPAIR_STATUS = {
  PENDING: { label: '待派单', color: '#e6a23c' },
  ASSIGNED: { label: '已派单', color: '#409eff' },
  PROCESSING: { label: '处理中', color: '#409eff' },
  DONE: { label: '已完成', color: '#67c23a' },
  REJECTED: { label: '已退回', color: '#f56c6c' }
}

export const VISITOR_STATUS = {
  PENDING: { label: '待审批', color: '#e6a23c' },
  APPROVED: { label: '已批准', color: '#67c23a' },
  REJECTED: { label: '已拒绝', color: '#f56c6c' },
  LEAVED: { label: '已离场', color: '#909399' }
}

export const ENROLL_STATUS = {
  ACTIVE: { label: '在读', color: '#67c23a' },
  CHECKED_OUT: { label: '已退宿', color: '#909399' },
  ON_LEAVE: { label: '请假', color: '#e6a23c' }
}

export const USAGE_TYPE = { WATER: '水费', ELECTRIC: '电费' }

export const SEVERITY = {
  LIGHT: { label: '轻微', color: '#e6a23c' },
  MEDIUM: { label: '一般', color: '#409eff' },
  HEAVY: { label: '严重', color: '#f56c6c' }
}

export const CYCLE = { WEEKLY: '每周', MONTHLY: '每月' }

export const STARS = (n) => '★★★★★'.slice(0, n) + '☆☆☆☆☆'.slice(0, 5 - n)

/** 通用状态映射取值 */
export function mapOf(map, key) {
  return map?.[key] || { label: key, color: '#909399' }
}
