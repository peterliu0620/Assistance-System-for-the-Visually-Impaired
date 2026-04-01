<template>
	<view class="page">
		<view class="card">
			<text class="title">账号中心</text>
			<text class="subtitle">登录后可进入智能识别与个性化设置</text>
			<view class="tabs">
				<view :class="['tab', mode === 'login' ? 'active' : '']" @click="mode = 'login'">登录</view>
				<view :class="['tab', mode === 'register' ? 'active' : '']" @click="mode = 'register'">注册</view>
			</view>

			<input class="input" v-model.trim="form.username" placeholder="用户名（4-50位）" />
			<input class="input" v-model.trim="form.password" password placeholder="密码（6-20位）" />
			<input v-if="mode === 'register'" class="input" v-model.trim="form.nickname" placeholder="昵称" />
			<input v-if="mode === 'register'" class="input" v-model.trim="form.phone" placeholder="手机号（可选）" />
			<input v-if="mode === 'register'" class="input" v-model.trim="form.email" placeholder="邮箱（可选）" />

			<button class="btn primary" @click="submit" :disabled="loading">
				{{ loading ? '提交中...' : mode === 'login' ? '立即登录' : '立即注册' }}
			</button>
			<button class="btn" @click="goBack">返回首页</button>

			<view class="result" v-if="userInfo">
				<text class="line">当前用户：{{ userInfo.nickname }}（{{ userInfo.username }}）</text>
			</view>
		</view>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				apiBase: 'http://10.135.102.177:8080',
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
							const msg = res.data && res.data.message ? res.data.message : '请求失败'
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
	}
</script>

<style>
	.page {
		min-height: 100vh;
		padding: 28rpx;
		background: radial-gradient(130% 100% at 15% 0%, #1a2f4a 0%, #090d14 64%);
	}

	.card {
		background: linear-gradient(160deg, rgba(18, 28, 44, 0.86), rgba(9, 15, 24, 0.86));
		border: 2rpx solid rgba(94, 200, 255, 0.38);
		border-radius: 22rpx;
		padding: 30rpx;
		box-shadow: var(--shadow-lg);
		backdrop-filter: blur(10rpx);
	}

	.title {
		display: block;
		font-size: 44rpx;
		color: #5cc6ff;
		font-weight: bold;
		margin-bottom: 10rpx;
	}

	.subtitle {
		display: block;
		font-size: 24rpx;
		color: #9ab5d2;
		margin-bottom: 20rpx;
	}

	.tabs {
		display: flex;
		background: #0a111a;
		border-radius: 14rpx;
		margin-bottom: 20rpx;
		overflow: hidden;
	}

	.tab {
		flex: 1;
		text-align: center;
		padding: 18rpx 0;
		color: #9ab4c9;
		font-size: 28rpx;
	}

	.active {
		background: #5cc6ff;
		color: #102033;
		font-weight: bold;
	}

	.input {
		height: 82rpx;
		background: rgba(10, 16, 24, 0.92);
		border: 1px solid rgba(101, 139, 180, 0.36);
		border-radius: 12rpx;
		padding: 0 20rpx;
		color: #eaf2fb;
		font-size: 28rpx;
		margin-bottom: 16rpx;
	}

	.btn {
		margin-top: 10rpx;
		background: linear-gradient(145deg, #2a3d53, #1f2e40);
		color: #f2f8ff;
	}

	.primary {
		background: linear-gradient(135deg, #62d0ff, #3caef5);
		color: #102033;
		font-weight: bold;
	}

	.result {
		margin-top: 20rpx;
		padding: 16rpx;
		background: #0b141f;
		border: 1px dashed #4f7a9e;
		border-radius: 12rpx;
	}

	.line {
		color: #b9d7ef;
		font-size: 26rpx;
	}
</style>
