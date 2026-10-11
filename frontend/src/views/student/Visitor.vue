<template>
  <div class="page">
    <div class="page-head">
      <h2>🚪 访客登记</h2>
      <el-button type="primary" @click="openCreate">＋ 登记访客</el-button>
    </div>

    <el-card>
      <el-table :data="rows" v-loading="loading">
        <el-table-column prop="name" label="访客姓名" width="110" />
        <el-table-column prop="relation" label="关系" width="90" />
        <el-table-column prop="phone" label="联系电话" width="130" />
        <el-table-column prop="visitFrom" label="来访时间" width="160" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :color="mapOf(VISITOR_STATUS, row.status).color" style="border:none" size="small">
              {{ mapOf(VISITOR_STATUS, row.status).label }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && rows.length === 0" description="暂无访客记录" />
    </el-card>

    <el-dialog v-model="createVisible" title="登记访客" width="460px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="访客姓名"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="关系">
          <el-select v-model="form.relation" style="width: 100%">
            <el-option label="家长" value="家长" /><el-option label="亲戚" value="亲戚" />
            <el-option label="朋友" value="朋友" /><el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="联系电话"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item label="来访时间"><el-date-picker v-model="form.visitFrom" type="datetime" style="width:100%" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submit">提交登记</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { myVisitors, createVisitor } from '@/api/dorm'
import { mapOf, VISITOR_STATUS } from '@/utils/format'

const loading = ref(false)
const rows = ref([])
const createVisible = ref(false)
const submitting = ref(false)
const form = reactive({ name: '', relation: '家长', phone: '', visitFrom: '' })

async function load() {
  loading.value = true
  try {
    const data = await myVisitors({ current: 1, size: 50 })
    rows.value = data?.records || []
  } catch (e) { } finally { loading.value = false }
}

function openCreate() {
  form.name = ''; form.phone = ''; form.visitFrom = ''
  createVisible.value = true
}

async function submit() {
  if (!form.name.trim()) return ElMessage.warning('请填写访客姓名')
  submitting.value = true
  try {
    await createVisitor({ ...form })
    ElMessage.success('登记成功，等待宿管审批')
    createVisible.value = false
    load()
  } catch (e) { } finally { submitting.value = false }
}

onMounted(load)
</script>

<style scoped>
.page { padding: 4px; }
.page-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.page-head h2 { margin: 0; font-size: 18px; color: #303133; }
</style>
