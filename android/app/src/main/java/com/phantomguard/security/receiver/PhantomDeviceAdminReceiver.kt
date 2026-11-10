package com.phantomguard.security.receiver

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class PhantomDeviceAdminReceiver : DeviceAdminReceiver() {

    companion object {
        private const val TAG = "PhantomAdminReceiver"
        var failedUnlockCount = 0
            private set
    }

    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        Log.i(TAG, "Phantom Guard Device Administrator privileges granted.")
    }

    override fun onDisableRequested(context: Context, intent: Intent): CharSequence {
        return "WARNING: Disabling Phantom Guard will leave your device unprotected against theft and unauthorized access!"
    }

    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
        Log.w(TAG, "Phantom Guard Device Administrator privileges revoked by user.")
    }

    override fun onPasswordFailed(context: Context, intent: Intent) {
        super.onPasswordFailed(context, intent)
        failedUnlockCount++
        Log.w(TAG, "Unauthorized screen unlock attempt detected! Total failures: $failedUnlockCount")
    }

    override fun onPasswordSucceeded(context: Context, intent: Intent) {
        super.onPasswordSucceeded(context, intent)
        failedUnlockCount = 0
        Log.i(TAG, "Device successfully unlocked by owner.")
    }

    override fun onLockTaskModeEntering(context: Context, intent: Intent, pkg: String) {
        super.onLockTaskModeEntering(context, intent, pkg)
        Log.i(TAG, "Emergency Lockdown mode entered.")
    }

    override fun onLockTaskModeExiting(context: Context, intent: Intent) {
        super.onLockTaskModeExiting(context, intent)
        Log.i(TAG, "Emergency Lockdown mode exited.")
    }
}
