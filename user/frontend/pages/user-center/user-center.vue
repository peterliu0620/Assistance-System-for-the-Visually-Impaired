<template>
	<view :class="['page', themeClass, largeFontClass]">
		<view class="card">
			<text class="title">个性化用户中心</text>

			<view class="section">
				<text class="section-title">语音自定义</text>
				<text class="label">语速：{{ form.speechRate.toFixed(2) }}x</text>
				<slider :value="form.speechRate * 100" :min="50" :max="300" :step="1" activeColor="#ffcf4b" @change="onRateChange" />

				<text class="label">音色</text>
				<picker :range="voiceOptions" range-key="label" :value="voiceIndex" @change="onVoiceChange">
					<view class="picker">{{ voiceOptions[voiceIndex].label }}</view>
				</picker>

				<text class="label">播报粒度</text>
				<picker :range="granularityOptions" range-key="label" :value="granularityIndex" @change="onGranularityChange">
					<view class="picker">{{ granularityOptions[granularityIndex].label }}</view>
				</picker>
			</view>

			<view class="section">
				<text class="section-title">多维交互</text>
				<text class="label">单击手势</text>
				<picker :range="gestureOptions" range-key="label" :value="gestureSingleIndex" @change="onGestureChange('gestureSingleTap', $event)">
					<view class="picker">{{ gestureOptions[gestureSingleIndex].label }}</view>
				</picker>

				<text class="label">双击手势</text>
				<picker :range="gestureOptions" range-key="label" :value="gestureDoubleIndex" @change="onGestureChange('gestureDoubleTap', $event)">
					<view class="picker">{{ gestureOptions[gestureDoubleIndex].label }}</view>
				</picker>

				<text class="label">长按手势</text>
				<picker :range="gestureOptions" range-key="label" :value="gestureLongIndex" @change="onGestureChange('gestureLongPress', $event)">
					<view class="picker">{{ gestureOptions[gestureLongIndex].label }}</view>
				</picker>

				<text class="label">振动强度：{{ hapticLabel }}</text>
				<slider :value="form.hapticLevel" :min="1" :max="3" :step="1" activeColor="#ffcf4b" @change="onHapticChange" />
				<button class="btn ghost" @click="testHaptic">测试振动</button>
			</view>

			<view class="section">
				<text class="section-title">低视力适配</text>
				<text class="label">高对比模式</text>
				<picker :range="contrastOptions" range-key="label" :value="contrastIndex" @change="onContrastChange">
					<view class="picker">{{ contrastOptions[contrastIndex].label }}</view>
				</picker>

				<view class="switch-row">
					<text class="label">超大字符渲染</text>
					<switch :checked="form.extraLargeText" @change="onLargeTextChange" color="#ffcf4b" />
				</view>
			</view>

			<button class="btn primary" @click="save">保存设置</button>
			<button class="btn" @click="backToHome">返回识别页</button>
		</view>
	</view>
</template>

<script>
	import { loadUserSettings, saveUserSettings } from '../../utils/user-settings.js'

	export default {
		data() {
			return {
				voiceOptions: [
					{ label: '柔和女声 Cherry', value: 'Cherry' },
					{ label: '沉稳男声 Ethan', value: 'Ethan' },
					{ label: '清晰女声 Serena', value: 'Serena' }
				],
				granularityOptions: [
					{ label: '简洁', value: 'concise' },
					{ label: '详细', value: 'detailed' }
				],
				gestureOptions: [
					{ label: '无动作', value: 'none' },
					{ label: '语音触发识别', value: 'voice_trigger' },
					{ label: '直接拍照识别', value: 'direct_scan' },
					{ label: '打开用户中心', value: 'open_user_center' }
				],
				contrastOptions: [
					{ label: '黑金模式', value: 'black-gold' },
					{ label: '黑黄模式', value: 'black-yellow' }
				],
				form: loadUserSettings()
			}
		},
		onLoad() {
			const user = uni.getStorageSync('auth_user')
			if (!user || !user.id) {
				uni.reLaunch({ url: '/pages/auth/auth' })
			}
		},
		computed: {
			voiceIndex() {
				return Math.max(0, this.voiceOptions.findIndex(item => item.value === this.form.voiceTimbre))
			},
			granularityIndex() {
				return Math.max(0, this.granularityOptions.findIndex(item => item.value === this.form.broadcastGranularity))
			},
			gestureSingleIndex() {
				return Math.max(0, this.gestureOptions.findIndex(item => item.value === this.form.gestureSingleTap))
			},
			gestureDoubleIndex() {
				return Math.max(0, this.gestureOptions.findIndex(item => item.value === this.form.gestureDoubleTap))
			},
			gestureLongIndex() {
				return Math.max(0, this.gestureOptions.findIndex(item => item.value === this.form.gestureLongPress))
			},
			contrastIndex() {
				return Math.max(0, this.contrastOptions.findIndex(item => item.value === this.form.contrastMode))
			},
			hapticLabel() {
				if (this.form.hapticLevel === 1) return '轻'
				if (this.form.hapticLevel === 2) return '中'
				return '强'
			},
			themeClass() {
				return this.form.contrastMode === 'black-yellow' ? 'theme-yellow' : 'theme-gold'
			},
			largeFontClass() {
				return this.form.extraLargeText ? 'font-large' : ''
			}
		},
		methods: {
			onRateChange(e) {
				this.form.speechRate = Number((e.detail.value / 100).toFixed(2))
			},
			onVoiceChange(e) {
				this.form.voiceTimbre = this.voiceOptions[e.detail.value].value
			},
			onGranularityChange(e) {
				this.form.broadcastGranularity = this.granularityOptions[e.detail.value].value
			},
			onGestureChange(field, e) {
				this.form[field] = this.gestureOptions[e.detail.value].value
			},
			onHapticChange(e) {
				this.form.hapticLevel = Number(e.detail.value)
			},
			onContrastChange(e) {
				this.form.contrastMode = this.contrastOptions[e.detail.value].value
			},
			onLargeTextChange(e) {
				this.form.extraLargeText = !!e.detail.value
			},
			testHaptic() {
				if (this.form.hapticLevel === 1) {
					uni.vibrateShort()
					return
				}
				if (this.form.hapticLevel === 2) {
					uni.vibrateShort()
					setTimeout(() => uni.vibrateShort(), 180)
					return
				}
				uni.vibrateLong()
			},
			save() {
				this.form = saveUserSettings(this.form)
				uni.showToast({ title: '设置已保存', icon: 'success' })
			},
			backToHome() {
				uni.navigateBack({
					fail: () => {
						uni.reLaunch({ url: '/pages/index/index' })
					}
				})
			}
		}
	}
</script>

<style>
	.page {
		min-height: 100vh;
		padding: 24rpx;
		background: radial-gradient(130% 100% at 15% 0%, #1a2f4a 0%, #090d14 64%);
	}

	.card {
		border-radius: 18rpx;
		padding: 24rpx;
		box-shadow: var(--shadow-lg);
		backdrop-filter: blur(10rpx);
	}

	.theme-gold .card {
		border: 2rpx solid rgba(255, 207, 75, 0.45);
		background: linear-gradient(160deg, rgba(22, 28, 36, 0.88), rgba(10, 14, 20, 0.9));
	}

	.theme-yellow .card {
		border: 2rpx solid rgba(255, 230, 0, 0.58);
		background: linear-gradient(160deg, rgba(22, 21, 11, 0.9), rgba(11, 11, 8, 0.92));
	}

	.title {
		display: block;
		font-size: 40rpx;
		font-weight: bold;
		color: #f3f5f8;
		margin-bottom: 16rpx;
	}

	.section {
		margin-bottom: 20rpx;
		padding: 16rpx;
		border-radius: 12rpx;
		background: rgba(255, 255, 255, 0.05);
		border: 1px solid rgba(110, 144, 185, 0.24);
	}

	.section-title {
		display: block;
		font-size: 30rpx;
		color: #ffcf4b;
		font-weight: bold;
		margin-bottom: 8rpx;
	}

	.label {
		display: block;
		font-size: 26rpx;
		color: #d8dde5;
		margin: 8rpx 0;
	}

	.picker {
		background: rgba(12, 17, 24, 0.92);
		border: 1px solid rgba(99, 133, 175, 0.38);
		border-radius: 10rpx;
		padding: 16rpx;
		color: #f0f5ff;
		font-size: 26rpx;
	}

	.switch-row {
		display: flex;
		align-items: center;
		justify-content: space-between;
	}

	.btn {
		margin-top: 12rpx;
		background: linear-gradient(145deg, #2a3d53, #1f2e40);
		color: #ffffff;
	}

	.btn.primary {
		background: #ffcf4b;
		color: #161a20;
		font-weight: bold;
	}

	.btn.ghost {
		background: #1e2630;
	}

	.font-large .title {
		font-size: 48rpx;
	}

	.font-large .section-title {
		font-size: 36rpx;
	}

	.font-large .label,
	.font-large .picker {
		font-size: 32rpx;
	}
</style>
