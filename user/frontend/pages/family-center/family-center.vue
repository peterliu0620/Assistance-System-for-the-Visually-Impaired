<template>
	<view :class="['page', themeClass, largeFontClass]">
		<view class="page-glow glow-one"></view>
		<view class="page-glow glow-two"></view>
		<view class="page-grid"></view>

		<view class="hero-shell">
			<view class="hero-copy">
				<text class="eyebrow">Family Desk</text>
				<text class="hero-title">从家属视角快速看懂识别动态与照护重点</text>
				<text class="hero-desc">支持在多位视障人士之间切换，聚合当前对象的最近识别记录、风险提醒和登记完成度。</text>

				<view class="hero-actions">
					<button class="action-btn primary" @click="goRecords">查看识别记录</button>
					<button class="action-btn ghost" @click="goProfile">完善信息登记</button>
					<button class="action-btn danger" @click="logout">退出登录</button>
				</view>
			</view>

			<view class="hero-stats">
				<view class="stat-card stat-card-strong">
					<text class="stat-label">今日识别</text>
					<text class="stat-value">{{ dashboard.todayCount || 0 }}</text>
				</view>
				<view class="stat-card">
					<text class="stat-label">风险提醒</text>
					<text class="stat-value">{{ dashboard.riskCount || 0 }}</text>
				</view>
				<view class="stat-card">
					<text class="stat-label">登记完成</text>
					<text class="stat-value">{{ dashboard.completedSections || 0 }}/3</text>
				</view>
				<view class="stat-card">
					<text class="stat-label">最近记录</text>
					<text class="stat-value">{{ dashboard.recordsCount || 0 }}</text>
				</view>
			</view>
		</view>

		<view class="section-panel" v-if="bindings.length">
			<view class="section-head">
				<view>
					<text class="section-kicker">关联对象</text>
					<text class="section-title">切换当前照护对象</text>
				</view>
				<text class="section-tag">已绑定 {{ bindings.length }} 位</text>
			</view>

			<picker class="binding-picker" :range="bindingLabels" :value="activeBindingIndex" @change="onBindingChange">
				<view class="picker-value">{{ activeBindingLabel }}</view>
			</picker>

			<view class="binding-list">
				<view
					v-for="binding in bindings"
					:key="binding.visionUserId"
					:class="['binding-card', binding.visionUserId === activeVisionUserId ? 'binding-card-active' : '']"
					@click="switchBinding(binding.visionUserId)"
				>
					<text class="binding-name">{{ binding.visionNickname || binding.visionUsername }}</text>
					<text class="binding-meta">{{ binding.visionUsername }} · {{ binding.relationship || '家人' }}</text>
				</view>
			</view>
		</view>

		<view class="section-panel focus-panel">
			<view class="section-head">
				<view>
					<text class="section-kicker">本次重点</text>
					<text class="section-title">最新照护提醒</text>
				</view>
				<text class="section-tag">优先处理</text>
			</view>

			<view class="alert-band">
				<text class="alert-title">{{ latestRiskTitle }}</text>
				<text class="alert-text">{{ dashboard.recentRiskText || '当前暂无新的风险提醒。' }}</text>
			</view>
		</view>

		<view class="section-panel">
			<view class="section-head">
				<view>
					<text class="section-kicker">当前对象</text>
					<text class="section-title">照护摘要</text>
				</view>
				<text class="section-tag">后端接口</text>
			</view>

			<view class="progress-grid">
				<view class="progress-item">
					<text class="progress-label">视障人士</text>
					<text class="progress-value">{{ dashboard.visionUserNickname || '尚未绑定' }}</text>
				</view>
				<view class="progress-item">
					<text class="progress-label">最近风险提醒</text>
					<text class="progress-value">{{ dashboard.riskCount || 0 }} 条</text>
				</view>
				<view class="progress-item">
					<text class="progress-label">信息登记完成度</text>
					<text class="progress-value">{{ dashboard.completedSections || 0 }}/3</text>
				</view>
			</view>
		</view>

		<view class="section-panel">
			<view class="section-head">
				<view>
					<text class="section-kicker">最近记录</text>
					<text class="section-title">识别摘要</text>
				</view>
				<text class="section-tag">最近 2 条</text>
			</view>

			<view v-if="!previewRecords.length" class="record-preview">
				<text class="preview-text">当前还没有可展示的识别记录。</text>
			</view>

			<view v-for="record in previewRecords" :key="record.id" class="record-preview">
				<view class="preview-head">
					<text class="preview-scene">{{ record.scene }}</text>
					<text class="preview-time">{{ record.time }}</text>
				</view>
				<text class="preview-text">{{ record.summary }}</text>
				<text class="preview-meta">{{ record.location }} · {{ record.imageLabel }}</text>
			</view>
		</view>

		<app-tab-bar current="family-home" />
	</view>
</template>

<script>
	import { defineComponent } from 'vue';
	import AppTabBar from '../../components/app-tab-bar.vue';
	import { API_BASE } from '../../utils/api';
	import { clearAuthUser, getAuthUser, isFamilyRole } from '../../utils/auth';
	import { clearActiveVisionUserId, getActiveVisionUserId, setActiveVisionUserId } from '../../utils/family-binding';
	import { loadUserSettings } from '../../utils/user-settings';

	export default defineComponent({
		components: {
			AppTabBar
		},
		data() {
			return {
				settings: loadUserSettings(),
				dashboard: {},
				previewRecords: [],
				bindings: [],
				activeVisionUserId: null
			};
		},
		computed: {
			themeClass() {
				return this.settings.contrastMode === 'black-yellow' ? 'theme-yellow' : 'theme-gold';
			},
			largeFontClass() {
				return this.settings.extraLargeText ? 'font-large' : '';
			},
			latestRiskTitle() {
				return this.dashboard.riskCount ? '最近出现新的风险提醒' : '当前识别记录整体平稳';
			},
			bindingLabels() {
				return this.bindings.map(item => `${item.visionNickname || item.visionUsername} · ${item.relationship || '家人'}`);
			},
			activeBindingIndex() {
				return Math.max(0, this.bindings.findIndex(item => item.visionUserId === this.activeVisionUserId));
			},
			activeBindingLabel() {
				const current = this.bindings.find(item => item.visionUserId === this.activeVisionUserId);
				return current ? `${current.visionNickname || current.visionUsername} · ${current.relationship || '家人'}` : '请选择照护对象';
			}
		},
		onLoad() {
			this.ensureFamilyRole();
		},
		onShow() {
			this.settings = loadUserSettings();
			this.loadBindings();
		},
		methods: {
			ensureFamilyRole() {
				const user = getAuthUser();
				if (!user || !user.id) {
					uni.reLaunch({ url: '/pages/auth/auth' });
					return false;
				}
				if (!isFamilyRole(user.role)) {
					uni.reLaunch({ url: '/pages/index/index' });
					return false;
				}
				return true;
			},
			loadBindings() {
				const user = getAuthUser();
				const storedVisionUserId = getActiveVisionUserId();
				const data = {
					familyUserId: user.id
				};
				if (storedVisionUserId) {
					data.visionUserId = storedVisionUserId;
				}
				uni.request({
					url: `${API_BASE}/api/family/profile`,
					method: 'GET',
					data,
					success: (res) => {
						if (res.statusCode !== 200) {
							return;
						}
						const response = res.data || {};
						this.bindings = Array.isArray(response.bindings) ? response.bindings : [];
						const currentBinding = response.bindingInfo || {};
						const firstBinding = this.bindings.length ? this.bindings[0] : null;
						this.activeVisionUserId = currentBinding.visionUserId || (firstBinding ? firstBinding.visionUserId : null);
						if (this.activeVisionUserId) {
							setActiveVisionUserId(this.activeVisionUserId);
						}
						this.loadDashboard();
						this.loadPreviewRecords();
					}
				});
			},
			loadDashboard() {
				const user = getAuthUser();
				const data = {
					familyUserId: user.id
				};
				if (this.activeVisionUserId) {
					data.visionUserId = this.activeVisionUserId;
				}
				uni.request({
					url: `${API_BASE}/api/family/dashboard`,
					method: 'GET',
					data,
					success: (res) => {
						if (res.statusCode === 200) {
							this.dashboard = res.data || {};
						}
					}
				});
			},
			loadPreviewRecords() {
				const user = getAuthUser();
				const data = {
					familyUserId: user.id,
					limit: 2
				};
				if (this.activeVisionUserId) {
					data.visionUserId = this.activeVisionUserId;
				}
				uni.request({
					url: `${API_BASE}/api/family/records`,
					method: 'GET',
					data,
					success: (res) => {
						if (res.statusCode === 200) {
							this.previewRecords = Array.isArray(res.data) ? res.data : [];
						}
					}
				});
			},
			onBindingChange(event) {
				const index = Number(event.detail.value || 0);
				const target = this.bindings[index];
				if (!target) {
					return;
				}
				this.switchBinding(target.visionUserId);
			},
			switchBinding(visionUserId) {
				if (!visionUserId || visionUserId === this.activeVisionUserId) {
					return;
				}
				this.activeVisionUserId = visionUserId;
				setActiveVisionUserId(visionUserId);
				this.loadDashboard();
				this.loadPreviewRecords();
			},
			goRecords() {
				uni.redirectTo({
					url: '/pages/family-records/family-records'
				});
			},
			goProfile() {
				uni.redirectTo({
					url: '/pages/family-profile/family-profile'
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
						clearActiveVisionUserId();
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

	.alert-band,
	.progress-item,
	.record-preview,
	.binding-picker,
	.binding-card {
		padding: 24rpx;
		border-radius: 24rpx;
		background: linear-gradient(180deg, rgba(255, 255, 255, 0.8), rgba(245, 249, 255, 0.72));
		border: 1px solid rgba(255, 255, 255, 0.84);
		box-shadow: 0 16rpx 40rpx rgba(79, 118, 172, 0.08);
	}

	.alert-band {
		background: linear-gradient(135deg, rgba(236, 244, 255, 0.96), rgba(255, 255, 255, 0.94));
		border-color: rgba(193, 220, 255, 0.92);
	}

	.binding-picker,
	.binding-card {
		padding: 22rpx;
	}

	.binding-list {
		display: grid;
		grid-template-columns: repeat(2, minmax(0, 1fr));
		gap: 14rpx;
		margin-top: 14rpx;
	}

	.binding-card-active {
		border-color: rgba(58, 109, 185, 0.38);
		box-shadow: 0 20rpx 44rpx rgba(58, 109, 185, 0.14);
	}

	.picker-value,
	.binding-name,
	.alert-title,
	.preview-scene {
		display: block;
		font-size: 34rpx;
		line-height: 1.25;
		font-weight: bold;
		color: var(--text-main);
	}

	.binding-meta,
	.alert-text,
	.preview-text,
	.preview-meta {
		display: block;
		font-size: 28rpx;
		line-height: 1.7;
		color: var(--text-soft);
	}

	.alert-text,
	.preview-text {
		margin-top: 12rpx;
	}

	.progress-grid {
		display: grid;
		grid-template-columns: repeat(3, minmax(0, 1fr));
		gap: 14rpx;
	}

	.progress-label,
	.preview-time {
		display: block;
		font-size: 22rpx;
		line-height: 1.6;
		color: var(--text-soft);
	}

	.progress-value {
		display: block;
		margin-top: 10rpx;
		font-size: 30rpx;
		line-height: 1.4;
		font-weight: bold;
		color: var(--text-main);
	}

	.record-preview + .record-preview {
		margin-top: 14rpx;
	}

	.action-btn.danger {
		background: rgba(227, 80, 80, 0.12);
		color: #b42318;
		border: 1px solid rgba(227, 80, 80, 0.22);
	}

	.preview-head {
		display: flex;
		align-items: center;
		justify-content: space-between;
		gap: 14rpx;
	}

	.preview-meta {
		margin-top: 10rpx;
	}

	@media screen and (max-width: 720px) {
		.progress-grid,
		.binding-list {
			grid-template-columns: 1fr;
		}

		.preview-head {
			flex-wrap: wrap;
			align-items: flex-start;
		}
	}
</style>
