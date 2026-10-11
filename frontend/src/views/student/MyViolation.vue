<template>
  <div class="page">
    <div class="page-head"><h2>📋 我的违纪</h2></div>
    <el-card>
      <el-table :data="rows" v-loading="loading">
        <el-table-column prop="type" label="违纪类型" width="130" />
        <el-table-column label="级别" width="90">
          <template #default="{ row }">
            <el-tag :color="mapOf(SEVERITY, row.severity).color" style="border:none" size="small">
              {{ mapOf(SEVERITY, row.severity).label }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column prop="happenedAt" label="发生时间" width="160" />
        <el-table-column prop="handleNote" label="处理结果" width="160" show-overflow-tooltip />
      </el-table>
      <el-empty v-if="!loading && rows.length === 0" description="暂无违纪记录，请继续保持" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { myViolations } from '@/api/eval'
import { mapOf, SEVERITY } from '@/utils/format'

const loading = ref(false)
const rows = ref([])

onMounted(async () => {
  loading.value = true
  try {
    const data = await myViolations({ current: 1, size: 100 })
    rows.value = data?.records || []
  } catch (e) { } finally { loading.value = false }
})
</script>

<style scoped>
.page { padding: 4px; }
.page-head h2 { margin: 0 0 12px; font-size: 18px; color: #303133; }
</style>
