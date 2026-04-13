import type { AppUser, AppUserPayload } from '@/types/models';
import http from '@/api/http';

export function fetchAppUsers(keyword?: string) {
  return http.get<AppUser[]>('/admin/app-users', {
    params: keyword ? { keyword } : {}
  });
}

export function createAppUser(payload: AppUserPayload) {
  return http.post<AppUser>('/admin/app-users', payload);
}

export function updateAppUser(id: number, payload: AppUserPayload) {
  return http.put<AppUser>(`/admin/app-users/${id}`, payload);
}

export function deleteAppUser(id: number) {
  return http.delete<void>(`/admin/app-users/${id}`);
}
