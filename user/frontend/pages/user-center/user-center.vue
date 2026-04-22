<template>
	<view :class="['page', themeClass, largeFontClass]">
		<view class="page-glow glow-one"></view>
		<view class="page-glow glow-two"></view>
		<view class="page-grid"></view>

		<view class="hero-shell">
			<view class="hero-copy">
				<text class="eyebrow">Accessibility</text>
				<text class="hero-title">在一个页面里完成账号确认与无障碍偏好调整</text>
				<text class="hero-desc">支持语音速率、播报粒度、音色、手势操作、振动强度，以及大字体和高对比度显示设置，方便视障和低视力用户按自己的习惯使用。</text>

				<view class="hero-actions">
					<button class="action-btn primary" @click="save">保存设置</button>
					<button class="action-btn ghost" @click="backToHome">返回识别页</button>
					<button class="action-btn danger" @click="logout">退出登录</button>
				</view>
			</view>

			<view class="hero-stats">
				<view class="stat-card stat-card-strong">
					<text class="stat-label">昵称</text>
					<text class="stat-value">{{ user.nickname || '未设置' }}</text>
				</view>
				<view class="stat-card">
					<text class="stat-label">用户名</text>
					<text class="stat-value stat-value-small">{{ user.username || '-' }}</text>
				</view>
				<view class="stat-card">
					<text class="stat-label">身份</text>
					<text class="stat-value">{{ roleLabel }}</text>
				</view>
				<view class="stat-card">
					<text class="stat-label">当前模式</text>
					<text class="stat-value stat-value-small">{{ contrastLabel }} / {{ form.extraLargeText ? '大字体' : '标准字' }}</text>
				</view>
			</view>
		</view>

		<view class="section-panel">
			<view class="section-head">
				<view>
					<text class="section-kicker">语音播报</text>
					<text class="section-title">语音速率、粒度与音色</text>
				</view>
				<text class="section-tag">TTS 偏好</text>
			</view>

			<view class="control-grid">
				<view class="control-panel">
					<text class="label">语音速率：{{ rateLabel }}</text>
					<slider
						:value="rateSliderValue"
						:min="50"
						:max="200"
						:step="10"
						activeColor="#0f6cbd"
						backgroundColor="rgba(191, 219, 254, 0.7)"
						@change="onRateChange"
					/>
				</view>

				<view class="control-panel">
					<text class="label">播报粒度</text>
					<picker :range="granularityOptions" range-key="label" :value="granularityIndex" @change="onGranularityChange">
						<view class="picker">{{ granularityOptions[granularityIndex].label }}</view>
					</picker>
				</view>

				<view class="control-panel">
					<text class="label">音色</text>
					<picker :range="voiceOptions" range-key="label" :value="voiceIndex" @change="onVoiceChange">
						<view class="picker">{{ voiceOptions[voiceIndex].label }}</view>
					</picker>
				</view>
			</view>
		</view>

		<view class="section-panel">
			<view class="section-head">
				<view>
					<text class="section-kicker">交互方式</text>
					<text class="section-title">手势操作与振动提醒</text>
				</view>
				<text class="section-tag">快捷触发</text>
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
					<text class="label">振动提醒强度：{{ hapticLabel }}</text>
					<slider
						:value="form.hapticLevel"
						:min="1"
						:max="3"
						:step="1"
						activeColor="#0f6cbd"
						backgroundColor="rgba(191, 219, 254, 0.7)"
						@change="onHapticChange"
					/>
					<button class="mini-btn primary-mini" @click="testHaptic">测试振动</button>
				</view>
			</view>
		</view>

		<view class="section-panel">
			<view class="section-head">
				<view>
					<text class="section-kicker">低视力适配</text>
					<text class="section-title">大字体与高对比显示</text>
				</view>
				<text class="section-tag">阅读优先</text>
			</view>

			<view class="control-grid">
				<view class="control-panel">
					<text class="label">高对比度模式</text>
					<picker :range="contrastOptions" range-key="label" :value="contrastIndex" @change="onContrastChange">
						<view class="picker">{{ contrastOptions[contrastIndex].label }}</view>
					</picker>
				</view>

				<view class="control-panel switch-panel">
					<view>
						<text class="label">大字体显示</text>
						<text class="helper-text">打开后，本页与识别页会使用更大的标题和文本层级。</text>
					</view>
					<switch :checked="form.extraLargeText" @change="onLargeTextChange" color="#0f6cbd" />
				</view>
			</view>
		</view>

		<view class="section-panel shortcut-panel">
			<view class="action-row">
				<button class="action-btn primary" @click="save">保存设置</button>
				<button class="action-btn ghost" @click="resetSettings">恢复默认</button>
			</view>
		</view>

		<app-tab-bar current="profile" />
	</view>
</template>

<script>
	import { defineComponent } from 'vue';
	import AppTabBar from '../../components/app-tab-bar.vue';
	import { clearAuthUser, getAuthUser, getRoleLabel, isFamilyRole } from '../../utils/auth';
	import { DEFAULT_USER_SETTINGS, loadUserSettings, saveUserSettings } from '../../utils/user-settings';

	export default defineComponent({
		components: {
			AppTabBar
		},
		data() {
			return {
				user: getAuthUser(),
				voiceOptions: [
					{ label: '柔和女声 Cherry', value: 'Cherry' },
					{ label: '沉稳男声 Ethan', value: 'Ethan' },
					{ label: '清晰女声 Serena', value: 'Serena' }
				],
				granularityOptions: [
					{ label: '简洁播报', value: 'concise' },
					{ label: '详细播报', value: 'detailed' }
				],
				gestureOptions: [
					{ label: '无动作', value: 'none' },
					{ label: '语音播报结果', value: 'voice_trigger' },
					{ label: '直接拍照识别', value: 'direct_scan' },
					{ label: '打开用户中心', value: 'open_user_center' }
				],
				contrastOptions: [
					{ label: '蓝白 Fluent', value: 'black-gold' },
					{ label: '暖黄高对比', value: 'black-yellow' }
				],
				form: loadUserSettings()
			};
		},
		computed: {
			roleLabel() {
				return getRoleLabel(this.user.role);
			},
			voiceIndex() {
				return Math.max(0, this.voiceOptions.findIndex(item => item.value === this.form.voiceTimbre));
			},
			granularityIndex() {
				return Math.max(0, this.granularityOptions.findIndex(item => item.value === this.form.broadcastGranularity));
			},
			gestureSingleIndex() {
				return Math.max(0, this.gestureOptions.findIndex(item => item.value === this.form.gestureSingleTap));
			},
			gestureDoubleIndex() {
				return Math.max(0, this.gestureOptions.findIndex(item => item.value === this.form.gestureDoubleTap));
			},
			gestureLongIndex() {
				return Math.max(0, this.gestureOptions.findIndex(item => item.value === this.form.gestureLongPress));
			},
			contrastIndex() {
				return Math.max(0, this.contrastOptions.findIndex(item => item.value === this.form.contrastMode));
			},
			contrastLabel() {
				return this.contrastOptions[this.contrastIndex].label;
			},
			themeClass() {
				return this.form.contrastMode === 'black-yellow' ? 'theme-yellow' : 'theme-gold';
			},
			largeFontClass() {
				return this.form.extraLargeText ? 'font-large' : '';
			},
			rateSliderValue() {
				return Math.round(Number(this.form.speechRate || 1) * 100);
			},
			rateLabel() {
				return `${Number(this.form.speechRate || 1).toFixed(1)}x`;
			},
			hapticLabel() {
				if (this.form.hapticLevel === 1) return '轻';
				if (this.form.hapticLevel === 2) return '中';
				return '强';
			}
		},
		onLoad() {
			const user = getAuthUser();
			if (!user || !user.id) {
				uni.reLaunch({ url: '/pages/auth/auth' });
				return;
			}
			if (isFamilyRole(user.role)) {
				uni.reLaunch({ url: '/pages/family-center/family-center' });
				return;
			}
			this.user = user;
			this.form = loadUserSettings();
		},
		methods: {
			persistSettings(showToast = false) {
				this.form = saveUserSettings(this.form);
				if (showToast) {
					uni.showToast({ title: '设置已保存', icon: 'success' });
				}
			},
			onRateChange(e) {
				this.form.speechRate = Number((Number(e.detail.value) / 100).toFixed(1));
				this.persistSettings();
			},
			onVoiceChange(e) {
				this.form.voiceTimbre = this.voiceOptions[e.detail.value].value;
				this.persistSettings();
			},
			onGranularityChange(e) {
				this.form.broadcastGranularity = this.granularityOptions[e.detail.value].value;
				this.persistSettings();
			},
			onGestureChange(field, e) {
				this.form[field] = this.gestureOptions[e.detail.value].value;
				this.persistSettings();
			},
			onHapticChange(e) {
				this.form.hapticLevel = Number(e.detail.value);
				this.persistSettings();
			},
			onContrastChange(e) {
				this.form.contrastMode = this.contrastOptions[e.detail.value].value;
				this.persistSettings();
			},
			onLargeTextChange(e) {
				this.form.extraLargeText = !!e.detail.value;
				this.persistSettings();
			},
			testHaptic() {
				if (this.form.hapticLevel === 1) {
					uni.vibrateShort();
					return;
				}
				if (this.form.hapticLevel === 2) {
					uni.vibrateShort();
					setTimeout(() => uni.vibrateShort(), 180);
					return;
				}
				uni.vibrateLong();
			},
			save() {
				this.persistSettings(true);
			},
			resetSettings() {
				this.form = { ...DEFAULT_USER_SETTINGS };
				this.persistSettings();
				uni.showToast({ title: '已恢复默认设置', icon: 'success' });
			},
			backToHome() {
				uni.redirectTo({
					url: '/pages/index/index'
				});
			},
			logout() {
				uni.showModal({
					title: '退出登录',
					content: '确认退出当前账号吗？',
					success: (res) => {
						if (!res.confirm) {
							return;
						}
						clearAuthUser();
						uni.reLaunch({
							url: '/pages/auth/auth'
						});
					}
				});
			}
		}
	});
</script>

<style>
	@import '../../styles/experience-shell.css';

	.label {
		display: block;
		font-size: 28rpx;
		line-height: 1.7;
		color: var(--text-soft);
	}

	.helper-text {
		display: block;
		margin-top: 8rpx;
		font-size: 22rpx;
		line-height: 1.6;
		color: var(--text-muted);
	}

	.control-grid {
		display: grid;
		grid-template-columns: repeat(2, minmax(0, 1fr));
		gap: 14rpx;
	}

	.control-panel {
		padding: 22rpx;
		border-radius: 24rpx;
		background: linear-gradient(180deg, rgba(255, 255, 255, 0.8), rgba(245, 249, 255, 0.72));
		border: 1px solid rgba(255, 255, 255, 0.84);
		box-shadow: 0 16rpx 40rpx rgba(79, 118, 172, 0.08);
	}

	.picker {
		margin-top: 10rpx;
		padding: 18rpx 20rpx;
		border-radius: 20rpx;
		background: rgba(255, 255, 255, 0.74);
		border: 1px solid rgba(255, 255, 255, 0.86);
		color: var(--text-main);
		font-size: 28rpx;
		box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.8);
	}

	.switch-panel {
		display: flex;
		align-items: center;
		justify-content: space-between;
		gap: 20rpx;
	}

	.action-btn.danger {
		background: rgba(227, 80, 80, 0.12);
		color: #b42318;
		border: 1px solid rgba(227, 80, 80, 0.22);
	}

	.mini-btn {
		margin-top: 18rpx;
	}

	.font-large .label,
	.font-large .picker,
	.font-large .stat-value {
		font-size: 34rpx;
	}

	.font-large .helper-text {
		font-size: 26rpx;
	}

	@media screen and (max-width: 720px) {
		.control-grid {
			grid-template-columns: 1fr;
		}

		.switch-panel {
			align-items: flex-start;
			flex-direction: column;
		}
	}
</style>
