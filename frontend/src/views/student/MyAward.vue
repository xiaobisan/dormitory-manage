<template>
  <div class="page">
    <div class="page-head"><h2>🏆 我的评优结果</h2></div>
    <el-card>
      <el-table :data="rows" v-loading="loading">
        <el-table-column prop="period" label="周期" width="90" />
        <el-table-column prop="hygieneScore" label="卫生分" width="80" />
        <el-table-column prop="disciplineScore" label="纪律分" width="80" />
        <el-table-column prop="bonusScore" label="加分" width="80" />
        <el-table-column label="总分" width="80">
          <template #default="{ row }"><b>{{ row.totalScore }}</b></template>
        </el-table-column>
        <el-table-column label="星级" width="140">
          <template #default="{ row }"><span class="stars">{{ STARS(row.starLevel) }}</span></template>
        </el-table-column>
        <el-table-column prop="awardName" label="称号" width="140" />
        <el-table-column label="审核状态" width="110">
          <template #default="{ row }">
            <el-tag size="small" :type="row.finalStatus === 'FINAL' ? 'success' : row.finalStatus === 'INITIAL' ? 'primary' : 'info'">
              {{ { PENDING: '待初审', INITIAL: '已初审', FINAL: '已终审' }[row.finalStatus] || row.finalStatus }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && rows.length === 0" description="暂无评优结果" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { myEvaluationScores } from '@/api/maint'
import { STARS } from '@/utils/format'

const loading = ref(false)
const rows = ref([])

onMounted(async () => {
  loading.value = true
  try {
    const data = await myEvaluationScores({ current: 1, size: 24 })
    rows.value = data?.records || []
  } catch (e) { } finally { loading.value = false }
})
</script>

<style scoped>
.page { padding: 4px; }
.page-head h2 { margin: 0 0 12px; font-size: 18px; color: #303133; }
.stars { color: #e6a23c; font-size: 16px; }
</style>
