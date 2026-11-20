package com.healthtrackai.app.core.network.contract

import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.ResolverStyle

object ContractFormats {
    private val utcInstantPattern = Regex("^\\d{4}-\\d{2}-\\d{2}T.*Z$")
    private val localDatePattern = Regex("^\\d{4}-\\d{2}-\\d{2}$")
    private val strictLocalDateFormatter = DateTimeFormatter.ISO_LOCAL_DATE.withResolverStyle(ResolverStyle.STRICT)

    fun parseUtcInstantOrNull(value: String): Instant? {
        if (!utcInstantPattern.matches(value)) return null
        return runCatching { Instant.parse(value) }.getOrNull()
    }

    fun parseLocalDateOrNull(value: String): LocalDate? {
        if (!localDatePattern.matches(value)) return null
        return runCatching { LocalDate.parse(value, strictLocalDateFormatter) }.getOrNull()
    }
}

enum class CanonicalUnit(val wireValue: String) {
    CENTIMETRE("cm"),
    KILOGRAM("kg"),
    BMI("kg/m²"),
    BEATS_PER_MINUTE("bpm"),
    MILLIMETRES_OF_MERCURY("mmHg"),
    CELSIUS("°C"),
    BREATHS_PER_MINUTE("breaths/minute"),
    PERCENT("percent"),
    STEPS("steps"),
    MINUTES("minutes"),
    MILLILITRES("millilitres"),
    KILOCALORIES("kcal"),
    UNKNOWN("unknown"),
    ;

    companion object {
        fun fromWire(value: String): CanonicalUnit = entries.firstOrNull { it.wireValue == value } ?: UNKNOWN
    }
}
