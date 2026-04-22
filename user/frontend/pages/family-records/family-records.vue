<template>
	<view :class="['page', themeClass, largeFontClass]">
		<view class="page-glow glow-one"></view>
		<view class="page-glow glow-two"></view>
		<view class="page-grid"></view>

		<view class="hero-shell">
			<view class="hero-copy">
				<text class="eyebrow">Family Records</text>
				<text class="hero-title">按家属视角回看最近识别记录与风险提醒</text>
				<text class="hero-desc">可在多位视障人士之间切换，查看当前对象的识别时间、摘要、场景名、位置备注和风险提醒。</text>

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

		<view class="section-panel" v-if="bindings.length">
			<view class="section-head">
				<view>
					<text class="section-kicker">切换对象</text>
					<text class="section-title">当前查看记录的视障人士</text>
				</view>
				<text class="section-tag">已绑定 {{ bindings.length }} 位</text>
			</view>

			<picker class="binding-picker" :range="bindingLabels" :value="activeBindingIndex" @change="onBindingChange">
				<view class="picker-value">{{ activeBindingLabel }}</view>
			</picker>
		</view>

		<view class="section-panel">
			<view class="section-head">
				<view>
					<text class="section-kicker">记录列表</text>
					<text class="section-title">最近识别</text>
				</view>
				<text class="section-tag">后端接口</text>
			</view>

			<view v-if="!records.length" class="record-card">
				<text class="record-summary">当前还没有识别记录。</text>
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
	import { API_BASE } from '../../utils/api';
	import { getAuthUser, isFamilyRole } from '../../utils/auth';
	import { getActiveVisionUserId, setActiveVisionUserId } from '../../utils/family-binding';
	import { loadUserSettings } from '../../utils/user-settings';

	export default defineComponent({
		components: {
			AppTabBar
		},
		data() {
			return {
				settings: loadUserSettings(),
				records: [],
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
			riskCount() {
				return this.records.filter(item => item.riskAlert).length;
			},
			latestScene() {
				return this.records[0] ? this.records[0].scene : '暂无';
			},
			latestLocation() {
				return this.records[0] ? this.records[0].location : '暂无';
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
						this.refreshRecords();
					}
				});
			},
			refreshRecords() {
				const user = getAuthUser();
				const data = {
					familyUserId: user.id,
					limit: 10
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
							this.records = Array.isArray(res.data) ? res.data : [];
							return;
						}
						const data = res.data || {};
						uni.showToast({ title: data.message || '记录加载失败', icon: 'none' });
					},
					fail: () => {
						uni.showToast({ title: '记录加载失败', icon: 'none' });
					}
				});
			},
			onBindingChange(event) {
				const index = Number(event.detail.value || 0);
				const target = this.bindings[index];
				if (!target) {
					return;
				}
				this.activeVisionUserId = target.visionUserId;
				setActiveVisionUserId(target.visionUserId);
				this.refreshRecords();
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
	.risk-panel,
	.binding-picker {
		border-radius: 24rpx;
	}

	.binding-picker,
	.record-card {
		padding: 24rpx;
		background: linear-gradient(180deg, rgba(255, 255, 255, 0.8), rgba(245, 249, 255, 0.72));
		border: 1px solid rgba(255, 255, 255, 0.84);
		box-shadow: 0 16rpx 40rpx rgba(79, 118, 172, 0.08);
	}

	.picker-value {
		font-size: 32rpx;
		font-weight: bold;
		color: var(--text-main);
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
		color: var(--accent-strong);
		background: rgba(255, 255, 255, 0.66);
		border: 1px solid rgba(255, 255, 255, 0.86);
	}

	.risk-badge-hot {
		color: #b42318;
		background: rgba(227, 80, 80, 0.12);
		border-color: rgba(227, 80, 80, 0.22);
	}

	.record-grid {
		display: grid;
		grid-template-columns: repeat(2, minmax(0, 1fr));
		gap: 14rpx;
		margin-top: 18rpx;
	}

	.record-meta {
		padding: 20rpx;
		background: rgba(255, 255, 255, 0.72);
		border: 1px solid rgba(255, 255, 255, 0.84);
		box-shadow: 0 16rpx 36rpx rgba(79, 118, 172, 0.08);
	}

	.risk-panel {
		margin-top: 16rpx;
		padding: 20rpx;
		background: linear-gradient(180deg, rgba(255, 246, 246, 0.92), rgba(255, 255, 255, 0.82));
		border: 1px solid rgba(244, 171, 171, 0.42);
		box-shadow: 0 16rpx 36rpx rgba(227, 80, 80, 0.08);
	}

	.risk-text {
		display: block;
		margin-top: 8rpx;
		font-size: 28rpx;
		line-height: 1.7;
		color: var(--text-soft);
	}

	@media screen and (max-width: 720px) {
		.record-grid {
			grid-template-columns: 1fr;
		}

		.record-head {
			flex-wrap: wrap;
		}
	}
</style>
