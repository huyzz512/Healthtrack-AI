package com.healthtrackai.app.core.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [FoundationMetadataEntity::class],
    exportSchema = true,
    version = 1,
)
abstract class HealthTrackDatabase : RoomDatabase() {
    abstract fun foundationMetadataDao(): FoundationMetadataDao
}
