package com.healthtrackai.app.core.healthconnect.data

import androidx.health.connect.client.HealthConnectClient
import com.healthtrackai.app.core.healthconnect.domain.HealthConnectAvailability

object HealthConnectAvailabilityMapper {
    fun fromSdkStatus(sdkStatus: Int): HealthConnectAvailability = when (sdkStatus) {
        HealthConnectClient.SDK_AVAILABLE -> HealthConnectAvailability.Available
        HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED -> {
            HealthConnectAvailability.ProviderUpdateRequired
        }

        HealthConnectClient.SDK_UNAVAILABLE -> HealthConnectAvailability.Unavailable
        else -> HealthConnectAvailability.Unknown(sdkStatus)
    }
}
