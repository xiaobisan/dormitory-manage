<template>
  <div class="page">
    <div class="page-head">
      <h2>🚪 访客审批</h2>
      <el-button @click="load">刷新</el-button>
    </div>
    <el-card>
      <el-form inline>
        <el-form-item label="状态">
          <el-select v-model="filter.status" clearable style="width: 130px" @change="load">
            <el-option v-for="(v, k) in VISITOR_STATUS" :key="k" :label="v.label" :value="k" />
          </el-select>
        </el-form-item>
        <el-button @click="load">查询</el-button>
      </el-form>

      <el-table :data="rows" v-loading="loading" border>
        <el-table-column prop="id" label="ID" width="90" />
        <el-table-column prop="studentName" label="被访学生" width="110" />
        <el-table-column prop="name" label="访客姓名" width="110" />
        <el-table-column prop="relation" label="关系" width="90" />
        <el-table-column prop="phone" label="电话" width="130" />
        <el-table-column prop="visitFrom" label="来访时间" width="160" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :color="mapOf(VISITOR_STATUS, row.status).color" style="border:none" size="small">
              {{ mapOf(VISITOR_STATUS, row.status).label }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <template v-if="row.status === 'PENDING'">
              <el-button type="success" link @click="audit(row, 'APPROVED')">通过</el-button>
              <el-button type="danger" link @click="audit(row, 'REJECTED')">拒绝</el-button>
            </template>
            <template v-else>
              <el-button type="info" link @click="markLeft(row)">标记离场</el-button>
            </template>
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { pageVisitors, auditVisitor } from '@/api/dorm'
import { mapOf, VISITOR_STATUS } from '@/utils/format'

const loading = ref(false)
const rows = ref([])
const filter = reactive({ status: 'PENDING' })
const page = reactive({ current: 1, size: 10, total: 0 })

async function load() {
  loading.value = true
  try {
    const data = await pageVisitors({ status: filter.status || undefined, current: page.current, size: page.size })
    rows.value = data?.records || []
    page.total = data?.total || 0
  } catch (e) { } finally { loading.value = false }
}

async function audit(row, status) {
  try {
    await auditVisitor({ visitorId: row.id, status })
    ElMessage.success('审批完成')
    load()
  } catch (e) { }
}

async function markLeft(row) {
  try {
    await auditVisitor({ visitorId: row.id, status: 'LEAVED' })
    ElMessage.success('已标记离场')
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
