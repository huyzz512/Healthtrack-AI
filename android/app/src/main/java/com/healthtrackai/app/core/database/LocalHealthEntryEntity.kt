package com.healthtrackai.app.core.database

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.healthtrackai.app.core.domain.LocalHealthEntry

@Entity(
    tableName = "local_health_entries",
    indices = [Index(value = ["featureRoute", "recordedAtEpochMillis"])],
)
data class LocalHealthEntryEntity(
    @PrimaryKey val id: String,
    val featureRoute: String,
    val title: String,
    val content: String,
    val recordedAtEpochMillis: Long,
) {
    fun toDomain() = LocalHealthEntry(id, featureRoute, title, content, recordedAtEpochMillis)

    companion object {
        fun fromDomain(entry: LocalHealthEntry) = LocalHealthEntryEntity(
            id = entry.id,
            featureRoute = entry.featureRoute,
            title = entry.title,
            content = entry.content,
            recordedAtEpochMillis = entry.recordedAtEpochMillis,
        )
    }
}
