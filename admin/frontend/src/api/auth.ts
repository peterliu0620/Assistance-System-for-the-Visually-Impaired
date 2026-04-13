import type { AuthSessionPayload } from '@/types/models';
import http from './http';

interface LoginPayload {
  username: string;
  password: string;
}

export function login(payload: LoginPayload) {
  return http.post<AuthSessionPayload>('/admin/auth/login', payload);
}
