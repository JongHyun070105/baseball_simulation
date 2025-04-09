package com.example.baseball_simulation_app.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateUtils {
    private const val API_DATE_FORMAT = "yyyyMMdd"
    private const val DISPLAY_DATE_FORMAT = "yyyy. MM. dd"

    fun convertDateToApiFormat(date: Date): String {
        val formatter = SimpleDateFormat(API_DATE_FORMAT, Locale.getDefault())
        return formatter.format(date)
    }

    fun convertDateToDisplayFormat(date: Date): String {
        val formatter = SimpleDateFormat(DISPLAY_DATE_FORMAT, Locale.getDefault())
        return formatter.format(date)
    }

    fun extractDateFromTimestamp(timestamp: Long): String {
        val date = Date(timestamp)
        val formatter = SimpleDateFormat(DISPLAY_DATE_FORMAT, Locale.getDefault())
        return formatter.format(date)
    }
}