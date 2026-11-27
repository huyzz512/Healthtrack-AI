package com.healthtrackai.app.core.data

import com.healthtrackai.app.core.database.LocalHealthEntryDao
import com.healthtrackai.app.core.database.LocalHealthEntryEntity
import com.healthtrackai.app.core.domain.LocalHealthEntry
import com.healthtrackai.app.core.domain.LocalHealthEntryRepository
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class LocalHealthEntryRepositoryImpl @Inject constructor(
    private val dao: LocalHealthEntryDao,
) : LocalHealthEntryRepository {
    override fun observe(featureRoute: String): Flow<List<LocalHealthEntry>> =
        dao.observe(featureRoute).map { entries -> entries.map(LocalHealthEntryEntity::toDomain) }

    override suspend fun add(featureRoute: String, title: String, content: String): LocalHealthEntry {
        val entry = LocalHealthEntry(
            id = UUID.randomUUID().toString(),
            featureRoute = featureRoute,
            title = title,
            content = content,
            recordedAtEpochMillis = System.currentTimeMillis(),
        )
        dao.upsert(LocalHealthEntryEntity.fromDomain(entry))
        return entry
    }

    override suspend fun update(entry: LocalHealthEntry) = dao.upsert(LocalHealthEntryEntity.fromDomain(entry))

    override suspend fun delete(id: String) = dao.delete(id)
}
