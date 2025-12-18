package org.reas.tracker.database

import androidx.room.TypeConverter
import kotlinx.serialization.json.Json


class Converters {
    @TypeConverter
    fun encodeList(list: MutableList<String>): String {
        return Json.encodeToString(list)
    }

    @TypeConverter
    fun decodeList(value: String): MutableList<String> {
        return Json.decodeFromString(value)
    }
}