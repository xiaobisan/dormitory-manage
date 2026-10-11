<template>
  <div class="page">
    <div class="page-head">
      <h2>🏆 评优管理</h2>
      <div>
        <el-input v-model="calcPeriod" placeholder="2026-10" style="width: 120px" />
        <el-button type="primary" :loading="calcLoading" @click="doCalculate">触发计算</el-button>
      </div>
    </div>

    <el-tabs v-model="tab">
      <!-- 规则配置 -->
      <el-tab-pane label="规则配置" name="rule">
        <el-card>
          <el-button type="primary" @click="openRule">＋ 新建规则</el-button>
          <el-table :data="rules" v-loading="loading" border style="margin-top: 12px">
            <el-table-column prop="id" label="ID" width="70" />
            <el-table-column prop="name" label="规则名称" width="150" />
            <el-table-column prop="hygieneWeight" label="卫生权重" width="90" />
            <el-table-column prop="disciplineWeight" label="纪律权重" width="90" />
            <el-table-column prop="bonusWeight" label="加分权重" width="90" />
            <el-table-column label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="row.active ? 'success' : 'info'" size="small">{{ row.active ? '启用' : '停用' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="140" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" link @click="openRule(row)">编辑</el-button>
                <el-button type="danger" link @click="removeRule(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <!-- 得分 -->
      <el-tab-pane label="评优得分" name="score">
        <el-card>
          <el-form inline>
            <el-form-item label="周期">
              <el-input v-model="scoreFilter.period" placeholder="2026-10" style="width: 130px" @change="loadScores" />
            </el-form-item>
            <el-button @click="loadScores">查询</el-button>
          </el-form>
          <el-table :data="scores" v-loading="scoreLoading" border>
            <el-table-column prop="period" label="周期" width="90" />
            <el-table-column prop="roomNo" label="宿舍" width="110" />
            <el-table-column prop="hygieneScore" label="卫生分" width="80" />
            <el-table-column prop="disciplineScore" label="纪律分" width="80" />
            <el-table-column prop="bonusScore" label="加分" width="80" />
            <el-table-column label="总分" width="80"><template #default="{ row }"><b>{{ row.totalScore }}</b></template></el-table-column>
            <el-table-column label="星级" width="150">
              <template #default="{ row }"><span class="stars">{{ STARS(row.starLevel) }}</span></template>
            </el-table-column>
          </el-table>
          <el-pagination
            style="margin-top: 12px; justify-content: flex-end"
            v-model:current-page="scorePage.current"
            :page-size="scorePage.size"
            :total="scorePage.total"
            layout="total, prev, pager, next"
            @current-change="loadScores"
          />
        </el-card>
      </el-tab-pane>

      <!-- 结果公示 + 初审 -->
      <el-tab-pane label="结果公示 / 初审" name="award">
        <el-card>
          <el-table :data="awards" v-loading="awardLoading" border>
            <el-table-column prop="period" label="周期" width="90" />
            <el-table-column prop="roomNo" label="宿舍" width="110" />
            <el-table-column prop="awardName" label="称号" width="150" />
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.finalStatus === 'FINAL' ? 'success' : row.finalStatus === 'INITIAL' ? 'primary' : 'info'" size="small">
                  {{ { PENDING: '待初审', INITIAL: '已初审', FINAL: '已终审' }[row.finalStatus] || row.finalStatus }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="140" fixed="right">
              <template #default="{ row }">
                <el-button v-if="row.finalStatus === 'PENDING'" type="success" link @click="initialReview(row)">初审通过</el-button>
                <el-button v-if="row.finalStatus === 'PENDING'" type="danger" link @click="initialReject(row)">驳回</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination
            style="margin-top: 12px; justify-content: flex-end"
            v-model:current-page="awardPage.current"
            :page-size="awardPage.size"
            :total="awardPage.total"
            layout="total, prev, pager, next"
            @current-change="loadAwards"
          />
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="ruleVisible" :title="ruleForm.id ? '编辑规则' : '新建规则'" width="480px">
      <el-form :model="ruleForm" label-width="90px">
        <el-form-item label="名称"><el-input v-model="ruleForm.name" /></el-form-item>
        <el-form-item label="卫生权重"><el-input-number v-model="ruleForm.hygieneWeight" :min="0" :max="100" style="width: 100%" /></el-form-item>
        <el-form-item label="纪律权重"><el-input-number v-model="ruleForm.disciplineWeight" :min="0" :max="100" style="width: 100%" /></el-form-item>
        <el-form-item label="加分权重"><el-input-number v-model="ruleForm.bonusWeight" :min="0" :max="100" style="width: 100%" /></el-form-item>
        <el-form-item label="星级阈值">
          <el-input v-model="ruleForm.thresholdsText" type="textarea" :rows="2" placeholder='JSON：{"3":60,"4":80,"5":95}' />
        </el-form-item>
        <el-form-item label="启用"><el-switch v-model="ruleForm.active" :active-value="1" :inactive-value="0" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="ruleVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveRule">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  listEvaluationRules, saveEvaluationRule, deleteEvaluationRule,
  calculateEvaluation, pageEvaluationScores, pageEvaluationAwards, initialReviewAward
} from '@/api/maint'
import { STARS } from '@/utils/format'

const tab = ref('rule')
const rules = ref([])
const scores = ref([])
const awards = ref([])
const loading = ref(false)
const scoreLoading = ref(false)
const awardLoading = ref(false)
const saving = ref(false)
const calcLoading = ref(false)
const calcPeriod = ref('')

const scoreFilter = reactive({ period: '' })
const scorePage = reactive({ current: 1, size: 10, total: 0 })
const awardPage = reactive({ current: 1, size: 10, total: 0 })

const ruleVisible = ref(false)
const ruleForm = reactive({ id: null, name: '', hygieneWeight: 50, disciplineWeight: 30, bonusWeight: 20, thresholdsText: '', active: 1 })

async function loadRules() {
  loading.value = true
  try {
    const data = await listEvaluationRules({ current: 1, size: 100 })
    rules.value = data?.records || data || []
  } catch (e) { rules.value = [] } finally { loading.value = false }
}

async function loadScores() {
  scoreLoading.value = true
  try {
    const data = await pageEvaluationScores({ period: scoreFilter.period || undefined, current: scorePage.current, size: scorePage.size })
    scores.value = data?.records || []
    scorePage.total = data?.total || 0
  } catch (e) { } finally { scoreLoading.value = false }
}

async function loadAwards() {
  awardLoading.value = true
  try {
    const data = await pageEvaluationAwards({ current: awardPage.current, size: awardPage.size })
    awards.value = data?.records || []
    awardPage.total = data?.total || 0
  } catch (e) { } finally { awardLoading.value = false }
}

function openRule(row) {
  if (row) {
    Object.assign(ruleForm, row)
    ruleForm.thresholdsText = typeof row.thresholds === 'string' ? row.thresholds : JSON.stringify(row.thresholds || {})
  } else {
    Object.assign(ruleForm, { id: null, name: '', hygieneWeight: 50, disciplineWeight: 30, bonusWeight: 20, thresholdsText: '', active: 1 })
  }
  ruleVisible.value = true
}

async function saveRule() {
  if (!ruleForm.name.trim()) return ElMessage.warning('请填写规则名称')
  saving.value = true
  try {
    await saveEvaluationRule({ ...ruleForm, thresholds: ruleForm.thresholdsText || undefined })
    ElMessage.success('保存成功')
    ruleVisible.value = false
    loadRules()
  } catch (e) { } finally { saving.value = false }
}

async function removeRule(row) {
  await ElMessageBox.confirm(`确定删除规则「${row.name}」？`, '警告', { type: 'warning' })
  try {
    await deleteEvaluationRule(row.id)
    ElMessage.success('删除成功')
    loadRules()
  } catch (e) { }
}

async function doCalculate() {
  if (!calcPeriod.value) return ElMessage.warning('请填写计算周期')
  calcLoading.value = true
  try {
    const r = await calculateEvaluation({ period: calcPeriod.value })
    ElMessage.success(`计算完成，共 ${r?.count ?? r ?? ''} 间宿舍参与评优`)
    scoreFilter.period = calcPeriod.value
    loadScores()
  } catch (e) { } finally { calcLoading.value = false }
}

async function initialReview(row) {
  try {
    await initialReviewAward({ awardId: row.id, finalStatus: 'INITIAL', note: '初审通过' })
    ElMessage.success('初审通过')
    loadAwards()
  } catch (e) { }
}
async function initialReject(row) {
  await ElMessageBox.confirm('确定驳回该评优结果？', '提示', { type: 'warning' })
  try {
    await initialReviewAward({ awardId: row.id, finalStatus: 'PENDING', note: '初审驳回' })
    ElMessage.success('已驳回')
    loadAwards()
  } catch (e) { }
}

onMounted(() => { loadRules(); loadScores(); loadAwards() })
</script>

<style scoped>
.page { padding: 4px; }
.page-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.page-head h2 { margin: 0; font-size: 18px; color: #303133; }
.stars { color: #e6a23c; font-size: 15px; }
</style>
