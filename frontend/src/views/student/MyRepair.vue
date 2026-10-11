<template>
  <div class="page">
    <div class="page-head">
      <h2>🔧 我的报修</h2>
      <el-button type="primary" @click="openCreate">＋ 提交报修</el-button>
    </div>

    <el-card>
      <el-table :data="rows" v-loading="loading">
        <el-table-column prop="id" label="工单号" width="120" />
        <el-table-column prop="category" label="类型" width="90" />
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :color="mapOf(REPAIR_STATUS, row.status).color" style="border:none" size="small">
              {{ mapOf(REPAIR_STATUS, row.status).label }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="assigneeName" label="维修工" width="90" />
        <el-table-column label="我的评价" width="110">
          <template #default="{ row }">
            <span v-if="row.rating" class="stars">{{ '★'.repeat(row.rating) }}</span>
            <span v-else class="muted">未评价</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="110">
          <template #default="{ row }">
            <el-button v-if="row.status === 'DONE' && !row.rating" type="primary" link @click="openRate(row)">验收评价</el-button>
            <el-button v-if="row.status === 'DONE'" size="small" link @click="detail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        style="margin-top: 12px; justify-content: flex-end"
        v-model:current-page="page.current"
        :page-size="page.size"
        :total="page.total"
        layout="prev, pager, next, total"
        @current-change="load"
      />
    </el-card>

    <!-- 提交报修 -->
    <el-dialog v-model="createVisible" title="提交报修" width="480px">
      <el-form :model="createForm" label-width="80px">
        <el-form-item label="报修类型">
          <el-select v-model="createForm.category" style="width: 100%">
            <el-option v-for="c in CATEGORIES" :key="c" :label="c" :value="c" />
          </el-select>
        </el-form-item>
        <el-form-item label="问题描述">
          <el-input v-model="createForm.description" type="textarea" :rows="3" placeholder="请描述故障情况" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="createForm.remark" placeholder="期望上门时间等（选填）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submit">提交</el-button>
      </template>
    </el-dialog>

    <!-- 验收评价 -->
    <el-dialog v-model="rateVisible" title="验收评价" width="400px">
      <el-rate v-model="rateForm.rating" :max="5" show-text />
      <el-input v-model="rateForm.ratingNote" type="textarea" :rows="2" placeholder="评价意见（选填）" style="margin-top: 8px" />
      <template #footer>
        <el-button @click="rateVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitRate">提交评价</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { myRepairs, submitRepair, rateRepair } from '@/api/dorm'
import { mapOf, REPAIR_STATUS } from '@/utils/format'

const CATEGORIES = ['门锁', '灯具', '水管', '插座', '空调', '床具', '其他']

const loading = ref(false)
const submitting = ref(false)
const rows = ref([])
const page = reactive({ current: 1, size: 10, total: 0 })

const createVisible = ref(false)
const createForm = reactive({ category: '门锁', description: '', remark: '' })

const rateVisible = ref(false)
const rateForm = reactive({ id: null, rating: 5, ratingNote: '' })

async function load() {
  loading.value = true
  try {
    const data = await myRepairs({ current: page.current, size: page.size })
    rows.value = data?.records || []
    page.total = data?.total || 0
  } catch (e) { /* 拦截器已提示 */ } finally {
    loading.value = false
  }
}

function openCreate() {
  createForm.category = '门锁'
  createForm.description = ''
  createForm.remark = ''
  createVisible.value = true
}

async function submit() {
  if (!createForm.description.trim()) return ElMessage.warning('请填写问题描述')
  submitting.value = true
  try {
    await submitRepair({ ...createForm })
    ElMessage.success('报修已提交')
    createVisible.value = false
    load()
  } catch (e) { } finally {
    submitting.value = false
  }
}

function openRate(row) {
  rateForm.id = row.id
  rateForm.rating = 5
  rateForm.ratingNote = ''
  rateVisible.value = true
}

async function submitRate() {
  submitting.value = true
  try {
    await rateRepair({ ...rateForm })
    ElMessage.success('评价已提交')
    rateVisible.value = false
    load()
  } catch (e) { } finally {
    submitting.value = false
  }
}

function detail(row) {
  ElMessageBox.alert(`${row.description}\n\n维修工反馈：${row.ratingNote || row.remark || '无'}`, `工单 ${row.id}`, {
    confirmButtonText: '关闭'
  })
}

onMounted(load)
</script>

<style scoped>
.page { padding: 4px; }
.page-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.page-head h2 { margin: 0; font-size: 18px; color: #303133; }
.stars { color: #e6a23c; }
.muted { color: #909399; font-size: 12px; }
</style>
