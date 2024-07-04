package com.practicum.playlistmaker.domain.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object TimeFormat {
    private const val TIME_FORMAT = "mm:ss"
    const val ZERO_TIME = "00:00"

    fun format(time: Int): String {
        val date = Date(time.toLong()) // Конвертирование Int в Long и создание объекта Date
        return SimpleDateFormat(TIME_FORMAT, Locale.getDefault()).format(date)
    }
}
