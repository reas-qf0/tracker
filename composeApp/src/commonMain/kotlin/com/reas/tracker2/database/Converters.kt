package com.reas.tracker2.database

import androidx.room.TypeConverter
import com.reas.tracker2.shared.EventInfo
import kotlinx.serialization.json.Json
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Instant


class Converters {
    @TypeConverter
    fun eventInfoListToString(list: MutableList<EventInfo?>): String = Json.encodeToString(list)

    @TypeConverter
    fun stringToEventInfoList(value: String): MutableList<EventInfo?> = Json.decodeFromString(value)

    @TypeConverter
    fun instantToLong(instant: Instant): Long = instant.toEpochMilliseconds()

    @TypeConverter
    fun longToInstant(long: Long): Instant = Instant.fromEpochMilliseconds(long)

    @TypeConverter
    fun durationToLong(duration: Duration): Long = duration.inWholeMilliseconds

    @TypeConverter
    fun longToDuration(long: Long): Duration = long.milliseconds

    @TypeConverter
    fun idListToString(list: List<Long>): String = list.joinToString(",")

    @TypeConverter
    fun stringToIdList(string: String): List<Long> = string.split(",").map { it.toLong() }
}