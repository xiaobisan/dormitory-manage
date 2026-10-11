<template>
  <div class="page">
    <div class="page-head">
      <h2>👥 用户管理（RBAC）</h2>
      <el-button type="primary" @click="openCreate">＋ 新建用户</el-button>
    </div>

    <el-card>
      <el-form inline>
        <el-form-item label="用户名"><el-input v-model="filter.keyword" placeholder="姓名/账号" clearable @keyup.enter="load" style="width:150px" /></el-form-item>
        <el-form-item label="角色">
          <el-select v-model="filter.role" placeholder="全部角色" clearable style="width:140px" @change="load">
            <el-option v-for="(v, k) in ROLE_MAP" :key="k" :label="v" :value="k" />
          </el-select>
        </el-form-item>
        <el-button @click="load">查询</el-button>
      </el-form>

      <el-table :data="rows" v-loading="loading" border>
        <el-table-column prop="id" label="ID" width="90" />
        <el-table-column prop="username" label="账号" width="120" />
        <el-table-column prop="name" label="姓名" width="120" />
        <el-table-column label="角色" width="110">
          <template #default="{ row }">
            <el-tag size="small" :type="roleTag(row.role)">{{ ROLE_MAP[row.role] || row.role }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="buildingName" label="绑定楼栋" width="110" />
        <el-table-column prop="deptName" label="院系" width="130" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
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

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑用户' : '新建用户'" width="480px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="账号"><el-input v-model="form.username" :disabled="!!form.id" /></el-form-item>
        <el-form-item label="姓名"><el-input v-model="form.name" /></el-form-item>
        <el-form-item v-if="!form.id" label="初始密码"><el-input v-model="form.password" type="password" show-password /></el-form-item>
        <el-form-item label="角色">
          <el-select v-model="form.role" style="width: 100%">
            <el-option v-for="(v, k) in ROLE_MAP" :key="k" :label="v" :value="k" />
          </el-select>
        </el-form-item>
        <el-form-item label="手机号"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item label="绑定楼栋">
          <el-select v-model="form.buildingId" clearable filterable style="width: 100%">
            <el-option v-for="b in buildings" :key="b.id" :label="b.name" :value="b.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="院系">
          <el-select v-model="form.deptId" clearable filterable style="width: 100%">
            <el-option v-for="d in depts" :key="d.id" :label="d.name" :value="d.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio><el-radio :value="0">禁用</el-radio>
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
import { pageUsers, createUser, updateUser, deleteUser } from '@/api/system'
import { pageBuildings } from '@/api/dorm'
import { ROLE_MAP } from '@/utils/format'

const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const rows = ref([])
const buildings = ref([])
const depts = ref([{ id: 1, name: '计算机系' }, { id: 2, name: '机械系' }, { id: 3, name: '经管系' }])

const filter = reactive({ keyword: '', role: '' })
const page = reactive({ current: 1, size: 10, total: 0 })
const form = reactive(emptyForm())

function emptyForm() {
  return { id: null, username: '', password: '', name: '', role: 'STUDENT', phone: '', buildingId: null, deptId: null, status: 1 }
}

function roleTag(role) {
  return { STUDENT: 'info', DORM_MANAGER: 'warning', REPAIR_WORKER: 'danger', COUNSELOR: 'success', ADMIN: '' }[role] || 'info'
}

async function load() {
  loading.value = true
  try {
    const data = await pageUsers({ ...filter, current: page.current, size: page.size })
    rows.value = data?.records || []
    page.total = data?.total || 0
  } catch (e) { } finally { loading.value = false }
}

async function loadBuildings() {
  try { buildings.value = (await pageBuildings({ current: 1, size: 100 }))?.records || [] } catch (e) { }
}

function openCreate() {
  Object.assign(form, emptyForm())
  dialogVisible.value = true
}
function openEdit(row) {
  Object.assign(form, row, { password: '' })
  dialogVisible.value = true
}

async function save() {
  if (!form.username.trim() || !form.name.trim()) return ElMessage.warning('请填写账号和姓名')
  if (!form.id && !form.password) return ElMessage.warning('新建用户请填写初始密码')
  saving.value = true
  try {
    if (form.id) await updateUser({ ...form })
    else await createUser({ ...form })
    ElMessage.success('保存成功')
    dialogVisible.value = false
    load()
  } catch (e) { } finally { saving.value = false }
}

async function remove(row) {
  await ElMessageBox.confirm(`确定删除用户「${row.name}」？`, '警告', { type: 'warning' })
  try {
    await deleteUser(row.id)
    ElMessage.success('删除成功')
    load()
  } catch (e) { }
}

onMounted(() => { load(); loadBuildings() })
</script>

<style scoped>
.page { padding: 4px; }
.page-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.page-head h2 { margin: 0; font-size: 18px; color: #303133; }
</style>
