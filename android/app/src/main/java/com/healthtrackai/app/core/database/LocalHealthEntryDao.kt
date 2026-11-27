package com.healthtrackai.app.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LocalHealthEntryDao {
    @Query("SELECT * FROM local_health_entries WHERE featureRoute = :featureRoute ORDER BY recordedAtEpochMillis DESC")
    fun observe(featureRoute: String): Flow<List<LocalHealthEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: LocalHealthEntryEntity)

    @Query("DELETE FROM local_health_entries WHERE id = :id")
    suspend fun delete(id: String)
}
