<template>
  <div class="page">
    <div class="page-head">
      <h2>✅ 评优终审</h2>
      <el-select v-model="filter.finalStatus" placeholder="状态" clearable style="width: 130px" @change="load">
        <el-option label="待终审" value="INITIAL" />
        <el-option label="已终审" value="FINAL" />
      </el-select>
    </div>

    <el-card>
      <el-table :data="rows" v-loading="loading" border>
        <el-table-column prop="period" label="周期" width="90" />
        <el-table-column prop="roomNo" label="宿舍" width="110" />
        <el-table-column prop="awardName" label="评优称号" width="160" />
        <el-table-column prop="reviewerName" label="初审人" width="110" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.finalStatus === 'FINAL' ? 'success' : row.finalStatus === 'INITIAL' ? 'primary' : 'info'" size="small">
              {{ { PENDING: '待初审', INITIAL: '待终审', FINAL: '已终审' }[row.finalStatus] || row.finalStatus }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="终审操作" width="180" fixed="right">
          <template #default="{ row }">
            <template v-if="row.finalStatus === 'INITIAL'">
              <el-button type="success" link @click="final(row, 'FINAL')">通过</el-button>
              <el-button type="danger" link @click="final(row, 'INITIAL')">退回</el-button>
            </template>
            <el-text v-else-if="row.finalStatus === 'FINAL'" type="success">已终审</el-text>
            <el-text v-else type="info">待初审</el-text>
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
import { pageEvaluationAwards, finalReviewAward } from '@/api/maint'

const loading = ref(false)
const rows = ref([])
const filter = reactive({ finalStatus: 'INITIAL' })
const page = reactive({ current: 1, size: 10, total: 0 })

async function load() {
  loading.value = true
  try {
    const data = await pageEvaluationAwards({ finalStatus: filter.finalStatus || undefined, current: page.current, size: page.size })
    rows.value = data?.records || []
    page.total = data?.total || 0
  } catch (e) { } finally { loading.value = false }
}

async function final(row, status) {
  try {
    await finalReviewAward({ awardId: row.id, finalStatus: status, note: status === 'FINAL' ? '终审通过' : '终审退回' })
    ElMessage.success('操作成功')
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
