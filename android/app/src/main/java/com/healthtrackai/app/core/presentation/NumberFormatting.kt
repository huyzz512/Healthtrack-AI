package com.healthtrackai.app.core.presentation

import java.text.NumberFormat
import java.util.Locale

fun formatVietnameseInteger(value: Int): String =
    NumberFormat.getIntegerInstance(Locale.forLanguageTag("vi-VN")).format(value)
