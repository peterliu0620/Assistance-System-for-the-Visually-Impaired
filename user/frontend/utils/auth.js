export const USER_ROLE_VISION = 'vision';
export const USER_ROLE_FAMILY = 'family';

export const AUTH_STORAGE_KEY = 'auth_user';
const MOCK_USERS_STORAGE_KEY = 'mock_auth_users';

const DEFAULT_MOCK_USERS = [
	{
		id: 1001,
		username: 'vision_demo',
		password: '123456',
		nickname: '视障体验账号',
		phone: '13800000001',
		email: 'vision@example.com',
		role: USER_ROLE_VISION
	},
	{
		id: 2001,
		username: 'family_demo',
		password: '123456',
		nickname: '家属体验账号',
		phone: '13800000002',
		email: 'family@example.com',
		role: USER_ROLE_FAMILY
	}
];

export const ROLE_OPTIONS = [
	{
		label: '视障人士',
		value: USER_ROLE_VISION,
		desc: '登录后进入智能视觉识别、寻物与知识归档。'
	},
	{
		label: '家人',
		value: USER_ROLE_FAMILY,
		desc: '登录后查看记录、关注风险提醒并登记照护信息。'
	}
];

function normalizeRole(role) {
	return role === USER_ROLE_FAMILY ? USER_ROLE_FAMILY : USER_ROLE_VISION;
}

function normalizeUser(user) {
	if (!user || typeof user !== 'object') {
		return null;
	}
	return {
		...user,
		role: normalizeRole(user.role)
	};
}

export function isFamilyRole(role) {
	return normalizeRole(role) === USER_ROLE_FAMILY;
}

export function isVisionRole(role) {
	return normalizeRole(role) === USER_ROLE_VISION;
}

export function getRoleLabel(role) {
	return isFamilyRole(role) ? '家人' : '视障人士';
}

export function getDefaultRouteByRole(role) {
	return isFamilyRole(role) ? '/pages/family-center/family-center' : '/pages/index/index';
}

export function getAuthUser() {
	const stored = uni.getStorageSync(AUTH_STORAGE_KEY);
	return normalizeUser(stored) || {};
}

export function saveAuthUser(user) {
	const normalized = normalizeUser(user) || {};
	uni.setStorageSync(AUTH_STORAGE_KEY, normalized);
	return normalized;
}

export function clearAuthUser() {
	uni.removeStorageSync(AUTH_STORAGE_KEY);
}

export function loadMockUsers() {
	const stored = uni.getStorageSync(MOCK_USERS_STORAGE_KEY);
	if (!Array.isArray(stored) || !stored.length) {
		uni.setStorageSync(MOCK_USERS_STORAGE_KEY, DEFAULT_MOCK_USERS);
		return DEFAULT_MOCK_USERS.map(item => ({ ...item }));
	}
	return stored.map(item => ({ ...item, role: normalizeRole(item.role) }));
}

export function saveMockUsers(users) {
	const normalized = (users || []).map(item => normalizeUser(item)).filter(Boolean);
	uni.setStorageSync(MOCK_USERS_STORAGE_KEY, normalized);
	return normalized;
}

export function loginWithMock({ username, password, role }) {
	const account = (username || '').trim();
	const selectedRole = normalizeRole(role);
	if (!account || !password) {
		return { ok: false, message: '用户名和密码不能为空' };
	}
	const user = loadMockUsers().find(item => item.username === account && item.role === selectedRole);
	if (!user || user.password !== password) {
		return { ok: false, message: '账号、密码或身份不正确' };
	}
	const safeUser = {
		...user
	};
	delete safeUser.password;
	return {
		ok: true,
		user: saveAuthUser(safeUser)
	};
}

export function registerWithMock(form) {
	const payload = form || {};
	const username = (payload.username || '').trim();
	const password = payload.password || '';
	const nickname = (payload.nickname || '').trim();
	const role = normalizeRole(payload.role);
	if (!username || !password || !nickname) {
		return { ok: false, message: '用户名、密码和昵称不能为空' };
	}
	if (username.length < 4) {
		return { ok: false, message: '用户名至少 4 位' };
	}
	if (password.length < 6) {
		return { ok: false, message: '密码至少 6 位' };
	}
	const users = loadMockUsers();
	if (users.some(item => item.username === username && item.role === role)) {
		return { ok: false, message: '该身份下用户名已存在' };
	}
	const user = {
		id: Date.now(),
		username,
		password,
		nickname,
		phone: (payload.phone || '').trim(),
		email: (payload.email || '').trim(),
		role
	};
	users.unshift(user);
	saveMockUsers(users);
	const safeUser = { ...user };
	delete safeUser.password;
	return {
		ok: true,
		user: saveAuthUser(safeUser)
	};
}
