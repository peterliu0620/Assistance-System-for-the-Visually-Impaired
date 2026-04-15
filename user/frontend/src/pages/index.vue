<template>
  <view class="page">
    <view class="panel">
      <text class="eyebrow">H5 Smoke Base</text>
      <text class="title" data-testid="app-title">user/frontend 最小测试基座</text>
      <text class="desc">
        这个页面只保留 H5 启动、最小交互和 Playwright 可断言标记，后续业务页可以在这个基座上继续挂接。
      </text>

      <view class="status-row">
        <view class="status-card">
          <text class="label">运行模式</text>
          <text class="value" data-testid="runtime-label">uni-app H5</text>
        </view>
        <view class="status-card">
          <text class="label">点击次数</text>
          <text class="value" data-testid="counter-value">{{ clicks }}</text>
        </view>
      </view>

      <button class="action" data-testid="increment-button" @click="increment">
        验证交互
      </button>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      clicks: 0
    }
  },
  methods: {
    increment() {
      this.clicks += 1
    }
  }
}
</script>

<style lang="scss">
.page {
  min-height: 100vh;
  padding: 32rpx;
}

.panel {
  max-width: 960rpx;
  margin: 0 auto;
  padding: 40rpx 36rpx;
  border-radius: 32rpx;
  background: rgba(9, 17, 26, 0.82);
  border: 1px solid rgba(125, 211, 252, 0.18);
  box-shadow: 0 20rpx 60rpx rgba(0, 0, 0, 0.28);
}

.eyebrow,
.label {
  display: block;
  color: #9fb3c8;
  font-size: 24rpx;
}

.eyebrow {
  letter-spacing: 4rpx;
  text-transform: uppercase;
}

.title {
  display: block;
  margin-top: 18rpx;
  color: #f8fafc;
  font-size: 54rpx;
  line-height: 1.2;
  font-weight: bold;
}

.desc {
  display: block;
  margin-top: 18rpx;
  color: #c8d3df;
  font-size: 28rpx;
  line-height: 1.7;
}

.status-row {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16rpx;
  margin-top: 28rpx;
}

.status-card {
  padding: 24rpx;
  border-radius: 24rpx;
  background: rgba(255, 255, 255, 0.04);
  border: 1px solid rgba(255, 255, 255, 0.08);
}

.value {
  display: block;
  margin-top: 8rpx;
  color: #f8fafc;
  font-size: 34rpx;
  font-weight: bold;
}

.action {
  margin-top: 28rpx;
  height: 88rpx;
  line-height: 88rpx;
  border-radius: 999rpx;
  background: linear-gradient(135deg, #7dd3fc, #fde68a);
  color: #08111a;
  font-size: 28rpx;
  font-weight: bold;
}

@media screen and (max-width: 720px) {
  .status-row {
    grid-template-columns: 1fr;
  }

  .title {
    font-size: 44rpx;
  }
}
</style>
