<template>
	<view :class="['page', themeClass, largeFontClass]">
		<view class="page-glow glow-one"></view>
		<view class="page-glow glow-two"></view>
		<view class="page-grid"></view>

		<view class="hero-shell">
			<view class="hero-copy">
				<text class="eyebrow">Knowledge Archive</text>
				<text class="hero-title">把识别记录沉淀成可追问的知识库</text>
				<text class="hero-desc">这里会自动归档最近识别结果，并围绕最近会话继续追问，帮助你把单次识别延展成连续理解。</text>

				<view class="hero-actions">
					<button class="action-btn primary" @click="refreshRecords">刷新记录</button>
					<button class="action-btn ghost" @click="goHome">返回首页</button>
				</view>
			</view>

			<view class="hero-stats">
				<view class="stat-card stat-card-strong">
					<text class="stat-label">今日扫描</text>
					<text class="stat-value">{{ knowledge.todayScanCount }}</text>
				</view>
				<view class="stat-card">
					<text class="stat-label">最近会话</text>
					<text class="stat-value stat-value-small">{{ knowledge.latestSessionId || '-' }}</text>
				</view>
				<view class="stat-card">
					<text class="stat-label">归档数量</text>
					<text class="stat-value">{{ knowledge.records.length }}</text>
				</view>
				<view class="stat-card">
					<text class="stat-label">最近回答</text>
					<text class="stat-value stat-value-small">{{ knowledge.lastAnswer ? '已生成' : '暂无' }}</text>
				</view>
			</view>
		</view>

		<view class="section-panel ask-panel">
			<view class="section-head">
				<view>
					<text class="section-kicker">连续追问</text>
					<text class="section-title">知识问答</text>
				</view>
				<text class="section-tag">围绕最近会话</text>
			</view>

			<input
				v-model="questionInput"
				class="ask-input"
				placeholder="例如：刚才那个瓶子左边是什么？"
				placeholder-class="placeholder"
			/>

			<view class="action-row">
				<button class="action-btn primary" @click="askQuestion" :disabled="asking">{{ asking ? '提问中' : '开始提问' }}</button>
				<button class="action-btn ghost" @click="refreshRecords">同步归档</button>
			</view>

			<view v-if="knowledge.lastAnswer" class="answer-panel">
				<text class="panel-kicker">最近回答</text>
				<text class="answer-text">{{ knowledge.lastAnswer }}</text>
			</view>
		</view>

		<view class="section-panel archive-panel">
			<view class="section-head">
				<view>
					<text class="section-kicker">结构化记录</text>
					<text class="section-title">最近归档</text>
				</view>
				<text class="section-tag">最近 8 条</text>
			</view>

			<view v-if="!knowledge.records.length" class="empty-panel">
				<text class="empty-title">还没有归档记录</text>
				<text class="empty-text">先回到首页识别一次物品，再回来继续追问会更完整。</text>
				<button class="action-btn primary compact-btn" @click="goHome">去识别页</button>
			</view>

			<view v-for="record in knowledge.records" :key="record.id" class="record-panel">
				<view class="record-head">
					<text class="record-title">{{ record.scene || '未知场景' }}</text>
					<text class="record-time">{{ formatTime(record.capturedAt) }}</text>
				</view>

				<view class="record-grid">
					<view class="record-meta">
						<text class="record-meta-label">触发指令</text>
						<text class="record-meta-value">{{ record.triggerCommand || '-' }}</text>
					</view>
					<view class="record-meta">
						<text class="record-meta-label">地点</text>
						<text class="record-meta-value">{{ record.locationText || '未记录地点' }}</text>
					</view>
				</view>

				<text class="record-line">播报：{{ record.narration || '-' }}</text>

				<view class="item-list" v-if="record.items && record.items.length">
					<view v-for="item in record.items || []" :key="item.id" class="item-chip">
						<text class="item-name">{{ item.name }}</text>
						<text class="item-meta">{{ item.position || '未知方位' }} / 置信度 {{ item.confidence || 0 }}%</text>
					</view>
				</view>
			</view>
		</view>

		<app-tab-bar current="knowledge" />
	</view>
</template>

<script>
	import { defineComponent } from 'vue';
	import { API_BASE } from '../../utils/api';
	import { loadUserSettings } from '../../utils/user-settings';
	import AppTabBar from '../../components/app-tab-bar.vue';

	export default defineComponent({
		components: {
			AppTabBar
		},
		data() {
			return {
				settings: loadUserSettings(),
				asking: false,
				questionInput: '',
				knowledge: {
					todayScanCount: 0,
					latestSessionId: '',
					lastAnswer: '',
					records: []
				},
				apiBase: API_BASE
			}
		},
		computed: {
			themeClass() {
				return this.settings.contrastMode === 'black-yellow' ? 'theme-yellow' : 'theme-gold'
			},
			largeFontClass() {
				return this.settings.extraLargeText ? 'font-large' : ''
			}
		},
		onLoad() {
			const user = uni.getStorageSync('auth_user')
			if (!user || !user.id) {
				uni.reLaunch({ url: '/pages/auth/auth' })
				return
			}
			this.refreshRecords()
		},
		onShow() {
			this.settings = loadUserSettings()
			this.refreshRecords()
		},
		methods: {
			getCurrentUser() {
				return uni.getStorageSync('auth_user') || {}
			},
			formatTime(value) {
				if (!value) {
					return '-'
				}
				return String(value).replace('T', ' ')
			},
			refreshRecords() {
				const user = this.getCurrentUser()
				if (!user.id) {
					return
				}
				uni.request({
					url: `${this.apiBase}/api/knowledge/records`,
					method: 'GET',
					data: {
						userId: user.id,
						limit: 8
					},
					success: (res) => {
						if (res.statusCode !== 200 || !res.data) {
							uni.showToast({ title: '加载知识库失败', icon: 'none' })
							return
						}
						const response = res.data || {}
						this.knowledge.todayScanCount = response.todayScanCount || 0
						this.knowledge.latestSessionId = response.latestSessionId || uni.getStorageSync('knowledge_last_session_id') || ''
						this.knowledge.records = response.records || []
					},
					fail: () => {
						uni.showToast({ title: '加载知识库失败', icon: 'none' })
					}
				})
			},
			askQuestion() {
				const user = this.getCurrentUser()
				const question = (this.questionInput || '').trim()
				if (!user.id || !question) {
					uni.showToast({ title: '请输入问题', icon: 'none' })
					return
				}
				this.asking = true
				uni.request({
					url: `${this.apiBase}/api/knowledge/ask`,
					method: 'POST',
					header: {
						'Content-Type': 'application/json'
					},
					data: {
						userId: user.id,
						question,
						sessionId: this.knowledge.latestSessionId || uni.getStorageSync('knowledge_last_session_id') || ''
					},
					success: (res) => {
						this.asking = false
						if (res.statusCode !== 200 || !res.data) {
							uni.showToast({ title: '提问失败', icon: 'none' })
							return
						}
						const response = res.data || {}
						this.knowledge.lastAnswer = response.answer || ''
						if (response.usedSessionId) {
							this.knowledge.latestSessionId = response.usedSessionId
							uni.setStorageSync('knowledge_last_session_id', response.usedSessionId)
						}
					},
					fail: () => {
						this.asking = false
						uni.showToast({ title: '提问失败', icon: 'none' })
					}
				})
			},
			goHome() {
				uni.redirectTo({
					url: '/pages/index/index',
					fail: () => {
						uni.reLaunch({ url: '/pages/index/index' })
					}
				})
			}
		}
	})
</script>

<style>
	@import '../../styles/experience-shell.css';

	.answer-text,
	.record-line,
	.empty-text {
		display: block;
		font-size: 28rpx;
		line-height: 1.7;
		color: var(--text-soft);
	}

	.record-head {
		display: flex;
		align-items: center;
		justify-content: space-between;
		gap: 14rpx;
	}

	.empty-title,
	.record-title {
		display: block;
		color: var(--text-main);
		font-weight: bold;
	}

	.record-grid {
		display: grid;
		grid-template-columns: repeat(2, minmax(0, 1fr));
		gap: 14rpx;
	}

	.record-meta,
	.item-chip,
	.answer-panel,
	.empty-panel,
	.record-panel {
		border-radius: 24rpx;
	}

	.record-meta-label {
		display: block;
		font-size: 22rpx;
		color: var(--text-soft);
	}

	.record-meta-value {
		display: block;
		margin-top: 10rpx;
		font-size: 32rpx;
		line-height: 1.35;
		font-weight: bold;
		color: var(--text-main);
	}

	.ask-input {
		height: 90rpx;
		margin-top: 18rpx;
		padding: 0 22rpx;
		border-radius: 22rpx;
		background: rgba(255, 255, 255, 0.05);
		border: 1px solid rgba(255, 255, 255, 0.08);
		color: var(--text-main);
		font-size: 28rpx;
	}

	.placeholder {
		color: rgba(255, 255, 255, 0.34);
	}

	.compact-btn {
		margin-top: 20rpx;
		min-width: 0;
	}

	.answer-panel,
	.empty-panel,
	.record-panel {
		margin-top: 18rpx;
		padding: 22rpx;
		background: rgba(255, 255, 255, 0.04);
		border: 1px solid rgba(255, 255, 255, 0.08);
	}

	.answer-panel {
		background: linear-gradient(160deg, rgba(67, 113, 212, 0.14), rgba(84, 198, 192, 0.08));
		border-color: rgba(125, 190, 255, 0.18);
	}

	.empty-title {
		font-size: 34rpx;
	}

	.record-title {
		font-size: 32rpx;
	}

	.record-time,
	.item-meta {
		display: block;
		font-size: 22rpx;
		line-height: 1.6;
		color: var(--text-soft);
	}

	.record-grid {
		margin-top: 16rpx;
	}

	.item-list {
		display: flex;
		flex-wrap: wrap;
		gap: 12rpx;
		margin-top: 16rpx;
	}

	.item-chip {
		min-width: 200rpx;
		padding: 16rpx 18rpx;
		background: rgba(255, 255, 255, 0.05);
		border: 1px solid rgba(255, 255, 255, 0.08);
	}

	.item-name {
		display: block;
		font-size: 26rpx;
		font-weight: bold;
		color: var(--text-main);
	}

	.font-large .hero-title,
	.font-large .answer-text,
	.font-large .record-line,
	.font-large .empty-text,
	.font-large .ask-input,
	.font-large .stat-value,
	.font-large .record-meta-value {
		font-size: 34rpx;
	}

	@media screen and (max-width: 720px) {
		.record-head {
			flex-wrap: wrap;
			align-items: flex-start;
		}

		.record-grid {
			grid-template-columns: 1fr;
		}
	}
</style>
