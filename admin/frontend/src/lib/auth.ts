import type { AdminProfile, AuthSessionPayload } from '@/types/models';

const TOKEN_KEY = 'admin_token';
const PROFILE_KEY = 'admin_profile';

export function getAuthToken(): string {
  return localStorage.getItem(TOKEN_KEY) || '';
}

export function hasAuthToken(): boolean {
  return Boolean(getAuthToken());
}

export function getAdminProfile(): AdminProfile | null {
  const raw = localStorage.getItem(PROFILE_KEY);

  if (!raw) {
    return null;
  }

  try {
    return JSON.parse(raw) as AdminProfile;
  } catch (error) {
    clearAuth();
    return null;
  }
}

export function setAuthSession(payload: AuthSessionPayload): void {
  localStorage.setItem(TOKEN_KEY, payload.token);
  localStorage.setItem(
    PROFILE_KEY,
    JSON.stringify({
      id: payload.id,
      username: payload.username,
      nickname: payload.nickname
    })
  );
}

export function clearAuth(): void {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(PROFILE_KEY);
}
