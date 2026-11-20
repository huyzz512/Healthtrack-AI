package com.healthtrackai.app.core.healthconnect.di

import com.healthtrackai.app.core.healthconnect.data.AndroidHealthConnectGateway
import com.healthtrackai.app.core.healthconnect.domain.HealthConnectGateway
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class HealthConnectModule {
    @Binds
    @Singleton
    abstract fun bindHealthConnectGateway(implementation: AndroidHealthConnectGateway): HealthConnectGateway
}
