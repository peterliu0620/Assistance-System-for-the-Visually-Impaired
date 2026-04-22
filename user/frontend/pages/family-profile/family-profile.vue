<template>
	<view :class="['page', themeClass, largeFontClass]">
		<view class="page-glow glow-one"></view>
		<view class="page-glow glow-two"></view>
		<view class="page-grid"></view>

		<view class="hero-shell">
			<view class="hero-copy">
				<text class="eyebrow">Care Profile</text>
				<text class="hero-title">家人可同时维护多位视障人士的照护档案</text>
				<text class="hero-desc">先添加视障人士账号，再切换当前对象分别填写基本信息、紧急联系人和药品/疾病注意事项。</text>

				<view class="hero-actions">
					<button class="action-btn primary" @click="saveAll">保存当前档案</button>
					<button class="action-btn ghost" @click="bindVisionUser">添加视障人士</button>
					<button class="action-btn ghost" @click="goRecords">查看记录</button>
				</view>
			</view>

			<view class="hero-stats">
				<view class="stat-card stat-card-strong">
					<text class="stat-label">资料完整度</text>
					<text class="stat-value">{{ completionText }}</text>
				</view>
				<view class="stat-card">
					<text class="stat-label">当前联系人</text>
					<text class="stat-value stat-value-small">{{ profile.emergencyContact.name || '待填写' }}</text>
				</view>
				<view class="stat-card">
					<text class="stat-label">常用药提醒</text>
					<text class="stat-value stat-value-small">{{ medicineShortText }}</text>
				</view>
				<view class="stat-card">
					<text class="stat-label">当前对象</text>
					<text class="stat-value stat-value-small">{{ activeBindingLabel }}</text>
				</view>
			</view>
		</view>

		<view class="section-panel">
			<view class="section-head">
				<view>
					<text class="section-kicker">绑定</text>
					<text class="section-title">添加与切换视障人士</text>
				</view>
				<text class="section-tag">{{ bindings.length ? `已绑定 ${bindings.length} 位` : '未绑定' }}</text>
			</view>

			<view class="form-grid">
				<input class="input-field" v-model.trim="bindingForm.visionUsername" placeholder="视障人士账号用户名" />
				<input class="input-field" v-model.trim="bindingForm.relationship" placeholder="关系，例如家人/子女/配偶" />
			</view>

			<view class="action-row">
				<button class="mini-btn primary-mini" @click="bindVisionUser">确认添加</button>
			</view>

			<view class="binding-switcher" v-if="bindings.length">
				<text class="switcher-label">当前正在编辑</text>
				<picker class="binding-picker" :range="bindingLabels" :value="activeBindingIndex" @change="onBindingChange">
					<view class="picker-value">{{ activeBindingLabel }}</view>
				</picker>
			</view>

			<view class="binding-list" v-if="bindings.length">
				<view
					v-for="binding in bindings"
					:key="binding.visionUserId"
					:class="['binding-card', binding.visionUserId === profile.bindingInfo.visionUserId ? 'binding-card-active' : '']"
					@click="switchBinding(binding.visionUserId)"
				>
					<text class="binding-name">{{ binding.visionNickname || binding.visionUsername }}</text>
					<text class="binding-meta">{{ binding.visionUsername }} · {{ binding.relationship || '家人' }}</text>
				</view>
			</view>
		</view>

		<view class="section-panel">
			<view class="section-head">
				<view>
					<text class="section-kicker">表单一</text>
					<text class="section-title">视障人士基本信息</text>
				</view>
				<text class="section-tag">{{ activeBindingLabel }}</text>
			</view>

			<view class="form-grid">
				<input class="input-field" v-model.trim="profile.basicInfo.name" placeholder="姓名" />
				<input class="input-field" v-model.trim="profile.basicInfo.gender" placeholder="性别" />
				<input class="input-field" v-model.trim="profile.basicInfo.age" placeholder="年龄" />
				<input class="input-field" v-model.trim="profile.basicInfo.visionLevel" placeholder="视力情况，例如全盲/低视力" />
			</view>
			<input class="input-field input-full" v-model.trim="profile.basicInfo.address" placeholder="常住地址" />
			<textarea class="textarea-field" v-model="profile.basicInfo.notes" placeholder="补充备注，例如出行习惯、居住情况等"></textarea>
		</view>

		<view class="section-panel">
			<view class="section-head">
				<view>
					<text class="section-kicker">表单二</text>
					<text class="section-title">紧急联系人信息</text>
				</view>
			</view>

			<view class="form-grid">
				<input class="input-field" v-model.trim="profile.emergencyContact.name" placeholder="联系人姓名" />
				<input class="input-field" v-model.trim="profile.emergencyContact.relation" placeholder="与视障人士关系" />
				<input class="input-field" v-model.trim="profile.emergencyContact.phone" placeholder="联系电话" />
				<input class="input-field" v-model.trim="profile.emergencyContact.backupPhone" placeholder="备用电话" />
			</view>
		</view>

		<view class="section-panel">
			<view class="section-head">
				<view>
					<text class="section-kicker">表单三</text>
					<text class="section-title">常用药品 / 疾病注意事项</text>
				</view>
			</view>

			<textarea class="textarea-field" v-model="profile.healthInfo.medicine" placeholder="常用药品、服药频次、放置位置"></textarea>
			<textarea class="textarea-field" v-model="profile.healthInfo.diseaseNote" placeholder="疾病史、就医提醒、外出注意事项"></textarea>
			<textarea class="textarea-field" v-model="profile.healthInfo.allergy" placeholder="过敏信息"></textarea>
			<textarea class="textarea-field" v-model="profile.healthInfo.reminder" placeholder="其他提醒，例如门锁、钥匙、陪诊安排"></textarea>
		</view>

		<view class="section-panel shortcut-panel">
			<view class="action-row">
				<button class="action-btn primary" @click="saveAll">保存当前档案</button>
				<button class="action-btn ghost" @click="goCenter">返回家属中心</button>
			</view>
		</view>

		<app-tab-bar current="family-profile" />
	</view>
</template>

<script>
	import { defineComponent } from 'vue';
	import AppTabBar from '../../components/app-tab-bar.vue';
	import { API_BASE } from '../../utils/api';
	import { getAuthUser, isFamilyRole } from '../../utils/auth';
	import { getActiveVisionUserId, setActiveVisionUserId } from '../../utils/family-binding';
	import { loadUserSettings } from '../../utils/user-settings';

	function emptyProfile() {
		return {
			bindings: [],
			bindingInfo: {
				visionUserId: null,
				visionUsername: '',
				visionNickname: '',
				relationship: ''
			},
			basicInfo: {
				name: '',
				gender: '',
				age: '',
				visionLevel: '',
				address: '',
				notes: ''
			},
			emergencyContact: {
				name: '',
				relation: '',
				phone: '',
				backupPhone: ''
			},
			healthInfo: {
				medicine: '',
				diseaseNote: '',
				allergy: '',
				reminder: ''
			}
		};
	}

	function normalizeProfile(data) {
		return {
			...emptyProfile(),
			...(data || {}),
			bindings: data && Array.isArray(data.bindings) ? data.bindings : []
		};
	}

	export default defineComponent({
		components: {
			AppTabBar
		},
		data() {
			return {
				settings: loadUserSettings(),
				profile: emptyProfile(),
				bindingForm: {
					visionUsername: '',
					relationship: '家人'
				}
			};
		},
		computed: {
			themeClass() {
				return this.settings.contrastMode === 'black-yellow' ? 'theme-yellow' : 'theme-gold';
			},
			largeFontClass() {
				return this.settings.extraLargeText ? 'font-large' : '';
			},
			bindings() {
				return Array.isArray(this.profile.bindings) ? this.profile.bindings : [];
			},
			bindingLabels() {
				return this.bindings.map(item => `${item.visionNickname || item.visionUsername} · ${item.relationship || '家人'}`);
			},
			activeBindingIndex() {
				return Math.max(0, this.bindings.findIndex(item => item.visionUserId === this.profile.bindingInfo.visionUserId));
			},
			activeBindingLabel() {
				return this.profile.bindingInfo.visionNickname || this.profile.bindingInfo.visionUsername || '待选择';
			},
			completionText() {
				let count = 0;
				if (this.profile.basicInfo.name && this.profile.basicInfo.age) count += 1;
				if (this.profile.emergencyContact.name && this.profile.emergencyContact.phone) count += 1;
				if (this.profile.healthInfo.medicine || this.profile.healthInfo.diseaseNote) count += 1;
				return `${count}/3`;
			},
			medicineShortText() {
				const medicine = this.profile.healthInfo.medicine || '';
				return medicine ? medicine.slice(0, 14) + (medicine.length > 14 ? '...' : '') : '待填写';
			}
		},
		onLoad() {
			this.ensureFamilyRole();
		},
		onShow() {
			this.settings = loadUserSettings();
			this.loadProfile(getActiveVisionUserId());
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
			applyProfile(responseData) {
				this.profile = normalizeProfile(responseData);
				if (this.profile.bindingInfo.visionUserId) {
					setActiveVisionUserId(this.profile.bindingInfo.visionUserId);
				}
			},
			loadProfile(visionUserId = null) {
				const user = getAuthUser();
				const data = {
					familyUserId: user.id
				};
				if (visionUserId) {
					data.visionUserId = visionUserId;
				}
				uni.request({
					url: `${API_BASE}/api/family/profile`,
					method: 'GET',
					data,
					success: (res) => {
						if (res.statusCode === 200) {
							this.applyProfile(res.data);
							this.bindingForm.relationship = '家人';
							this.bindingForm.visionUsername = '';
							return;
						}
						const data = res.data || {};
						uni.showToast({ title: data.message || '资料加载失败', icon: 'none' });
					},
					fail: () => {
						uni.showToast({ title: '资料加载失败', icon: 'none' });
					}
				});
			},
			bindVisionUser() {
				const user = getAuthUser();
				if (!this.bindingForm.visionUsername) {
					uni.showToast({ title: '请先输入视障人士账号', icon: 'none' });
					return;
				}
				uni.request({
					url: `${API_BASE}/api/family/bind-vision?familyUserId=${user.id}`,
					method: 'POST',
					header: {
						'Content-Type': 'application/json'
					},
					data: this.bindingForm,
					success: (res) => {
						if (res.statusCode === 200) {
							this.applyProfile(res.data);
							this.bindingForm.visionUsername = '';
							this.bindingForm.relationship = '家人';
							uni.showToast({ title: '添加成功', icon: 'success' });
							return;
						}
						const data = res.data || {};
						uni.showToast({ title: data.message || '添加失败', icon: 'none' });
					},
					fail: () => {
						uni.showToast({ title: '添加失败', icon: 'none' });
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
				if (!visionUserId || visionUserId === this.profile.bindingInfo.visionUserId) {
					return;
				}
				this.loadProfile(visionUserId);
			},
			saveAll() {
				const user = getAuthUser();
				const visionUserId = this.profile.bindingInfo.visionUserId;
				if (!visionUserId) {
					uni.showToast({ title: '请先添加视障人士', icon: 'none' });
					return;
				}
				uni.request({
					url: `${API_BASE}/api/family/profile?familyUserId=${user.id}&visionUserId=${visionUserId}`,
					method: 'POST',
					header: {
						'Content-Type': 'application/json'
					},
					data: this.profile,
					success: (res) => {
						if (res.statusCode === 200) {
							this.applyProfile(res.data);
							uni.showToast({ title: '登记信息已保存', icon: 'success' });
							return;
						}
						const data = res.data || {};
						uni.showToast({ title: data.message || '保存失败', icon: 'none' });
					},
					fail: () => {
						uni.showToast({ title: '保存失败', icon: 'none' });
					}
				});
			},
			goRecords() {
				uni.redirectTo({
					url: '/pages/family-records/family-records'
				});
			},
			goCenter() {
				uni.redirectTo({
					url: '/pages/family-center/family-center'
				});
			}
		}
	});
</script>

<style>
	@import '../../styles/experience-shell.css';

	.form-grid {
		display: grid;
		grid-template-columns: repeat(2, minmax(0, 1fr));
		gap: 14rpx;
	}

	.input-field,
	.textarea-field,
	.binding-picker,
	.binding-card {
		width: 100%;
		box-sizing: border-box;
		margin-top: 16rpx;
		padding: 22rpx;
		border-radius: 22rpx;
		background: rgba(255, 255, 255, 0.74);
		border: 1px solid rgba(255, 255, 255, 0.86);
		color: var(--text-main);
		font-size: 28rpx;
		box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.78);
	}

	.input-field {
		height: 88rpx;
	}

	.input-full {
		display: block;
	}

	.textarea-field {
		min-height: 180rpx;
		line-height: 1.65;
	}

	.binding-switcher {
		margin-top: 12rpx;
	}

	.switcher-label,
	.binding-meta {
		display: block;
		font-size: 24rpx;
		line-height: 1.6;
		color: var(--text-soft);
	}

	.picker-value,
	.binding-name {
		font-size: 28rpx;
		font-weight: bold;
		color: var(--text-main);
	}

	.binding-list {
		margin-top: 12rpx;
		display: grid;
		grid-template-columns: repeat(2, minmax(0, 1fr));
		gap: 14rpx;
	}

	.binding-card {
		margin-top: 0;
		background: linear-gradient(180deg, rgba(255, 255, 255, 0.82), rgba(245, 249, 255, 0.72));
		box-shadow: 0 16rpx 40rpx rgba(79, 118, 172, 0.08);
	}

	.binding-card-active {
		border-color: rgba(58, 109, 185, 0.38);
		box-shadow: 0 20rpx 44rpx rgba(58, 109, 185, 0.14);
	}

	@media screen and (max-width: 720px) {
		.form-grid,
		.binding-list {
			grid-template-columns: 1fr;
		}
	}
</style>
