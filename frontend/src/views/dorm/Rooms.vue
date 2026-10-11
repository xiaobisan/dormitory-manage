<template>
  <div class="page">
    <div class="page-head">
      <h2>🚪 宿舍管理</h2>
      <el-button type="primary" @click="openCreate">＋ 新建宿舍</el-button>
    </div>
    <el-card>
      <el-form inline @submit.prevent>
        <el-form-item label="楼栋">
          <el-select v-model="filter.buildingId" placeholder="全部楼栋" clearable style="width: 160px" @change="load">
            <el-option v-for="b in buildings" :key="b.id" :label="b.name" :value="b.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="楼层">
          <el-select v-model="filter.floor" placeholder="全部楼层" clearable style="width: 120px" @change="load">
            <el-option v-for="f in floors" :key="f" :label="f + ' 层'" :value="f" />
          </el-select>
        </el-form-item>
        <el-button @click="load">查询</el-button>
      </el-form>

      <el-table :data="rows" v-loading="loading" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="buildingName" label="楼栋" width="120" />
        <el-table-column prop="floor" label="楼层" width="70" />
        <el-table-column prop="roomNo" label="宿舍号" width="110" />
        <el-table-column prop="capacity" label="床位数" width="80" />
        <el-table-column label="入住" width="90">
          <template #default="{ row }">{{ row.occupancy ?? row.occupiedCount ?? '-' }}/{{ row.capacity }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :color="mapOf(ROOM_STATUS, row.status).color" style="border:none" size="small">
              {{ mapOf(ROOM_STATUS, row.status).label }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="headStudentName" label="宿舍长" width="100" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="openEdit(row)">编辑</el-button>
            <el-button type="success" link @click="showBeds(row)">床位</el-button>
            <el-button type="danger" link @click="remove(row)">删除</el-button>
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

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑宿舍' : '新建宿舍'" width="440px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="所属楼栋">
          <el-select v-model="form.buildingId" style="width: 100%">
            <el-option v-for="b in buildings" :key="b.id" :label="b.name" :value="b.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="楼层"><el-input-number v-model="form.floor" :min="1" style="width: 100%" /></el-form-item>
        <el-form-item label="宿舍号"><el-input v-model="form.roomNo" /></el-form-item>
        <el-form-item label="床位数">
          <el-input-number v-model="form.capacity" :min="1" :max="8" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>

    <!-- 床位子表 -->
    <el-dialog v-model="bedVisible" :title="`宿舍 ${bedRoom?.roomNo} 床位`" width="520px">
      <el-table :data="beds" size="small">
        <el-table-column prop="bedNo" label="床号" width="80" />
        <el-table-column label="状态">
          <template #default="{ row }">
            <el-tag :color="mapOf(BED_STATUS, row.status).color" style="border:none" size="small">
              {{ mapOf(BED_STATUS, row.status).label }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="studentName" label="入住学生" />
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  pageRooms, createRoom, updateRoom, deleteRoom, pageBuildings, listBedsByRoom
} from '@/api/dorm'
import { mapOf, ROOM_STATUS, BED_STATUS } from '@/utils/format'

const floors = Array.from({ length: 8 }, (_, i) => i + 1)
const buildings = ref([])
const rows = ref([])
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const filter = reactive({ buildingId: null, floor: null })
const page = reactive({ current: 1, size: 10, total: 0 })
const form = reactive({ id: null, buildingId: null, floor: 1, roomNo: '', capacity: 4, status: 'EMPTY' })

const bedVisible = ref(false)
const beds = ref([])
const bedRoom = ref(null)

async function load() {
  loading.value = true
  try {
    const data = await pageRooms({ ...filter, current: page.current, size: page.size })
    rows.value = data?.records || []
    page.total = data?.total || 0
  } catch (e) { } finally { loading.value = false }
}

async function loadBuildings() {
  try {
    const data = await pageBuildings({ current: 1, size: 100 })
    buildings.value = data?.records || []
  } catch (e) { }
}

function openCreate() {
  Object.assign(form, { id: null, buildingId: null, floor: 1, roomNo: '', capacity: 4, status: 'EMPTY' })
  dialogVisible.value = true
}
function openEdit(row) {
  Object.assign(form, row)
  dialogVisible.value = true
}

async function save() {
  if (!form.buildingId || !form.roomNo.trim()) return ElMessage.warning('请选择楼栋并填写宿舍号')
  saving.value = true
  try {
    if (form.id) await updateRoom({ ...form })
    else await createRoom({ ...form })
    ElMessage.success('保存成功')
    dialogVisible.value = false
    load()
  } catch (e) { } finally { saving.value = false }
}

async function remove(row) {
  await ElMessageBox.confirm(`确定删除宿舍「${row.roomNo}」？`, '警告', { type: 'warning' })
  try {
    await deleteRoom(row.id)
    ElMessage.success('删除成功')
    load()
  } catch (e) { }
}

async function showBeds(row) {
  bedRoom.value = row
  try {
    beds.value = await listBedsByRoom(row.id) || []
  } catch (e) {
    beds.value = []
  }
  bedVisible.value = true
}

onMounted(() => { loadBuildings(); load() })
</script>

<style scoped>
.page { padding: 4px; }
.page-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.page-head h2 { margin: 0; font-size: 18px; color: #303133; }
</style>
