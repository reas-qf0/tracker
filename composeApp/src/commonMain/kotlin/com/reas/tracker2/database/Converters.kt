package com.reas.tracker2.database

import androidx.room.TypeConverter
import com.reas.tracker2.shared.Play
import kotlinx.serialization.json.Json
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Instant


class Converters {
    @TypeConverter
    fun eventInfoListToString(list: MutableList<Play.EventInfo?>): String = Json.encodeToString(list)

    @TypeConverter
    fun stringToEventInfoList(value: String): MutableList<Play.EventInfo?> = Json.decodeFromString(value)

    @TypeConverter
    fun instantToLong(instant: Instant): Long = instant.toEpochMilliseconds()

    @TypeConverter
    fun durationToLong(duration: Duration): Long = duration.inWholeMilliseconds

    @TypeConverter
    fun longToDuration(long: Long): Duration = long.milliseconds
}