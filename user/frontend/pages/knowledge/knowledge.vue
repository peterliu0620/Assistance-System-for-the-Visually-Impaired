<template>
	<view :class="['page', themeClass, largeFontClass]">
		<view class="card">
			<text class="title">智能知识库</text>
			<text class="desc">自动归档识别记录，并支持围绕最近会话和今日记录进行自然语言追问。</text>

			<view class="stats-grid">
				<view class="stat-card strong">
					<text class="stat-label">今日扫描</text>
					<text class="stat-value">{{ knowledge.todayScanCount }}</text>
				</view>
				<view class="stat-card">
					<text class="stat-label">最近会话</text>
					<text class="stat-value small">{{ knowledge.latestSessionId || '-' }}</text>
				</view>
			</view>
		</view>

		<view class="card">
			<text class="section-title">知识问答</text>
			<input
				v-model="questionInput"
				class="ask-input"
				placeholder="例如：刚才那个瓶子左边是什么？"
				placeholder-class="placeholder"
			/>
			<view class="ask-actions">
				<button class="btn primary" @click="askQuestion" :disabled="asking">提问</button>
				<button class="btn" @click="refreshRecords">刷新记录</button>
			</view>

			<view v-if="knowledge.lastAnswer" class="answer-card">
				<text class="answer-title">最近回答</text>
				<text class="answer-text">{{ knowledge.lastAnswer }}</text>
			</view>
		</view>

		<view class="card">
			<text class="section-title">最近归档</text>
			<view v-if="!knowledge.records.length" class="empty-box">
				<text class="empty-text">还没有归档记录，先去识别一次物品吧。</text>
			</view>
			<view v-for="record in knowledge.records" :key="record.id" class="record-card">
				<text class="record-title">{{ record.scene || '未知场景' }}</text>
				<text class="record-line">指令：{{ record.triggerCommand || '-' }}</text>
				<text class="record-line">地点：{{ record.locationText || '未记录地点' }}</text>
				<text class="record-line">时间：{{ formatTime(record.capturedAt) }}</text>
				<text class="record-line">播报：{{ record.narration || '-' }}</text>
				<text class="record-subtitle">物品明细</text>
				<text v-for="item in record.items || []" :key="item.id" class="record-line item-line">
					{{ item.name }} / {{ item.position || '未知方位' }} / 置信度 {{ item.confidence || 0 }}%
				</text>
			</view>
		</view>
	</view>
</template>

<script>
	import { loadUserSettings } from '../../utils/user-settings.js'

	export default {
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
				apiBase: 'http://10.69.65.44:8080'
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
						this.knowledge.todayScanCount = res.data.todayScanCount || 0
						this.knowledge.latestSessionId = res.data.latestSessionId || uni.getStorageSync('knowledge_last_session_id') || ''
						this.knowledge.records = res.data.records || []
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
						this.knowledge.lastAnswer = res.data.answer || ''
						if (res.data.usedSessionId) {
							this.knowledge.latestSessionId = res.data.usedSessionId
							uni.setStorageSync('knowledge_last_session_id', res.data.usedSessionId)
						}
					},
					fail: () => {
						this.asking = false
						uni.showToast({ title: '提问失败', icon: 'none' })
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
	}

	.theme-gold {
		background: radial-gradient(130% 100% at 15% 0%, #1a2f4a 0%, #090d14 64%);
	}

	.theme-yellow {
		background: radial-gradient(130% 100% at 15% 0%, #2d250d 0%, #090d14 64%);
	}

	.card {
		margin-bottom: 20rpx;
		padding: 24rpx;
		border-radius: 18rpx;
		box-shadow: var(--shadow-lg);
	}

	.theme-gold .card {
		border: 2rpx solid rgba(255, 207, 75, 0.35);
		background: linear-gradient(160deg, rgba(22, 28, 36, 0.88), rgba(10, 14, 20, 0.9));
	}

	.theme-yellow .card {
		border: 2rpx solid rgba(255, 230, 0, 0.45);
		background: linear-gradient(160deg, rgba(22, 21, 11, 0.9), rgba(11, 11, 8, 0.92));
	}

	.title,
	.section-title,
	.answer-title,
	.record-title {
		display: block;
		color: #f5f7fb;
		font-weight: bold;
	}

	.title {
		font-size: 40rpx;
		margin-bottom: 12rpx;
	}

	.section-title,
	.answer-title {
		font-size: 30rpx;
		margin-bottom: 12rpx;
	}

	.record-title {
		font-size: 28rpx;
		margin-bottom: 8rpx;
	}

	.desc,
	.record-line,
	.answer-text,
	.empty-text {
		display: block;
		color: #d5deea;
		font-size: 24rpx;
		line-height: 1.7;
	}

	.stats-grid {
		display: grid;
		grid-template-columns: repeat(2, minmax(0, 1fr));
		gap: 12rpx;
		margin-top: 16rpx;
	}

	.stat-card {
		padding: 18rpx;
		border-radius: 14rpx;
		background: rgba(255, 255, 255, 0.05);
	}

	.stat-card.strong {
		background: linear-gradient(145deg, rgba(255, 207, 75, 0.16), rgba(100, 214, 255, 0.12));
	}

	.stat-label {
		display: block;
		font-size: 22rpx;
		color: #9bb4cd;
	}

	.stat-value {
		display: block;
		margin-top: 8rpx;
		font-size: 34rpx;
		color: #fff4d4;
		font-weight: bold;
	}

	.stat-value.small {
		font-size: 22rpx;
		word-break: break-all;
	}

	.ask-input {
		height: 84rpx;
		padding: 0 18rpx;
		border-radius: 12rpx;
		background: rgba(8, 12, 18, 0.92);
		border: 1px solid rgba(99, 133, 175, 0.38);
		color: #f0f5ff;
		font-size: 26rpx;
	}

	.placeholder {
		color: #7f96ac;
	}

	.ask-actions {
		display: flex;
		gap: 12rpx;
		margin-top: 16rpx;
	}

	.btn {
		flex: 1;
		height: 78rpx;
		line-height: 78rpx;
		font-size: 24rpx;
		border-radius: 12rpx;
		margin: 0;
		background: linear-gradient(145deg, #2a3d53, #1f2e40);
		color: #ffffff;
	}

	.btn.primary {
		background: #ffcf4b;
		color: #161a20;
		font-weight: bold;
	}

	.answer-card,
	.record-card,
	.empty-box {
		margin-top: 16rpx;
		padding: 18rpx;
		border-radius: 14rpx;
		background: rgba(255, 255, 255, 0.05);
		border: 1px solid rgba(110, 144, 185, 0.24);
	}

	.record-subtitle {
		display: block;
		margin-top: 10rpx;
		margin-bottom: 6rpx;
		font-size: 24rpx;
		color: #ffcf4b;
		font-weight: bold;
	}

	.item-line {
		color: #bfe7ff;
	}

	.font-large .title {
		font-size: 48rpx;
	}

	.font-large .section-title,
	.font-large .record-title,
	.font-large .answer-title {
		font-size: 34rpx;
	}

	.font-large .desc,
	.font-large .record-line,
	.font-large .answer-text,
	.font-large .empty-text,
	.font-large .ask-input {
		font-size: 30rpx;
	}
</style>
