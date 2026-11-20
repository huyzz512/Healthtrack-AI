package com.healthtrackai.app.core.database

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
    ): HealthTrackDatabase = Room.databaseBuilder(
        context,
        HealthTrackDatabase::class.java,
        "healthtrack.db",
    ).build()

    @Provides
    fun provideFoundationMetadataDao(
        database: HealthTrackDatabase,
    ): FoundationMetadataDao = database.foundationMetadataDao()
}
