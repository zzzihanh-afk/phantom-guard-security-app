# PHANTOM GUARD — API & Command Specification

## 1. Remote Command Protocol

Every remote management instruction sent across FCM or Firestore follows this standard specification:

### 1.1 Command Envelope Schema
```json
{
  "commandId": "cmd_7c9e6679-7425-40de-944b-e07fc1f90ae7",
  "deviceId": "dev_d290f1ee-6c54-4b01-90e6-d701748f0851",
  "ownerId": "usr_99a8b12f",
  "commandType": "LOCK",
  "timestamp": 1794293000000,
  "expiresAt": 1794293900000,
  "nonce": "a8f3b2c9d1e4",
  "payload": {
    "message": "This device is locked by Phantom Guard. Call owner: +8801700000000",
    "phoneNumber": "+8801700000000",
    "requireReauth": true
  },
  "signature": "MEQCIAz7B...3K7g91a="
}
```

### 1.2 Supported Command Types
- `LOCK`: Forces device lock immediately via `DevicePolicyManager.lockNow()`. Displays recovery message on keyguard if provisioned.
- `ALARM`: Triggers max-volume audible siren in foreground service, ignoring silent/do-not-disturb modes.
- `STOLEN_MODE_ENABLE`: Engages full theft containment: locks screen, turns on rapid GPS tracking, enables tamper snapshot detection, and locks down UI.
- `STOLEN_MODE_DISABLE`: Disengages theft containment after authorized PIN/biometric verification.
- `LOCATE_NOW`: Triggers instantaneous high-accuracy GPS fix and reports back coordinates to Firestore.
- `SEND_MESSAGE`: Sends custom pop-up dialog with owner instructions and contact button.
- `EMERGENCY_LOCKDOWN`: Deep enterprise lock task mode blocking navigation bar, home button, and power menu where permitted.
- `WIPE_DATA`: Enterprise factory reset via `DevicePolicyManager.wipeData(0)`. Requires master owner cryptographic signature.
