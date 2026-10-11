<template>
  <div class="page">
    <div class="page-head"><h2>📊 违纪统计（本系）</h2></div>

    <el-row :gutter="16">
      <el-col :span="10">
        <el-card>
          <div ref="pieEl" class="chart" />
        </el-card>
      </el-col>
      <el-col :span="14">
        <el-card>
          <div ref="trendEl" class="chart" />
        </el-card>
      </el-col>
    </el-row>

    <el-card style="margin-top: 16px">
      <template #header>
        <div style="display:flex;justify-content:space-between;align-items:center">
          <span>违纪记录明细</span>
          <el-button type="primary" @click="openCreate">＋ 录入违纪</el-button>
        </div>
      </template>
      <el-table :data="rows" v-loading="loading" border>
        <el-table-column prop="studentName" label="学生" width="110" />
        <el-table-column prop="typeName" label="违纪类型" width="130" />
        <el-table-column label="级别" width="90">
          <template #default="{ row }">
            <el-tag :color="mapOf(SEVERITY, row.severity).color" style="border:none" size="small">
              {{ mapOf(SEVERITY, row.severity).label }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column prop="happenedAt" label="发生时间" width="160" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button v-if="!row.handledAt" type="primary" link @click="openHandle(row)">处理</el-button>
            <el-text v-else type="success">已处理</el-text>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        style="margin-top: 12px; justify-content: flex-end"
        v-model:current-page="page.current"
        :page-size="page.size"
        :total="page.total"
        layout="total, prev, pager, next"
        @current-change="load"
      />
    </el-card>

    <el-dialog v-model="createVisible" title="录入违纪" width="440px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="学生">
          <el-select v-model="form.studentId" filterable style="width: 100%">
            <el-option v-for="s in students" :key="s.id" :label="s.name" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="违纪类型">
          <el-select v-model="form.typeId" style="width: 100%">
            <el-option v-for="t in types" :key="t.id" :label="t.name" :value="t.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="级别">
          <el-radio-group v-model="form.severity">
            <el-radio value="LIGHT">轻微</el-radio><el-radio value="MEDIUM">一般</el-radio><el-radio value="HEAVY">严重</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="发生时间"><el-date-picker v-model="form.happenedAt" type="datetime" style="width:100%" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="handleVisible" title="处理违纪" width="400px">
      <el-form label-width="80px">
        <el-form-item label="处理结果"><el-input v-model="handleForm.note" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="handleVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitHandle">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount } from 'vue'
import { ElMessage } from 'element-plus'
import { pageViolations, createViolation, handleViolation, violationStats, listViolationTypes } from '@/api/eval'
import { pageUsers } from '@/api/system'
import { initChart, watchResize, pieOption, lineOrBar } from '@/utils/echarts'
import { mapOf, SEVERITY } from '@/utils/format'

const loading = ref(false)
const saving = ref(false)
const rows = ref([])
const page = reactive({ current: 1, size: 10, total: 0 })
const types = ref([])
const students = ref([])

const createVisible = ref(false)
const form = reactive({ studentId: null, typeId: null, severity: 'LIGHT', happenedAt: '', description: '' })

const handleVisible = ref(false)
const handleForm = reactive({ id: null, note: '' })

const pieEl = ref(null)
const trendEl = ref(null)
let pieChart = null
let trendChart = null
let resizeCleanups = []

async function load() {
  loading.value = true
  try {
    const data = await pageViolations({ current: page.current, size: page.size })
    rows.value = data?.records || []
    page.total = data?.total || 0
  } catch (e) { } finally { loading.value = false }
}

async function loadTypes() {
  try {
    const data = await listViolationTypes({ current: 1, size: 100 })
    types.value = data?.records || data || []
  } catch (e) { }
}

async function loadStudents() {
  try {
    const data = await pageUsers({ current: 1, size: 300, role: 'STUDENT' })
    students.value = data?.records || []
  } catch (e) { }
}

async function renderCharts() {
  let stats = null
  try { stats = await violationStats({}) } catch (e) { }
  const byType = stats?.byType || stats?.distribution || []
  pieChart = initChart(pieEl.value, pieOption({ title: '违纪类型分布', data: byType }))

  const trend = stats?.trend || []
  trendChart = initChart(trendEl.value, lineOrBar({
    title: '近 6 个月违纪趋势',
    x: trend.map((t) => t.period || t.month || ''),
    series: [{ name: '违纪次数', data: trend.map((t) => t.count || t.value || 0) }]
  }))

  resizeCleanups = [watchResize(pieEl.value, pieChart), watchResize(trendEl.value, trendChart)]
}

async function save() {
  if (!form.studentId) return ElMessage.warning('请选择学生')
  saving.value = true
  try {
    await createViolation({ ...form })
    ElMessage.success('已录入')
    createVisible.value = false
    load()
  } catch (e) { } finally { saving.value = false }
}

function openCreate() {
  Object.assign(form, { studentId: null, typeId: types.value[0]?.id, severity: 'LIGHT', happenedAt: '', description: '' })
  createVisible.value = true
}

function openHandle(row) {
  handleForm.id = row.id
  handleForm.note = ''
  handleVisible.value = true
}

async function submitHandle() {
  saving.value = true
  try {
    await handleViolation({ id: handleForm.id, handleNote: handleForm.note })
    ElMessage.success('处理完成')
    handleVisible.value = false
    load()
  } catch (e) { } finally { saving.value = false }
}

onMounted(() => {
  load()
  loadTypes()
  loadStudents()
  renderCharts()
})
onBeforeUnmount(() => {
  resizeCleanups.forEach((fn) => fn?.())
  pieChart?.dispose()
  trendChart?.dispose()
})
</script>

<style scoped>
.page { padding: 4px; }
.page-head h2 { margin: 0 0 12px; font-size: 18px; color: #303133; }
.chart { width: 100%; height: 300px; }
</style>
