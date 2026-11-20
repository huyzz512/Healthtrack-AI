package com.healthtrackai.app.core.healthconnect.data

import androidx.health.connect.client.HealthConnectClient
import com.healthtrackai.app.core.healthconnect.domain.HealthConnectAvailability
import org.junit.Assert.assertEquals
import org.junit.Test

class HealthConnectAvailabilityMapperTest {
    @Test
    fun `maps all published Health Connect availability statuses`() {
        assertEquals(
            HealthConnectAvailability.Available,
            HealthConnectAvailabilityMapper.fromSdkStatus(HealthConnectClient.SDK_AVAILABLE),
        )
        assertEquals(
            HealthConnectAvailability.ProviderUpdateRequired,
            HealthConnectAvailabilityMapper.fromSdkStatus(
                HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED,
            ),
        )
        assertEquals(
            HealthConnectAvailability.Unavailable,
            HealthConnectAvailabilityMapper.fromSdkStatus(HealthConnectClient.SDK_UNAVAILABLE),
        )
    }

    @Test
    fun `preserves unknown SDK status without crashing`() {
        assertEquals(
            HealthConnectAvailability.Unknown(Int.MAX_VALUE),
            HealthConnectAvailabilityMapper.fromSdkStatus(Int.MAX_VALUE),
        )
    }
}
