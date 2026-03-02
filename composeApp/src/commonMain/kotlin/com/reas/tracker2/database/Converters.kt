package com.reas.tracker2.database

import androidx.room.TypeConverter
import com.reas.tracker2.database.entities.EventEntity
import com.reas.tracker2.database.entities.PlayEntity
import com.reas.tracker2.shared.Album
import com.reas.tracker2.shared.Event
import com.reas.tracker2.shared.Metadata
import com.reas.tracker2.shared.Play
import com.reas.tracker2.shared.Source
import com.reas.tracker2.shared.Track
import com.reas.tracker2.shared.TrackWithOptionalAlbum
import kotlinx.serialization.json.Json
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.DurationUnit
import kotlin.time.Instant
import kotlin.time.toDuration


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