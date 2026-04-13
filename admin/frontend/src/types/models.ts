export interface AdminProfile {
  id: number;
  username: string;
  nickname: string;
}

export interface AuthSessionPayload extends AdminProfile {
  token: string;
}

export interface AppUser {
  id: number;
  username: string;
  nickname: string;
  phone?: string | null;
  email?: string | null;
  status: number;
  lastLoginAt?: string | null;
  createdAt?: string | null;
}

export interface AppUserPayload {
  username?: string;
  password: string;
  nickname: string;
  phone: string;
  email: string;
  status: number;
}

export interface FamilyBinding {
  id: number;
  userId: number;
  username: string;
  nickname: string;
  familyMemberId: number;
  familyName: string;
  familyPhone?: string | null;
  familyEmail?: string | null;
  relationship: string;
  status: 'ACTIVE' | 'INACTIVE' | string;
  createdAt?: string | null;
}

export interface FamilyBindingPayload {
  userId: number;
  familyName: string;
  familyPhone: string;
  familyEmail: string;
  relationship: string;
  status: 'ACTIVE';
}

export interface SharedLogFilters {
  familyMemberId: number;
  userId?: number;
  mode?: string;
  date?: string;
}

export interface SharedLog {
  recordId: string | number;
  userId: number;
  username: string;
  nickname: string;
  mode: string;
  coreItem?: string | null;
  scene?: string | null;
  triggerCommand?: string | null;
  locationText?: string | null;
  alertTriggered: boolean;
  alertType?: string | null;
  alertMessage?: string | null;
  capturedAt?: string | null;
}

export interface MedicineProfile {
  id: number;
  userId: number;
  username: string;
  nickname: string;
  familyMemberId?: number | null;
  medicineName: string;
  genericName?: string | null;
  description?: string | null;
  dosageUsage?: string | null;
  suitablePeople?: string | null;
  contraindications?: string | null;
  expiryDate?: string | null;
  barcodeOrAlias?: string | null;
}

export interface MedicineProfilePayload {
  userId: number;
  familyMemberId?: number | null;
  medicineName: string;
  genericName: string;
  description: string;
  dosageUsage: string;
  suitablePeople: string;
  contraindications: string;
  expiryDate?: string;
  barcodeOrAlias: string;
}
