<template>
  <div class="page">
    <div class="page-head">
      <h2>🗺️ 床位地图</h2>
      <div>
        <el-select v-model="buildingId" placeholder="选择楼栋" style="width: 180px" @change="load">
          <el-option v-for="b in buildings" :key="b.id" :label="b.name" :value="b.id" />
        </el-select>
      </div>
    </div>

    <div class="legend">
      <span v-for="(v, k) in BED_STATUS" :key="k" class="legend-item">
        <i class="dot" :style="{ background: v.color }"></i>{{ v.label }}
      </span>
    </div>

    <el-card v-if="!buildingId" class="empty-card">
      <el-empty description="请先选择楼栋查看床位地图" />
    </el-card>

    <div v-loading="loading">
      <div v-for="floor in floorsOf(building)" :key="floor" class="floor-block">
        <div class="floor-title">{{ floor }} 层</div>
        <div class="rooms">
          <div v-for="room in roomsByFloor(floor)" :key="room.id" class="room-card">
            <div class="room-no">{{ room.roomNo }}</div>
            <div class="beds">
              <div
                v-for="bed in room.beds"
                :key="bed.id"
                class="bed"
                :style="bedStyle(bed)"
                :title="bed.studentName ? `${bed.studentName}（${bed.studentNo || ''}）` : mapOf(BED_STATUS, bed.status).label"
              >
                <div class="bed-no">{{ bed.bedNo }}</div>
                <div class="bed-name">{{ bed.studentName || mapOf(BED_STATUS, bed.status).label }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
      <el-empty v-if="!loading && rooms.length === 0" description="该楼栋暂无宿舍/床位数据" />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { pageBuildings, buildingBedMap } from '@/api/dorm'
import { mapOf, BED_STATUS } from '@/utils/format'

const buildings = ref([])
const buildingId = ref(null)
const building = ref(null)
const rooms = ref([])
const loading = ref(false)

const floorsOf = (b) => {
  const n = Number(b?.floorCount) || 4
  return Array.from({ length: n }, (_, i) => i + 1)
}

const roomsByFloor = (f) => rooms.value.filter((r) => r.floor === f)

function bedStyle(bed) {
  const c = mapOf(BED_STATUS, bed.status).color
  return { background: c + '22', borderColor: c }
}

async function load() {
  if (!buildingId.value) return
  loading.value = true
  try {
    const data = await buildingBedMap(buildingId.value)
    // 后端可能返回 { building, rooms: [{...beds}] } 或 rooms 数组
    rooms.value = data?.rooms || data || []
    building.value = data?.building || buildings.value.find((b) => b.id === buildingId.value)
  } catch (e) {
    rooms.value = []
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  try {
    const data = await pageBuildings({ current: 1, size: 100 })
    buildings.value = data?.records || []
  } catch (e) { }
})
</script>

<style scoped>
.page { padding: 4px; }
.page-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.page-head h2 { margin: 0; font-size: 18px; color: #303133; }
.legend { margin-bottom: 12px; }
.legend-item { display: inline-flex; align-items: center; margin-right: 18px; font-size: 13px; color: #606266; }
.dot { width: 12px; height: 12px; border-radius: 50%; display: inline-block; margin-right: 6px; }
.empty-card { margin-bottom: 12px; }
.floor-block { margin-bottom: 20px; }
.floor-title { font-weight: 600; color: #303133; margin-bottom: 10px; font-size: 14px; }
.rooms { display: flex; flex-wrap: wrap; gap: 12px; }
.room-card {
  background: #fff; border: 1px solid #e4e7ed; border-radius: 8px; padding: 10px 12px; min-width: 130px;
}
.room-no { font-weight: 600; color: #409eff; margin-bottom: 8px; font-size: 13px; }
.beds { display: grid; grid-template-columns: repeat(2, 1fr); gap: 6px; }
.bed {
  border: 2px solid; border-radius: 6px; padding: 5px 6px; min-width: 62px; font-size: 11px;
}
.bed-no { font-weight: 700; color: #303133; font-size: 12px; }
.bed-name { color: #606266; margin-top: 2px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
</style>
