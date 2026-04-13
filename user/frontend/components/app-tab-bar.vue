<template>
	<view class="app-tab-shell">
		<view class="app-tab-safe"></view>
		<view class="app-tab-bar">
			<view
				v-for="item in items"
				:key="item.key"
				:class="['app-tab-item', current === item.key ? 'app-tab-item-active' : '']"
				@click="switchTo(item)"
			>
				<text class="app-tab-icon">{{ item.icon }}</text>
				<text class="app-tab-text">{{ item.label }}</text>
			</view>
		</view>
	</view>
</template>

<script>
	export default {
		props: {
			current: {
				type: String,
				default: 'home'
			}
		},
		data() {
			return {
				items: [
					{ key: 'home', label: '识别', icon: '◎', url: '/pages/index/index' },
					{ key: 'knowledge', label: '知识库', icon: '◌', url: '/pages/knowledge/knowledge' },
					{ key: 'profile', label: '我的', icon: '◐', url: '/pages/user-center/user-center' }
				]
			}
		},
		methods: {
			switchTo(item) {
				if (!item || item.key === this.current) {
					return
				}
				uni.redirectTo({
					url: item.url,
					fail: () => {
						uni.reLaunch({
							url: item.url
						})
					}
				})
			}
		}
	}
</script>

<style>
	.app-tab-shell {
		position: relative;
		z-index: 20;
	}

	.app-tab-safe {
		height: 156rpx;
	}

	.app-tab-bar {
		position: fixed;
		left: 24rpx;
		right: 24rpx;
		bottom: calc(env(safe-area-inset-bottom) + 18rpx);
		display: flex;
		align-items: center;
		gap: 12rpx;
		padding: 14rpx;
		border-radius: 30rpx;
		background: rgba(8, 14, 24, 0.9);
		border: 1px solid rgba(255, 255, 255, 0.08);
		box-shadow: 0 24rpx 60rpx rgba(0, 0, 0, 0.34);
		backdrop-filter: blur(18rpx);
	}

	.app-tab-item {
		flex: 1;
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		gap: 6rpx;
		min-height: 88rpx;
		border-radius: 22rpx;
		color: rgba(244, 248, 255, 0.7);
	}

	.app-tab-item-active {
		background: linear-gradient(135deg, rgba(255, 212, 107, 0.18), rgba(135, 215, 255, 0.12));
		color: #fff5d1;
	}

	.app-tab-icon {
		font-size: 28rpx;
		line-height: 1;
	}

	.app-tab-text {
		font-size: 22rpx;
		line-height: 1.2;
		font-weight: bold;
	}
</style>
