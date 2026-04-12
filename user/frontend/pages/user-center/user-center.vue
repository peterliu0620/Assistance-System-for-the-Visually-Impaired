<template>
	<view :class="['page', themeClass, largeFontClass]">
		<view class="page-glow glow-one"></view>
		<view class="page-glow glow-two"></view>
		<view class="page-grid"></view>

		<view class="hero-shell">
			<view class="hero-copy">
				<text class="eyebrow">Personal Center</text>
				<text class="hero-title">把语音、手势和视觉辅助调成最适合你的状态</text>
				<text class="hero-desc">这里集中管理播报方式、手势映射、振动强度和低视力适配，让首页反馈更贴合你的使用习惯。</text>

				<view class="hero-actions">
					<button class="action-btn primary" @click="save">保存设置</button>
					<button class="action-btn ghost" @click="backToHome">返回首页</button>
				</view>
			</view>

			<view class="hero-stats">
				<view class="stat-card stat-card-strong">
					<text class="stat-label">语速</text>
					<text class="stat-value">{{ form.speechRate.toFixed(2) }}x</text>
				</view>
				<view class="stat-card">
					<text class="stat-label">音色</text>
					<text class="stat-value stat-value-small">{{ voiceOptions[voiceIndex].label }}</text>
				</view>
				<view class="stat-card">
					<text class="stat-label">振动强度</text>
					<text class="stat-value">{{ hapticLabel }}</text>
				</view>
				<view class="stat-card">
					<text class="stat-label">对比模式</text>
					<text class="stat-value stat-value-small">{{ contrastOptions[contrastIndex].label }}</text>
				</view>
			</view>
		</view>

		<view class="section-panel">
			<view class="section-head">
				<view>
					<text class="section-kicker">播报偏好</text>
					<text class="section-title">语音自定义</text>
				</view>
				<text class="section-tag">实时生效</text>
			</view>

			<view class="control-panel">
				<text class="label">语速：{{ form.speechRate.toFixed(2) }}x</text>
				<slider :value="form.speechRate * 100" :min="50" :max="300" :step="1" activeColor="#ffd46b" @change="onRateChange" />
			</view>

			<view class="control-grid">
				<view class="control-panel">
					<text class="label">音色</text>
					<picker :range="voiceOptions" range-key="label" :value="voiceIndex" @change="onVoiceChange">
						<view class="picker">{{ voiceOptions[voiceIndex].label }}</view>
					</picker>
				</view>

				<view class="control-panel">
					<text class="label">播报粒度</text>
					<picker :range="granularityOptions" range-key="label" :value="granularityIndex" @change="onGranularityChange">
						<view class="picker">{{ granularityOptions[granularityIndex].label }}</view>
					</picker>
				</view>
			</view>
		</view>

		<view class="section-panel">
			<view class="section-head">
				<view>
					<text class="section-kicker">触发方式</text>
					<text class="section-title">多维交互</text>
				</view>
				<text class="section-tag">手势 + 振动</text>
			</view>

			<view class="control-grid">
				<view class="control-panel">
					<text class="label">单击手势</text>
					<picker :range="gestureOptions" range-key="label" :value="gestureSingleIndex" @change="onGestureChange('gestureSingleTap', $event)">
						<view class="picker">{{ gestureOptions[gestureSingleIndex].label }}</view>
					</picker>
				</view>

				<view class="control-panel">
					<text class="label">双击手势</text>
					<picker :range="gestureOptions" range-key="label" :value="gestureDoubleIndex" @change="onGestureChange('gestureDoubleTap', $event)">
						<view class="picker">{{ gestureOptions[gestureDoubleIndex].label }}</view>
					</picker>
				</view>

				<view class="control-panel">
					<text class="label">长按手势</text>
					<picker :range="gestureOptions" range-key="label" :value="gestureLongIndex" @change="onGestureChange('gestureLongPress', $event)">
						<view class="picker">{{ gestureOptions[gestureLongIndex].label }}</view>
					</picker>
				</view>

				<view class="control-panel">
					<text class="label">振动强度：{{ hapticLabel }}</text>
					<slider :value="form.hapticLevel" :min="1" :max="3" :step="1" activeColor="#ffd46b" @change="onHapticChange" />
					<button class="action-btn ghost compact-btn" @click="testHaptic">测试振动</button>
				</view>
			</view>
		</view>

		<view class="section-panel">
			<view class="section-head">
				<view>
					<text class="section-kicker">低视力支持</text>
					<text class="section-title">视觉适配</text>
				</view>
				<text class="section-tag">可读性优先</text>
			</view>

			<view class="control-grid">
				<view class="control-panel">
					<text class="label">高对比模式</text>
					<picker :range="contrastOptions" range-key="label" :value="contrastIndex" @change="onContrastChange">
						<view class="picker">{{ contrastOptions[contrastIndex].label }}</view>
					</picker>
				</view>

				<view class="control-panel switch-panel">
					<text class="label">超大字符渲染</text>
					<switch :checked="form.extraLargeText" @change="onLargeTextChange" color="#ffd46b" />
				</view>
			</view>
		</view>

		<view class="section-panel shortcut-panel">
			<view class="section-head">
				<view>
					<text class="section-kicker">快捷入口</text>
					<text class="section-title">继续浏览</text>
				</view>
			</view>

			<view class="action-row">
				<button class="action-btn primary" @click="save">保存设置</button>
				<button class="action-btn ghost" @click="goKnowledge">打开知识库</button>
				<button class="action-btn ghost" @click="backToHome">返回识别页</button>
			</view>
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
			goKnowledge() {
				uni.navigateTo({
					url: '/pages/knowledge/knowledge'
				})
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
	@import '../../styles/experience-shell.css';

	.label {
		display: block;
		font-size: 28rpx;
		line-height: 1.7;
		color: var(--text-soft);
	}

	.control-grid {
		display: grid;
		grid-template-columns: repeat(2, minmax(0, 1fr));
		gap: 14rpx;
	}

	.control-panel {
		padding: 22rpx;
		border-radius: 24rpx;
		background: rgba(255, 255, 255, 0.04);
		border: 1px solid rgba(255, 255, 255, 0.08);
	}

	.control-panel + .control-panel {
		margin-top: 0;
	}

	.picker {
		margin-top: 10rpx;
		padding: 18rpx 20rpx;
		border-radius: 20rpx;
		background: rgba(255, 255, 255, 0.05);
		border: 1px solid rgba(255, 255, 255, 0.08);
		color: var(--text-main);
		font-size: 28rpx;
	}

	.switch-panel {
		display: flex;
		align-items: center;
		justify-content: space-between;
	}

	.compact-btn {
		margin-top: 16rpx;
		min-width: 0;
	}

	.font-large .label,
	.font-large .picker,
	.font-large .stat-value {
		font-size: 34rpx;
	}

	@media screen and (max-width: 720px) {
		.control-grid {
			grid-template-columns: 1fr;
		}
	}
</style>
