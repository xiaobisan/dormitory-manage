<template>
  <div class="page">
    <div class="page-head">
      <h2>💰 费用缴费</h2>
      <el-select v-model="period" style="width: 140px" @change="load">
        <el-option v-for="p in periods" :key="p" :label="p" :value="p" />
      </el-select>
    </div>

    <el-card>
      <el-table :data="rows" v-loading="loading">
        <el-table-column prop="period" label="账期" width="90" />
        <el-table-column prop="amount" label="金额（元）" width="110">
          <template #default="{ row }">¥ {{ Number(row.amount || 0).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :color="mapOf(BILL_STATUS, row.status).color" style="border:none" size="small">
              {{ mapOf(BILL_STATUS, row.status).label }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="paidAt" label="缴费时间" width="160" />
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button v-if="row.status !== 'PAID'" type="primary" @click="pay(row)">立即缴费</el-button>
            <el-text v-else type="success">已缴</el-text>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && rows.length === 0" description="暂无账单" />
    </el-card>

    <!-- 缴费 -->
    <el-dialog v-model="payVisible" title="在线缴费" width="400px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="账期">{{ payBill?.period }}</el-descriptions-item>
        <el-descriptions-item label="应缴金额">¥ {{ Number(payBill?.amount || 0).toFixed(2) }}</el-descriptions-item>
      </el-descriptions>
      <el-form label-width="80px" style="margin-top: 16px">
        <el-form-item label="支付方式">
          <el-radio-group v-model="payForm.method">
            <el-radio value="ALIPAY">支付宝</el-radio>
            <el-radio value="WECHAT">微信</el-radio>
            <el-radio value="CASH">现金</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="payVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitPay">确认支付</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { myBills, payBill as payApi } from '@/api/dorm'
import { mapOf, BILL_STATUS } from '@/utils/format'

const loading = ref(false)
const rows = ref([])
const period = ref(currentPeriod())
const periods = ref(buildPeriods())

const payVisible = ref(false)
const submitting = ref(false)
const payBill = ref(null)
const payForm = ref({ billId: null, method: 'ALIPAY' })

function currentPeriod() {
  const d = new Date()
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`
}
function buildPeriods() {
  const arr = []
  const d = new Date()
  for (let i = 0; i < 6; i++) {
    const dt = new Date(d.getFullYear(), d.getMonth() - i, 1)
    arr.push(`${dt.getFullYear()}-${String(dt.getMonth() + 1).padStart(2, '0')}`)
  }
  return arr
}

async function load() {
  loading.value = true
  try {
    const data = await myBills({ current: 1, size: 50, period: period.value })
    rows.value = data?.records || []
  } catch (e) { } finally {
    loading.value = false
  }
}

function pay(row) {
  payBill.value = row
  payForm.value = { billId: row.id, method: 'ALIPAY' }
  payVisible.value = true
}

async function submitPay() {
  submitting.value = true
  try {
    await payApi({ billId: payForm.value.billId, method: payForm.value.method })
    ElMessage.success('缴费成功')
    payVisible.value = false
    load()
  } catch (e) { } finally {
    submitting.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.page { padding: 4px; }
.page-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.page-head h2 { margin: 0; font-size: 18px; color: #303133; }
</style>
