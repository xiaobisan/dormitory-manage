<template>
  <div class="page">
    <div class="page-head">
      <h2>🧹 卫生检查</h2>
      <el-button type="primary" @click="openPlan">＋ 新建检查计划</el-button>
    </div>

    <el-tabs v-model="tab">
      <el-tab-pane label="检查计划" name="plan">
        <el-card>
          <el-table :data="plans" v-loading="loading" border>
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="name" label="计划名称" width="160" />
            <el-table-column label="周期" width="90">
              <template #default="{ row }">{{ CYCLE[row.cycle] || row.cycle || '-' }}</template>
            </el-table-column>
            <el-table-column prop="active" label="状态" width="90">
              <template #default="{ row }">
                <el-tag :type="row.active ? 'success' : 'info'" size="small">
                  {{ row.active ? '启用' : '停用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="检查项" show-overflow-tooltip>
              <template #default="{ row }">{{ itemLabels(row) }}</template>
            </el-table-column>
            <el-table-column label="操作" width="180" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" link @click="openPlan(row)">编辑</el-button>
                <el-button type="success" link @click="scoreByPlan(row)">打分</el-button>
                <el-button type="danger" link @click="removePlan(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="检查记录" name="record">
        <el-card>
          <el-table :data="records" v-loading="recLoading" border>
            <el-table-column prop="id" label="ID" width="90" />
            <el-table-column prop="planName" label="计划" width="150" />
            <el-table-column prop="roomNo" label="宿舍" width="110" />
            <el-table-column prop="score" label="得分" width="80" />
            <el-table-column prop="checkedBy" label="检查人" width="100" />
            <el-table-column prop="checkedAt" label="检查时间" width="160" />
          </el-table>
          <el-pagination
            style="margin-top: 12px; justify-content: flex-end"
            v-model:current-page="recPage.current"
            :page-size="recPage.size"
            :total="recPage.total"
            layout="total, prev, pager, next"
            @current-change="loadRecords"
          />
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <!-- 计划编辑 -->
    <el-dialog v-model="planVisible" :title="planForm.id ? '编辑计划' : '新建计划'" width="520px">
      <el-form :model="planForm" label-width="90px">
        <el-form-item label="计划名称"><el-input v-model="planForm.name" /></el-form-item>
        <el-form-item label="周期">
          <el-select v-model="planForm.cycle" style="width: 100%">
            <el-option label="每周" value="WEEKLY" /><el-option label="每月" value="MONTHLY" />
          </el-select>
        </el-form-item>
        <el-form-item label="检查项模板">
          <el-input
            v-model="planForm.itemTemplateText"
            type="textarea"
            :rows="4"
            placeholder='JSON 数组，例如：[{"name":"地面清洁","score":10},{"name":"物品摆放","score":10}]'
          />
        </el-form-item>
        <el-form-item label="启用">
          <el-switch v-model="planForm.active" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="planVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="savePlan">保存</el-button>
      </template>
    </el-dialog>

    <!-- 打分 -->
    <el-dialog v-model="scoreVisible" :title="`卫生打分 · ${scorePlan?.name}`" width="480px">
      <el-form label-width="90px">
        <el-form-item label="宿舍">
          <el-select v-model="scoreForm.roomId" filterable style="width: 100%" @change="applyTemplate">
            <el-option v-for="r in roomOptions" :key="r.id" :label="`${r.buildingName || ''} ${r.roomNo}`" :value="r.id" />
          </el-select>
        </el-form-item>
        <el-form-item v-for="(item, idx) in scoreItems" :key="idx" :label="item.name || '检查项'">
          <el-input-number v-model="item.actual" :min="0" :max="item.max" style="width: 100%" />
        </el-form-item>
        <el-form-item label="总分">
          <b style="font-size: 18px">{{ totalScore }}</b> / {{ maxScore }}
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="scoreVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitScore">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  pageCheckPlans, saveCheckPlan, deleteCheckPlan,
  pageCheckRecords, submitCheckRecord, pageRooms
} from '@/api/maint'
import { CYCLE } from '@/utils/format'

const tab = ref('plan')
const plans = ref([])
const records = ref([])
const loading = ref(false)
const recLoading = ref(false)
const saving = ref(false)
const roomOptions = ref([])
const recPage = reactive({ current: 1, size: 10, total: 0 })

const planVisible = ref(false)
const planForm = reactive({ id: null, name: '', cycle: 'WEEKLY', itemTemplateText: '', active: 1 })

const scoreVisible = ref(false)
const scorePlan = ref(null)
const scoreForm = reactive({ planId: null, roomId: null })
const scoreItems = ref([])

const totalScore = computed(() => scoreItems.value.reduce((s, i) => s + (Number(i.actual) || 0), 0))
const maxScore = computed(() => scoreItems.value.reduce((s, i) => s + (Number(i.max) || 0), 0))

function itemLabels(row) {
  try {
    const arr = JSON.parse(row.itemTemplate || '[]')
    return arr.map((i) => i.name).filter(Boolean).join('、')
  } catch (e) { return '-' }
}

async function loadPlans() {
  loading.value = true
  try {
    const data = await pageCheckPlans({ current: 1, size: 100 })
    plans.value = data?.records || data || []
  } catch (e) { plans.value = [] } finally { loading.value = false }
}

async function loadRecords() {
  recLoading.value = true
  try {
    const data = await pageCheckRecords({ current: recPage.current, size: recPage.size })
    records.value = data?.records || []
    recPage.total = data?.total || 0
  } catch (e) { } finally { recLoading.value = false }
}

function openPlan(row) {
  if (row) {
    Object.assign(planForm, row)
    try { planForm.itemTemplateText = JSON.stringify(JSON.parse(row.itemTemplate || '[]'), null, 2) }
    catch (e) { planForm.itemTemplateText = row.itemTemplate || '' }
  } else {
    Object.assign(planForm, { id: null, name: '', cycle: 'WEEKLY', itemTemplateText: '', active: 1 })
  }
  planVisible.value = true
}

async function savePlan() {
  if (!planForm.name.trim()) return ElMessage.warning('请填写计划名称')
  saving.value = true
  try {
    await saveCheckPlan({ ...planForm, itemTemplate: planForm.itemTemplateText })
    ElMessage.success('保存成功')
    planVisible.value = false
    loadPlans()
  } catch (e) { } finally { saving.value = false }
}

async function removePlan(row) {
  await ElMessageBox.confirm(`确定删除计划「${row.name}」？`, '警告', { type: 'warning' })
  try {
    await deleteCheckPlan(row.id)
    ElMessage.success('删除成功')
    loadPlans()
  } catch (e) { }
}

async function scoreByPlan(plan) {
  scorePlan.value = plan
  scoreForm.planId = plan.id
  scoreForm.roomId = null
  applyTemplate(plan)
  if (!roomOptions.value.length) {
    try { roomOptions.value = (await pageRooms({ current: 1, size: 200 }))?.records || [] } catch (e) { }
  }
  scoreVisible.value = true
}

function applyTemplate(plan) {
  try {
    const arr = JSON.parse(plan?.itemTemplate || '[]')
    scoreItems.value = arr.map((i) => ({ name: i.name, max: i.score ?? i.max ?? 10, actual: i.score ?? i.max ?? 0 }))
  } catch (e) {
    scoreItems.value = [
      { name: '地面清洁', max: 20, actual: 20 },
      { name: '物品摆放', max: 20, actual: 20 },
      { name: '卫生死角', max: 20, actual: 20 },
      { name: '内务整洁', max: 20, actual: 20 },
      { name: '其他', max: 20, actual: 20 }
    ]
  }
}

async function submitScore() {
  if (!scoreForm.roomId) return ElMessage.warning('请选择宿舍')
  saving.value = true
  try {
    await submitCheckRecord({
      planId: scoreForm.planId,
      roomId: scoreForm.roomId,
      score: totalScore.value,
      detail: JSON.stringify(scoreItems.value)
    })
    ElMessage.success('检查记录已提交')
    scoreVisible.value = false
    tab.value = 'record'
    loadRecords()
  } catch (e) { } finally { saving.value = false }
}

onMounted(() => { loadPlans(); loadRecords() })
</script>

<style scoped>
.page { padding: 4px; }
.page-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.page-head h2 { margin: 0; font-size: 18px; color: #303133; }
</style>
