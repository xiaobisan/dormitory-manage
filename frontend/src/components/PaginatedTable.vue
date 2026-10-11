<template>
  <div>
    <slot name="search" />
    <el-table :data="data" v-loading="loading" :border="border">
      <slot />
      <template #empty>
        <el-empty :description="emptyText" :image-size="70" />
      </template>
    </el-table>
    <el-pagination
      v-if="showPager"
      style="margin-top: 12px; justify-content: flex-end"
      v-model:current-page="page.current"
      :page-size="page.size"
      :total="total"
      layout="total, prev, pager, next, sizes"
      @current-change="onPage"
      @size-change="onSize"
    />
  </div>
</template>

<script setup>
/**
 * 通用分页表格容器。
 * 用法：把表格列放在默认 slot；父组件负责 fetch 后填充 data。
 */
defineProps({
  data: { type: Array, required: true },
  loading: { type: Boolean, default: false },
  total: { type: Number, default: 0 },
  border: { type: Boolean, default: true },
  showPager: { type: Boolean, default: true },
  emptyText: { type: String, default: '暂无数据' }
})
const page = defineModel({ name: 'page', default: () => ({ current: 1, size: 10 }) })
const emit = defineEmits(['page-change'])
function onPage(c) { emit('page-change', c) }
function onPage2(c) { emit('page-change', c) }
function onSize(s) { page.value.size = s; page.value.current = 1; emit('page-change', 1, s) }
</script>
