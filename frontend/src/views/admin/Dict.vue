<template>
  <div class="page">
    <div class="page-head">
      <h2>📚 字典管理</h2>
      <el-button type="primary" @click="openCreate">＋ 新建字典项</el-button>
    </div>

    <el-card>
      <el-form inline>
        <el-form-item label="字典类型">
          <el-input v-model="filter.type" placeholder="如 REPAIR_CATEGORY" clearable @keyup.enter="load" style="width: 200px" />
        </el-form-item>
        <el-button @click="load">查询</el-button>
      </el-form>

      <el-table :data="rows" v-loading="loading" border>
        <el-table-column prop="id" label="ID" width="90" />
        <el-table-column prop="type" label="字典类型" width="180" />
        <el-table-column prop="dictKey" label="键" width="120" />
        <el-table-column prop="label" label="值/名称" width="180" />
        <el-table-column prop="sort" label="排序" width="80" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
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

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑字典项' : '新建字典项'" width="440px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="字典类型"><el-input v-model="form.type" :disabled="!!form.id" placeholder="REPAIR_CATEGORY" /></el-form-item>
        <el-form-item label="键"><el-input v-model="form.dictKey" :disabled="!!form.id" /></el-form-item>
        <el-form-item label="值/名称"><el-input v-model="form.label" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sort" :min="0" style="width: 100%" /></el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio><el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
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
import { listDictItems, createDictItem, updateDictItem, deleteDictItem } from '@/api/system'

const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const rows = ref([])
const filter = reactive({ type: '' })
const page = reactive({ current: 1, size: 10, total: 0 })
const form = reactive(emptyForm())

function emptyForm() {
  return { id: null, type: '', dictKey: '', label: '', sort: 0, status: 1 }
}

async function load() {
  loading.value = true
  try {
    const data = await listDictItems(filter.type || undefined)
    rows.value = Array.isArray(data) ? data : data?.records || []
  } catch (e) { rows.value = [] } finally { loading.value = false }
}

function openCreate() {
  Object.assign(form, emptyForm(), { type: filter.type })
  dialogVisible.value = true
}
function openEdit(row) {
  Object.assign(form, row)
  dialogVisible.value = true
}

async function save() {
  if (!form.type.trim() || !form.dictKey.trim()) return ElMessage.warning('请填写字典类型和键')
  saving.value = true
  try {
    if (form.id) await updateDictItem({ ...form })
    else await createDictItem({ ...form })
    ElMessage.success('保存成功')
    dialogVisible.value = false
    load()
  } catch (e) { } finally { saving.value = false }
}

async function remove(row) {
  await ElMessageBox.confirm(`确定删除字典项「${row.label}」？`, '警告', { type: 'warning' })
  try {
    await deleteDictItem(row.id)
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
