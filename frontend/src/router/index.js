import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/store/user'

const roles = {
  STUDENT: ['STUDENT'],
  DORM_MANAGER: ['DORM_MANAGER'],
  REPAIR_WORKER: ['REPAIR_WORKER'],
  COUNSELOR: ['COUNSELOR'],
  ADMIN: ['ADMIN']
}

const routes = [
  { path: '/login', name: 'Login', component: () => import('@/views/Login.vue'), meta: { public: true } },

  {
    path: '/',
    component: () => import('@/views/layout/AdminLayout.vue'),
    children: [
      // 学生视角
      {
        path: 'workbench',
        name: 'Workbench',
        component: () => import('@/views/student/Workbench.vue'),
        meta: { title: '工作台', roles: ['STUDENT', 'ADMIN'] }
      },
      {
        path: 'my-repair',
        name: 'MyRepair',
        component: () => import('@/views/student/MyRepair.vue'),
        meta: { title: '我的报修', roles: ['STUDENT', 'ADMIN'] }
      },
      {
        path: 'my-payment',
        name: 'MyPayment',
        component: () => import('@/views/student/MyPayment.vue'),
        meta: { title: '费用缴费', roles: ['STUDENT', 'ADMIN'] }
      },
      {
        path: 'visitor',
        name: 'Visitor',
        component: () => import('@/views/student/Visitor.vue'),
        meta: { title: '访客登记', roles: ['STUDENT', 'ADMIN'] }
      },
      {
        path: 'my-violation',
        name: 'MyViolation',
        component: () => import('@/views/student/MyViolation.vue'),
        meta: { title: '我的违纪', roles: ['STUDENT', 'ADMIN'] }
      },
      {
        path: 'my-award',
        name: 'MyAward',
        component: () => import('@/views/student/MyAward.vue'),
        meta: { title: '我的评优', roles: ['STUDENT', 'ADMIN'] }
      },

      // 宿管视角
      {
        path: 'dorm/buildings',
        name: 'DormBuildings',
        component: () => import('@/views/dorm/Buildings.vue'),
        meta: { title: '楼栋管理', roles: ['DORM_MANAGER', 'ADMIN'] }
      },
      {
        path: 'dorm/rooms',
        name: 'DormRooms',
        component: () => import('@/views/dorm/Rooms.vue'),
        meta: { title: '宿舍管理', roles: ['DORM_MANAGER', 'ADMIN'] }
      },
      {
        path: 'dorm/bed-map',
        name: 'DormBedMap',
        component: () => import('@/views/dorm/BedMap.vue'),
        meta: { title: '床位地图', roles: ['DORM_MANAGER', 'ADMIN'] }
      },
      {
        path: 'dorm/enroll',
        name: 'DormEnroll',
        component: () => import('@/views/dorm/Enrollment.vue'),
        meta: { title: '入住/退宿', roles: ['DORM_MANAGER', 'ADMIN'] }
      },
      {
        path: 'dorm/utility',
        name: 'DormUtility',
        component: () => import('@/views/dorm/Utility.vue'),
        meta: { title: '水电管理', roles: ['DORM_MANAGER', 'ADMIN'] }
      },
      {
        path: 'dorm/repair-dispatch',
        name: 'RepairDispatch',
        component: () => import('@/views/dorm/RepairDispatch.vue'),
        meta: { title: '报修派单', roles: ['DORM_MANAGER', 'ADMIN'] }
      },
      {
        path: 'dorm/visitor-audit',
        name: 'VisitorAudit',
        component: () => import('@/views/dorm/VisitorAudit.vue'),
        meta: { title: '访客审批', roles: ['DORM_MANAGER', 'ADMIN'] }
      },
      {
        path: 'maint/check',
        name: 'HygieneCheck',
        component: () => import('@/views/dorm/HygieneCheck.vue'),
        meta: { title: '卫生检查', roles: ['DORM_MANAGER', 'ADMIN'] }
      },
      {
        path: 'maint/evaluation',
        name: 'Evaluation',
        component: () => import('@/views/dorm/Evaluation.vue'),
        meta: { title: '评优管理', roles: ['DORM_MANAGER', 'ADMIN'] }
      },

      // 维修工视角
      {
        path: 'repair/orders',
        name: 'RepairOrders',
        component: () => import('@/views/repair/Orders.vue'),
        meta: { title: '维修工单', roles: ['REPAIR_WORKER', 'ADMIN'] }
      },

      // 辅导员视角
      {
        path: 'counselor/violation',
        name: 'CounselorViolation',
        component: () => import('@/views/counselor/Violation.vue'),
        meta: { title: '违纪统计', roles: ['COUNSELOR', 'ADMIN'] }
      },
      {
        path: 'counselor/award-final',
        name: 'AwardFinal',
        component: () => import('@/views/counselor/AwardFinal.vue'),
        meta: { title: '评优终审', roles: ['COUNSELOR', 'ADMIN'] }
      },
      {
        path: 'counselor/dashboard',
        name: 'CounselorDashboard',
        component: () => import('@/views/counselor/Dashboard.vue'),
        meta: { title: '看板报表', roles: ['COUNSELOR', 'ADMIN'] }
      },

      // 管理员视角
      {
        path: 'admin/users',
        name: 'AdminUsers',
        component: () => import('@/views/admin/Users.vue'),
        meta: { title: '用户管理', roles: ['ADMIN'] }
      },
      {
        path: 'admin/dict',
        name: 'AdminDict',
        component: () => import('@/views/admin/Dict.vue'),
        meta: { title: '字典管理', roles: ['ADMIN'] }
      },
      {
        path: 'admin/dashboard',
        name: 'AdminDashboard',
        component: () => import('@/views/admin/Dashboard.vue'),
        meta: { title: '全局看板', roles: ['ADMIN', 'COUNSELOR'] }
      },

      // 移动端 H5
      {
        path: 'mobile',
        name: 'MobileHome',
        component: () => import('@/views/mobile/Home.vue'),
        meta: { title: '移动端', roles: ['STUDENT', 'DORM_MANAGER', 'ADMIN'] }
      },
      {
        path: 'mobile/check',
        name: 'MobileCheck',
        component: () => import('@/views/mobile/Check.vue'),
        meta: { title: '扫码检查', roles: ['DORM_MANAGER', 'STUDENT', 'ADMIN'] }
      }
    ]
  },

  { path: '/:pathMatch(.*)*', redirect: '/workbench' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to) => {
  const user = useUserStore()
  if (to.meta.public) return true
  if (!user.isLoggedIn) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  if (to.meta.roles && !to.meta.roles.includes(user.role)) {
    // 无权：回退到第一个可访问页面（学生/默认）
    return to.path === '/workbench' ? '/mobile' : '/workbench'
  }
  return true
})

export default router
