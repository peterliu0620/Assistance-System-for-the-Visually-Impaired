<template>
	<view class="page auth-page">
		<view class="auth-glow auth-glow-one"></view>
		<view class="auth-glow auth-glow-two"></view>
		<view class="auth-grid"></view>

		<view class="auth-shell">
			<view class="brand-column">
				<text class="brand-eyebrow">Vision Guide</text>
				<text class="brand-title">同一入口，切换两种照护视角</text>
				<text class="brand-desc">视障人士进入识别工作台，家人进入记录与登记中心。首版全部使用本地 mock 和缓存，方便我们先把前端流程走通。</text>

				<view class="role-showcase">
					<view
						v-for="item in roleOptions"
						:key="item.value"
						:class="['role-card', selectedRole === item.value ? 'role-card-active' : '']"
						@click="selectedRole = item.value"
					>
						<text class="role-name">{{ item.label }}</text>
						<text class="role-desc">{{ item.desc }}</text>
					</view>
				</view>

				<view class="brand-rail">
					<view class="brand-rail-item">
						<text class="brand-rail-label">当前身份</text>
						<text class="brand-rail-value">{{ currentRoleLabel }}</text>
					</view>
					<view class="brand-rail-item">
						<text class="brand-rail-label">进入后首页</text>
						<text class="brand-rail-value">{{ selectedRole === USER_ROLE_FAMILY ? '家属中心' : '识别首页' }}</text>
					</view>
				</view>

				<view class="demo-panel">
					<text class="demo-title">体验账号</text>
					<text class="demo-line">视障人士：`vision_demo / 123456`</text>
					<text class="demo-line">家人：`family_demo / 123456`</text>
				</view>
			</view>

			<view class="auth-panel">
				<view class="panel-head">
					<text class="panel-kicker">身份登录</text>
					<text class="panel-title">{{ mode === 'login' ? '先选择身份，再进入对应工作区' : '创建当前身份下的新账号' }}</text>
				</view>

				<view class="tabs">
					<view :class="['tab', mode === 'login' ? 'active' : '']" @click="mode = 'login'">登录</view>
					<view :class="['tab', mode === 'register' ? 'active' : '']" @click="mode = 'register'">注册</view>
				</view>

				<view class="role-picker">
					<view
						v-for="item in roleOptions"
						:key="item.value"
						:class="['picker-chip', selectedRole === item.value ? 'picker-chip-active' : '']"
						@click="selectedRole = item.value"
					>
						{{ item.label }}
					</view>
				</view>

				<view class="form-stack">
					<input class="input" v-model.trim="form.username" placeholder="用户名（至少 4 位）" />
					<input class="input" v-model.trim="form.password" password placeholder="密码（至少 6 位）" />
					<input v-if="mode === 'register'" class="input" v-model.trim="form.nickname" placeholder="昵称" />
					<input v-if="mode === 'register'" class="input" v-model.trim="form.phone" placeholder="手机号（可选）" />
					<input v-if="mode === 'register'" class="input" v-model.trim="form.email" placeholder="邮箱（可选）" />
				</view>

				<view class="action-group">
					<button class="btn primary" @click="submit" :disabled="loading">
						{{ loading ? '提交中...' : mode === 'login' ? `以${currentRoleLabel}身份登录` : `注册${currentRoleLabel}账号` }}
					</button>
					<button class="btn secondary" @click="fillDemoAccount">填入体验账号</button>
				</view>

				<view class="result" v-if="userInfo && userInfo.id">
					<text class="line">当前用户：{{ userInfo.nickname }}（{{ userInfo.username }}）</text>
					<text class="line">身份：{{ getRoleLabel(userInfo.role) }}</text>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
	import { defineComponent } from 'vue';
	import {
		ROLE_OPTIONS,
		USER_ROLE_FAMILY,
		USER_ROLE_VISION,
		getAuthUser,
		getDefaultRouteByRole,
		getRoleLabel,
		loginWithMock,
		registerWithMock
	} from '../../utils/auth';

	export default defineComponent({
		data() {
			return {
				mode: 'login',
				loading: false,
				userInfo: null,
				roleOptions: ROLE_OPTIONS,
				selectedRole: USER_ROLE_VISION,
				form: {
					username: '',
					password: '',
					nickname: '',
					phone: '',
					email: ''
				},
				USER_ROLE_FAMILY,
				USER_ROLE_VISION
			};
		},
		computed: {
			currentRoleLabel() {
				return getRoleLabel(this.selectedRole);
			}
		},
		onLoad() {
			const cached = getAuthUser();
			if (cached && cached.id) {
				this.userInfo = cached;
				uni.reLaunch({
					url: getDefaultRouteByRole(cached.role)
				});
			}
		},
		methods: {
			getRoleLabel,
			fillDemoAccount() {
				this.form.username = this.selectedRole === USER_ROLE_FAMILY ? 'family_demo' : 'vision_demo';
				this.form.password = '123456';
				if (this.mode === 'register') {
					this.form.nickname = this.selectedRole === USER_ROLE_FAMILY ? '新家属账号' : '新视障账号';
				}
			},
			submit() {
				const payload = {
					...this.form,
					role: this.selectedRole
				};
				this.loading = true;

				setTimeout(() => {
					const result = this.mode === 'login' ? loginWithMock(payload) : registerWithMock(payload);
					this.loading = false;

					if (!result.ok) {
						uni.showToast({ title: result.message, icon: 'none' });
						return;
					}

					this.userInfo = result.user;
					uni.showToast({ title: this.mode === 'login' ? '登录成功' : '注册成功', icon: 'success' });
					setTimeout(() => {
						uni.reLaunch({
							url: getDefaultRouteByRole(result.user.role)
						});
					}, 240);
				}, 160);
			}
		}
	});
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

	.brand-desc,
	.role-desc,
	.demo-line,
	.line {
		display: block;
		font-size: 28rpx;
		line-height: 1.72;
		color: #a9bfd7;
	}

	.role-showcase,
	.form-stack {
		display: grid;
		gap: 14rpx;
	}

	.role-showcase {
		margin-top: 30rpx;
	}

	.role-card,
	.brand-rail-item,
	.demo-panel,
	.result {
		border-radius: 24rpx;
		background: rgba(255, 255, 255, 0.04);
		border: 1px solid rgba(255, 255, 255, 0.08);
	}

	.role-card {
		padding: 24rpx;
	}

	.role-card-active {
		background: linear-gradient(155deg, rgba(255, 212, 107, 0.16), rgba(135, 215, 255, 0.08));
		border-color: rgba(255, 212, 107, 0.22);
		box-shadow: 0 18rpx 40rpx rgba(255, 212, 107, 0.1);
	}

	.role-name,
	.demo-title {
		display: block;
		font-size: 30rpx;
		font-weight: bold;
		color: #f3f8ff;
	}

	.brand-rail {
		display: grid;
		grid-template-columns: repeat(2, minmax(0, 1fr));
		gap: 14rpx;
		margin-top: 24rpx;
	}

	.brand-rail-item,
	.demo-panel,
	.result {
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

	.demo-panel {
		margin-top: 18rpx;
	}

	.panel-head {
		margin-bottom: 24rpx;
	}

	.tabs,
	.role-picker {
		display: flex;
		padding: 8rpx;
		border-radius: 999rpx;
		overflow: hidden;
	}

	.tabs {
		background: rgba(255, 255, 255, 0.04);
		border: 1px solid rgba(255, 255, 255, 0.08);
		margin-bottom: 22rpx;
	}

	.role-picker {
		gap: 10rpx;
		background: rgba(255, 255, 255, 0.03);
		border: 1px solid rgba(255, 255, 255, 0.06);
		margin-bottom: 22rpx;
	}

	.tab,
	.picker-chip {
		flex: 1;
		text-align: center;
		padding: 18rpx 0;
		border-radius: 999rpx;
		font-size: 28rpx;
		transition: all 0.24s ease;
	}

	.tab {
		color: #9ab4c9;
	}

	.active,
	.picker-chip-active {
		background: linear-gradient(135deg, #ffd46b, #fff0bc);
		color: #13202f;
		font-weight: bold;
		box-shadow: 0 14rpx 34rpx rgba(255, 212, 107, 0.18);
	}

	.picker-chip {
		color: #d8e8f7;
		background: rgba(255, 255, 255, 0.03);
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

		.role-picker {
			flex-wrap: wrap;
		}

		.picker-chip {
			min-width: 220rpx;
		}
	}
</style>
