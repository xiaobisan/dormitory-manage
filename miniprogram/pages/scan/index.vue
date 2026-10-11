<template>
  <view class="scan">
    <view class="title">扫码卫生检查</view>
    <button class="btn primary" @click="scan">扫描宿舍门牌二维码</button>
    <view v-if="roomId" class="card">
      <view>宿舍：{{ roomNo }}（{{ roomId }}）</view>
      <view>周期：{{ period }}</view>
      <template v-if="items.length">
        <view v-for="it in items" :key="it.key" class="row">
          <text>{{ it.name }}</text>
          <input v-model.number="scores[it.key]" type="digit" :placeholder="`0-${it.max}`" />
        </view>
      </template>
      <button class="btn" @click="submit" :disabled="!items.length">提交检查（得分 {{ total }} 分）</button>
    </view>
    <button class="btn ghost" @click="pickRoom">手动选宿舍（调试用）</button>
  </view>
</template>

<script>
import { getRoomByNo, getPlanList, submitCheck } from '@/api/index.js';

export default {
  data() {
    return {
      roomId: null,
      roomNo: '',
      period: new Date().toISOString().slice(0, 7),
      items: [],
      scores: {},
      total: 0
    };
  },
  methods: {
    async scan() {
      const res = await uni.scanCode({ onlyFromCamera: false });
      // 二维码内容约定为 roomId；解析后加载该宿舍检查项
      this.load(String(res.result));
    },
    async pickRoom() {
      const roomId = this.roomId || 2001; // 调试：默认演示宿舍
      this.load(String(roomId));
    },
    async load(rid) {
      this.roomId = Number(rid);
      try {
        const room = await getRoomByNo(this.roomId);
        this.roomNo = room ? room.roomNo : rid;
      } catch (e) {
        this.roomNo = rid;
      }
      const plans = await getPlanList();
      const p = plans && plans.length ? plans[0] : null;
      this.items = p ? JSON.parse(p.itemTemplate) : [];
      this.scores = {};
      this.items.forEach((it) => (this.scores[it.key] = it.max));
      this.total = this.items.reduce((s, it) => s + Number(this.scores[it.key] || 0), 0);
    },
    watch: {
      scores: { deep: true, handler() { this.total = this.items.reduce((s, it) => s + Number(this.scores[it.key] || 0), 0); } }
    },
    async submit() {
      await submitCheck({
        planId: this.items.length ? this.items[0].planId : undefined,
        roomId: this.roomId,
        period: this.period,
        itemScores: this.scores
      });
      uni.showToast({ title: '提交成功' });
      this.load(this.roomId);
    }
  }
};
</script>

<style>
.scan { padding: 24rpx; }
.title { font-size: 36rpx; font-weight: 600; margin: 20rpx 0; }
.btn { margin: 20rpx 0; }
.card { background: #f7f8fa; border-radius: 16rpx; padding: 24rpx; margin-top: 20rpx; }
.row { display: flex; justify-content: space-between; align-items: center; padding: 16rpx 0; border-bottom: 1rpx solid #eee; }
</style>
