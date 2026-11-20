package com.healthtrackai.app.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "foundation_metadata")
data class FoundationMetadataEntity(
    @PrimaryKey val key: String,
    val value: String,
)
