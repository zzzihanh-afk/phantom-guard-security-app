export type UserRole = 'REGISTERED_ADMIN' | 'GLOBAL_MASTER_ADMIN';

export interface Device {
  id: string;
  name: string;
  manufacturer: string;
  model: string;
  androidVersion: string;
  apiLevel: number;
  batteryLevel: number;
  isCharging: boolean;
  isOnline: boolean;
  protectionTier: 'DEEP_PROTECTION' | 'STANDARD_PROTECTION' | 'BASIC_PROTECTION';
  lastSeen: string;
  location: string;
  latitude: number;
  longitude: number;
}

export interface SecurityAlert {
  id: string;
  deviceId: string;
  deviceName: string;
  title: string;
  message: string;
  severity: 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'INFO';
  timestamp: string;
}

export interface ChatMessage {
  id: string;
  sender: string;
  isOwner: boolean;
  text: string;
  timestamp: string;
}

export interface AdminUser {
  id: string;
  name: string;
  email: string;
  devicesCount: number;
  status: 'ACTIVE' | 'SUSPENDED';
  registeredAt: string;
}
