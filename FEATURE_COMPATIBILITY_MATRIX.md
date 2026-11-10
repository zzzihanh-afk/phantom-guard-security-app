# PHANTOM GUARD — Feature & OEM Compatibility Matrix

## 1. Protection Level Classification

Phantom Guard dynamically categorizes security features into three architectural levels depending on Android OS version, OEM vendor modifications, and provisioning state:

| Protection Level | Requirement | Capabilities |
|---|---|---|
| **Level 1: Basic APK** | Standard Installation, Runtime Permissions | GPS Location, SIM Change Detection, Foreground Siren, Recovery Screen, Evidence Photo, Web Dashboard sync |
| **Level 2: Device Admin** | Device Administrator Activated (`DeviceAdminReceiver`) | Hardware Screen Lock, Failed Unlock Attempt Detection, Password Expiry Policy, Remote Factory Wipe |
| **Level 3: Deep Protection (Device Owner / DPC)** | Provisioned via QR Code on fresh device or ADB setup | Anti-Uninstall Protection, Factory Reset Protection (FRP), Lock Task / Kiosk Mode, Disable Safe Boot, Block USB debugging/data |

---

## 2. Feature Availability by Android API Level

| Security Feature | Android 7.0–8.1 (API 24–27) | Android 9.0–11 (API 28–30) | Android 12–14 (API 31–34) | Android 15+ (API 35+) | Fallback Behavior |
|---|---|---|---|---|---|
| **Remote Lock** | Supported | Supported | Supported | Supported | DeviceAdmin `lockNow()` |
| **Remote Alarm Siren** | Supported | Supported | Supported (Foreground Audio) | Supported | Max stream volume override |
| **Silent Camera Capture** | Supported | Supported | Requires foreground service notification | Foreground camera service | User privacy banner shown |
| **SIM Card Change Alert** | TelephonyManager | SubscriptionManager | SubscriptionManager (hashed) | Hashed IMSI/ICCID | Compares last known carrier hash |
| **Device Owner Anti-Uninstall** | Supported | Supported | Supported | Supported | Hides uninstall button if DPC |
| **Factory Reset Protection** | Supported (API 24+) | Supported | Supported | Supported | Enforces master admin Google ID |
| **Bootloader Status Check** | `ro.boot.flash.locked` | SystemProperties reflection | Keystore Key Attestation | Key Attestation | Detects unlocked flag via properties |
| **Root Detection** | Su binary / Busybox check | Rootbeer heuristics | Multi-vector integrity inspection | Play Integrity API / Keystore | Flags rooted devices with warning |
| **Emergency Lockdown Mode** | Overlay / Keyguard | Overlay / Keyguard | LockTaskMode (DPC) / Overlay | LockTaskMode | Prevents home/recent switch |

---

## 3. OEM Vendor Compatibility & Power Management Guide

| Manufacturer / OS | Autostart & Background Whitelist | Battery Optimization Guidance | OEM-Specific Notes |
|---|---|---|---|
| **Samsung (One UI)** | Settings → Device Care → Battery → Background usage limits → Never sleeping apps | Whitelist Phantom Guard | Full Knox support; excellent DPC compatibility |
| **Xiaomi / Redmi / POCO (MIUI / HyperOS)** | Security App → Manage Apps → Phantom Guard → Autostart: ON; Battery Saver: No Restrictions | Grant "Display pop-up windows while running in the background" | Requires enabling background pop-up permissions for lock overlay |
| **OPPO / Realme (ColorOS / Realme UI)** | Settings → Battery → App battery management → Allow background activity; Auto-launch: ON | Turn off "Sleep standby optimization" for Phantom Guard | Keep locked in recent apps tray |
| **Vivo (Funtouch OS / OriginOS)** | Settings → Battery → High background power consumption → Enable for Phantom Guard | Whitelist in iManager | Autostart permission required |
| **OnePlus (OxygenOS)** | Settings → Battery → Battery optimization → Don't optimize | Enable Lock in App Switcher | High stability on recent builds |
| **Google Pixel (Stock Android)** | Standard battery optimization whitelist prompt | Battery → Unrestricted | Native standard Android Enterprise compliance |
| **Motorola (MyUX)** | Standard Android Enterprise compliance | Battery → Unrestricted | Highly compatible with standard APIs |
| **Transsion (Tecno / Infinix - HiOS / XOS)** | Phone Master → Auto-start management → Enable | Battery Lab → Protected Apps | Whitelist background service |
| **Huawei (EMUI / HarmonyOS)** | App Launch → Manage Manually (Auto-launch, Secondary launch, Run in background: ALL ON) | Battery optimization ignore | Fallback to MicroG / direct FCM alternative if Play Services absent |
