export const API_BASE = 'http://10.135.159.248:8080';

export function buildApiUrl(path) {
	return `${API_BASE}${path}`;
}
