<template>
  <div ref="box" :style="{ width: '100%', height: height }" />
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, watch } from 'vue'
import { initChart, watchResize } from '@/utils/echarts'

const props = defineProps({
  option: { type: Object, required: true },
  height: { type: String, default: '320px' }
})
const emit = defineEmits(['chart-ready'])

const box = ref(null)
let chart = null
let resizeFn = null

onMounted(() => {
  chart = initChart(box.value, props.option)
  emit('chart-ready', chart)
  resizeFn = watchResize(box.value, chart)
})

watch(
  () => props.option,
  (o) => {
    if (chart) chart.setOption(o, true)
  }
)

onBeforeUnmount(() => {
  if (resizeFn) resizeFn()
  if (chart) chart.dispose()
})
</script>
