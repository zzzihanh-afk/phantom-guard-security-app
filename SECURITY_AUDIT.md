# PHANTOM GUARD — Security Architecture & Threat Model Audit

## 1. Threat Model & Security Posture

Phantom Guard was architected with a zero-trust model between client applications, relay channels, and peripheral services.

### 1.1 Remote Command Cryptographic Assurance
1. **No Plaintext or Unsigned Action Execution:**
   Destructive actions (`WIPE_DATA`, `LOCK`, `EMERGENCY_LOCKDOWN`) cannot be executed merely on the presence of a Firestore document.
2. **Signature Verification (ECDSA P-256):**
   Commands dispatched via the server/worker backend are signed using an Elliptic Curve private key. The target Android device retains only the public key in its secure keystore / app assets to verify integrity.
3. **Replay Attack Elimination:**
   Every command requires a unique random `nonce` and a cryptographic `expiresAt` timestamp (default: 15 minutes). The local Android SQLite database records previously executed nonces. Even if an attacker intercepts and resends an old network packet, the device rejects it as duplicate or expired.

### 1.2 Android Enterprise / DPC Demarcation
- **Legitimate Device Owner Privileges:**
  Phantom Guard uses official Android DevicePolicyManager APIs. It never relies on unpatched root vulnerabilities or dirty-pipe exploits that compromise device stability.
- **Factory Reset Protection (FRP):**
  When provisioned as Device Owner, FRP policy binds to the Master Admin's Google Account ID, preventing device re-activation even if an unauthorized wipe occurs via Recovery mode.
- **Anti-Uninstall Protection:**
  Device Owner mode blocks package uninstallation (`setUninstallBlocked(adminComponent, packageName, true)`).

### 1.3 Transparency & Privacy Guarantees
- **No Covert Surveillance:**
  Phantom Guard does not record user phone conversations, read personal WhatsApp/SMS messages, or harvest photos unrelated to security events.
- **Explicit Permission Consent:**
  Location tracking displays a persistent status indicator in the notification shade, respecting Android platform standards and privacy regulations.
- **Immutable Evidence Vault:**
  Security incidents generate SHA-256 hash chains that cannot be altered or deleted by malicious actors on the local device.
