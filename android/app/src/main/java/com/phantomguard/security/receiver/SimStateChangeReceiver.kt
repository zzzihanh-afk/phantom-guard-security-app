package com.phantomguard.security.receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.phantomguard.security.PhantomGuardApplication
import com.phantomguard.security.domain.engine.SimMonitoringEngine

class SimStateChangeReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == "android.intent.action.SIM_STATE_CHANGED" ||
            action == "android.intent.action.SIM_CARD_STATE_CHANGED"
        ) {
            val engine = SimMonitoringEngine(context)
            val simReport = engine.checkSimStatus()
            Log.i("SimReceiver", "SIM state changed. Carrier: ${simReport.carrierName}, Present: ${simReport.isSimPresent}")

            if (!simReport.isSimPresent) {
                notifySimAlert(context, "SIM Card Removed", "Device SIM card was ejected or removed.")
            }
        }
    }

    private fun notifySimAlert(context: Context, title: String, message: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(context, PhantomGuardApplication.CHANNEL_ALERTS)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1001, notification)
    }
}
