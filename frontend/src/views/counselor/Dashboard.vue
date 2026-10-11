<template>
  <div class="page">
    <div class="page-head"><h2>📈 看板报表（本系）</h2></div>

    <el-row :gutter="16">
      <el-col :span="6" v-for="kpi in kpis" :key="kpi.label">
        <el-card class="kpi-card">
          <div class="kpi-value">{{ kpi.value }}</div>
          <div class="kpi-label">{{ kpi.label }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" style="margin-top: 16px">
      <el-col :span="12">
        <el-card><div ref="chart1" class="chart" /></el-card>
      </el-col>
      <el-col :span="12">
        <el-card><div ref="chart2" class="chart" /></el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { counselorDashboard } from '@/api/eval'
import { initChart, watchResize, lineOrBar, pieOption } from '@/utils/echarts'

const kpis = ref([])
const chart1 = ref(null)
const chart2 = ref(null)
let c1 = null
let c2 = null
let cleanups = []

async function load() {
  try {
    const data = await counselorDashboard({})
    if (!data) return
    kpis.value = [
      { label: '学生总数', value: data.studentCount ?? '-' },
      { label: '本月违纪', value: data.violationCount ?? '-' },
      { label: '评优宿舍', value: data.awardCount ?? '-' },
      { label: '卫生平均分', value: data.avgHygieneScore ?? '-' }
    ]
    const trend = data.violationTrend || []
    c1 = initChart(chart1.value, lineOrBar({
      title: '违纪趋势（近 6 月）',
      x: trend.map((t) => t.period || t.month || ''),
      series: [{ name: '违纪次数', data: trend.map((t) => t.count || t.value || 0) }]
    }))
    const top = data.topRooms || []
    c2 = initChart(chart2.value, {
      title: { text: '卫生 Top 宿舍', left: 'center', textStyle: { fontSize: 14 } },
      tooltip: { trigger: 'axis' },
      grid: { left: 40, right: 20, top: 40, bottom: 30 },
      xAxis: { type: 'value' },
      yAxis: { type: 'category', data: top.map((r) => r.roomNo || r.name || '') },
      series: [{ name: '卫生分', type: 'bar', data: top.map((r) => r.score || r.hygieneScore || 0) }]
    })
    cleanups = [watchResize(chart1.value, c1), watchResize(chart2.value, c2)]
  } catch (e) {
    /* 后端未就绪时展示 KPI 占位 */
    kpis.value = [
      { label: '学生总数', value: '-' },
      { label: '本月违纪', value: '-' },
      { label: '评优宿舍', value: '-' },
      { label: '卫生平均分', value: '-' }
    ]
    c1 = initChart(chart1.value, { title: { text: '违纪趋势（暂无数据）', left: 'center' } })
    c2 = initChart(chart2.value, { title: { text: '卫生 Top（暂无数据）', left: 'center' } })
  }
}

onMounted(load)
onBeforeUnmount(() => {
  cleanups.forEach((fn) => fn?.())
  c1?.dispose()
  c2?.dispose()
})
</script>

<style scoped>
.page { padding: 4px; }
.page-head h2 { margin: 0 0 12px; font-size: 18px; color: #303133; }
.kpi-card { text-align: center; }
.kpi-value { font-size: 26px; font-weight: 700; color: #409eff; }
.kpi-label { color: #909399; font-size: 13px; margin-top: 4px; }
.chart { width: 100%; height: 300px; }
</style>
