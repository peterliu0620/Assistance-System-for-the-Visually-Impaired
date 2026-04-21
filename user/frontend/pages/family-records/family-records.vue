<template>
	<view :class="['page', themeClass, largeFontClass]">
		<view class="page-glow glow-one"></view>
		<view class="page-glow glow-two"></view>
		<view class="page-grid"></view>

		<view class="hero-shell">
			<view class="hero-copy">
				<text class="eyebrow">Family Records</text>
				<text class="hero-title">按家属视角回看最近识别记录与风险提醒</text>
				<text class="hero-desc">首版先展示识别时间、摘要、场景名、位置备注和风险提醒，方便你快速确认需要回访的时段。</text>

				<view class="hero-actions">
					<button class="action-btn primary" @click="refreshRecords">刷新记录</button>
					<button class="action-btn ghost" @click="goProfile">去信息登记</button>
				</view>
			</view>

			<view class="hero-stats">
				<view class="stat-card stat-card-strong">
					<text class="stat-label">记录总数</text>
					<text class="stat-value">{{ records.length }}</text>
				</view>
				<view class="stat-card">
					<text class="stat-label">风险提醒</text>
					<text class="stat-value">{{ riskCount }}</text>
				</view>
				<view class="stat-card">
					<text class="stat-label">最新场景</text>
					<text class="stat-value stat-value-small">{{ latestScene }}</text>
				</view>
				<view class="stat-card">
					<text class="stat-label">位置信息</text>
					<text class="stat-value stat-value-small">{{ latestLocation }}</text>
				</view>
			</view>
		</view>

		<view class="section-panel">
			<view class="section-head">
				<view>
					<text class="section-kicker">记录列表</text>
					<text class="section-title">最近识别</text>
				</view>
				<text class="section-tag">Mock 数据</text>
			</view>

			<view v-for="record in records" :key="record.id" class="record-card">
				<view class="record-head">
					<view>
						<text class="record-time">{{ record.time }}</text>
						<text class="record-summary">{{ record.summary }}</text>
					</view>
					<text :class="['risk-badge', record.riskAlert ? 'risk-badge-hot' : '']">
						{{ record.riskAlert ? '有风险提醒' : '无风险提醒' }}
					</text>
				</view>

				<view class="record-grid">
					<view class="record-meta">
						<text class="meta-label">场景名</text>
						<text class="meta-value">{{ record.scene }}</text>
					</view>
					<view class="record-meta">
						<text class="meta-label">缩略信息</text>
						<text class="meta-value">{{ record.imageLabel }}</text>
					</view>
					<view class="record-meta">
						<text class="meta-label">位置 / 备注</text>
						<text class="meta-value">{{ record.location }}</text>
					</view>
					<view class="record-meta">
						<text class="meta-label">补充说明</text>
						<text class="meta-value">{{ record.note }}</text>
					</view>
				</view>

				<view v-if="record.riskAlert" class="risk-panel">
					<text class="risk-title">风险提醒</text>
					<text class="risk-text">{{ record.riskText }}</text>
				</view>
			</view>
		</view>

		<app-tab-bar current="family-records" />
	</view>
</template>

<script>
	import { defineComponent } from 'vue';
	import AppTabBar from '../../components/app-tab-bar.vue';
	import { getAuthUser, isFamilyRole } from '../../utils/auth';
	import { loadFamilyRecords } from '../../utils/family-data';
	import { loadUserSettings } from '../../utils/user-settings';

	export default defineComponent({
		components: {
			AppTabBar
		},
		data() {
			return {
				settings: loadUserSettings(),
				records: loadFamilyRecords()
			};
		},
		computed: {
			themeClass() {
				return this.settings.contrastMode === 'black-yellow' ? 'theme-yellow' : 'theme-gold';
			},
			largeFontClass() {
				return this.settings.extraLargeText ? 'font-large' : '';
			},
			riskCount() {
				return this.records.filter(item => item.riskAlert).length;
			},
			latestScene() {
				return this.records[0] ? this.records[0].scene : '暂无';
			},
			latestLocation() {
				return this.records[0] ? this.records[0].location : '暂无';
			}
		},
		onLoad() {
			this.ensureFamilyRole();
		},
		onShow() {
			this.settings = loadUserSettings();
			this.refreshRecords();
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
			refreshRecords() {
				this.records = loadFamilyRecords();
			},
			goProfile() {
				uni.redirectTo({
					url: '/pages/family-profile/family-profile'
				});
			}
		}
	});
</script>

<style>
	@import '../../styles/experience-shell.css';

	.record-card,
	.record-meta,
	.risk-panel {
		border-radius: 24rpx;
	}

	.record-card {
		padding: 24rpx;
		background: rgba(255, 255, 255, 0.04);
		border: 1px solid rgba(255, 255, 255, 0.08);
	}

	.record-card + .record-card {
		margin-top: 16rpx;
	}

	.record-head {
		display: flex;
		align-items: flex-start;
		justify-content: space-between;
		gap: 16rpx;
	}

	.record-time,
	.meta-label,
	.risk-title {
		display: block;
		font-size: 22rpx;
		line-height: 1.6;
		color: var(--text-soft);
	}

	.record-summary,
	.meta-value {
		display: block;
		margin-top: 8rpx;
		font-size: 30rpx;
		line-height: 1.45;
		font-weight: bold;
		color: var(--text-main);
	}

	.risk-badge {
		padding: 10rpx 18rpx;
		border-radius: 999rpx;
		font-size: 22rpx;
		color: var(--text-main);
		background: rgba(255, 255, 255, 0.06);
		border: 1px solid rgba(255, 255, 255, 0.08);
	}

	.risk-badge-hot {
		color: #ffe2d5;
		background: rgba(255, 123, 96, 0.12);
		border-color: rgba(255, 123, 96, 0.24);
	}

	.record-grid {
		display: grid;
		grid-template-columns: repeat(2, minmax(0, 1fr));
		gap: 14rpx;
		margin-top: 18rpx;
	}

	.record-meta {
		padding: 20rpx;
		background: rgba(255, 255, 255, 0.03);
		border: 1px solid rgba(255, 255, 255, 0.08);
	}

	.risk-panel {
		margin-top: 16rpx;
		padding: 20rpx;
		background: rgba(255, 123, 96, 0.08);
		border: 1px solid rgba(255, 123, 96, 0.16);
	}

	.risk-text {
		display: block;
		margin-top: 8rpx;
		font-size: 28rpx;
		line-height: 1.7;
		color: #ffd7cc;
	}

	.font-large .record-summary,
	.font-large .meta-value,
	.font-large .risk-text {
		font-size: 34rpx;
	}

	@media screen and (max-width: 720px) {
		.record-head,
		.record-grid {
			grid-template-columns: 1fr;
		}

		.record-head {
			display: block;
		}

		.risk-badge {
			display: inline-block;
			margin-top: 12rpx;
		}
	}
</style>
