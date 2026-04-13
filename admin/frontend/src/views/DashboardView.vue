<template>
	<div class="dashboard-page">
		<section class="hero-panel">
			<div class="hero-copy">
				<div class="panel-kicker">Safety Control Center</div>
				<h2>{{ greetingTitle }}</h2>
				<p>
					后台不再只是“登录后能管理数据”的页面，而是把视障用户辅助、家属协同、药品安全和日志追踪收进同一张可演示的运营看板。
				</p>
				<div class="toolbar-insights">
					<div class="insight-pill">
						<span class="insight-dot active"></span>
						家属安全桥梁
					</div>
					<div class="insight-pill">
						<span class="insight-dot warm"></span>
						药品强预警
					</div>
					<div class="insight-pill">
						<span class="insight-dot muted"></span>
						日志共享摘要
					</div>
				</div>
			</div>

			<div class="hero-stats">
				<div class="stat-card highlight">
					<div class="stat-label">管理员账号</div>
					<div class="stat-value">{{ loading ? '--' : stats.adminName }}</div>
					<div class="stat-meta">当前活跃后台身份</div>
				</div>
				<div class="stat-card">
					<div class="stat-label">当前工作流</div>
					<div class="stat-value">4 大模块</div>
					<div class="stat-meta">用户、家属、日志、药品</div>
				</div>
				<div class="stat-card">
					<div class="stat-label">首要目标</div>
					<div class="stat-value">守护闭环</div>
					<div class="stat-meta">优先强调安全与协同</div>
				</div>
			</div>
		</section>

		<section class="metrics-grid">
			<div class="metric-panel accent">
				<div class="metric-label">当前身份</div>
				<div class="metric-value">{{ profile?.nickname || '管理员' }}</div>
				<div class="metric-meta">统一使用单管理员模型，适合作业演示和课程答辩。</div>
			</div>
			<div class="metric-panel">
				<div class="metric-label">后台端口</div>
				<div class="metric-value">8081</div>
				<div class="metric-meta">管理端接口统一经由 `/api/admin/*` 暴露。</div>
			</div>
			<div class="metric-panel">
				<div class="metric-label">平台能力</div>
				<div class="metric-value">用户 + 家属</div>
				<div class="metric-meta">覆盖应用用户维护与家属协同两条核心链路。</div>
			</div>
			<div class="metric-panel">
				<div class="metric-label">安全主线</div>
				<div class="metric-value">药品预警</div>
				<div class="metric-meta">支持档案优先播报、过期预警、重复扫码预警。</div>
			</div>
		</section>

		<section class="dashboard-grid">
			<a-card :bordered="false" class="surface-card command-card">
				<template #title>
					<div class="card-title-wrap">
						<span class="card-title">核心能力总览</span>
						<span class="card-subtitle">当前平台最适合演示的能力路径</span>
					</div>
				</template>
				<a-skeleton active :loading="loading" :paragraph="{ rows: 4 }">
					<div class="command-list">
						<div v-for="item in highlights" :key="item.title" class="command-item">
							<div class="command-index">{{ item.index }}</div>
							<div class="command-copy">
								<div class="command-name">{{ item.title }}</div>
								<div class="command-meta">{{ item.description }}</div>
							</div>
							<a-tag :color="item.color">{{ item.tag }}</a-tag>
						</div>
					</div>
				</a-skeleton>
			</a-card>

			<a-card :bordered="false" class="surface-card shortcut-card">
				<template #title>
					<div class="card-title-wrap">
						<span class="card-title">工作台捷径</span>
						<span class="card-subtitle">从首页直接进入主要模块</span>
					</div>
				</template>
				<div class="shortcut-list">
					<div
						v-for="shortcut in shortcuts"
						:key="shortcut.title"
						class="shortcut-item"
						@click="goTo(shortcut.path)"
					>
						<div class="shortcut-copy">
							<div class="shortcut-title">{{ shortcut.title }}</div>
							<div class="shortcut-meta">{{ shortcut.description }}</div>
						</div>
						<a-tag :color="shortcut.color">{{ shortcut.tag }}</a-tag>
					</div>
				</div>
			</a-card>
		</section>

		<section class="story-grid">
			<div class="story-panel">
				<div class="panel-kicker">Demo Flow</div>
				<div class="story-title">答辩时可以怎么讲这套系统</div>
				<div class="story-list">
					<div class="story-item">
						<div class="story-step">01</div>
						<div class="story-copy">
							<div class="story-name">先看 App 用户</div>
							<div class="story-meta">说明后台先管理真实使用者，为后续家属绑定和日志留存打底。</div>
						</div>
					</div>
					<div class="story-item">
						<div class="story-step">02</div>
						<div class="story-copy">
							<div class="story-name">再看家属绑定</div>
							<div class="story-meta">强调系统不仅服务视障用户，还支持家属远程关注关键事件。</div>
						</div>
					</div>
					<div class="story-item">
						<div class="story-step">03</div>
						<div class="story-copy">
							<div class="story-name">最后讲药品和日志</div>
							<div class="story-meta">把风险提醒与共享记录合并成完整的安全闭环。</div>
						</div>
					</div>
				</div>
			</div>

			<div class="story-panel emphasis">
				<div class="panel-kicker">Platform Focus</div>
				<div class="story-title">后台优化重点</div>
				<div class="focus-stack">
					<div class="focus-item">
						<div class="focus-name">信息更聚焦</div>
						<div class="focus-meta">先讲现在能做什么，再讲可以进哪里，不让首页变成零散卡片墙。</div>
					</div>
					<div class="focus-item">
						<div class="focus-name">导航更清楚</div>
						<div class="focus-meta">左侧明确工作台角色，顶部聚焦当前页面职责。</div>
					</div>
					<div class="focus-item">
						<div class="focus-name">演示更顺手</div>
						<div class="focus-meta">答辩或展示时，管理员端一进来就能讲清平台价值与模块关系。</div>
					</div>
				</div>
			</div>
		</section>
	</div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { getAdminProfile } from '@/lib/auth';

interface DashboardStats {
	adminName: string;
}

interface HighlightItem {
	index: string;
	title: string;
	description: string;
	tag: string;
	color: string;
}

interface ShortcutItem {
	title: string;
	description: string;
	path: string;
	tag: string;
	color: string;
}

const router = useRouter();
const loading = ref(false);
const profile = getAdminProfile();
const stats = reactive<DashboardStats>({
	adminName: 'admin'
});

const highlights: HighlightItem[] = [
	{
		index: '01',
		title: 'App 用户统一管理',
		description: '支持增删改查、状态维护与基础资料管理，作为所有协同功能的底座。',
		tag: '用户',
		color: 'blue'
	},
	{
		index: '02',
		title: '家属绑定与日志共享',
		description: '通过后台直接绑定家属与用户，让核心生活记录可以远程查看。',
		tag: '协同',
		color: 'cyan'
	},
	{
		index: '03',
		title: '药品档案与风险提醒',
		description: '预录药品档案后，识别链路会优先播报用法用量并触发安全预警。',
		tag: '安全',
		color: 'orange'
	}
];

const shortcuts: ShortcutItem[] = [
	{
		title: '进入 App 用户',
		description: '集中维护登录用户与账号状态。',
		path: '/app-users',
		tag: '基础',
		color: 'blue'
	},
	{
		title: '进入 家属绑定',
		description: '为家属建立与视障用户的查看关系。',
		path: '/family-bindings',
		tag: '协同',
		color: 'cyan'
	},
	{
		title: '进入 共享日志',
		description: '查看药品识别、寻物、地点与预警摘要。',
		path: '/shared-logs',
		tag: '日志',
		color: 'green'
	},
	{
		title: '进入 药品档案',
		description: '预录药品信息，支撑命中优先播报。',
		path: '/medicine-profiles',
		tag: '药品',
		color: 'orange'
	}
];

const greetingTitle = computed(() => `你好，${profile?.nickname || '管理员'}。`);

onMounted(() => {
	loadDashboard();
});

async function loadDashboard() {
	loading.value = true;
	stats.adminName = profile?.username || 'admin';
	loading.value = false;
}

function goTo(path: string) {
	router.push(path);
}
</script>

<style scoped>
.dashboard-page {
	display: flex;
	flex-direction: column;
	gap: 20px;
}

.hero-panel,
.story-panel {
	position: relative;
	overflow: hidden;
	border-radius: 28px;
	background: linear-gradient(160deg, rgba(8, 18, 32, 0.82), rgba(10, 17, 30, 0.62));
	border: 1px solid rgba(148, 163, 184, 0.12);
	box-shadow: 0 28px 70px rgba(2, 6, 23, 0.24);
}

.hero-panel {
	display: grid;
	grid-template-columns: minmax(0, 1.2fr) 360px;
	gap: 20px;
	padding: 32px;
}

.hero-panel::before {
	content: '';
	position: absolute;
	top: -120px;
	right: -60px;
	width: 300px;
	height: 300px;
	border-radius: 999px;
	background: radial-gradient(circle, rgba(45, 212, 191, 0.22), rgba(45, 212, 191, 0));
}

.panel-kicker {
	font-size: 12px;
	font-weight: 700;
	letter-spacing: 0.18em;
	text-transform: uppercase;
	color: rgba(148, 163, 184, 0.74);
}

.hero-copy,
.hero-stats {
	position: relative;
	z-index: 1;
}

.hero-copy h2 {
	margin: 14px 0 0;
	font-size: 40px;
	line-height: 1.08;
	color: #f8fafc;
}

.hero-copy p {
	max-width: 720px;
	margin: 16px 0 0;
	font-size: 15px;
	line-height: 1.8;
	color: rgba(226, 232, 240, 0.84);
}

.toolbar-insights {
	display: flex;
	flex-wrap: wrap;
	gap: 10px;
	margin-top: 24px;
}

.insight-pill {
	display: inline-flex;
	align-items: center;
	gap: 8px;
	padding: 10px 14px;
	border-radius: 999px;
	background: rgba(255, 255, 255, 0.05);
	border: 1px solid rgba(148, 163, 184, 0.12);
	color: rgba(226, 232, 240, 0.86);
	font-size: 13px;
}

.insight-dot {
	width: 8px;
	height: 8px;
	border-radius: 999px;
	background: #94a3b8;
}

.insight-dot.active {
	background: #34d399;
}

.insight-dot.warm {
	background: #fb923c;
}

.hero-stats {
	display: grid;
	grid-template-columns: 1fr;
	gap: 12px;
}

.stat-card,
.metric-panel,
.surface-card :deep(.ant-card-body),
.story-panel {
	backdrop-filter: blur(16px);
}

.stat-card,
.metric-panel {
	padding: 20px;
	border-radius: 22px;
	background: rgba(255, 255, 255, 0.04);
	border: 1px solid rgba(148, 163, 184, 0.12);
}

.stat-card.highlight {
	background: linear-gradient(155deg, rgba(20, 184, 166, 0.18), rgba(59, 130, 246, 0.12));
	border-color: rgba(45, 212, 191, 0.18);
}

.stat-label,
.metric-label {
	font-size: 12px;
	font-weight: 700;
	letter-spacing: 0.12em;
	text-transform: uppercase;
	color: rgba(148, 163, 184, 0.74);
}

.stat-value,
.metric-value {
	margin-top: 10px;
	font-size: 30px;
	font-weight: 700;
	line-height: 1.14;
	color: #f8fafc;
}

.stat-meta,
.metric-meta,
.command-meta,
.shortcut-meta,
.story-meta,
.focus-meta {
	margin-top: 8px;
	font-size: 14px;
	line-height: 1.75;
	color: rgba(203, 213, 225, 0.78);
}

.metrics-grid,
.dashboard-grid,
.story-grid {
	display: grid;
	gap: 20px;
}

.metrics-grid {
	grid-template-columns: repeat(4, minmax(0, 1fr));
}

.metric-panel.accent {
	background: linear-gradient(155deg, rgba(14, 165, 233, 0.16), rgba(20, 184, 166, 0.1));
	border-color: rgba(45, 212, 191, 0.18);
}

.dashboard-grid,
.story-grid {
	grid-template-columns: repeat(2, minmax(0, 1fr));
}

.surface-card {
	border-radius: 28px;
	background: linear-gradient(160deg, rgba(8, 18, 32, 0.82), rgba(10, 17, 30, 0.62));
	border: 1px solid rgba(148, 163, 184, 0.12);
	box-shadow: 0 28px 70px rgba(2, 6, 23, 0.2);
}

:deep(.surface-card .ant-card-head) {
	border-bottom: 1px solid rgba(148, 163, 184, 0.1);
	min-height: 74px;
}

:deep(.surface-card .ant-card-head-title) {
	padding: 18px 0;
}

:deep(.surface-card .ant-card-body) {
	padding: 22px;
}

.card-title-wrap {
	display: flex;
	flex-direction: column;
	gap: 4px;
}

.card-title {
	font-size: 18px;
	font-weight: 700;
	color: #f8fafc;
}

.card-subtitle {
	font-size: 13px;
	color: rgba(148, 163, 184, 0.78);
}

.command-list,
.shortcut-list,
.story-list,
.focus-stack {
	display: flex;
	flex-direction: column;
	gap: 12px;
}

.command-item,
.shortcut-item,
.story-item,
.focus-item {
	display: flex;
	align-items: flex-start;
	justify-content: space-between;
	gap: 14px;
	padding: 16px 18px;
	border-radius: 20px;
	background: rgba(255, 255, 255, 0.04);
	border: 1px solid rgba(148, 163, 184, 0.1);
}

.shortcut-item {
	cursor: pointer;
	transition: transform 0.18s ease, border-color 0.18s ease, background 0.18s ease;
}

.shortcut-item:hover {
	transform: translateY(-2px);
	background: rgba(255, 255, 255, 0.06);
	border-color: rgba(45, 212, 191, 0.2);
}

.command-index,
.story-step {
	flex: 0 0 auto;
	display: grid;
	place-items: center;
	width: 38px;
	height: 38px;
	border-radius: 14px;
	font-size: 13px;
	font-weight: 700;
	color: #04111f;
	background: linear-gradient(135deg, #67e8f9, #facc15);
}

.command-copy,
.shortcut-copy,
.story-copy {
	flex: 1;
}

.command-name,
.shortcut-title,
.story-title,
.story-name,
.focus-name {
	font-size: 16px;
	font-weight: 700;
	color: #f8fafc;
}

.story-panel {
	padding: 24px;
}

.story-panel.emphasis {
	background: linear-gradient(160deg, rgba(12, 24, 39, 0.84), rgba(17, 24, 39, 0.68));
}

.story-title {
	margin-top: 10px;
}

.focus-item {
	flex-direction: column;
	align-items: flex-start;
}

@media (max-width: 1280px) {
	.metrics-grid {
		grid-template-columns: repeat(2, minmax(0, 1fr));
	}
}

@media (max-width: 992px) {
	.hero-panel,
	.dashboard-grid,
	.story-grid {
		grid-template-columns: 1fr;
	}
}

@media (max-width: 640px) {
	.metrics-grid {
		grid-template-columns: 1fr;
	}

	.hero-copy h2 {
		font-size: 32px;
	}
}
</style>
