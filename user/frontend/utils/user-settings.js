export const DEFAULT_USER_SETTINGS = {
	speechRate: 1.0,
	voiceTimbre: 'Cherry',
	broadcastGranularity: 'detailed',
	gestureSingleTap: 'none',
	gestureDoubleTap: 'voice_trigger',
	gestureLongPress: 'direct_scan',
	hapticLevel: 2,
	contrastMode: 'black-gold',
	extraLargeText: false
}

export function loadUserSettings() {
	const stored = uni.getStorageSync('user_settings')
	if (!stored || typeof stored !== 'object') {
		return { ...DEFAULT_USER_SETTINGS }
	}
	return {
		...DEFAULT_USER_SETTINGS,
		...stored
	}
}

export function saveUserSettings(settings) {
	const merged = {
		...DEFAULT_USER_SETTINGS,
		...(settings || {})
	}
	uni.setStorageSync('user_settings', merged)
	return merged
}
