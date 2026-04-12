<template>
	<a-config-provider :theme="themeConfig" :locale="zhCN">
		<router-view />
	</a-config-provider>
</template>

<script setup>
import zhCN from 'ant-design-vue/es/locale/zh_CN';

const themeConfig = {
	token: {
		colorPrimary: '#14b8a6',
		colorInfo: '#14b8a6',
		colorSuccess: '#14b8a6',
		colorWarning: '#f97316',
		colorError: '#ef4444',
		borderRadius: 18,
		fontFamily: "'Bahnschrift', 'Segoe UI', 'PingFang SC', 'Microsoft YaHei', sans-serif",
		colorBgBase: '#0b1424',
		colorTextBase: '#e2e8f0',
		colorBorderSecondary: 'rgba(148, 163, 184, 0.18)',
		boxShadowSecondary: '0 24px 64px rgba(2, 6, 23, 0.22)'
	}
};
</script>
