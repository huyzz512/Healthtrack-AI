package com.healthtrackai.app.core.di

import android.content.Context
import com.healthtrackai.app.core.domain.LocalHealthEntryRepository
import com.healthtrackai.app.core.healthconnect.domain.HealthConnectGateway
import com.healthtrackai.app.core.network.contract.FoundationHealthApi
import com.healthtrackai.app.feature.auth.AuthRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface HealthTrackEntryPoint {
    fun localHealthEntryRepository(): LocalHealthEntryRepository
    fun healthConnectGateway(): HealthConnectGateway
    fun foundationHealthApi(): FoundationHealthApi
    fun authRepository(): AuthRepository
}

fun healthTrackEntryPoint(context: Context): HealthTrackEntryPoint =
    EntryPointAccessors.fromApplication(context.applicationContext, HealthTrackEntryPoint::class.java)
