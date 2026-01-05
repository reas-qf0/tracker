package org.reas.tracker.util

import java.text.SimpleDateFormat
import java.util.Date

object DateTimeFormatter {
    private val dateFormatter = SimpleDateFormat.getDateInstance()
    private val dateTimeFormatter = SimpleDateFormat.getDateTimeInstance()

    fun timeMsToString(time: Long): String {
        var minutesTotal = time / 1000 / 60
        if (time % (1000 * 60) > 1000 * 30)
            minutesTotal++

        val minutes = minutesTotal % 60
        val hours = (minutesTotal / 60) % 24
        val days = (minutesTotal / 60 / 24)
        return "${days}d ${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}"
    }

    fun dateToString(timestamp: Long): String {
        val secondsPassed = (System.currentTimeMillis() - timestamp) / 1000
        if (secondsPassed < 60L)
            return "$secondsPassed secs ago"
        if (secondsPassed < 60L * 60L)
            return "${secondsPassed / 60} mins ago"
        if (secondsPassed < 60L * 60L * 24L)
            return "${secondsPassed / 60 / 60} hrs ago"
        return dateFormatter.format(Date(timestamp))
    }

    fun dateTimeToString(timestamp: Long): String {
        return dateTimeFormatter.format(Date(timestamp))
    }
}