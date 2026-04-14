import type { UserSettings } from '../types/models';

export const DEFAULT_USER_SETTINGS: UserSettings = {
	speechRate: 1.0,
	voiceTimbre: 'Cherry',
	broadcastGranularity: 'detailed',
	gestureSingleTap: 'none',
	gestureDoubleTap: 'voice_trigger',
	gestureLongPress: 'direct_scan',
	hapticLevel: 2,
	contrastMode: 'black-gold',
	extraLargeText: false,
	findModeFeedbackIntervalMs: 2600,
	findModeAutoStopSeconds: 90
};

export function loadUserSettings(): UserSettings {
	const stored = uni.getStorageSync('user_settings');
	if (!stored || typeof stored !== 'object') {
		return { ...DEFAULT_USER_SETTINGS };
	}
	return {
		...DEFAULT_USER_SETTINGS,
		...(stored as Partial<UserSettings>)
	};
}

export function saveUserSettings(settings?: Partial<UserSettings>): UserSettings {
	const merged: UserSettings = {
		...DEFAULT_USER_SETTINGS,
		...(settings || {})
	};
	uni.setStorageSync('user_settings', merged);
	return merged;
}
