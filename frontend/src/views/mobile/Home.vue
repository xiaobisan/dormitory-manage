<template>
  <div class="mobile">
    <div class="m-header">
      <h1>🏫 宿舍 H5</h1>
      <el-dropdown @command="onCommand">
        <div class="user">{{ userStore.username || '未登录' }} <el-icon><ArrowDown /></el-icon></div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="logout">退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>

    <!-- 学生核心页 -->
    <div class="m-grid" v-if="isStudent">
      <div class="m-card" @click="go('/my-repair')"><div class="m-icon">🔧</div><div>报修</div></div>
      <div class="m-card" @click="go('/my-payment')"><div class="m-icon">💰</div><div>缴费</div></div>
      <div class="m-card" @click="go('/my-award')"><div class="m-icon">🏆</div><div>查分/评优</div></div>
      <div class="m-card" @click="go('/my-violation')"><div class="m-icon">📋</div><div>违纪</div></div>
      <div class="m-card" @click="go('/visitor')"><div class="m-icon">🚪</div><div>访客</div></div>
      <div class="m-card" @click="go('/workbench')"><div class="m-icon">🏠</div><div>我的宿舍</div></div>
    </div>

    <!-- 检查员扫码 -->
    <div class="m-section" v-if="isManager">
      <h2>卫生检查</h2>
      <div class="m-card" @click="go('/mobile/check')">
        <div class="m-icon">📷</div><div>扫码检查</div>
        <p>扫宿舍二维码进入打分页</p>
      </div>
    </div>

    <el-empty description="登录后可使用对应功能" v-if="!isStudent && !isManager" />
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowDown } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()

const isStudent = computed(() => ['STUDENT', 'ADMIN'].includes(userStore.role))
const isManager = computed(() => ['DORM_MANAGER', 'ADMIN'].includes(userStore.role))

const go = (path) => router.push(path)
function onCommand(cmd) {
  if (cmd === 'logout') {
    userStore.logout()
    ElMessage.success('已退出')
    router.push('/login')
  }
}
</script>

<style scoped>
.mobile {
  max-width: 520px;
  margin: 0 auto;
  min-height: 100vh;
  background: #f5f7fa;
}
.m-header {
  background: linear-gradient(135deg, #1e3c72, #2a5298);
  color: #fff;
  padding: 16px;
  display: flex; justify-content: space-between; align-items: center;
}
.m-header h1 { margin: 0; font-size: 18px; }
.user { font-size: 13px; cursor: pointer; }
.m-grid {
  display: grid; grid-template-columns: repeat(3, 1fr); gap: 12px; padding: 16px;
}
.m-section { padding: 16px; }
.m-section h2 { font-size: 15px; color: #303133; margin: 0 0 12px; }
.m-card {
  background: #fff; border-radius: 12px; padding: 16px 8px; text-align: center;
  cursor: pointer; box-shadow: 0 2px 8px rgba(0,0,0,0.06); transition: transform .1s;
}
.m-card:active { transform: scale(0.97); }
.m-icon { font-size: 28px; margin-bottom: 8px; }
.m-card p { font-size: 11px; color: #909399; margin-top: 6px; }
</style>
