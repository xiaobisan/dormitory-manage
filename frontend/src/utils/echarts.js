import * as echarts from 'echarts'

/**
 * ECharts 初始化助手。
 * @param {HTMLElement} el 容器元素
 * @param {object} option 图表配置
 * @returns {echarts.ECharts} 实例（需手动 dispose / resize）
 */
export function initChart(el, option) {
  if (!el) return null
  const chart = echarts.init(el)
  chart.setOption(option)
  return chart
}

/** 监听容器尺寸变化（在组件 onMounted 时调用） */
export function watchResize(el, chart) {
  if (!el || !chart) return
  const onResize = () => chart.resize()
  window.addEventListener('resize', onResize)
  return () => window.removeEventListener('resize', onResize)
}

/** 通用柱状/折线 option 工厂 */
export function lineOrBar({ title = '', x = [], series = [], type = 'line' }) {
  return {
    title: { text: title, left: 'center', textStyle: { fontSize: 14 } },
    tooltip: { trigger: 'axis' },
    grid: { left: 40, right: 20, top: 40, bottom: 30 },
    xAxis: { type: 'category', data: x },
    yAxis: { type: 'value' },
    series: series.map((s) => ({ name: s.name, type, data: s.data, smooth: true }))
  }
}

/** 饼图 option 工厂 */
export function pieOption({ title = '', data = [] }) {
  return {
    title: { text: title, left: 'center', textStyle: { fontSize: 14 } },
    tooltip: { trigger: 'item' },
    legend: { bottom: 0 },
    series: [
      {
        name: title,
        type: 'pie',
        radius: ['40%', '70%'],
        data,
        label: { formatter: '{b}: {c}' }
      }
    ]
  }
}
