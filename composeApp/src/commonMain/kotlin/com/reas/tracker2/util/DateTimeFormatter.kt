package com.reas.tracker2.util


object DateTimeFormatter {
    fun timeMsToString(time: Long): String {
        var minutesTotal = time / 1000 / 60
        if (time % (1000 * 60) > 1000 * 30)
            minutesTotal++

        val minutes = minutesTotal % 60
        val hours = (minutesTotal / 60) % 24
        val days = (minutesTotal / 60 / 24)
        return "${days}d ${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}"
    }
}