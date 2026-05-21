package com.sujin.nubloompilot.repository

import android.content.Context
import androidx.health.connect.client.HealthConnectClient

class HealthConnectRepository(
    private val context: Context
) {
    fun getAvailabilityStatus(): Int {
        return HealthConnectClient.getSdkStatus(context)
    }

    fun isHealthConnectAvailable(): Boolean {
        return getAvailabilityStatus() == HealthConnectClient.SDK_AVAILABLE
    }

    fun getClient(): HealthConnectClient? {
        return if (isHealthConnectAvailable()) {
            HealthConnectClient.getOrCreate(context)
        } else {
            null
        }
    }
}