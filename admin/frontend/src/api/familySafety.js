import http from '@/api/http';

export function fetchFamilyBindings(keyword) {
  return http.get('/admin/family-bindings', {
    params: keyword ? { keyword } : {}
  });
}

export function createFamilyBinding(payload) {
  return http.post('/admin/family-bindings', payload);
}

export function fetchSharedLogs(params) {
  return http.get('/admin/shared-logs', { params });
}

export function fetchMedicineProfiles(params) {
  return http.get('/admin/medicine-profiles', { params });
}

export function createMedicineProfile(payload) {
  return http.post('/admin/medicine-profiles', payload);
}

export function updateMedicineProfile(id, payload) {
  return http.put(`/admin/medicine-profiles/${id}`, payload);
}

export function deleteMedicineProfile(id) {
  return http.delete(`/admin/medicine-profiles/${id}`);
}
