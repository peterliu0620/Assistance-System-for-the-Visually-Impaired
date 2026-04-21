<template>
	<view class="page auth-page">
		<view class="auth-glow auth-glow-one"></view>
		<view class="auth-glow auth-glow-two"></view>
		<view class="auth-grid"></view>

		<view class="auth-shell">
			<view class="brand-column">
				<text class="brand-eyebrow">Vision Guide</text>
				<text class="brand-title">把识别、寻物和知识归档收进一个更顺手的入口</text>
				<text class="brand-desc">登录后即可进入实时识别、导盲寻物与个性化无障碍设置，让播报和交互习惯都保持一致。</text>

				<view class="brand-rail">
					<view class="brand-rail-item">
						<text class="brand-rail-label">模式</text>
						<text class="brand-rail-value">{{ mode === 'login' ? '账号登录' : '新用户注册' }}</text>
					</view>
					<view class="brand-rail-item">
						<text class="brand-rail-label">能力</text>
						<text class="brand-rail-value">识别 / 寻物 / 知识追问</text>
					</view>
				</view>

				<view class="feature-list">
					<view class="feature-item">连续抽帧追踪目标物，语音与震动同步反馈</view>
					<view class="feature-item">自动归档识别记录，支持围绕最近会话继续提问</view>
					<view class="feature-item">手势、语速、对比模式集中管理，进入首页后即时生效</view>
				</view>
			</view>

			<view class="auth-panel">
				<view class="panel-head">
					<text class="panel-kicker">账号中心</text>
					<text class="panel-title">{{ mode === 'login' ? '继续使用你的辅助工作台' : '创建一个新的个人空间' }}</text>
				</view>

				<view class="tabs">
					<view :class="['tab', mode === 'login' ? 'active' : '']" @click="mode = 'login'">登录</view>
					<view :class="['tab', mode === 'register' ? 'active' : '']" @click="mode = 'register'">注册</view>
				</view>

				<view class="form-stack">
					<input class="input" v-model.trim="form.username" placeholder="用户名（4-50位）" />
					<input class="input" v-model.trim="form.password" password placeholder="密码（6-20位）" />
					<input v-if="mode === 'register'" class="input" v-model.trim="form.nickname" placeholder="昵称" />
					<input v-if="mode === 'register'" class="input" v-model.trim="form.phone" placeholder="手机号（可选）" />
					<input v-if="mode === 'register'" class="input" v-model.trim="form.email" placeholder="邮箱（可选）" />
				</view>

				<view class="action-group">
					<button class="btn primary" @click="submit" :disabled="loading">
						{{ loading ? '提交中...' : mode === 'login' ? '立即登录' : '立即注册' }}
					</button>
					<button class="btn secondary" @click="goBack">返回首页</button>
				</view>

				<view class="result" v-if="userInfo">
					<text class="line">当前用户：{{ userInfo.nickname }}（{{ userInfo.username }}）</text>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
	import { defineComponent } from 'vue';
	import { API_BASE } from '../../utils/api';

	export default defineComponent({
		data() {
			return {
				apiBase: API_BASE,
				mode: 'login',
				loading: false,
				userInfo: null,
				form: {
					username: '',
					password: '',
					nickname: '',
					phone: '',
					email: ''
				}
			}
		},
		onLoad() {
			const cached = uni.getStorageSync('auth_user')
			if (cached && cached.id) {
				this.userInfo = cached
				uni.reLaunch({
					url: '/pages/index/index'
				})
			}
		},
		methods: {
			submit() {
				if (!this.form.username || !this.form.password) {
					uni.showToast({ title: '用户名和密码不能为空', icon: 'none' })
					return
				}
				if (this.mode === 'register' && !this.form.nickname) {
					uni.showToast({ title: '昵称不能为空', icon: 'none' })
					return
				}

				this.loading = true
				const url = this.mode === 'login' ? `${this.apiBase}/api/auth/login` : `${this.apiBase}/api/auth/register`
				const data = this.mode === 'login'
					? {
						username: this.form.username,
						password: this.form.password
					}
					: {
						username: this.form.username,
						password: this.form.password,
						nickname: this.form.nickname,
						phone: this.form.phone,
						email: this.form.email
					}

				uni.request({
					url,
					method: 'POST',
					header: {
						'Content-Type': 'application/json'
					},
					data,
					success: (res) => {
						this.loading = false
						if (res.statusCode !== 200) {
							const response = res.data || {}
							const msg = response && response.message ? response.message : '请求失败'
							uni.showToast({ title: msg, icon: 'none' })
							return
						}
						const user = res.data
						this.userInfo = user
						uni.setStorageSync('auth_user', user)
						uni.showToast({ title: this.mode === 'login' ? '登录成功' : '注册成功', icon: 'success' })
						setTimeout(() => {
							uni.reLaunch({
								url: '/pages/index/index'
							})
						}, 300)
					},
					fail: () => {
						this.loading = false
						uni.showToast({ title: '请求失败，请检查后端服务', icon: 'none' })
					}
				})
			},
			goBack() {
				uni.navigateBack({
					fail: () => {
						uni.reLaunch({ url: '/pages/index/index' })
					}
				})
			}
		}
	})
</script>

<style>
	.page {
		min-height: 100vh;
	}

	.auth-page {
		position: relative;
		overflow: hidden;
		padding: 36rpx 28rpx;
		background:
			radial-gradient(120% 100% at 12% 0%, rgba(31, 59, 97, 0.96) 0%, rgba(8, 15, 26, 0) 54%),
			linear-gradient(180deg, #050c15 0%, #09111a 44%, #071019 100%);
	}

	.auth-glow {
		position: absolute;
		border-radius: 999rpx;
		filter: blur(32rpx);
		pointer-events: none;
	}

	.auth-glow-one {
		top: -80rpx;
		right: -120rpx;
		width: 420rpx;
		height: 420rpx;
		background: radial-gradient(circle, rgba(255, 212, 107, 0.46) 0%, rgba(255, 212, 107, 0) 70%);
	}

	.auth-glow-two {
		left: -140rpx;
		bottom: 120rpx;
		width: 480rpx;
		height: 480rpx;
		background: radial-gradient(circle, rgba(110, 215, 255, 0.24) 0%, rgba(110, 215, 255, 0) 72%);
	}

	.auth-grid {
		position: absolute;
		inset: 0;
		background-image:
			linear-gradient(rgba(255, 255, 255, 0.03) 1px, transparent 1px),
			linear-gradient(90deg, rgba(255, 255, 255, 0.03) 1px, transparent 1px);
		background-size: 46rpx 46rpx;
		mask-image: linear-gradient(180deg, rgba(0, 0, 0, 0.65), transparent 84%);
		pointer-events: none;
	}

	.auth-shell {
		position: relative;
		z-index: 1;
		display: grid;
		grid-template-columns: minmax(0, 1.08fr) minmax(320rpx, 0.92fr);
		gap: 24rpx;
		align-items: stretch;
	}

	.brand-column,
	.auth-panel {
		border-radius: 34rpx;
		padding: 34rpx;
		backdrop-filter: blur(18rpx);
		box-shadow: 0 28rpx 90rpx rgba(0, 0, 0, 0.28);
	}

	.brand-column {
		position: relative;
		overflow: hidden;
		background: linear-gradient(160deg, rgba(13, 22, 36, 0.92), rgba(9, 16, 28, 0.76));
		border: 2rpx solid rgba(135, 215, 255, 0.12);
	}

	.brand-column::after {
		content: '';
		position: absolute;
		right: -120rpx;
		top: -80rpx;
		width: 320rpx;
		height: 320rpx;
		border-radius: 50%;
		background: radial-gradient(circle, rgba(255, 212, 107, 0.16), rgba(255, 212, 107, 0));
	}

	.auth-panel {
		background: linear-gradient(160deg, rgba(9, 16, 27, 0.92), rgba(8, 14, 24, 0.82));
		border: 2rpx solid rgba(255, 255, 255, 0.08);
	}

	.brand-eyebrow,
	.panel-kicker {
		display: block;
		font-size: 20rpx;
		letter-spacing: 6rpx;
		text-transform: uppercase;
		color: #9db6d3;
	}

	.brand-title,
	.panel-title {
		display: block;
		margin-top: 18rpx;
		line-height: 1.14;
		font-weight: bold;
		color: #f3f8ff;
	}

	.brand-title {
		max-width: 11em;
		font-size: 60rpx;
	}

	.panel-title {
		font-size: 42rpx;
	}

	.brand-desc {
		display: block;
		max-width: 14em;
		margin-top: 18rpx;
		font-size: 28rpx;
		line-height: 1.72;
		color: #a9bfd7;
	}

	.brand-rail {
		display: grid;
		grid-template-columns: repeat(2, minmax(0, 1fr));
		gap: 14rpx;
		margin-top: 30rpx;
	}

	.brand-rail-item,
	.feature-item,
	.result {
		border-radius: 24rpx;
		background: rgba(255, 255, 255, 0.04);
		border: 1px solid rgba(255, 255, 255, 0.08);
	}

	.brand-rail-item {
		padding: 22rpx;
	}

	.brand-rail-label {
		display: block;
		font-size: 22rpx;
		color: #94adc9;
	}

	.brand-rail-value {
		display: block;
		margin-top: 10rpx;
		font-size: 30rpx;
		line-height: 1.45;
		font-weight: bold;
		color: #f3f8ff;
	}

	.feature-list {
		display: grid;
		gap: 14rpx;
		margin-top: 18rpx;
	}

	.feature-item {
		padding: 22rpx 24rpx;
		font-size: 26rpx;
		line-height: 1.68;
		color: #d6e5f5;
	}

	.panel-head {
		margin-bottom: 24rpx;
	}

	.tabs {
		display: flex;
		padding: 8rpx;
		background: rgba(255, 255, 255, 0.04);
		border: 1px solid rgba(255, 255, 255, 0.08);
		border-radius: 999rpx;
		margin-bottom: 22rpx;
		overflow: hidden;
	}

	.tab {
		flex: 1;
		text-align: center;
		padding: 18rpx 0;
		border-radius: 999rpx;
		color: #9ab4c9;
		font-size: 28rpx;
		transition: all 0.24s ease;
	}

	.active {
		background: linear-gradient(135deg, #ffd46b, #fff0bc);
		color: #13202f;
		font-weight: bold;
		box-shadow: 0 14rpx 34rpx rgba(255, 212, 107, 0.18);
	}

	.form-stack {
		display: grid;
		gap: 14rpx;
	}

	.input {
		height: 86rpx;
		background: rgba(255, 255, 255, 0.04);
		border: 1px solid rgba(255, 255, 255, 0.08);
		border-radius: 20rpx;
		padding: 0 22rpx;
		color: #eaf2fb;
		font-size: 28rpx;
	}

	.btn {
		display: inline-flex;
		align-items: center;
		justify-content: center;
		height: 88rpx;
		border-radius: 999rpx;
		font-size: 28rpx;
		font-weight: bold;
		margin: 0;
	}

	.primary {
		background: linear-gradient(135deg, #ffd46b, #fff0bc);
		color: #13202f;
		box-shadow: 0 18rpx 42rpx rgba(255, 212, 107, 0.2);
	}

	.secondary {
		background: rgba(255, 255, 255, 0.05);
		color: #f2f8ff;
		border: 1px solid rgba(255, 255, 255, 0.08);
	}

	.action-group {
		display: grid;
		gap: 14rpx;
		margin-top: 22rpx;
	}

	.result {
		margin-top: 20rpx;
		padding: 18rpx 20rpx;
	}

	.line {
		color: #b9d7ef;
		font-size: 26rpx;
		line-height: 1.6;
	}

	@media screen and (max-width: 720px) {
		.auth-shell,
		.brand-rail {
			grid-template-columns: 1fr;
		}

		.brand-title {
			font-size: 52rpx;
		}

		.panel-title {
			font-size: 38rpx;
		}

		.brand-column,
		.auth-panel {
			padding: 28rpx;
			border-radius: 28rpx;
		}
	}
</style>
