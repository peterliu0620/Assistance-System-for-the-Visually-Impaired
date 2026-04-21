<template>
	<view :class="['page', themeClass, largeFontClass]">
		<view class="page-glow glow-one"></view>
		<view class="page-glow glow-two"></view>
		<view class="page-grid"></view>

		<view class="hero-shell">
			<view class="hero-copy">
				<text class="eyebrow">Family Desk</text>
				<text class="hero-title">从家属视角快速看懂识别动态与照护重点</text>
				<text class="hero-desc">家人端先聚合最近识别记录、风险提醒和登记信息完成度，首版全部基于本地 mock，方便先把照护流程跑起来。</text>

				<view class="hero-actions">
					<button class="action-btn primary" @click="goRecords">查看识别记录</button>
					<button class="action-btn ghost" @click="goProfile">完善信息登记</button>
					<button class="action-btn danger" @click="logout">退出登录</button>
				</view>
			</view>

			<view class="hero-stats">
				<view class="stat-card stat-card-strong">
					<text class="stat-label">今日识别</text>
					<text class="stat-value">{{ dashboard.todayCount }}</text>
				</view>
				<view class="stat-card">
					<text class="stat-label">风险提醒</text>
					<text class="stat-value">{{ dashboard.riskCount }}</text>
				</view>
				<view class="stat-card">
					<text class="stat-label">登记完成</text>
					<text class="stat-value">{{ dashboard.completedSections }}/3</text>
				</view>
				<view class="stat-card">
					<text class="stat-label">最近记录</text>
					<text class="stat-value">{{ dashboard.records.length }}</text>
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
				<text class="alert-text">{{ dashboard.recentRiskText }}</text>
			</view>
		</view>

		<view class="section-panel">
			<view class="section-head">
				<view>
					<text class="section-kicker">登记概览</text>
					<text class="section-title">照护资料状态</text>
				</view>
				<text class="section-tag">本地缓存</text>
			</view>

			<view class="progress-grid">
				<view class="progress-item">
					<text class="progress-label">视障人士基本信息</text>
					<text class="progress-value">{{ profile.basicInfo.name || '待填写' }}</text>
				</view>
				<view class="progress-item">
					<text class="progress-label">紧急联系人</text>
					<text class="progress-value">{{ profile.emergencyContact.name || '待填写' }}</text>
				</view>
				<view class="progress-item">
					<text class="progress-label">药品/疾病注意事项</text>
					<text class="progress-value">{{ healthSummary }}</text>
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
	import { clearAuthUser, getAuthUser, isFamilyRole } from '../../utils/auth';
	import { getFamilyDashboard } from '../../utils/family-data';
	import { loadUserSettings } from '../../utils/user-settings';

	export default defineComponent({
		components: {
			AppTabBar
		},
		data() {
			return {
				settings: loadUserSettings(),
				dashboard: getFamilyDashboard()
			};
		},
		computed: {
			themeClass() {
				return this.settings.contrastMode === 'black-yellow' ? 'theme-yellow' : 'theme-gold';
			},
			largeFontClass() {
				return this.settings.extraLargeText ? 'font-large' : '';
			},
			profile() {
				return this.dashboard.profile || {};
			},
			previewRecords() {
				return (this.dashboard.records || []).slice(0, 2);
			},
			latestRiskTitle() {
				return this.dashboard.riskCount ? '最近出现新的风险提醒' : '当前识别记录整体平稳';
			},
			healthSummary() {
				const health = this.profile.healthInfo || {};
				return health.medicine || health.diseaseNote || '待填写';
			}
		},
		onLoad() {
			this.ensureFamilyRole();
		},
		onShow() {
			this.settings = loadUserSettings();
			this.dashboard = getFamilyDashboard();
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
	.record-preview {
		padding: 24rpx;
		border-radius: 24rpx;
		background: rgba(255, 255, 255, 0.04);
		border: 1px solid rgba(255, 255, 255, 0.08);
	}

	.alert-band {
		background: linear-gradient(155deg, rgba(255, 212, 107, 0.16), rgba(135, 215, 255, 0.08));
		border-color: rgba(255, 212, 107, 0.2);
	}

	.alert-title,
	.preview-scene {
		display: block;
		font-size: 34rpx;
		line-height: 1.25;
		font-weight: bold;
		color: var(--text-main);
	}

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
		background: rgba(255, 107, 107, 0.14);
		color: #ffd7d7;
		border: 1px solid rgba(255, 107, 107, 0.28);
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

	.font-large .alert-text,
	.font-large .preview-text,
	.font-large .progress-value {
		font-size: 34rpx;
	}

	@media screen and (max-width: 720px) {
		.progress-grid {
			grid-template-columns: 1fr;
		}

		.preview-head {
			flex-wrap: wrap;
			align-items: flex-start;
		}
	}
</style>
