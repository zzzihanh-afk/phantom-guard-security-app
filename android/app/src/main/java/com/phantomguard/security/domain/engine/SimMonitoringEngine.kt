package com.phantomguard.security.domain.engine

import android.content.Context
import android.telephony.TelephonyManager
import java.security.MessageDigest

data class SimStatusReport(
    val simState: Int,
    val carrierName: String,
    val countryIso: String,
    val simHash: String,
    val isSimPresent: Boolean
)

class SimMonitoringEngine(private val context: Context) {

    fun checkSimStatus(): SimStatusReport {
        val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val simState = telephonyManager.simState
        val isPresent = simState != TelephonyManager.SIM_STATE_ABSENT && simState != TelephonyManager.SIM_STATE_UNKNOWN

        val carrier = telephonyManager.simOperatorName.ifEmpty { telephonyManager.networkOperatorName }.ifEmpty { "Unknown Carrier" }
        val country = telephonyManager.simCountryIso.ifEmpty { "Unknown" }

        val rawSignature = "$carrier:$country:$simState"
        val hash = sha256(rawSignature)

        return SimStatusReport(
            simState = simState,
            carrierName = carrier,
            countryIso = country,
            simHash = hash,
            isSimPresent = isPresent
        )
    }

    private fun sha256(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
