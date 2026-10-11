<template>
  <el-container class="layout">
    <!-- 侧边菜单 -->
    <el-aside width="220px" class="aside">
      <div class="logo">🏫 宿舍管理系统</div>
      <el-menu
        :default-active="route.path"
        :collapse="isCollapse"
        background-color="#001529"
        text-color="rgba(255,255,255,0.68)"
        active-text-color="#409eff"
        router
      >
        <!-- 学生 -->
        <el-sub-menu v-if="show('STUDENT')" index="student" popper-class="menu-popper">
          <template #title><el-icon><User /></el-icon>学生</template>
          <el-menu-item index="/workbench">工作台</el-menu-item>
          <el-menu-item index="/my-repair">我的报修</el-menu-item>
          <el-menu-item index="/my-payment">费用缴费</el-menu-item>
          <el-menu-item index="/visitor">访客登记</el-menu-item>
          <el-menu-item index="/my-violation">我的违纪</el-menu-item>
          <el-menu-item index="/my-award">我的评优</el-menu-item>
        </el-sub-menu>

        <!-- 宿管 -->
        <el-sub-menu v-if="show('DORM_MANAGER')" index="dorm" popper-class="menu-popper">
          <template #title><el-icon><House /></el-icon>宿管</template>
          <el-menu-item index="/dorm/buildings">楼栋管理</el-menu-item>
          <el-menu-item index="/dorm/rooms">宿舍管理</el-menu-item>
          <el-menu-item index="/dorm/bed-map">床位地图</el-menu-item>
          <el-menu-item index="/dorm/enroll">入住/退宿</el-menu-item>
          <el-menu-item index="/dorm/utility">水电管理</el-menu-item>
          <el-menu-item index="/dorm/repair-dispatch">报修派单</el-menu-item>
          <el-menu-item index="/dorm/visitor-audit">访客审批</el-menu-item>
          <el-menu-item index="/maint/check">卫生检查</el-menu-item>
          <el-menu-item index="/maint/evaluation">评优管理</el-menu-item>
        </el-sub-menu>

        <!-- 维修工 -->
        <el-sub-menu v-if="show('REPAIR_WORKER')" index="repair" popper-class="menu-popper">
          <template #title><el-icon><Tools /></el-icon>维修</template>
          <el-menu-item index="/repair/orders">工单管理</el-menu-item>
        </el-sub-menu>

        <!-- 辅导员 -->
        <el-sub-menu v-if="show('COUNSELOR')" index="counselor" popper-class="menu-popper">
          <template #title><el-icon><DataAnalysis /></el-icon>辅导员</template>
          <el-menu-item index="/counselor/violation">违纪统计</el-menu-item>
          <el-menu-item index="/counselor/award-final">评优终审</el-menu-item>
          <el-menu-item index="/counselor/dashboard">看板报表</el-menu-item>
        </el-sub-menu>

        <!-- 管理员 -->
        <el-sub-menu v-if="show('ADMIN')" index="admin" popper-class="menu-popper">
          <template #title><el-icon><Setting /></el-icon>管理员</template>
          <el-menu-item index="/admin/users">用户管理</el-menu-item>
          <el-menu-item index="/admin/dict">字典管理</el-menu-item>
          <el-menu-item index="/admin/dashboard">全局看板</el-menu-item>
        </el-sub-menu>

        <!-- 移动端 H5 -->
        <el-menu-item index="/mobile" v-if="show('STUDENT') || show('DORM_MANAGER') || show('ADMIN')">
          <el-icon><Iphone /></el-icon>移动端 H5
        </el-menu-item>
      </el-menu>
    </el-aside>

    <!-- 主区域 -->
    <el-container class="main">
      <el-header class="header">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="isCollapse = !isCollapse">
            <Fold v-if="!isCollapse" />
            <Expand v-else />
          </el-icon>
          <span class="page-title">{{ route.meta.title || '宿舍管理系统' }}</span>
        </div>
        <el-dropdown @command="onCommand">
          <div class="user-box">
            <el-avatar :size="30" style="background:#409eff">{{ userStore.username?.[0] || '用' }}</el-avatar>
            <span class="user-name">{{ userStore.username }}</span>
            <el-tag size="small" type="info">{{ roleLabel }}</el-tag>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="mobile">移动端 H5</el-dropdown-item>
              <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-header>

      <el-main class="content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  User, House, Tools, DataAnalysis, Setting, Iphone, Fold, Expand
} from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import { ROLE_MAP } from '@/utils/format'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const isCollapse = ref(false)

const show = (role) => role === 'ADMIN' || userStore.role === role || userStore.role === 'ADMIN'
const roleLabel = computed(() => ROLE_MAP[userStore.role] || userStore.role || '未知角色')

function onCommand(cmd) {
  if (cmd === 'mobile') router.push('/mobile')
  if (cmd === 'logout') {
    userStore.logout()
    ElMessage.success('已退出')
    router.push('/login')
  }
}
</script>

<style scoped>
.layout { height: 100vh; }
.aside { background: #001529; overflow-x: hidden; }
.logo {
  height: 56px; line-height: 56px; text-align: center; color: #fff;
  font-weight: 600; font-size: 15px; background: #002140; letter-spacing: 1px;
}
.aside :deep(.el-menu) { border-right: none; }
.main { display: flex; flex-direction: column; min-width: 0; }
.header {
  background: #fff; border-bottom: 1px solid #ebeef5;
  display: flex; align-items: center; justify-content: space-between; padding: 0 16px; height: 56px;
}
.header-left { display: flex; align-items: center; gap: 10px; }
.collapse-btn { cursor: pointer; font-size: 20px; color: #606266; }
.page-title { font-size: 15px; font-weight: 600; color: #303133; }
.user-box { display: flex; align-items: center; gap: 8px; cursor: pointer; }
.user-name { font-size: 13px; color: #303133; }
.content { padding: 16px; overflow-y: auto; background: #f5f7fa; flex: 1; }
.menu-popper { --el-menu-bg-color: #001529; }
</style>
