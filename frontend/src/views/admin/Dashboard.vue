<template>
  <div class="page">
    <div class="page-head"><h2>📊 全局看板</h2></div>

    <el-row :gutter="16">
      <el-col :span="4" v-for="kpi in kpis" :key="kpi.label">
        <el-card class="kpi">
          <div class="kpi-value" :style="{ color: kpi.color }">{{ kpi.value }}</div>
          <div class="kpi-label">{{ kpi.label }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" style="margin-top: 16px">
      <el-col :span="12">
        <el-card><div ref="chartOccupancy" class="chart" /></el-card>
      </el-col>
      <el-col :span="12">
        <el-card><div ref="chartCollect" class="chart" /></el-card>
      </el-col>
    </el-row>
    <el-row :gutter="16" style="margin-top: 16px">
      <el-col :span="8">
        <el-card><div ref="chartRepair" class="chart" /></el-card>
      </el-col>
      <el-col :span="8">
        <el-card><div ref="chartViolation" class="chart" /></el-card>
      </el-col>
      <el-col :span="8">
        <el-card><div ref="chartTop" class="chart" /></el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { globalDashboard } from '@/api/eval'
import { initChart, watchResize, lineOrBar } from '@/utils/echarts'

const kpis = ref(defaultKpis())
const chartOccupancy = ref(null)
const chartCollect = ref(null)
const chartRepair = ref(null)
const chartViolation = ref(null)
const chartTop = ref(null)
let charts = []
let cleanups = []

function defaultKpis() {
  return [
    { label: '入住率', value: '-', color: '#409eff' },
    { label: '收缴率', value: '-', color: '#67c23a' },
    { label: '报修平均时效', value: '-', color: '#e6a23c' },
    { label: '本月违纪', value: '-', color: '#f56c6c' },
    { label: '卫生平均分', value: '-', color: '#9b59b6' }
  ]
}

function chartOption(title) {
  return {
    title: { text: title, left: 'center', textStyle: { fontSize: 14 } },
    tooltip: { trigger: 'axis' },
    grid: { left: 40, right: 20, top: 40, bottom: 30 }
  }
}

async function load() {
  try {
    const d = await globalDashboard({})
    if (!d) return
    kpis.value = [
      { label: '入住率', value: d.occupancyRate != null ? (Number(d.occupancyRate) * 100).toFixed(1) + '%' : '-', color: '#409eff' },
      { label: '收缴率', value: d.collectionRate != null ? (Number(d.collectionRate) * 100).toFixed(1) + '%' : '-', color: '#67c23a' },
      { label: '报修平均时效', value: d.avgRepairHours != null ? d.avgRepairHours + 'h' : '-', color: '#e6a23c' },
      { label: '本月违纪', value: d.violationCount ?? '-', color: '#f56c6c' },
      { label: '卫生平均分', value: d.avgHygieneScore ?? '-', color: '#9b59b6' }
    ]

    // 入住率（按楼栋）
    const occ = d.occupancyByBuilding || []
    charts.push(initChart(chartOccupancy.value, {
      ...chartOption('各楼栋入住率'),
      tooltip: { trigger: 'axis' },
      legend: { bottom: 0 },
      xAxis: { type: 'category', data: occ.map((o) => o.name || o.buildingName || '') },
      yAxis: { type: 'value', max: 100 },
      series: [{ name: '入住率%', type: 'bar', data: occ.map((o) => Number(o.rate ?? o.occupancyRate ?? 0)) }]
    }))

    // 收缴率趋势
    const coll = d.collectTrend || []
    charts.push(initChart(chartCollect.value, lineOrBar({
      title: '收缴率趋势',
      x: coll.map((c) => c.period || c.month || ''),
      series: [{ name: '收缴率%', data: coll.map((c) => Number(c.rate ?? c.value ?? 0)) }]
    })))

    // 报修时效
    const rep = d.repairByStatus || []
    charts.push(initChart(chartRepair.value, {
      ...chartOption('报修状态分布'),
      tooltip: { trigger: 'item' },
      legend: { bottom: 0 },
      series: [{
        name: '工单', type: 'pie', radius: ['40%', '70%'],
        data: rep.map((r) => ({ name: r.status || r.label, value: r.count ?? r.value ?? 0 }))
      }]
    }))

    // 违纪趋势
    const vio = d.violationTrend || []
    charts.push(initChart(chartViolation.value, lineOrBar({
      title: '违纪趋势（近 6 月）',
      x: vio.map((v) => v.period || v.month || ''),
      series: [{ name: '违纪次数', data: vio.map((v) => v.count || v.value || 0) }]
    })))

    // 卫生 Top
    const top = d.topRooms || []
    charts.push(initChart(chartTop.value, {
      ...chartOption('卫生 Top 宿舍'),
      tooltip: { trigger: 'axis' },
      grid: { left: 60, right: 20, top: 40, bottom: 30 },
      xAxis: { type: 'value' },
      yAxis: { type: 'category', data: top.map((t) => t.roomNo || t.name || '') },
      series: [{ name: '卫生分', type: 'bar', data: top.map((t) => t.score ?? t.hygieneScore ?? 0) }]
    }))

    cleanups = charts.map((c, i) => watchResize([chartOccupancy, chartCollect, chartRepair, chartViolation, chartTop][i].value, c))
  } catch (e) {
    // 后端未就绪：画占位图
    ;[chartOccupancy, chartCollect, chartRepair, chartViolation, chartTop].forEach((el, i) => {
      charts.push(initChart(el.value, chartOption(['各楼栋入住率', '收缴率趋势', '报修状态分布', '违纪趋势', '卫生 Top 宿舍'][i] + '（暂无数据）')))
    })
  }
}

onMounted(load)
onBeforeUnmount(() => {
  cleanups.forEach((fn) => fn?.())
  charts.forEach((c) => c?.dispose())
})
</script>

<style scoped>
.page { padding: 4px; }
.page-head h2 { margin: 0 0 12px; font-size: 18px; color: #303133; }
.kpi { text-align: center; }
.kpi-value { font-size: 24px; font-weight: 700; }
.kpi-label { color: #909399; font-size: 12px; margin-top: 4px; }
.chart { width: 100%; height: 280px; }
</style>
