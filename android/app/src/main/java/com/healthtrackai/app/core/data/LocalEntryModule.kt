package com.healthtrackai.app.core.data

import com.healthtrackai.app.core.domain.LocalHealthEntryRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class LocalEntryModule {
    @Binds
    @Singleton
    abstract fun bindLocalHealthEntryRepository(
        implementation: LocalHealthEntryRepositoryImpl,
    ): LocalHealthEntryRepository
}
