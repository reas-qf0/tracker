package com.reas.tracker2.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import androidx.room.Update

@Dao
interface CustomImageDao {
    @Insert(onConflict = REPLACE)
    fun insert(customImage: CustomImage)

    @Update
    fun update(customImage: CustomImage)

    @Delete
    fun delete(customImage: CustomImage)

    @Query("SELECT filename FROM custom_images WHERE arguments = :arguments")
    fun get(arguments: List<String>): String?
}