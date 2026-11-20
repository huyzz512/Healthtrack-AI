package com.healthtrackai.app.core.healthconnect.domain

sealed interface HealthConnectAvailability {
    data object Available : HealthConnectAvailability
    data object ProviderUpdateRequired : HealthConnectAvailability
    data object Unavailable : HealthConnectAvailability
    data class Unknown(val sdkStatus: Int) : HealthConnectAvailability
}

interface HealthConnectGateway {
    fun availability(): HealthConnectAvailability
}
