package com.healthtrackai.app.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [FoundationMetadataEntity::class, LocalHealthEntryEntity::class],
    exportSchema = true,
    version = 2,
)
abstract class HealthTrackDatabase : RoomDatabase() {
    abstract fun foundationMetadataDao(): FoundationMetadataDao
    abstract fun localHealthEntryDao(): LocalHealthEntryDao

    companion object {
        val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS `local_health_entries` (
                        `id` TEXT NOT NULL,
                        `featureRoute` TEXT NOT NULL,
                        `title` TEXT NOT NULL,
                        `content` TEXT NOT NULL,
                        `recordedAtEpochMillis` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )""".trimIndent(),
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_local_health_entries_featureRoute_recordedAtEpochMillis` " +
                        "ON `local_health_entries` (`featureRoute`, `recordedAtEpochMillis`)",
                )
            }
        }
    }
}
