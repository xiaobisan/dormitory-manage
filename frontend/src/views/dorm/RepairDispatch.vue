<template>
  <div class="page">
    <div class="page-head"><h2>📦 报修派单</h2></div>

    <el-card>
      <el-form inline>
        <el-form-item label="状态">
          <el-select v-model="filter.status" clearable style="width: 140px" @change="load">
            <el-option v-for="(v, k) in REPAIR_STATUS" :key="k" :label="v.label" :value="k" />
          </el-select>
        </el-form-item>
        <el-button @click="load">查询</el-button>
      </el-form>

      <el-table :data="rows" v-loading="loading" border>
        <el-table-column prop="id" label="工单号" width="100" />
        <el-table-column prop="roomNo" label="宿舍" width="100" />
        <el-table-column prop="category" label="类型" width="90" />
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :color="mapOf(REPAIR_STATUS, row.status).color" style="border:none" size="small">
              {{ mapOf(REPAIR_STATUS, row.status).label }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="提交时间" width="160" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 'PENDING'" type="primary" @click="openAssign(row)">派单</el-button>
            <el-button v-else type="info" link @click="showDetail(row)">详情</el-button>
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

    <el-dialog v-model="assignVisible" title="派单给维修工" width="400px">
      <el-form label-width="80px">
        <el-form-item label="维修工">
          <el-select v-model="assignForm.assigneeId" filterable style="width: 100%">
            <el-option v-for="w in workers" :key="w.id" :label="w.name" :value="w.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="assignVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="doAssign">确认派单</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { pageRepairs, assignRepair, getRepair } from '@/api/dorm'
import { pageUsers } from '@/api/system'
import { mapOf, REPAIR_STATUS } from '@/utils/format'

const loading = ref(false)
const saving = ref(false)
const assignVisible = ref(false)
const rows = ref([])
const filter = reactive({ status: '' })
const page = reactive({ current: 1, size: 10, total: 0 })
const workers = ref([])
const currentRepair = ref(null)
const assignForm = reactive({ repairId: null, assigneeId: null })

async function load() {
  loading.value = true
  try {
    const data = await pageRepairs({ status: filter.status || undefined, current: page.current, size: page.size })
    rows.value = data?.records || []
    page.total = data?.total || 0
  } catch (e) { } finally { loading.value = false }
}

async function loadWorkers() {
  try {
    const data = await pageUsers({ current: 1, size: 100, role: 'REPAIR_WORKER' })
    workers.value = data?.records || []
  } catch (e) { workers.value = [] }
}

function openAssign(row) {
  currentRepair.value = row
  assignForm.repairId = row.id
  assignForm.assigneeId = null
  assignVisible.value = true
}

async function doAssign() {
  if (!assignForm.assigneeId) return ElMessage.warning('请选择维修工')
  saving.value = true
  try {
    await assignRepair({ ...assignForm })
    ElMessage.success('派单成功')
    assignVisible.value = false
    load()
  } catch (e) { } finally { saving.value = false }
}

async function showDetail(row) {
  let detail = row
  try { detail = await getRepair(row.id) } catch (e) { }
  ElMessageBox.alert(
    `描述：${detail.description}\n状态：${mapOf(REPAIR_STATUS, detail.status).label}\n维修工：${detail.assigneeName || '未分配'}`,
    `工单 ${row.id}`,
    { confirmButtonText: '关闭' }
  )
}

onMounted(() => { load(); loadWorkers() })
</script>

<style scoped>
.page { padding: 4px; }
.page-head h2 { margin: 0 0 12px; font-size: 18px; color: #303133; }
</style>
