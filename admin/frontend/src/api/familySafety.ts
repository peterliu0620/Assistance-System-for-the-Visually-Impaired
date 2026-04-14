import type {
  FamilyBinding,
  FamilyBindingPayload,
  FamilyMember,
  FamilyMemberBindingPayload,
  FamilyMemberPayload,
  MedicineProfile,
  MedicineProfilePayload,
  SharedLog,
  SharedLogFilters
} from '@/types/models';
import http from '@/api/http';

export function fetchFamilyBindings(keyword?: string) {
  return http.get<FamilyBinding[]>('/admin/family-bindings', {
    params: keyword ? { keyword } : {}
  });
}

export function fetchFamilyMembers(keyword?: string) {
  return http.get<FamilyMember[]>('/admin/family-members', {
    params: keyword ? { keyword } : {}
  });
}

export function createFamilyMember(payload: FamilyMemberPayload) {
  return http.post<FamilyMember>('/admin/family-members', payload);
}

export function updateFamilyMember(id: number, payload: FamilyMemberPayload) {
  return http.put<FamilyMember>(`/admin/family-members/${id}`, payload);
}

export function deleteFamilyMember(id: number) {
  return http.delete<void>(`/admin/family-members/${id}`);
}

export function fetchFamilyMemberBlindUsers(id: number) {
  return http.get<FamilyBinding[]>(`/admin/family-members/${id}/blind-users`);
}

export function createFamilyMemberBinding(id: number, payload: FamilyMemberBindingPayload) {
  return http.post<FamilyBinding>(`/admin/family-members/${id}/bindings`, payload);
}

export function deleteFamilyMemberBinding(id: number, blindUserId: number) {
  return http.delete<void>(`/admin/family-members/${id}/bindings/${blindUserId}`);
}

export function createFamilyBinding(payload: FamilyBindingPayload) {
  return http.post<FamilyBinding>('/admin/family-bindings', payload);
}

export function fetchSharedLogs(params: SharedLogFilters) {
  return http.get<SharedLog[]>('/admin/shared-logs', { params });
}

export function fetchMedicineProfiles(params: { keyword?: string }) {
  return http.get<MedicineProfile[]>('/admin/medicine-profiles', { params });
}

export function createMedicineProfile(payload: MedicineProfilePayload) {
  return http.post<MedicineProfile>('/admin/medicine-profiles', payload);
}

export function updateMedicineProfile(id: number, payload: MedicineProfilePayload) {
  return http.put<MedicineProfile>(`/admin/medicine-profiles/${id}`, payload);
}

export function deleteMedicineProfile(id: number) {
  return http.delete<void>(`/admin/medicine-profiles/${id}`);
}
