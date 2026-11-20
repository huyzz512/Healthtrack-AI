package com.healthtrackai.app.core.network.contract

import java.time.Instant
import java.time.LocalDate
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FoundationContractTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `decodes the approved health envelope and maps known values`() {
        val envelope = json.decodeFromString<ApiDataEnvelope<HealthStatusDto>>(
            """
            {
              "data": {
                "status": "ok",
                "database": "connected",
                "version": "foundation-v0.1",
                "futureOptionalField": true
              }
            }
            """.trimIndent(),
        )

        assertEquals(
            HealthStatus(ServiceStatus.OK, DatabaseStatus.CONNECTED, "foundation-v0.1"),
            envelope.data.toDomain(),
        )
    }

    @Test
    fun `maps unknown server enum and error values to safe fallbacks`() {
        val health = HealthStatusDto("degraded", "replica", "future").toDomain()
        assertEquals(ServiceStatus.UNKNOWN, health.status)
        assertEquals(DatabaseStatus.UNKNOWN, health.database)
        assertEquals(ApiErrorCode.UNKNOWN, ApiErrorCode.fromWire("FUTURE_ERROR"))
        assertEquals(CanonicalUnit.UNKNOWN, CanonicalUnit.fromWire("future-unit"))
        assertEquals(DataSource.UNKNOWN, DataSource.fromWire("FUTURE_SOURCE"))
        assertEquals(MoodType.UNKNOWN, MoodType.fromWire("FUTURE_MOOD"))
        assertEquals(AlertSeverity.UNKNOWN, AlertSeverity.fromWire("FUTURE_SEVERITY"))
        assertEquals(ReportStatus.UNKNOWN, ReportStatus.fromWire("FUTURE_STATUS"))
        assertEquals(ConsentType.UNKNOWN, ConsentType.fromWire("FUTURE_CONSENT"))
        assertEquals(ConsentStatus.UNKNOWN, ConsentStatus.fromWire("FUTURE_CONSENT_STATUS"))
    }

    @Test
    fun `maps the released Foundation shared enum values`() {
        assertEquals(DataSource.IMPORTED, DataSource.fromWire("IMPORTED"))
        assertEquals(DataSource.SYSTEM_GENERATED, DataSource.fromWire("SYSTEM_GENERATED"))
        assertEquals(MoodType.NORMAL, MoodType.fromWire("NORMAL"))
        assertEquals(AlertSeverity.CRITICAL, AlertSeverity.fromWire("CRITICAL"))
        assertEquals(ReportStatus.REQUESTED, ReportStatus.fromWire("REQUESTED"))
        assertEquals(ReportStatus.READY, ReportStatus.fromWire("READY"))
        assertEquals(ConsentType.AI_HEALTH_CONTEXT, ConsentType.fromWire("AI_HEALTH_CONTEXT"))
        assertEquals(ConsentStatus.GRANTED, ConsentStatus.fromWire("GRANTED"))
    }

    @Test
    fun `accepts only contracted UTC instants and calendar dates`() {
        assertEquals(
            Instant.parse("2026-11-17T03:30:00Z"),
            ContractFormats.parseUtcInstantOrNull("2026-11-17T03:30:00Z"),
        )
        assertNull(ContractFormats.parseUtcInstantOrNull("2026-11-17T10:30:00+07:00"))
        assertEquals(
            LocalDate.of(2026, 11, 17),
            ContractFormats.parseLocalDateOrNull("2026-11-17"),
        )
        assertNull(ContractFormats.parseLocalDateOrNull("2026-02-30"))
    }

    @Test
    fun `decodes the approved error envelope without branching on message`() {
        val envelope = json.decodeFromString<ApiErrorEnvelope>(
            """
            {
              "error": {
                "code": "DATABASE_UNAVAILABLE",
                "message": "Database is unavailable.",
                "details": null,
                "traceId": "trace-123"
              }
            }
            """.trimIndent(),
        )

        assertEquals(ApiErrorCode.DATABASE_UNAVAILABLE, envelope.error.stableCode())
        assertEquals("trace-123", envelope.error.traceId)
    }
}
