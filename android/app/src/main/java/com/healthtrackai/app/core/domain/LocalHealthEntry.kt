package com.healthtrackai.app.core.domain

import kotlinx.coroutines.flow.Flow

data class LocalHealthEntry(
    val id: String,
    val featureRoute: String,
    val title: String,
    val content: String,
    val recordedAtEpochMillis: Long,
)

interface LocalHealthEntryRepository {
    fun observe(featureRoute: String): Flow<List<LocalHealthEntry>>
    suspend fun add(featureRoute: String, title: String, content: String): LocalHealthEntry
    suspend fun update(entry: LocalHealthEntry)
    suspend fun delete(id: String)
}
