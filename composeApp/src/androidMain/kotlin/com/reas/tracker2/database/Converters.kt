package com.reas.tracker2.database

import androidx.room.TypeConverter
import kotlinx.serialization.json.Json


class Converters {
    @TypeConverter
    fun encodeMutableList(list: MutableList<String>): String = Json.encodeToString(list)

    @TypeConverter
    fun decodeMutableList(value: String): MutableList<String> = Json.decodeFromString(value)

    @TypeConverter
    fun encodeList(list: List<String>): String = Json.encodeToString(list)

    @TypeConverter
    fun decodeList(value: String): List<String> = Json.decodeFromString(value)
}