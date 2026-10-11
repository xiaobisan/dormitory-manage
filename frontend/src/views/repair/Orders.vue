<template>
  <div class="page">
    <div class="page-head"><h2>🔧 维修工单（{{ name }}）</h2></div>

    <el-tabs v-model="tab" @tab-change="load">
      <el-tab-pane :label="`待接单（${pendingCount}）`" name="PENDING" />
      <el-tab-pane :label="`处理中（${processingCount}）`" name="PROCESSING" />
      <el-tab-pane label="已完成" name="DONE" />
      <el-tab-pane label="全部" name="ALL" />
    </el-tabs>

    <el-card>
      <el-table :data="rows" v-loading="loading" border>
        <el-table-column prop="id" label="工单号" width="100" />
        <el-table-column prop="roomNo" label="宿舍" width="100" />
        <el-table-column prop="category" label="类型" width="90" />
        <el-table-column prop="description" label="故障描述" show-overflow-tooltip />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :color="mapOf(REPAIR_STATUS, row.status).color" style="border:none" size="small">
              {{ mapOf(REPAIR_STATUS, row.status).label }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="提交时间" width="160" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 'PENDING'" type="primary" @click="accept(row)">接单</el-button>
            <el-button v-if="row.status === 'PROCESSING'" type="success" @click="openDone(row)">标记完成</el-button>
            <el-button v-if="row.status === 'ASSIGNED'" type="warning" @click="start(row)">开始处理</el-button>
            <el-button v-if="row.rating" type="info" link @click="showNote(row)">评价：{{ row.rating }}★</el-button>
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

    <el-dialog v-model="doneVisible" :title="`完成工单 #${doneForm.repairId}`" width="420px">
      <el-form label-width="80px">
        <el-form-item label="处理说明"><el-input v-model="doneForm.note" type="textarea" :rows="3" placeholder="请说明维修结果" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="doneVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitDone">确认完成</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { pageRepairs, handleRepair, getRepair } from '@/api/dorm'
import { mapOf, REPAIR_STATUS } from '@/utils/format'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const name = ref('')
const tab = ref('PENDING')
const rows = ref([])
const loading = ref(false)
const saving = ref(false)
const page = reactive({ current: 1, size: 10, total: 0 })

const pendingCount = ref(0)
const processingCount = ref(0)

const doneVisible = ref(false)
const doneForm = reactive({ repairId: null, note: '' })

async function loadCounts() {
  const [p, proc] = await Promise.allSettled([
    pageRepairs({ status: 'PENDING', current: 1, size: 1 }),
    pageRepairs({ status: 'PROCESSING', current: 1, size: 1 })
  ])
  if (p.status === 'fulfilled') pendingCount.value = p.value?.total || 0
  if (proc.status === 'fulfilled') processingCount.value = proc.value?.total || 0
}

async function load() {
  loading.value = true
  try {
    const status = tab.value === 'ALL' ? undefined : tab.value
    const data = await pageRepairs({ status, current: page.current, size: page.size })
    rows.value = data?.records || []
    page.total = data?.total || 0
  } catch (e) { } finally { loading.value = false }
}

function accept(row) {
  handleRepair({ repairId: row.id, status: 'PROCESSING', note: '已接单' }).then(() => {
    ElMessage.success('已接单')
    load(); loadCounts()
  }).catch(() => {})
}

function start(row) {
  handleRepair({ repairId: row.id, status: 'PROCESSING', note: '开始处理' }).then(() => {
    ElMessage.success('状态已更新')
    load(); loadCounts()
  }).catch(() => {})
}

function openDone(row) {
  doneForm.repairId = row.id
  doneForm.note = ''
  doneVisible.value = true
}

async function submitDone() {
  saving.value = true
  try {
    await handleRepair({ repairId: doneForm.repairId, status: 'DONE', note: doneForm.note })
    ElMessage.success('工单已完成，等待学生验收')
    doneVisible.value = false
    tab.value = 'ALL'
    load(); loadCounts()
  } catch (e) { } finally { saving.value = false }
}

async function showNote(row) {
  try {
    const d = await getRepair(row.id)
    ElMessage.info(`学生评价 ${row.rating} 分：${d.ratingNote || '无留言'}`)
  } catch (e) {
    ElMessage.info(`学生评价 ${row.rating} 分`)
  }
}

onMounted(() => {
  name.value = userStore.username
  load()
  loadCounts()
})
</script>

<style scoped>
.page { padding: 4px; }
.page-head h2 { margin: 0 0 12px; font-size: 18px; color: #303133; }
</style>
