# PHANTOM GUARD — Firestore Database Schema & Security Architecture

This document defines the complete Cloud Firestore schema and access control rules for PHANTOM GUARD.
All collections adhere strictly to the principle of least privilege, preventing Insecure Direct Object References (IDOR) and unauthorized data leakage.

---

## 1. Firestore Collections Overview

### 1.1 `users/{userId}`
Holds user profile and account status.
- `userId` (string, PK): Firebase Auth UID
- `email` (string): User email address
- `displayName` (string): User's full name or handle (e.g. "Tanzimul")
- `role` (string): `"registered_admin"` or `"global_master_admin"`
- `isSuspended` (boolean): Whether account is suspended by Global Master Admin
- `twoFactorEnabled` (boolean): Whether 2FA pin is required for destructive commands
- `twoFactorPinHash` (string, nullable): SHA-256 / BCrypt hashed re-auth PIN
- `createdAt` (timestamp): Account creation timestamp
- `lastLoginAt` (timestamp): Last authentication timestamp
- `deviceCount` (number): Current enrolled device count

### 1.2 `userRoles/{userId}`
Immutable role definition table checked by Firestore rules.
- `role` (string): `"global_master_admin"` or `"registered_admin"`
- `assignedAt` (timestamp): Timestamp of bootstrap role grant
- `bootstrapKeyUsed` (boolean): Flag indicating initial root setup completed

### 1.3 `devices/{deviceId}`
Master inventory of enrolled Android devices.
- `deviceId` (string, PK): Unique device identifier (UUID / Keystore derived)
- `ownerId` (string, FK -> users): Owner UID
- `deviceName` (string): Friendly name (e.g. "Redmi Note 13")
- `manufacturer` (string): e.g. "Xiaomi", "Samsung", "Google"
- `model` (string): e.g. "23124RA7EO"
- `androidVersion` (string): e.g. "14 (Upside Down Cake)"
- `apiLevel` (number): e.g. 34
- `securityPatch` (string): e.g. "2026-10-01"
- `protectionLevel` (string): `"DEEP_PROTECTION"` (DPC), `"STANDARD_PROTECTION"` (Admin), or `"BASIC_PROTECTION"`
- `isDeviceOwner` (boolean): True if provisioned as Device Owner
- `isDeviceAdmin` (boolean): True if Device Administrator active
- `appVersion` (string): e.g. "1.0.0"
- `isOnline` (boolean): Realtime presence indicator
- `batteryLevel` (number): 0-100 percentage
- `isCharging` (boolean): Charging status
- `stolenModeActive` (boolean): True if Stolen Mode triggered
- `emergencyLockActive` (boolean): True if Emergency Lockdown active
- `lastSeenAt` (timestamp): Heartbeat timestamp
- `enrolledAt` (timestamp): Initial enrollment timestamp

### 1.4 `deviceCapabilities/{deviceId}`
Dynamic capability report evaluated by the Device Compatibility Engine.
- `deviceId` (string, PK)
- `hasHardwareKeystore` (boolean)
- `hasStrongBox` (boolean)
- `hasFingerprint` (boolean)
- `hasBiometricStrong` (boolean)
- `bootloaderUnlocked` (boolean)
- `isRooted` (boolean)
- `verifiedBootState` (string): "GREEN", "YELLOW", "ORANGE", "RED"
- `playServicesAvailable` (boolean)
- `oemPowerOptimizationWhitelisted` (boolean)
- `canBlockUninstall` (boolean)
- `canEnforceFRP` (boolean)
- `canLockTask` (boolean)

### 1.5 `remoteCommands/{commandId}`
Cryptographically authenticated remote command dispatch queue.
- `commandId` (string, PK): UUIDv4
- `deviceId` (string, FK -> devices)
- `ownerId` (string, FK -> users)
- `commandType` (string): `"LOCK"`, `"ALARM"`, `"STOLEN_MODE_ENABLE"`, `"STOLEN_MODE_DISABLE"`, `"LOCATE_NOW"`, `"SEND_MESSAGE"`, `"WIPE_DATA"`, `"EMERGENCY_LOCKDOWN"`
- `payload` (map): Command parameters (e.g. `{"message": "Please return this phone", "phoneNumber": "+1234567890"}`)
- `nonce` (string): High-entropy unique random string for anti-replay
- `timestamp` (timestamp): Command issuance time
- `expiresAt` (timestamp): Strict expiry time (e.g. issue time + 15 minutes)
- `signature` (string): ECDSA P-256 digital signature computed by authorized backend
- `status` (string): `"PENDING"`, `"DELIVERED"`, `"EXECUTED"`, `"FAILED"`, `"EXPIRED"`
- `executedAt` (timestamp, nullable)
- `executionResult` (string, nullable)

### 1.6 `commandResults/{resultId}`
Proof of execution audit log emitted by target device.
- `resultId` (string, PK)
- `commandId` (string, FK -> remoteCommands)
- `deviceId` (string)
- `status` (string): `"SUCCESS"`, `"REJECTED_EXPIRED"`, `"REJECTED_REPLAY"`, `"REJECTED_SIGNATURE"`, `"FAILED_UNSUPPORTED"`
- `message` (string): Diagnostic details
- `executedAt` (timestamp)

### 1.7 `locationRecords/{recordId}`
Consent-based geolocation logs.
- `recordId` (string, PK)
- `deviceId` (string, FK -> devices)
- `latitude` (number)
- `longitude` (number)
- `accuracyMeters` (number)
- `provider` (string): `"gps"`, `"network"`, `"fused"`
- `timestamp` (timestamp)
- `reverseGeocodedAddress` (string): e.g. "Dhaka, Bangladesh"

### 1.8 `securityAlerts/{alertId}`
Urgent security events requiring attention.
- `alertId` (string, PK)
- `deviceId` (string)
- `ownerId` (string)
- `alertType` (string): `"SIM_CHANGED"`, `"DEVICE_OFFLINE"`, `"FAILED_UNLOCK"`, `"TAMPER_DETECTED"`, `"BOOTLOADER_UNLOCKED"`, `"ROOT_DETECTED"`
- `severity` (string): `"CRITICAL"`, `"HIGH"`, `"MEDIUM"`, `"INFO"`
- `title` (string): e.g. "SIM Change Detected"
- `message` (string): e.g. "New SIM detected on Redmi Note 13"
- `timestamp` (timestamp)
- `isResolved` (boolean)

### 1.9 `simEvents/{eventId}`
SIM card audit log.
- `eventId` (string, PK)
- `deviceId` (string)
- `imsiHash` (string): Salted SHA-256 of IMSI (privacy-preserving)
- `iccidHash` (string): Salted SHA-256 of ICCID
- `carrierName` (string)
- `countryIso` (string)
- `detectedAt` (timestamp)

### 1.10 `recoveryConversations/{conversationId}`
Anonymous secure chat between phone owner and honest finder or authorities.
- `conversationId` (string, PK): `device_${deviceId}`
- `deviceId` (string)
- `ownerId` (string)
- `rewardOfferedAmount` (number): e.g. 5000 (BDT / USD)
- `rewardCurrency` (string): e.g. "৳" or "$"
- `status` (string): `"OPEN"`, `"RESOLVED"`, `"CLOSED"`
- `messages` (subcollection or list):
  - `senderType` ("OWNER" or "FINDER")
  - `messageText` (string)
  - `sentAt` (timestamp)

### 1.11 `evidenceVault/{evidenceId}`
Encrypted tamper snapshots and audit tokens.
- `evidenceId` (string, PK)
- `deviceId` (string)
- `ownerId` (string)
- `eventType` (string): `"FAILED_UNLOCK_ATTEMPT"`, `"SIM_TAMPER"`, `"UNAUTHORIZED_SHUTDOWN_ATTEMPT"`
- `timestamp` (timestamp)
- `evidenceHash` (string): SHA-256 checksum of captured data
- `metadata` (map): Battery, network, cell tower ID, location snapshot
- `photoUrl` (string, nullable): Optional encrypted photo capture URL

### 1.12 `adminAuditLogs/{logId}`
Platform administration audit log visible only to Global Master Admin.
- `logId` (string, PK)
- `adminId` (string)
- `action` (string): e.g. `"SUSPEND_ACCOUNT"`, `"UPDATE_PLATFORM_CONFIG"`, `"VIEW_AUDIT_LOGS"`
- `targetUserId` (string, nullable)
- `timestamp` (timestamp)
- `ipAddress` (string)
