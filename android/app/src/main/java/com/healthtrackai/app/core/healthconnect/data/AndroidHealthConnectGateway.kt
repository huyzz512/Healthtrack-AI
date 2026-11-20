package com.healthtrackai.app.core.healthconnect.data

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import com.healthtrackai.app.core.healthconnect.domain.HealthConnectAvailability
import com.healthtrackai.app.core.healthconnect.domain.HealthConnectGateway
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidHealthConnectGateway @Inject constructor(
    @ApplicationContext private val context: Context,
) : HealthConnectGateway {
    override fun availability(): HealthConnectAvailability =
        HealthConnectAvailabilityMapper.fromSdkStatus(HealthConnectClient.getSdkStatus(context))
}
