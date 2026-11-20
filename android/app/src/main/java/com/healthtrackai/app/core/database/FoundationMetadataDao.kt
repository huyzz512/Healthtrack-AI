package com.healthtrackai.app.core.database

import androidx.room.Dao
import androidx.room.Query

@Dao
interface FoundationMetadataDao {
    @Query("SELECT * FROM foundation_metadata WHERE `key` = :key LIMIT 1")
    suspend fun findByKey(key: String): FoundationMetadataEntity?
}
