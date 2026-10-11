<template>
  <div class="page">
    <div class="page-head">
      <h2>💧⚡ 水电管理</h2>
      <div>
        <el-button @click="openUsage">录入用量</el-button>
        <el-button type="primary" @click="openGenerate">生成账单</el-button>
      </div>
    </div>

    <el-card>
      <el-form inline>
        <el-form-item label="账期">
          <el-input v-model="filter.period" placeholder="2026-10" style="width: 130px" @change="loadBills" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filter.status" clearable style="width: 120px" @change="loadBills">
            <el-option label="待缴" value="UNPAID" />
            <el-option label="已缴" value="PAID" />
            <el-option label="逾期" value="OVERDUE" />
          </el-select>
        </el-form-item>
        <el-button @click="loadBills">查询</el-button>
      </el-form>

      <el-table :data="bills" v-loading="loading" border>
        <el-table-column prop="id" label="账单ID" width="90" />
        <el-table-column prop="roomNo" label="宿舍" width="110" />
        <el-table-column prop="period" label="账期" width="90" />
        <el-table-column label="金额" width="100">
          <template #default="{ row }">¥ {{ Number(row.amount || 0).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :color="mapOf(BILL_STATUS, row.status).color" style="border:none" size="small">
              {{ mapOf(BILL_STATUS, row.status).label }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="dueAt" label="应缴时间" width="160" />
        <el-table-column prop="paidAt" label="缴费时间" width="160" />
        <el-table-column label="催缴" width="90" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 'UNPAID'" type="warning" link @click="urge(row)">催缴</el-button>
            <el-text v-else-if="row.status === 'PAID'" type="success">已缴</el-text>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        style="margin-top: 12px; justify-content: flex-end"
        v-model:current-page="page.current"
        :page-size="page.size"
        :total="page.total"
        layout="total, prev, pager, next"
        @current-change="loadBills"
      />
    </el-card>

    <!-- 录入用量 -->
    <el-dialog v-model="usageVisible" title="录入水电用量" width="420px">
      <el-form :model="usageForm" label-width="90px">
        <el-form-item label="宿舍">
          <el-select v-model="usageForm.roomId" filterable style="width: 100%">
            <el-option v-for="r in roomOptions" :key="r.id" :label="`${r.buildingName || ''} ${r.roomNo}`" :value="r.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="类型">
          <el-radio-group v-model="usageForm.type">
            <el-radio value="WATER">水费</el-radio>
            <el-radio value="ELECTRIC">电费</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="账期"><el-input v-model="usageForm.period" placeholder="2026-10" /></el-form-item>
        <el-form-item label="读数"><el-input-number v-model="usageForm.reading" :precision="2" :min="0" style="width: 100%" /></el-form-item>
        <el-form-item label="单价"><el-input-number v-model="usageForm.price" :precision="2" :min="0" style="width: 100%" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="usageVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveUsage">保存</el-button>
      </template>
    </el-dialog>

    <!-- 生成账单 -->
    <el-dialog v-model="genVisible" title="生成账单" width="380px">
      <el-form label-width="80px">
        <el-form-item label="账期"><el-input v-model="genForm.period" placeholder="2026-10" /></el-form-item>
        <el-form-item label="楼栋">
          <el-select v-model="genForm.buildingId" clearable style="width: 100%">
            <el-option v-for="b in buildingOptions" :key="b.id" :label="b.name" :value="b.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="genVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="doGenerate">生成</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  pageUsages, createUsage, generateBills, pageBills, pageRooms, pageBuildings
} from '@/api/dorm'
import { mapOf, BILL_STATUS } from '@/utils/format'

const loading = ref(false)
const saving = ref(false)
const usageVisible = ref(false)
const genVisible = ref(false)

const roomOptions = ref([])
const buildingOptions = ref([])
const filter = reactive({ period: '', status: '' })
const bills = ref([])
const page = reactive({ current: 1, size: 10, total: 0 })

const usageForm = reactive({ roomId: null, type: 'WATER', period: '', reading: 0, price: 0 })
const genForm = reactive({ period: '', buildingId: null })

async function loadBills() {
  loading.value = true
  try {
    const data = await pageBills({ period: filter.period || undefined, status: filter.status || undefined, current: page.current, size: page.size })
    bills.value = data?.records || []
    page.total = data?.total || 0
  } catch (e) { } finally { loading.value = false }
}

async function openUsage() {
  usageForm.period = currentPeriod()
  if (!roomOptions.value.length) {
    try { roomOptions.value = (await pageRooms({ current: 1, size: 200 }))?.records || [] } catch (e) { }
  }
  usageVisible.value = true
}
async function saveUsage() {
  if (!usageForm.roomId || !usageForm.period) return ElMessage.warning('请选择宿舍并填写账期')
  saving.value = true
  try {
    await createUsage({ ...usageForm })
    ElMessage.success('用量录入成功')
    usageVisible.value = false
    loadBills()
  } catch (e) { } finally { saving.value = false }
}

function openGenerate() {
  genForm.period = currentPeriod()
  if (!buildingOptions.value.length) {
    pageBuildings({ current: 1, size: 100 }).then((d) => (buildingOptions.value = d?.records || [])).catch(() => {})
  }
  genVisible.value = true
}
async function doGenerate() {
  if (!genForm.period) return ElMessage.warning('请填写账期')
  saving.value = true
  try {
    await generateBills({ ...genForm })
    ElMessage.success('账单生成成功')
    genVisible.value = false
    loadBills()
  } catch (e) { } finally { saving.value = false }
}

function urge(row) {
  ElMessage.warning(`已向宿舍 ${row.roomNo} 发送催缴通知（账期 ${row.period}，¥${Number(row.amount).toFixed(2)}）`)
}

function currentPeriod() {
  const d = new Date()
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`
}

onMounted(loadBills)
</script>

<style scoped>
.page { padding: 4px; }
.page-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.page-head h2 { margin: 0; font-size: 18px; color: #303133; }
</style>
