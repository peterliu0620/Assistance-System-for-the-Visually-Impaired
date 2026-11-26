// 部署环境：使用云服务器后端地址
// export const API_BASE = 'http://154.12.17.96:8080';

// 本地调试：使用本机后端
export const API_BASE = 'http://localhost:8080';

export function buildApiUrl(path) {
	return `${API_BASE}${path}`;
}
