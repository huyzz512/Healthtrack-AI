package com.healthtrackai.app.core.network.contract

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class ApiDataEnvelope<T>(val data: T)

@Serializable
data class ApiPageMeta(
    val page: Int,
    val pageSize: Int,
    val total: Int,
)

@Serializable
data class ApiPageEnvelope<T>(
    val data: List<T>,
    val meta: ApiPageMeta,
)

@Serializable
data class ApiErrorEnvelope(val error: ApiErrorDto)

@Serializable
data class ApiErrorDto(
    val code: String,
    val message: String,
    val details: JsonElement? = null,
    val traceId: String,
) {
    fun stableCode(): ApiErrorCode = ApiErrorCode.fromWire(code)
}

enum class ApiErrorCode(val wireValue: String) {
    AUTH_INVALID_CREDENTIALS("AUTH_INVALID_CREDENTIALS"),
    AUTH_TOKEN_EXPIRED("AUTH_TOKEN_EXPIRED"),
    AUTH_REFRESH_INVALID("AUTH_REFRESH_INVALID"),
    AUTH_ACCOUNT_LOCKED("AUTH_ACCOUNT_LOCKED"),
    VALIDATION_FAILED("VALIDATION_FAILED"),
    FORBIDDEN_RESOURCE("FORBIDDEN_RESOURCE"),
    RESOURCE_NOT_FOUND("RESOURCE_NOT_FOUND"),
    FILE_TYPE_UNSUPPORTED("FILE_TYPE_UNSUPPORTED"),
    FILE_TOO_LARGE("FILE_TOO_LARGE"),
    FILE_STORAGE_UNAVAILABLE("FILE_STORAGE_UNAVAILABLE"),
    HEALTH_METRIC_INVALID("HEALTH_METRIC_INVALID"),
    HEALTH_SYNC_DUPLICATE("HEALTH_SYNC_DUPLICATE"),
    HEALTH_SYNC_REJECTED("HEALTH_SYNC_REJECTED"),
    HEALTH_CONNECT_CONSENT_REQUIRED("HEALTH_CONNECT_CONSENT_REQUIRED"),
    AI_CONSENT_REQUIRED("AI_CONSENT_REQUIRED"),
    AI_PROVIDER_UNAVAILABLE("AI_PROVIDER_UNAVAILABLE"),
    REMINDER_INVALID_SCHEDULE("REMINDER_INVALID_SCHEDULE"),
    REPORT_NOT_READY("REPORT_NOT_READY"),
    REPORT_GENERATION_FAILED("REPORT_GENERATION_FAILED"),
    VERSION_CONFLICT("VERSION_CONFLICT"),
    DATABASE_UNAVAILABLE("DATABASE_UNAVAILABLE"),
    INTERNAL_SERVER_ERROR("INTERNAL_SERVER_ERROR"),
    UNKNOWN("UNKNOWN"),
    ;

    companion object {
        fun fromWire(value: String): ApiErrorCode = entries.firstOrNull { it.wireValue == value } ?: UNKNOWN
    }
}
