# PHANTOM GUARD — Setup & Deployment Guide

This guide covers building the Android Application, configuring Firebase, provisioning Android Enterprise Device Owner, and running the Web Dashboard.

---

## 1. Prerequisites
- **Java Development Kit:** OpenJDK 17 or 21 (or Android Studio JBR).
- **Android SDK:** Platforms 34 / 35, Build-tools 36.0.0.
- **Node.js:** v18+ & npm for the Web Dashboard.
- **Firebase Project:** Firebase Spark (Free Tier) project.

---

## 2. Android App Build & Installation

### 2.1 Building Debug APK
```bash
cd android
./gradlew assembleDebug
```
The resulting APK will be at:
`android/app/build/outputs/apk/debug/app-debug.apk`

### 2.2 Installing via ADB
```bash
adb install android/app/build/outputs/apk/debug/app-debug.apk
```

---

## 3. Provisioning Device Owner (Deep Protection Mode)

For full Anti-Uninstall, Lockdown Mode, and Factory Reset Protection:

### Option A: ADB Provisioning (Development / Testing)
1. Ensure the phone has **no Google or corporate accounts** added in `Settings -> Accounts`.
2. Connect phone via USB with Developer Options & USB Debugging enabled.
3. Run:
```bash
adb shell dpm set-device-owner com.phantomguard.security.debug/com.phantomguard.security.receiver.PhantomDeviceAdminReceiver
```
*(For release builds, use `com.phantomguard.security/com.phantomguard.security.receiver.PhantomDeviceAdminReceiver`)*

### Option B: QR Code Zero-Touch Provisioning (Production Factory Reset)
1. Factory reset phone.
2. At the initial "Hi there" welcome screen, tap 6 times on any blank area.
3. The camera QR scanner will activate.
4. Scan the Phantom Guard Enrollment QR code generated in the Web Dashboard.

---

## 4. Web Dashboard Setup

```bash
cd web-dashboard
npm install
npm run dev
```
Open `http://localhost:5173` to access the Registered Admin and Global Master Admin portal.

---

## 5. Firebase Free Tier Configuration
1. Create a Firebase project at `https://console.firebase.google.com`.
2. Enable **Authentication** (Email/Password, Google).
3. Enable **Cloud Firestore** in test or production mode.
4. Deploy security rules from `backend/firestore.rules`.
5. Add Android app package `com.phantomguard.security` and download `google-services.json` into `android/app/`.
