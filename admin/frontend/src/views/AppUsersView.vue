<template>
	<div class="user-page">
		<section class="toolbar-panel">
			<div>
				<div class="hero-kicker">APP USER OPS</div>
				<h2>集中管理应用端用户。</h2>
				<p>
					支持管理员对 app 用户进行查询、新增、编辑、删除，并直接控制账号启用状态。
				</p>
				<div class="toolbar-insights">
					<div class="insight-pill">
						<span class="insight-dot active"></span>
						启用账号 {{ activeCount }}
					</div>
					<div class="insight-pill">
						<span class="insight-dot muted"></span>
						禁用账号 {{ disabledCount }}
					</div>
					<div class="insight-pill">
						<span class="insight-dot warm"></span>
						最近 7 天新增 {{ recentCount }}
					</div>
				</div>
			</div>
			<div class="toolbar-actions">
				<a-input-search
					v-model:value="keyword"
					class="search-box"
					placeholder="搜索用户名 / 昵称 / 手机号 / 邮箱"
					allow-clear
					enter-button="搜索"
					@search="loadUsers"
				/>
				<a-button size="large" @click="resetSearch">重置</a-button>
				<a-button type="primary" size="large" @click="openCreateModal">新增用户</a-button>
			</div>
		</section>

		<section class="metrics-grid">
			<div class="metric-panel accent">
				<div class="metric-label">用户总量</div>
				<div class="metric-value">{{ users.length }}</div>
				<div class="metric-meta">当前后台可直接维护的 app 账号总数</div>
			</div>
			<div class="metric-panel">
				<div class="metric-label">登录活跃</div>
				<div class="metric-value">{{ loggedInCount }}</div>
				<div class="metric-meta">至少登录过一次的用户数量</div>
			</div>
			<div class="metric-panel">
				<div class="metric-label">资料完整</div>
				<div class="metric-value">{{ profileCompleteCount }}</div>
				<div class="metric-meta">同时填写手机号和邮箱的用户</div>
			</div>
		</section>

		<a-card :bordered="false" class="surface-card table-card">
			<template #title>
				<div class="table-headline">
					<div>
						<div class="table-title">用户列表</div>
						<div class="table-subtitle">支持快速检索、编辑资料与状态维护</div>
					</div>
					<a-tag color="cyan">{{ loading ? '加载中' : `共 ${users.length} 人` }}</a-tag>
				</div>
			</template>
			<a-table
				row-key="id"
				:loading="loading"
				:data-source="users"
				:columns="columns"
				:pagination="{ pageSize: 8, showSizeChanger: false }"
				:scroll="{ x: 980 }"
			>
				<template #bodyCell="{ column, record }">
					<template v-if="column.key === 'username'">
						<div class="identity-cell">
							<div class="identity-avatar">
								{{ record.username?.slice(0, 1)?.toUpperCase() || 'U' }}
							</div>
							<div>
								<div class="identity-name">{{ record.username }}</div>
								<div class="identity-meta">ID {{ record.id }}</div>
							</div>
						</div>
					</template>

					<template v-else-if="column.key === 'nickname'">
						<div>
							<div class="identity-name">{{ record.nickname }}</div>
							<div class="identity-meta">
								{{ record.email || '未填写邮箱' }}
							</div>
						</div>
					</template>

					<template v-else-if="column.key === 'phone'">
						<span class="muted-cell">{{ record.phone || '--' }}</span>
					</template>

					<template v-else-if="column.key === 'email'">
						<span class="muted-cell">{{ record.email || '--' }}</span>
					</template>

					<template v-else-if="column.key === 'status'">
						<div class="status-cell">
							<a-tag :color="record.status === 1 ? 'green' : 'red'" class="status-tag">
								{{ record.status === 1 ? '启用' : '禁用' }}
							</a-tag>
						</div>
					</template>

					<template v-else-if="column.key === 'lastLoginAt'">
						<span class="muted-cell">{{ formatDateTime(record.lastLoginAt) }}</span>
					</template>

					<template v-else-if="column.key === 'createdAt'">
						<span class="muted-cell">{{ formatDateTime(record.createdAt) }}</span>
					</template>

					<template v-else-if="column.key === 'action'">
						<div class="action-row">
							<a-button type="link" @click="openEditModal(record)">编辑</a-button>
							<a-popconfirm
								title="确认删除该用户吗？"
								ok-text="删除"
								cancel-text="取消"
								@confirm="handleDelete(record)"
							>
								<a-button type="link" danger>删除</a-button>
							</a-popconfirm>
						</div>
					</template>
				</template>
				<template #emptyText>
					<div class="table-empty">
						<div class="empty-orb">U</div>
						<div class="empty-title">还没有匹配到用户</div>
						<div class="empty-desc">试试更换关键词，或者直接创建一个新的 app 用户。</div>
						<a-button type="primary" @click="openCreateModal">立即新增</a-button>
					</div>
				</template>
			</a-table>
		</a-card>

		<a-modal
			v-model:open="modalOpen"
			:title="modalMode === 'create' ? '新增 App 用户' : '编辑 App 用户'"
			wrap-class-name="user-modal-wrap"
			:confirm-loading="submitLoading"
			:mask-closable="false"
			@ok="handleSubmit"
			@cancel="handleCancel"
		>
			<a-form
				ref="formRef"
				:model="formState"
				:label-col="{ span: 5 }"
				:wrapper-col="{ span: 18 }"
			>
				<a-form-item
					v-if="modalMode === 'create'"
					label="用户名"
					name="username"
					:rules="[{ required: true, message: '请输入用户名' }]"
				>
					<a-input v-model:value="formState.username" placeholder="4-50 位用户名" />
				</a-form-item>

				<a-form-item
					:label="modalMode === 'create' ? '密码' : '密码重置'"
					name="password"
					:rules="passwordRules"
				>
					<a-input-password
						v-model:value="formState.password"
						:placeholder="modalMode === 'create' ? '6-20 位密码' : '留空则不修改密码'"
					/>
				</a-form-item>

				<a-form-item
					label="昵称"
					name="nickname"
					:rules="[{ required: true, message: '请输入昵称' }]"
				>
					<a-input v-model:value="formState.nickname" placeholder="请输入昵称" />
				</a-form-item>

				<a-form-item
					label="手机号"
					name="phone"
					:rules="[{ pattern: PHONE_PATTERN, message: '手机号格式不正确' }]"
				>
					<a-input v-model:value="formState.phone" placeholder="选填，11 位手机号" />
				</a-form-item>

				<a-form-item
					label="邮箱"
					name="email"
					:rules="[{ pattern: EMAIL_PATTERN, message: '邮箱格式不正确' }]"
				>
					<a-input v-model:value="formState.email" placeholder="选填，邮箱地址" />
				</a-form-item>

				<a-form-item
					label="状态"
					name="status"
					:rules="[{ required: true, message: '请选择状态' }]"
				>
					<a-radio-group v-model:value="formState.status">
						<a-radio :value="1">启用</a-radio>
						<a-radio :value="0">禁用</a-radio>
					</a-radio-group>
				</a-form-item>
			</a-form>
		</a-modal>
	</div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { message, type TableColumnsType } from 'ant-design-vue';
import type { FormInstance, Rule } from 'ant-design-vue/es/form';
import { createAppUser, deleteAppUser, fetchAppUsers, updateAppUser } from '@/api/appUser';
import { formatDateTime } from '@/lib/format';
import type { AppUser, AppUserPayload } from '@/types/models';

const PHONE_PATTERN = /^$|^1\d{10}$/;
const EMAIL_PATTERN = /^$|^[\w.-]+@[\w.-]+\.[A-Za-z]{2,}$/;
type ModalMode = 'create' | 'edit';

interface AppUserFormState {
	username: string;
	password: string;
	nickname: string;
	phone: string;
	email: string;
	status: number;
}

const DEFAULT_FORM_STATE: AppUserFormState = {
	username: '',
	password: '',
	nickname: '',
	phone: '',
	email: '',
	status: 1
};

function createDefaultFormState(): AppUserFormState {
	return { ...DEFAULT_FORM_STATE };
}

function normalizeFormPayload(): AppUserPayload {
	return {
		password: formState.password,
		nickname: formState.nickname.trim(),
		phone: formState.phone.trim(),
		email: formState.email.trim(),
		status: formState.status
	};
}

const loading = ref(false);
const submitLoading = ref(false);
const users = ref<AppUser[]>([]);
const keyword = ref('');
const modalOpen = ref(false);
const modalMode = ref<ModalMode>('create');
const currentId = ref<number | null>(null);
const formRef = ref<FormInstance>();

const formState = reactive<AppUserFormState>(createDefaultFormState());

const columns: TableColumnsType<AppUser> = [
	{
		title: '用户名',
		dataIndex: 'username',
		key: 'username',
		width: 150
	},
	{
		title: '昵称',
		dataIndex: 'nickname',
		key: 'nickname',
		width: 160
	},
	{
		title: '手机号',
		dataIndex: 'phone',
		key: 'phone',
		width: 150
	},
	{
		title: '邮箱',
		dataIndex: 'email',
		key: 'email',
		width: 220
	},
	{
		title: '状态',
		dataIndex: 'status',
		key: 'status',
		width: 100
	},
	{
		title: '最近登录',
		dataIndex: 'lastLoginAt',
		key: 'lastLoginAt',
		width: 180
	},
	{
		title: '创建时间',
		dataIndex: 'createdAt',
		key: 'createdAt',
		width: 180
	},
	{
		title: '操作',
		key: 'action',
		fixed: 'right',
		width: 130
	}
];

const passwordRules = computed<Rule[]>(() => {
	if (modalMode.value === 'create') {
		return [
			{ required: true, message: '请输入密码' },
			{ min: 6, max: 20, message: '密码长度需在6-20之间' }
		];
	}

	return [
		{
			validator: async (_rule, value?: string) => {
				if (!value) {
					return Promise.resolve();
				}
				if (value.length < 6 || value.length > 20) {
					return Promise.reject(new Error('密码长度需在6-20之间'));
				}
				return Promise.resolve();
			}
		}
	];
});

const activeCount = computed(() => users.value.filter((item) => item.status === 1).length);
const disabledCount = computed(() => users.value.filter((item) => item.status !== 1).length);
const loggedInCount = computed(() => users.value.filter((item) => item.lastLoginAt).length);
const profileCompleteCount = computed(() => users.value.filter((item) => item.phone && item.email).length);
const recentCount = computed(() => {
	const now = Date.now();
	const sevenDays = 7 * 24 * 60 * 60 * 1000;
	return users.value.filter((item) => {
		if (!item.createdAt) {
			return false;
		}
		const createdAt = new Date(item.createdAt).getTime();
		return !Number.isNaN(createdAt) && now - createdAt <= sevenDays;
	}).length;
});

onMounted(() => {
	loadUsers();
});

async function loadUsers() {
	loading.value = true;
	try {
		users.value = await fetchAppUsers(keyword.value.trim());
	} catch (error) {
		message.error(error instanceof Error ? error.message : '加载用户失败');
	} finally {
		loading.value = false;
	}
}

function resetSearch() {
	keyword.value = '';
	loadUsers();
}

function openCreateModal() {
	modalMode.value = 'create';
	currentId.value = null;
	resetForm();
	modalOpen.value = true;
}

function openEditModal(record: AppUser) {
	modalMode.value = 'edit';
	currentId.value = record.id;
	resetForm();
	Object.assign(formState, {
		username: record.username,
		nickname: record.nickname,
		phone: record.phone || '',
		email: record.email || '',
		status: record.status
	});
	modalOpen.value = true;
}

function handleCancel() {
	modalOpen.value = false;
	resetForm();
}

async function handleSubmit() {
	try {
		await formRef.value?.validate();
		submitLoading.value = true;

		if (modalMode.value === 'create') {
			await createAppUser(buildCreatePayload());
			message.success('用户创建成功');
		} else {
			await updateAppUser(currentId.value as number, buildUpdatePayload());
			message.success('用户更新成功');
		}

		modalOpen.value = false;
		resetForm();
		await loadUsers();
	} catch (error) {
		message.error(error instanceof Error ? error.message : '保存用户失败');
	} finally {
		submitLoading.value = false;
	}
}

async function handleDelete(record: AppUser) {
	try {
		await deleteAppUser(record.id);
		message.success(`已删除用户 ${record.username}`);
		await loadUsers();
	} catch (error) {
		message.error(error instanceof Error ? error.message : '删除用户失败');
	}
}

function buildCreatePayload(): AppUserPayload {
	return {
		username: formState.username.trim(),
		...normalizeFormPayload()
	};
}

function buildUpdatePayload(): AppUserPayload {
	return normalizeFormPayload();
}

function resetForm(): void {
	formRef.value?.resetFields();
	Object.assign(formState, createDefaultFormState());
}
</script>

<style scoped>
@import '@/styles/ops-surface.css';

.toolbar-panel {
	background: linear-gradient(135deg, #0c2037, #123c67 52%, #0f766e 100%);
}

.metrics-grid {
	grid-template-columns: repeat(3, minmax(0, 1fr));
}

.status-tag {
	border-radius: 999px;
	padding-inline: 10px;
}

.surface-card :deep(.ant-card-body) {
	padding: 16px 20px;
}

.user-modal-wrap :deep(.ant-modal-content) {
	border-radius: 24px;
	overflow: hidden;
}

.user-modal-wrap :deep(.ant-modal-header) {
	border-bottom: 1px solid rgba(148, 163, 184, 0.12);
}
</style>
