export const API_BASE = 'http://10.135.244.98:8080';

export function buildApiUrl(path: string): string {
	return `${API_BASE}${path}`;
}
