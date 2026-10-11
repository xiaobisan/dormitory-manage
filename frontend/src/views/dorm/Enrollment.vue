<template>
  <div class="page">
    <div class="page-head"><h2>📥 入住 / 退宿办理</h2></div>

    <el-tabs v-model="tab">
      <!-- 入住 -->
      <el-tab-pane label="入住办理" name="in">
        <el-card>
          <el-form :model="checkInForm" label-width="90px" style="max-width: 560px">
            <el-form-item label="学生">
              <el-select v-model="checkInForm.studentId" filterable placeholder="选择学生" style="width: 100%">
                <el-option v-for="s in students" :key="s.id" :label="`${s.name}（${s.username}）`" :value="s.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="宿舍">
              <el-select v-model="checkInForm.roomId" filterable placeholder="选择宿舍" style="width: 100%">
                <el-option v-for="r in freeRooms" :key="r.id" :label="`${r.buildingName || ''} ${r.roomNo}`" :value="r.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="床位">
              <el-select v-model="checkInForm.bedId" placeholder="选择床位" style="width: 100%">
                <el-option v-for="b in freeBeds" :key="b.id" :label="`第 ${b.bedNo} 床`" :value="b.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="备注"><el-input v-model="checkInForm.reason" /></el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="saving" @click="doCheckIn">办理入住</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>

      <!-- 退宿 -->
      <el-tab-pane label="退宿办理" name="out">
        <el-card>
          <el-form :model="checkOutForm" label-width="90px" style="max-width: 560px">
            <el-form-item label="学生">
              <el-select v-model="checkOutForm.studentId" filterable placeholder="选择学生" style="width: 100%">
                <el-option v-for="s in students" :key="s.id" :label="`${s.name}（${s.username}）`" :value="s.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="原因"><el-input v-model="checkOutForm.reason" placeholder="退宿原因" /></el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="saving" @click="doCheckOut">办理退宿</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>

      <!-- 记录 -->
      <el-tab-pane label="入住记录" name="records">
        <el-card>
          <el-table :data="records" v-loading="loading" border>
            <el-table-column prop="studentName" label="学生" width="110" />
            <el-table-column prop="buildingName" label="楼栋" width="100" />
            <el-table-column prop="roomNo" label="宿舍" width="90" />
            <el-table-column prop="bedNo" label="床号" width="70" />
            <el-table-column prop="checkInAt" label="入住时间" width="160" />
            <el-table-column prop="checkOutAt" label="退宿时间" width="160" />
            <el-table-column label="状态" width="90">
              <template #default="{ row }">
                <el-tag :color="mapOf(ENROLL_STATUS, row.status).color" style="border:none" size="small">
                  {{ mapOf(ENROLL_STATUS, row.status).label }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="reason" label="备注" show-overflow-tooltip />
          </el-table>
          <el-pagination
            style="margin-top: 12px; justify-content: flex-end"
            v-model:current-page="page.current"
            :page-size="page.size"
            :total="page.total"
            layout="total, prev, pager, next"
            @current-change="loadRecords"
          />
        </el-card>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { pageUsers } from '@/api/system'
import { pageRooms as roomApi, listBedsByRoom as bedApi, pageEnrollments, checkIn, checkOut } from '@/api/dorm'
import { mapOf, ENROLL_STATUS } from '@/utils/format'

const tab = ref('in')
const students = ref([])
const allRooms = ref([])
const saving = ref(false)
const checkInForm = reactive({ studentId: null, roomId: null, bedId: null, reason: '' })
const checkOutForm = reactive({ studentId: null, reason: '' })

const freeRooms = computed(() => allRooms.value)
const freeBeds = computed(() =>
  allRooms.value
    .filter((r) => r.id === checkInForm.roomId)
    .flatMap((r) => r.beds || [])
    .filter((b) => b.status === 'EMPTY')
)

const records = ref([])
const loading = ref(false)
const page = reactive({ current: 1, size: 10, total: 0 })

async function loadBase() {
  const [u, r] = await Promise.allSettled([
    pageUsers({ current: 1, size: 200 }),
    roomApi({ current: 1, size: 200 })
  ])
  students.value = u.status === 'fulfilled' ? u.value?.records || [] : []
  allRooms.value = r.status === 'fulfilled'
    ? (r.value?.records || []).map((rm) => ({ ...rm, beds: [] }))
    : []
  // 给每个宿舍挂床位（批量拉取，失败则空）
  for (const rm of allRooms.value) {
    bedApi(rm.id).then((bs) => (rm.beds = bs || [])).catch(() => (rm.beds = []))
  }
}

async function doCheckIn() {
  if (!checkInForm.studentId || !checkInForm.roomId || !checkInForm.bedId) {
    return ElMessage.warning('请完整选择学生、宿舍、床位')
  }
  saving.value = true
  try {
    await checkIn({ ...checkInForm })
    ElMessage.success('入住办理成功')
    Object.assign(checkInForm, { studentId: null, roomId: null, bedId: null, reason: '' })
    loadRecords()
  } catch (e) { } finally { saving.value = false }
}

async function doCheckOut() {
  if (!checkOutForm.studentId) return ElMessage.warning('请选择学生')
  saving.value = true
  try {
    await checkOut({ ...checkOutForm })
    ElMessage.success('退宿办理成功')
    Object.assign(checkOutForm, { studentId: null, reason: '' })
    loadRecords()
  } catch (e) { } finally { saving.value = false }
}

async function loadRecords() {
  loading.value = true
  try {
    const data = await pageEnrollments({ current: page.current, size: page.size })
    records.value = data?.records || []
    page.total = data?.total || 0
  } catch (e) { } finally { loading.value = false }
}

onMounted(() => { loadBase(); loadRecords() })
</script>

<style scoped>
.page { padding: 4px; }
.page-head h2 { margin: 0 0 12px; font-size: 18px; color: #303133; }
</style>
