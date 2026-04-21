const FAMILY_PROFILE_STORAGE_KEY = 'family_profile';
const FAMILY_RECORDS_STORAGE_KEY = 'family_records';

export const DEFAULT_FAMILY_PROFILE = {
	basicInfo: {
		name: '王阿姨',
		gender: '女',
		age: '62',
		visionLevel: '低视力',
		address: '上海市浦东新区',
		notes: '独居，熟悉社区周边环境，白天外出频率较高。'
	},
	emergencyContact: {
		name: '李明',
		relation: '儿子',
		phone: '13900001111',
		backupPhone: '021-66668888'
	},
	healthInfo: {
		medicine: '降压药早晚各一次，外出需携带。',
		diseaseNote: '高血压，偶发头晕，避免长时间暴晒。',
		allergy: '青霉素过敏',
		reminder: '药盒放在玄关右侧第二层抽屉。'
	}
};

const DEFAULT_FAMILY_RECORDS = [
	{
		id: 'REC-240421-01',
		time: '2026-04-21 09:18',
		summary: '识别到药盒、水杯和餐桌边缘，播报已完成。',
		scene: '厨房台面',
		imageLabel: '药盒 / 水杯',
		location: '家中厨房',
		note: '提醒饭后服药，已完成语音播报。',
		riskAlert: true,
		riskText: '药盒靠近热水壶，已提醒注意拿取顺序。'
	},
	{
		id: 'REC-240421-02',
		time: '2026-04-21 11:42',
		summary: '识别到门口鞋柜与快递包裹。',
		scene: '玄关',
		imageLabel: '鞋柜 / 快递箱',
		location: '家中玄关',
		note: '家属可提醒先清理门口通道。',
		riskAlert: true,
		riskText: '门口有纸箱遮挡，存在绊倒风险。'
	},
	{
		id: 'REC-240420-03',
		time: '2026-04-20 19:26',
		summary: '识别到沙发、遥控器和靠垫。',
		scene: '客厅休息区',
		imageLabel: '遥控器 / 靠垫',
		location: '客厅',
		note: '画面稳定，无异常提醒。',
		riskAlert: false,
		riskText: ''
	},
	{
		id: 'REC-240420-04',
		time: '2026-04-20 07:55',
		summary: '识别到早餐面包、牛奶和桌边椅子。',
		scene: '餐桌',
		imageLabel: '早餐 / 椅子',
		location: '家中餐厅',
		note: '识别内容完整，建议补充水杯位置提醒。',
		riskAlert: false,
		riskText: ''
	}
];

function clone(data) {
	return JSON.parse(JSON.stringify(data));
}

function mergeSection(defaultValue, value) {
	return {
		...defaultValue,
		...(value || {})
	};
}

export function loadFamilyProfile() {
	const stored = uni.getStorageSync(FAMILY_PROFILE_STORAGE_KEY);
	return {
		basicInfo: mergeSection(DEFAULT_FAMILY_PROFILE.basicInfo, stored && stored.basicInfo),
		emergencyContact: mergeSection(DEFAULT_FAMILY_PROFILE.emergencyContact, stored && stored.emergencyContact),
		healthInfo: mergeSection(DEFAULT_FAMILY_PROFILE.healthInfo, stored && stored.healthInfo)
	};
}

export function saveFamilyProfile(profile) {
	const merged = {
		basicInfo: mergeSection(DEFAULT_FAMILY_PROFILE.basicInfo, profile && profile.basicInfo),
		emergencyContact: mergeSection(DEFAULT_FAMILY_PROFILE.emergencyContact, profile && profile.emergencyContact),
		healthInfo: mergeSection(DEFAULT_FAMILY_PROFILE.healthInfo, profile && profile.healthInfo)
	};
	uni.setStorageSync(FAMILY_PROFILE_STORAGE_KEY, merged);
	return merged;
}

export function loadFamilyRecords() {
	const stored = uni.getStorageSync(FAMILY_RECORDS_STORAGE_KEY);
	if (!Array.isArray(stored) || !stored.length) {
		uni.setStorageSync(FAMILY_RECORDS_STORAGE_KEY, clone(DEFAULT_FAMILY_RECORDS));
		return clone(DEFAULT_FAMILY_RECORDS);
	}
	return stored.map(item => ({ ...item }));
}

export function saveFamilyRecords(records) {
	const next = Array.isArray(records) ? records.map(item => ({ ...item })) : [];
	uni.setStorageSync(FAMILY_RECORDS_STORAGE_KEY, next);
	return next;
}

export function getFamilyDashboard() {
	const records = loadFamilyRecords();
	const profile = loadFamilyProfile();
	const riskCount = records.filter(item => item.riskAlert).length;
	const completedSections = [
		profile.basicInfo.name && profile.basicInfo.age,
		profile.emergencyContact.name && profile.emergencyContact.phone,
		profile.healthInfo.medicine || profile.healthInfo.diseaseNote
	].filter(Boolean).length;

	return {
		records,
		profile,
		riskCount,
		todayCount: records.filter(item => String(item.time || '').startsWith('2026-04-21')).length,
		completedSections,
		recentRiskText: records.find(item => item.riskAlert)?.riskText || '当前暂无新的风险提醒。'
	};
}
