<template>
  <div class="page">
    <h2 class="page-title">👋 工作台 · 我的宿舍</h2>

    <el-row :gutter="16">
      <el-col :span="8">
        <el-card class="card-item">
          <template #header><span>🏠 我的宿舍 / 床位</span></template>
          <el-descriptions :column="1" border v-if="my.room">
            <el-descriptions-item label="楼栋">{{ my.buildingName }}</el-descriptions-item>
            <el-descriptions-item label="宿舍">{{ my.roomNo }} 房（{{ my.room?.floor }} 层）</el-descriptions-item>
            <el-descriptions-item label="床位">第 {{ my.bedNo }} 床</el-descriptions-item>
            <el-descriptions-item label="入住状态">{{ statusLabel(my.status) }}</el-descriptions-item>
          </el-descriptions>
          <el-empty v-else description="暂未分配宿舍，请联系宿管" :image-size="60" />
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card class="card-item">
          <template #header><span>💡 本月水电费（{{ my.currentPeriod }}）</span></template>
          <div class="bill-row">
            <div class="bill">
              <div class="bill-label">水费</div>
              <div class="bill-amount">¥ {{ money(my.waterAmount) }}</div>
              <el-tag :type="billTag(my.waterStatus)" size="small">{{ billLabel(my.waterStatus) }}</el-tag>
            </div>
            <div class="bill">
              <div class="bill-label">电费</div>
              <div class="bill-amount">¥ {{ money(my.electricAmount) }}</div>
              <el-tag :type="billTag(my.electricStatus)" size="small">{{ billLabel(my.electricStatus) }}</el-tag>
            </div>
          </div>
          <el-button type="primary" link @click="$router.push('/my-payment')">去缴费 →</el-button>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card class="card-item">
          <template #header><span>🔧 我的报修（进行中 {{ my.repairingCount }}）</span></template>
          <el-table :data="my.repairPreview" size="small" max-height="180">
            <el-table-column prop="category" label="类型" width="80" />
            <el-table-column prop="status" label="状态" :formatter="repairFormatter" />
            <el-table-column prop="createdAt" label="提交时间" width="150" />
          </el-table>
          <el-button type="primary" link @click="$router.push('/my-repair')">我的报修 →</el-button>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" style="margin-top: 16px">
      <el-col :span="12">
        <el-card class="card-item">
          <template #header><span>📋 我的违纪（累计 {{ my.violationCount }} 次）</span></template>
          <el-table :data="my.violationPreview" size="small">
            <el-table-column prop="type" label="类型" />
            <el-table-column prop="severity" label="级别" />
            <el-table-column prop="happenedAt" label="时间" width="160" />
          </el-table>
          <el-button type="primary" link @click="$router.push('/my-violation')">全部违纪 →</el-button>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="card-item">
          <template #header><span>🏆 我的评优（当前 {{ my.starLevel || '-' }} 星）</span></template>
          <div v-if="my.starLevel" class="stars">{{ STARS(my.starLevel) }} <span class="total">总分 {{ my.totalScore }}</span></div>
          <el-empty v-else description="本周期暂无评优结果" :image-size="50" />
          <el-button type="primary" link @click="$router.push('/my-award')">评优详情 →</el-button>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { studentWorkbench } from '@/api/eval'
import { myBills, myRepairs, myViolations, myEvaluationScores } from '@/api/dorm'
import { STARS, mapOf, REPAIR_STATUS, BILL_STATUS } from '@/utils/format'

const $router = useRouter()
const my = ref({ repairPreview: [], violationPreview: [], waterStatus: '', electricStatus: '' })

function statusLabel(s) { return mapOf({ ACTIVE: { label: '在读' }, CHECKED_OUT: { label: '已退宿' } }, s).label }
function money(n) { return Number(n || 0).toFixed(2) }
function billLabel(s) { return mapOf(BILL_STATUS, s).label }
function billTag(s) { return { UNPAID: 'warning', PAID: 'success', OVERDUE: 'danger' }[s] || 'info' }
function repairFormatter(row) { return mapOf(REPAIR_STATUS, row.status).label }

onMounted(async () => {
  // 优先用聚合工作台接口；失败则分项拉取
  try {
    const data = await studentWorkbench()
    if (data && Object.keys(data).length) my.value = { ...my.value, ...data }
  } catch (e) { /* 后端未就绪时回退 */ }

  const [bills, repairs, violations, scores] = await Promise.allSettled([
    myBills({ current: 1, size: 50 }),
    myRepairs({ current: 1, size: 5 }),
    myViolations({ current: 1, size: 5 }),
    myEvaluationScores({ current: 1, size: 1 })
  ])
  if (bills.status === 'fulfilled' && Array.isArray(bills.value?.records)) {
    my.value.repairPreview = repairs.status === 'fulfilled' ? repairs.value?.records || [] : []
    my.value.violationCount = violations.status === 'fulfilled' ? violations.value?.total || 0 : 0
    my.value.violationPreview = violations.status === 'fulfilled' ? (violations.value?.records || []).slice(0, 3) : []
    const sc = scores.status === 'fulfilled' ? scores.value?.records?.[0] : null
    if (sc) { my.value.starLevel = sc.starLevel; my.value.totalScore = sc.totalScore }
  }
})
</script>

<style scoped>
.page-title { margin: 0 0 16px; font-size: 18px; color: #303133; }
.card-item { margin-bottom: 4px; }
.bill-row { display: flex; gap: 24px; margin-bottom: 8px; }
.bill { flex: 1; }
.bill-label { color: #909399; font-size: 12px; }
.bill-amount { font-size: 22px; font-weight: 600; color: #303133; margin: 4px 0; }
.stars { font-size: 24px; color: #e6a23c; letter-spacing: 4px; }
.stars .total { font-size: 13px; color: #909399; margin-left: 8px; }
</style>
