package com.healthtrackai.app.core.network.contract

import kotlinx.serialization.Serializable
import retrofit2.http.GET

interface FoundationHealthApi {
    @GET("health")
    suspend fun health(): ApiDataEnvelope<HealthStatusDto>
}

@Serializable
data class HealthStatusDto(
    val status: String,
    val database: String,
    val version: String,
) {
    fun toDomain(): HealthStatus = HealthStatus(
        status = ServiceStatus.fromWire(status),
        database = DatabaseStatus.fromWire(database),
        version = version,
    )
}

data class HealthStatus(
    val status: ServiceStatus,
    val database: DatabaseStatus,
    val version: String,
)

enum class ServiceStatus {
    OK,
    UNKNOWN,
    ;

    companion object {
        fun fromWire(value: String): ServiceStatus = if (value == "ok") OK else UNKNOWN
    }
}

enum class DatabaseStatus {
    CONNECTED,
    UNKNOWN,
    ;

    companion object {
        fun fromWire(value: String): DatabaseStatus = if (value == "connected") CONNECTED else UNKNOWN
    }
}
