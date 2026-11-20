package com.healthtrackai.app.core.network.contract

interface WireEnum {
    val wireValue: String
}

private inline fun <reified T> enumFromWire(
    value: String,
    unknown: T,
): T where T : Enum<T>, T : WireEnum =
    enumValues<T>().firstOrNull { it.wireValue == value } ?: unknown

enum class DataSource(override val wireValue: String) : WireEnum {
    USER_INPUT("USER_INPUT"),
    HEALTH_CONNECT("HEALTH_CONNECT"),
    IMPORTED("IMPORTED"),
    SYSTEM_GENERATED("SYSTEM_GENERATED"),
    UNKNOWN("UNKNOWN"),
    ;

    companion object {
        fun fromWire(value: String): DataSource = enumFromWire(value, UNKNOWN)
    }
}

enum class MoodType(override val wireValue: String) : WireEnum {
    HAPPY("HAPPY"),
    NORMAL("NORMAL"),
    SAD("SAD"),
    STRESSED("STRESSED"),
    ANXIOUS("ANXIOUS"),
    UNKNOWN("UNKNOWN"),
    ;

    companion object {
        fun fromWire(value: String): MoodType = enumFromWire(value, UNKNOWN)
    }
}

enum class AlertSeverity(override val wireValue: String) : WireEnum {
    INFO("INFO"),
    WARNING("WARNING"),
    CRITICAL("CRITICAL"),
    UNKNOWN("UNKNOWN"),
    ;

    companion object {
        fun fromWire(value: String): AlertSeverity = enumFromWire(value, UNKNOWN)
    }
}

enum class ReportStatus(override val wireValue: String) : WireEnum {
    REQUESTED("REQUESTED"),
    GENERATING("GENERATING"),
    READY("READY"),
    FAILED("FAILED"),
    UNKNOWN("UNKNOWN"),
    ;

    companion object {
        fun fromWire(value: String): ReportStatus = enumFromWire(value, UNKNOWN)
    }
}

enum class ConsentType(override val wireValue: String) : WireEnum {
    AI_HEALTH_CONTEXT("AI_HEALTH_CONTEXT"),
    AI_MEDICAL_DOCUMENT("AI_MEDICAL_DOCUMENT"),
    AI_REPORT_SUMMARY("AI_REPORT_SUMMARY"),
    UNKNOWN("UNKNOWN"),
    ;

    companion object {
        fun fromWire(value: String): ConsentType = enumFromWire(value, UNKNOWN)
    }
}

enum class ConsentStatus(override val wireValue: String) : WireEnum {
    GRANTED("GRANTED"),
    REVOKED("REVOKED"),
    UNKNOWN("UNKNOWN"),
    ;

    companion object {
        fun fromWire(value: String): ConsentStatus = enumFromWire(value, UNKNOWN)
    }
}
