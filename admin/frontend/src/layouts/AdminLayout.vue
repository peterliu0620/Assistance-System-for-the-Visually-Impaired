<template>
	<a-layout class="admin-shell">
		<div class="shell-glow shell-glow-primary"></div>
		<div class="shell-glow shell-glow-secondary"></div>

		<a-layout-sider
			v-model:collapsed="collapsed"
			:width="272"
			class="admin-sider"
			breakpoint="lg"
			collapsible
		>
			<div class="brand-block">
				<div class="brand-mark">VG</div>
				<div v-if="!collapsed" class="brand-copy">
					<div class="brand-title">Vision Guard</div>
					<div class="brand-subtitle">视障守护管理台</div>
				</div>
			</div>

			<div v-if="!collapsed" class="sider-panel">
				<div class="panel-kicker">Workspace</div>
				<div class="panel-title">{{ pageTitle }}</div>
				<div class="panel-meta">{{ pageDescription }}</div>
			</div>

			<div v-if="!collapsed" class="nav-section-label">总览</div>
			<a-menu :selected-keys="selectedKeys" theme="dark" mode="inline" class="nav-menu">
				<a-menu-item key="/dashboard" @click="router.push('/dashboard')">
					<template #icon>
						<dashboard-outlined />
					</template>
					概览
				</a-menu-item>
			</a-menu>

			<div v-if="!collapsed" class="nav-section-label">核心模块</div>
			<a-menu :selected-keys="selectedKeys" theme="dark" mode="inline" class="nav-menu nav-menu-compact">
				<a-menu-item key="/app-users" @click="router.push('/app-users')">
					<template #icon>
						<user-outlined />
					</template>
					App 用户
				</a-menu-item>
				<a-menu-item key="/family-bindings" @click="router.push('/family-bindings')">
					<template #icon>
						<team-outlined />
					</template>
					家属绑定
				</a-menu-item>
				<a-menu-item key="/shared-logs" @click="router.push('/shared-logs')">
					<template #icon>
						<file-search-outlined />
					</template>
					共享日志
				</a-menu-item>
				<a-menu-item key="/medicine-profiles" @click="router.push('/medicine-profiles')">
					<template #icon>
						<medicine-box-outlined />
					</template>
					药品档案
				</a-menu-item>
			</a-menu>

			<div v-if="!collapsed" class="sider-summary">
				<div class="summary-title">当前工作台</div>
				<div class="summary-value">{{ pageTitle }}</div>
				<div class="summary-meta">统一处理用户、家属、药品和日志协同，优先服务守护闭环。</div>
			</div>
		</a-layout-sider>

		<a-layout class="admin-main">
			<a-layout-header class="admin-header">
				<div class="header-copy">
					<div class="header-kicker">Safety Operations</div>
					<div class="header-title">{{ pageTitle }}</div>
					<div class="header-desc">{{ pageDescription }}</div>
				</div>

				<div class="header-actions">
					<div class="header-chip-list">
						<div class="header-chip">
							<span class="chip-dot chip-dot-live"></span>
							核心安全场景
						</div>
						<div class="header-chip">
							<span class="chip-dot"></span>
							后台直绑协同
						</div>
					</div>
					<div class="profile-chip">
						<div class="profile-label">当前账号</div>
						<div class="profile-name">{{ profileName }}</div>
						<div class="profile-role">{{ profile?.username || 'admin' }}</div>
					</div>
					<a-button type="primary" class="logout-btn" @click="handleLogout">退出登录</a-button>
				</div>
			</a-layout-header>

			<a-layout-content class="admin-content">
				<router-view />
			</a-layout-content>
		</a-layout>
	</a-layout>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { DashboardOutlined, FileSearchOutlined, MedicineBoxOutlined, TeamOutlined, UserOutlined } from '@ant-design/icons-vue';
import { clearAuth, getAdminProfile } from '@/lib/auth';

const route = useRoute();
const router = useRouter();
const collapsed = ref(false);
const profile = getAdminProfile();

const selectedKeys = computed(() => [route.path]);

const pageTitle = computed(() => {
	if (route.path.startsWith('/login')) {
		return '管理员登录';
	}

	if (route.path.startsWith('/app-users')) {
		return 'App 用户管理';
	}
	if (route.path.startsWith('/family-bindings')) {
		return '家属绑定管理';
	}
	if (route.path.startsWith('/shared-logs')) {
		return '共享日志摘要';
	}
	if (route.path.startsWith('/medicine-profiles')) {
		return '药品档案管理';
	}

	return '系统概览';
});

const pageDescription = computed(() => {
	if (route.path.startsWith('/app-users')) {
		return '维护应用用户账户、状态与基础资料。';
	}
	if (route.path.startsWith('/family-bindings')) {
		return '配置家属与视障用户的协同关系。';
	}
	if (route.path.startsWith('/shared-logs')) {
		return '查看关键生活日志与安全预警摘要。';
	}
	if (route.path.startsWith('/medicine-profiles')) {
		return '预录药品档案，支撑扫描优先播报与强预警。';
	}
	return '围绕视障辅助、家属协同与药品安全进行统一管理。';
});

const profileName = computed(() => profile?.nickname || '管理员');

function handleLogout() {
	clearAuth();
	router.replace('/login');
}
</script>

<style scoped>
.admin-shell {
	position: relative;
	min-height: 100vh;
	background:
		radial-gradient(1200px 700px at -10% -10%, rgba(20, 184, 166, 0.18), transparent 60%),
		radial-gradient(900px 700px at 100% 0%, rgba(249, 115, 22, 0.16), transparent 55%),
		linear-gradient(180deg, #08111f 0%, #0b1424 42%, #08111a 100%);
	overflow: hidden;
}

.shell-glow {
	position: absolute;
	border-radius: 999px;
	filter: blur(80px);
	pointer-events: none;
	opacity: 0.6;
}

.shell-glow-primary {
	top: -120px;
	right: -120px;
	width: 360px;
	height: 360px;
	background: rgba(45, 212, 191, 0.24);
}

.shell-glow-secondary {
	left: -140px;
	bottom: 120px;
	width: 420px;
	height: 420px;
	background: rgba(249, 115, 22, 0.18);
}

.admin-sider {
	position: relative;
	z-index: 1;
	margin: 20px 0 20px 20px;
	padding: 18px 14px;
	border: 1px solid rgba(148, 163, 184, 0.14);
	border-radius: 28px;
	background: linear-gradient(180deg, rgba(10, 18, 32, 0.94), rgba(9, 16, 28, 0.86));
	box-shadow: 0 30px 80px rgba(2, 6, 23, 0.45);
}

:deep(.admin-sider .ant-layout-sider-children) {
	display: flex;
	flex-direction: column;
	height: 100%;
}

:deep(.admin-sider .ant-layout-sider-trigger) {
	margin: 10px 8px 0;
	border-radius: 16px;
	background: rgba(255, 255, 255, 0.06);
}

.brand-block {
	display: flex;
	align-items: center;
	gap: 14px;
	padding: 8px 10px 18px;
}

.brand-mark {
	display: grid;
	place-items: center;
	width: 46px;
	height: 46px;
	border-radius: 16px;
	font-size: 15px;
	font-weight: 700;
	letter-spacing: 0.08em;
	color: #07111e;
	background: linear-gradient(135deg, #67e8f9, #facc15);
	box-shadow: 0 20px 36px rgba(103, 232, 249, 0.18);
}

.brand-title {
	font-size: 16px;
	font-weight: 700;
	color: #f8fafc;
}

.brand-subtitle {
	margin-top: 4px;
	font-size: 12px;
	letter-spacing: 0.08em;
	text-transform: uppercase;
	color: rgba(148, 163, 184, 0.8);
}

.sider-panel,
.sider-summary {
	margin: 10px 8px 18px;
	padding: 18px;
	border-radius: 22px;
	background: rgba(255, 255, 255, 0.04);
	border: 1px solid rgba(148, 163, 184, 0.12);
}

.panel-kicker,
.summary-title,
.nav-section-label,
.header-kicker,
.profile-label {
	font-size: 11px;
	font-weight: 600;
	letter-spacing: 0.16em;
	text-transform: uppercase;
	color: rgba(148, 163, 184, 0.72);
}

.panel-title,
.summary-value {
	margin-top: 10px;
	font-size: 20px;
	font-weight: 700;
	line-height: 1.2;
	color: #f8fafc;
}

.panel-meta,
.summary-meta,
.header-desc,
.profile-role {
	margin-top: 8px;
	font-size: 13px;
	line-height: 1.7;
	color: rgba(203, 213, 225, 0.78);
}

.nav-section-label {
	margin: 0 14px 10px;
}

.nav-menu {
	margin-bottom: 12px;
	background: transparent;
	border-inline-end: none;
}

:deep(.nav-menu .ant-menu-item) {
	height: 48px;
	margin: 6px 0;
	border-radius: 16px;
	color: rgba(226, 232, 240, 0.82);
}

:deep(.nav-menu .ant-menu-item:hover) {
	color: #ffffff;
	background: rgba(255, 255, 255, 0.06);
}

:deep(.nav-menu .ant-menu-item-selected) {
	background: linear-gradient(135deg, rgba(20, 184, 166, 0.22), rgba(59, 130, 246, 0.18));
	color: #f8fafc;
}

.admin-main {
	position: relative;
	z-index: 1;
	background: transparent;
}

.admin-header {
	display: flex;
	align-items: flex-start;
	justify-content: space-between;
	gap: 18px;
	height: auto;
	margin: 20px 20px 0;
	padding: 24px 28px;
	border: 1px solid rgba(148, 163, 184, 0.14);
	border-radius: 28px;
	background: linear-gradient(160deg, rgba(10, 18, 32, 0.8), rgba(9, 16, 28, 0.62));
	backdrop-filter: blur(18px);
	box-shadow: 0 30px 80px rgba(2, 6, 23, 0.28);
}

.header-title {
	margin-top: 8px;
	font-size: 34px;
	font-weight: 700;
	line-height: 1.12;
	color: #f8fafc;
}

.header-actions {
	display: flex;
	align-items: center;
	flex-wrap: wrap;
	justify-content: flex-end;
	gap: 12px;
}

.header-chip-list {
	display: flex;
	flex-wrap: wrap;
	gap: 10px;
}

.header-chip {
	display: inline-flex;
	align-items: center;
	gap: 8px;
	padding: 8px 12px;
	border-radius: 999px;
	background: rgba(255, 255, 255, 0.05);
	border: 1px solid rgba(148, 163, 184, 0.14);
	color: rgba(226, 232, 240, 0.88);
	font-size: 12px;
}

.chip-dot {
	width: 8px;
	height: 8px;
	border-radius: 999px;
	background: #38bdf8;
	box-shadow: 0 0 0 4px rgba(56, 189, 248, 0.18);
}

.chip-dot-live {
	background: #34d399;
	box-shadow: 0 0 0 4px rgba(52, 211, 153, 0.18);
}

.profile-chip {
	min-width: 0;
	padding: 8px 12px;
	border-radius: 14px;
	background: rgba(255, 255, 255, 0.05);
	border: 1px solid rgba(148, 163, 184, 0.14);
	display: flex;
	flex-direction: column;
	gap: 2px;
}

.profile-name {
	margin-top: 0;
	font-size: 13px;
	font-weight: 700;
	color: #f8fafc;
	line-height: 1.2;
}

.profile-label,
.profile-role {
	font-size: 11px;
	line-height: 1.2;
}

.logout-btn {
	height: 40px;
	padding-inline: 18px;
	border-radius: 12px;
	border: none;
	background: linear-gradient(135deg, #14b8a6, #0f766e);
	box-shadow: 0 16px 32px rgba(20, 184, 166, 0.22);
}

.admin-content {
	padding: 20px;
	background: transparent;
}

@media (max-width: 992px) {
	.admin-sider {
		margin-right: 0;
	}

	.admin-header {
		flex-direction: column;
	}

	.header-actions {
		width: 100%;
		justify-content: flex-start;
	}
}
</style>
