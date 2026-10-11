<template>
  <div class="mobile">
    <div class="m-header">
      <div class="back" @click="$router.back()">← 返回</div>
      <h1>📷 扫码卫生检查</h1>
    </div>

    <div class="m-body">
      <!-- 扫码区 -->
      <div class="scan-box">
        <div class="scan-frame">
          <div class="scan-line" />
          <div class="scan-hint">将宿舍二维码放入框内</div>
        </div>
        <el-button @click="simulateScan" style="width: 100%; margin-top: 12px">模拟扫码（联调）</el-button>
        <el-input v-model="manualRoomNo" placeholder="或手动输入宿舍号，如 3-201" style="margin-top: 12px" />
        <el-button type="primary" @click="resolveRoom" style="width: 100%; margin-top: 8px">进入检查</el-button>
      </div>

      <!-- 检查表单 -->
      <template v-if="room">
        <div class="room-info">
          宿舍：<b>{{ room.roomNo }}</b>（{{ room.buildingName }} · {{ room.floor }} 层）
        </div>
        <div class="plan-row" v-if="planOptions.length">
          <el-select v-model="planId" size="small" style="width: 100%">
            <el-option v-for="p in planOptions" :key="p.id" :label="p.name" :value="p.id" />
          </el-select>
        </div>
        <div v-for="(item, idx) in items" :key="idx" class="item-row">
          <span class="item-name">{{ item.name }}</span>
          <div>
            <el-slider v-model="item.actual" :min="0" :max="item.max" :show-input="false" style="width: 160px" />
            <el-input-number v-model="item.actual" :min="0" :max="item.max" size="small" style="width: 80px" />
          </div>
        </div>
        <div class="total">总分：<b>{{ total }} / {{ max }}</b></div>
        <el-button type="primary" :loading="submitting" @click="submit" style="width: 100%">提交检查记录</el-button>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { pageCheckPlans, submitCheckRecord } from '@/api/maint'
import { pageRooms } from '@/api/dorm'

const manualRoomNo = ref('')
const room = ref(null)
const planId = ref(null)
const planOptions = ref([])
const items = ref([])
const submitting = ref(false)

const total = computed(() => items.value.reduce((s, i) => s + (Number(i.actual) || 0), 0))
const max = computed(() => items.value.reduce((s, i) => s + (Number(i.max) || 0), 0))

async function loadPlans() {
  try {
    const data = await pageCheckPlans({ current: 1, size: 50 })
    planOptions.value = (data?.records || data || []).filter((p) => p.active)
    if (planOptions.value.length) planId.value = planOptions.value[0].id
  } catch (e) { }
}

function applyTemplate(plan) {
  try {
    const arr = JSON.parse(plan?.itemTemplate || '[]')
    if (arr.length) {
      items.value = arr.map((i) => ({ name: i.name, max: i.score ?? i.max ?? 10, actual: i.score ?? i.max ?? 0 }))
      return
    }
  } catch (e) { }
  items.value = [
    { name: '地面清洁', max: 20, actual: 20 },
    { name: '物品摆放', max: 20, actual: 20 },
    { name: '卫生死角', max: 20, actual: 20 },
    { name: '内务整洁', max: 20, actual: 20 },
    { name: '其他', max: 20, actual: 20 }
  ]
}

// 根据宿舍号定位宿舍：解析 "3-201" -> 楼栋名/号 或 房间号
async function resolveRoom() {
  const key = (manualRoomNo.value || '').trim()
  if (!key) return ElMessage.warning('请先扫码或输入宿舍号')
  try {
    const data = await pageRooms({ current: 1, size: 200, roomNo: key.replace(/^[^-]+-/, '') })
    const rec = data?.records?.find((r) => String(r.roomNo).includes(key.replace(/^[^-]+-/, ''))) || data?.records?.[0]
    if (!rec) return ElMessage.warning('未找到该宿舍，请确认宿舍号')
    room.value = rec
    loadPlans().then(() => {
      const plan = planOptions.value.find((p) => p.id === planId.value)
      applyTemplate(plan)
    })
  } catch (e) {
    ElMessage.error('定位宿舍失败')
  }
}

// 模拟扫码（开发联调用：随机取一个宿舍）
async function simulateScan() {
  try {
    const data = await pageRooms({ current: 1, size: 1 })
    const rec = data?.records?.[0]
    if (!rec) return ElMessage.warning('暂无宿舍数据')
    room.value = rec
    if (!planOptions.value.length) await loadPlans()
    applyTemplate(planOptions.value.find((p) => p.id === planId.value))
    ElMessage.success(`已定位宿舍 ${rec.roomNo}`)
  } catch (e) { }
}

async function submit() {
  if (!planId.value) return ElMessage.warning('请选择检查计划')
  submitting.value = true
  try {
    await submitCheckRecord({
      planId: planId.value,
      roomId: room.value.id,
      score: total.value,
      detail: JSON.stringify(items.value)
    })
    ElMessage.success('检查记录已提交')
    items.value = []
    room.value = null
  } catch (e) { } finally {
    submitting.value = false
  }
}

onMounted(loadPlans)
</script>

<style scoped>
.mobile { max-width: 520px; margin: 0 auto; min-height: 100vh; background: #f5f7fa; }
.m-header {
  background: linear-gradient(135deg, #1e3c72, #2a5298); color: #fff;
  padding: 14px 16px; display: flex; align-items: center; gap: 12px;
}
.m-header .back { cursor: pointer; font-size: 14px; }
.m-header h1 { margin: 0; font-size: 16px; }
.m-body { padding: 16px; }
.scan-box { background: #fff; border-radius: 12px; padding: 20px; text-align: center; }
.scan-frame {
  width: 200px; height: 200px; margin: 0 auto; position: relative;
  border: 2px dashed #409eff; border-radius: 12px; overflow: hidden;
}
.scan-line {
  position: absolute; left: 8%; right: 8%; height: 2px; background: #409eff;
  animation: scan 2s linear infinite; box-shadow: 0 0 8px #409eff;
}
.scan-hint { position: absolute; bottom: 8px; left: 0; right: 0; font-size: 11px; color: #909399; }
@keyframes scan { 0% { top: 8%; } 50% { top: 90%; } 100% { top: 8%; } }
.room-info { background: #fff; border-radius: 10px; padding: 12px; margin-top: 16px; font-size: 14px; }
.plan-row { margin-top: 12px; }
.item-row {
  display: flex; justify-content: space-between; align-items: center;
  background: #fff; border-radius: 10px; padding: 12px; margin-top: 8px;
}
.item-name { font-size: 14px; color: #303133; }
.total { margin: 16px 0; text-align: center; font-size: 16px; }
.total b { color: #409eff; font-size: 22px; }
</style>
