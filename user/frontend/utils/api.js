export const API_BASE = 'http://154.12.17.96:8080';

export function buildApiUrl(path) {
	return `${API_BASE}${path}`;
}
