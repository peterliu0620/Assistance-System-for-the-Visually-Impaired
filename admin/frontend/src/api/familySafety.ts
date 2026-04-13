import type {
  FamilyBinding,
  FamilyBindingPayload,
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
