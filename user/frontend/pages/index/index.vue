<template>
	<view :class="['page', themeClass, largeFontClass]">
		<view class="page-glow glow-one"></view>
		<view class="page-glow glow-two"></view>
		<view class="page-grid"></view>

		<view class="hero-shell">
			<view class="hero-copy">
				<text class="eyebrow">Vision Scan</text>
				<text class="hero-title">首版只保留单次智能视觉识别</text>
				<text class="hero-desc">拍一张图后，系统会返回场景、识别摘要和物体清单。知识问答、寻物和调试模块都已经移除。</text>

				<view class="hero-actions">
					<button class="action-btn primary" @click="pickImage" :disabled="loading">拍照识别</button>
				</view>
			</view>

			<view class="hero-stats">
				<view class="stat-card stat-card-strong">
					<text class="stat-label">当前状态</text>
					<text class="stat-value">{{ loading ? '识别中' : '待识别' }}</text>
				</view>
				<view class="stat-card">
					<text class="stat-label">最近场景</text>
					<text class="stat-value stat-value-small">{{ result ? result.scene : '暂无' }}</text>
				</view>
				<view class="stat-card">
					<text class="stat-label">识别物体</text>
					<text class="stat-value">{{ itemCount }}</text>
				</view>
				<view class="stat-card">
					<text class="stat-label">最近时间</text>
					<text class="stat-value stat-value-small">{{ capturedAtText }}</text>
				</view>
			</view>
		</view>

		<view v-if="imagePath" class="section-panel">
			<view class="section-head">
				<view>
					<text class="section-kicker">当前图片</text>
					<text class="section-title">待识别画面</text>
				</view>
			</view>
			<image class="preview-image" :src="imagePath" mode="aspectFill"></image>
		</view>

		<view v-if="result" class="section-panel">
			<view class="section-head">
				<view>
					<text class="section-kicker">识别结果</text>
					<text class="section-title">{{ result.scene || '未识别到明确场景' }}</text>
				</view>
				<text class="section-tag">最新一次</text>
			</view>

			<view class="result-highlight">
				<view class="highlight-head">
					<text class="result-title">语音摘要</text>
					<button class="replay-btn" @click="speakResult" :disabled="!speechText || ttsLoading">
						{{ audioPlaying ? '播放中' : (ttsLoading ? '生成中' : '重新播放') }}
					</button>
				</view>
				<text class="result-text">{{ result.narration || '本次没有返回语音摘要。' }}</text>
			</view>

			<view class="result-grid">
				<view class="result-meta">
					<text class="meta-label">触发指令</text>
					<text class="meta-value">{{ result.triggerCommand || '识别当前画面' }}</text>
				</view>
				<view class="result-meta">
					<text class="meta-label">位置摘要</text>
					<text class="meta-value">{{ result.positionSummary || '暂无' }}</text>
				</view>
			</view>

			<view class="item-list" v-if="result.items && result.items.length">
				<view v-for="(item, index) in result.items" :key="`${item.name}-${index}`" class="item-card">
					<text class="item-name">{{ item.name }}</text>
					<text class="item-meta">{{ item.position || '未知位置' }} / 置信度 {{ item.confidence || 0 }}%</text>
				</view>
			</view>
		</view>

		<view v-if="!result" class="section-panel empty-panel">
			<text class="empty-title">还没有识别结果</text>
			<text class="empty-text">点击上方按钮拍一张图，就可以开始首版识别流程。</text>
		</view>

		<app-tab-bar current="home" />
	</view>
</template>

<script>
	import { defineComponent } from 'vue';
	import AppTabBar from '../../components/app-tab-bar.vue';
	import { API_BASE } from '../../utils/api';
	import { getAuthUser, isVisionRole } from '../../utils/auth';
	import { loadUserSettings } from '../../utils/user-settings';

	export default defineComponent({
		components: {
			AppTabBar
		},
		data() {
			return {
				loading: false,
				result: null,
				imagePath: '',
				settings: loadUserSettings(),
				audioContext: null,
				audioPlaying: false,
				ttsLoading: false
			};
		},
		computed: {
			itemCount() {
				return this.result && this.result.items ? this.result.items.length : 0;
			},
			capturedAtText() {
				return this.result && this.result.capturedAt ? String(this.result.capturedAt).replace('T', ' ') : '暂无';
			},
			themeClass() {
				return this.settings.contrastMode === 'black-yellow' ? 'theme-yellow' : 'theme-gold';
			},
			largeFontClass() {
				return this.settings.extraLargeText ? 'font-large' : '';
			},
			speechText() {
				if (!this.result) {
					return '';
				}
				if (this.settings.broadcastGranularity === 'concise') {
					return this.result.scene || this.result.positionSummary || this.result.narration || '';
				}
				return this.result.narration || this.result.scene || this.result.positionSummary || '';
			}
		},
		onLoad() {
			this.ensureVisionUser();
			this.settings = loadUserSettings();
			this.initAudioContext();
		},
		onShow() {
			this.settings = loadUserSettings();
		},
		onUnload() {
			this.destroyAudioContext();
		},
		methods: {
			ensureVisionUser() {
				const user = getAuthUser();
				if (!user || !user.id) {
					uni.reLaunch({ url: '/pages/auth/auth' });
					return false;
				}
				if (!isVisionRole(user.role)) {
					uni.reLaunch({ url: '/pages/family-center/family-center' });
					return false;
				}
				return true;
			},
			pickImage() {
				if (!this.ensureVisionUser()) {
					return;
				}
				uni.chooseImage({
					count: 1,
					sizeType: ['compressed'],
					sourceType: ['camera'],
					success: (res) => {
						const filePath = res.tempFilePaths && res.tempFilePaths[0];
						if (!filePath) {
							return;
						}
						this.imagePath = filePath;
						this.uploadImage(filePath);
					}
				});
			},
			uploadImage(filePath) {
				const user = getAuthUser();
				this.loading = true;
				uni.uploadFile({
					url: `${API_BASE}/api/vision/analyze`,
					filePath,
					name: 'image',
					formData: {
						userId: String(user.id),
						command: '识别当前画面'
					},
					success: (res) => {
						this.loading = false;
						if (res.statusCode !== 200) {
							const data = this.parseJson(res.data);
							uni.showToast({ title: (data && data.message) || '识别失败', icon: 'none' });
							return;
						}
						this.result = this.parseJson(res.data) || null;
						this.speakResult();
					},
					fail: () => {
						this.loading = false;
						uni.showToast({ title: '识别请求失败', icon: 'none' });
					}
				});
			},
			initAudioContext() {
				if (typeof uni.createInnerAudioContext !== 'function') {
					return;
				}
				if (this.audioContext) {
					return;
				}
				const audioContext = uni.createInnerAudioContext();
				audioContext.autoplay = false;
				audioContext.onPlay(() => {
					this.audioPlaying = true;
				});
				audioContext.onStop(() => {
					this.audioPlaying = false;
				});
				audioContext.onEnded(() => {
					this.audioPlaying = false;
				});
				audioContext.onError(() => {
					this.audioPlaying = false;
					this.ttsLoading = false;
					uni.showToast({ title: '语音播放失败', icon: 'none' });
				});
				this.audioContext = audioContext;
			},
			speakResult() {
				const text = this.speechText;
				if (!text) {
					uni.showToast({ title: '暂无可播报内容', icon: 'none' });
					return;
				}
				this.initAudioContext();
				if (!this.audioContext) {
					uni.showToast({ title: '当前环境不支持音频播放', icon: 'none' });
					return;
				}
				this.stopAudio();
				this.ttsLoading = true;
				uni.request({
					url: `${API_BASE}/api/voice/tts`,
					method: 'POST',
					header: {
						'Content-Type': 'application/json'
					},
					data: {
						text,
						voice: this.settings.voiceTimbre || 'Cherry'
					},
					success: (res) => {
						this.ttsLoading = false;
						if (res.statusCode !== 200) {
							const data = res.data || {};
							uni.showToast({ title: data.message || '语音生成失败', icon: 'none' });
							return;
						}
						const audioUrl = res.data && res.data.audioUrl ? res.data.audioUrl : '';
						if (!audioUrl) {
							uni.showToast({ title: '未返回音频地址', icon: 'none' });
							return;
						}
						this.playAudio(audioUrl);
					},
					fail: () => {
						this.ttsLoading = false;
						uni.showToast({ title: '语音生成失败', icon: 'none' });
					}
				});
			},
			playAudio(audioUrl) {
				if (!this.audioContext) {
					return;
				}
				this.audioContext.src = audioUrl;
				this.audioContext.play();
			},
			stopAudio() {
				if (!this.audioContext) {
					return;
				}
				try {
					this.audioContext.stop();
				} catch (error) {}
				this.audioPlaying = false;
			},
			destroyAudioContext() {
				if (!this.audioContext) {
					return;
				}
				try {
					this.audioContext.stop();
					this.audioContext.destroy();
				} catch (error) {}
				this.audioContext = null;
				this.audioPlaying = false;
			},
			parseJson(value) {
				if (!value) {
					return null;
				}
				if (typeof value === 'object') {
					return value;
				}
				try {
					return JSON.parse(value);
				} catch (error) {
					return null;
				}
			}
		}
	});
</script>

<style>
	@import '../../styles/experience-shell.css';

	.preview-image {
		display: block;
		width: 100%;
		height: 420rpx;
		border-radius: 24rpx;
		margin-top: 18rpx;
	}

	.result-highlight,
	.result-meta,
	.item-card,
	.empty-panel {
		border-radius: 24rpx;
	}

	.highlight-head {
		display: flex;
		align-items: center;
		justify-content: space-between;
		gap: 14rpx;
	}

	.result-highlight {
		padding: 22rpx;
		background: linear-gradient(135deg, rgba(236, 244, 255, 0.96), rgba(255, 255, 255, 0.94));
		border: 1px solid rgba(193, 220, 255, 0.92);
		box-shadow: 0 18rpx 40rpx rgba(79, 118, 172, 0.08);
	}

	.result-title,
	.item-name,
	.empty-title {
		display: block;
		font-size: 32rpx;
		line-height: 1.3;
		font-weight: bold;
		color: var(--text-main);
	}

	.result-text,
	.item-meta,
	.empty-text {
		display: block;
		margin-top: 10rpx;
		font-size: 28rpx;
		line-height: 1.7;
		color: var(--text-soft);
	}

	.replay-btn {
		display: inline-flex;
		align-items: center;
		justify-content: center;
		height: 60rpx;
		padding: 0 20rpx;
		margin: 0;
		border-radius: 999rpx;
		background: rgba(255, 255, 255, 0.74);
		color: var(--accent-strong);
		border: 1px solid rgba(255, 255, 255, 0.86);
		font-size: 22rpx;
		font-weight: bold;
		box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.82);
	}

	.result-grid {
		display: grid;
		grid-template-columns: repeat(2, minmax(0, 1fr));
		gap: 14rpx;
		margin-top: 18rpx;
	}

	.result-meta {
		padding: 20rpx;
		background: linear-gradient(180deg, rgba(255, 255, 255, 0.8), rgba(245, 249, 255, 0.72));
		border: 1px solid rgba(255, 255, 255, 0.84);
		box-shadow: 0 16rpx 36rpx rgba(79, 118, 172, 0.08);
	}

	.meta-label {
		display: block;
		font-size: 22rpx;
		color: var(--text-soft);
	}

	.meta-value {
		display: block;
		margin-top: 8rpx;
		font-size: 28rpx;
		line-height: 1.5;
		font-weight: bold;
		color: var(--text-main);
	}

	.item-list {
		display: grid;
		gap: 14rpx;
		margin-top: 18rpx;
	}

	.item-card {
		padding: 20rpx;
		background: rgba(255, 255, 255, 0.72);
		border: 1px solid rgba(255, 255, 255, 0.84);
		box-shadow: 0 16rpx 36rpx rgba(79, 118, 172, 0.08);
	}

	.empty-title {
		font-size: 36rpx;
	}

	@media screen and (max-width: 720px) {
		.result-grid {
			grid-template-columns: 1fr;
		}

		.highlight-head {
			align-items: flex-start;
			flex-direction: column;
		}
	}
</style>
