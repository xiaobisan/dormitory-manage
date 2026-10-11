<template>
  <div class="page">
    <div class="page-head">
      <h2>🏢 楼栋管理</h2>
      <el-button type="primary" @click="openCreate">＋ 新建楼栋</el-button>
    </div>
    <el-card>
      <el-table :data="rows" v-loading="loading" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="楼栋名称" width="150" />
        <el-table-column prop="floorCount" label="楼层数" width="90" />
        <el-table-column prop="roomCount" label="宿舍数" width="90" />
        <el-table-column prop="occupancy" label="入住率" width="140">
          <template #default="{ row }">
            <el-progress :percentage="Number(row.occupancy || 0)" :stroke-width="10" />
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" show-overflow-tooltip />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="openEdit(row)">编辑</el-button>
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

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑楼栋' : '新建楼栋'" width="420px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="楼栋名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="楼层数">
          <el-input-number v-model="form.floorCount" :min="0" :max="30" style="width: 100%" />
        </el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { pageBuildings, createBuilding, updateBuilding, deleteBuilding } from '@/api/dorm'

const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const rows = ref([])
const page = reactive({ current: 1, size: 10, total: 0 })
const form = reactive({ id: null, name: '', floorCount: 4, remark: '' })

async function load() {
  loading.value = true
  try {
    const data = await pageBuildings({ current: page.current, size: page.size })
    rows.value = data?.records || []
    page.total = data?.total || 0
  } catch (e) { } finally { loading.value = false }
}

function openCreate() {
  Object.assign(form, { id: null, name: '', floorCount: 4, remark: '' })
  dialogVisible.value = true
}
function openEdit(row) {
  Object.assign(form, row)
  dialogVisible.value = true
}

async function save() {
  if (!form.name.trim()) return ElMessage.warning('请填写楼栋名称')
  saving.value = true
  try {
    if (form.id) await updateBuilding({ ...form })
    else await createBuilding({ ...form })
    ElMessage.success('保存成功')
    dialogVisible.value = false
    load()
  } catch (e) { } finally { saving.value = false }
}

async function remove(row) {
  await ElMessageBox.confirm(`确定删除楼栋「${row.name}」？其下宿舍/床位会一并影响`, '警告', { type: 'warning' })
  try {
    await deleteBuilding(row.id)
    ElMessage.success('删除成功')
    load()
  } catch (e) { }
}

onMounted(load)
</script>

<style scoped>
.page { padding: 4px; }
.page-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.page-head h2 { margin: 0; font-size: 18px; color: #303133; }
</style>
