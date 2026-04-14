export type GestureAction = 'none' | 'voice_trigger' | 'direct_scan' | 'open_user_center';
export type ContrastMode = 'black-gold' | 'black-yellow';
export type BroadcastGranularity = 'concise' | 'detailed';

export interface UserSettings {
  speechRate: number;
  voiceTimbre: string;
  broadcastGranularity: BroadcastGranularity;
  gestureSingleTap: GestureAction;
  gestureDoubleTap: GestureAction;
  gestureLongPress: GestureAction;
  hapticLevel: number;
  contrastMode: ContrastMode;
  extraLargeText: boolean;
  findModeFeedbackIntervalMs: number;
  findModeAutoStopSeconds: number;
}

export interface AuthUser {
  id?: number;
  username?: string;
  nickname?: string;
  phone?: string;
  email?: string;
}

export interface KnowledgeRecordItem {
  id?: number | string;
  name: string;
  position?: string;
  confidence?: number;
}

export interface KnowledgeRecord {
  id: number | string;
  scene?: string;
  capturedAt?: string;
  triggerCommand?: string;
  locationText?: string;
  narration?: string;
  items?: KnowledgeRecordItem[];
}
