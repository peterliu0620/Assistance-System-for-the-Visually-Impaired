export const API_BASE = 'http://192.168.18.239:8080';

export function buildApiUrl(path: string): string {
	return `${API_BASE}${path}`;
}
