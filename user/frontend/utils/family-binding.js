const STORAGE_KEY = 'family_active_vision_user_id';

function normalizeVisionUserId(value) {
	if (value === null || value === undefined || value === '') {
		return null;
	}
	const parsed = Number(value);
	return Number.isFinite(parsed) ? parsed : null;
}

export function getActiveVisionUserId() {
	return normalizeVisionUserId(uni.getStorageSync(STORAGE_KEY));
}

export function setActiveVisionUserId(visionUserId) {
	const normalized = normalizeVisionUserId(visionUserId);
	if (normalized === null) {
		uni.removeStorageSync(STORAGE_KEY);
		return null;
	}
	uni.setStorageSync(STORAGE_KEY, normalized);
	return normalized;
}

export function clearActiveVisionUserId() {
	uni.removeStorageSync(STORAGE_KEY);
}
