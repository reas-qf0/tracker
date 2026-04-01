package com.reas.tracker2.util

import com.reas.tracker2.platform.IS_DEBUG
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

fun Duration.toDisplayString() = when {
    this.isNegative() -> "..."
    (IS_DEBUG && this < 1.hours) || this < 1.minutes -> inWholeSeconds.seconds.toString()
    else -> inWholeMinutes.minutes.toString()
}