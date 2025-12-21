package dev.nutrisport.shared.util

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
fun Long.toLocalDateString(): String {
    val instant = Instant.fromEpochMilliseconds(this)
    val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    return localDateTime.date.toString()
}

@OptIn(ExperimentalTime::class)
fun Long.toLocalDateTimeString(): String {
    return this.toLocalDateTime().toString()
}

@OptIn(ExperimentalTime::class)
fun Long.toLocalDateTime(): LocalDateTime {
    val instant = Instant.fromEpochMilliseconds(this)
    return instant.toLocalDateTime(TimeZone.currentSystemDefault())
}


@OptIn(ExperimentalTime::class)
fun Long.toFormattedString(pattern: String): String {
    val instant = Instant.fromEpochMilliseconds(this)
    val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    return when (pattern) {
        "yyyy-MM-dd" -> "${dateTime.year}-${dateTime.month.number.toString().padStart(2, '0')}-${dateTime.day.toString().padStart(2, '0')}"
        "yyyy.MM.dd" -> "${dateTime.year}.${dateTime.month.number.toString().padStart(2, '0')}.${dateTime.day.toString().padStart(2, '0')}"
        "yyyy-MM-dd HH:mm:ss" -> "${dateTime.year}-${dateTime.month.number.toString().padStart(2, '0')}-${dateTime.day.toString().padStart(2, '0')} " +
                "${dateTime.hour.toString().padStart(2, '0')}:${dateTime.minute.toString().padStart(2, '0')}:${dateTime.second.toString().padStart(2, '0')}"
        "yyyy-MM-dd HH:mm" -> "${dateTime.year}-${dateTime.month.number.toString().padStart(2, '0')}-${dateTime.day.toString().padStart(2, '0')} " +
                "${dateTime.hour.toString().padStart(2, '0')}:${dateTime.minute.toString().padStart(2, '0')}"
        "HH:mm:ss" -> "${dateTime.hour.toString().padStart(2, '0')}:${dateTime.minute.toString().padStart(2, '0')}:${dateTime.second.toString().padStart(2, '0')}"
        "yyyy/MM/dd" -> "${dateTime.year}/${dateTime.month.number.toString().padStart(2, '0')}/${dateTime.day.toString().padStart(2, '0')}"
        "MM-dd" -> "${dateTime.month.number.toString().padStart(2, '0')}-${dateTime.day.toString().padStart(2, '0')}"
        "yyyy年MM月dd日" -> "${dateTime.year}年${dateTime.month.number.toString().padStart(2, '0')}月${dateTime.day.toString().padStart(2, '0')}日"
        "HH:mm" -> "${dateTime.hour.toString().padStart(2, '0')}:${dateTime.minute.toString().padStart(2, '0')}"
        "yyyy-MM" -> "${dateTime.year}-${dateTime.month.number.toString().padStart(2, '0')}"
        "MM/dd" -> "${dateTime.month.number.toString().padStart(2, '0')}/${dateTime.day.toString().padStart(2, '0')}"
        "yyyyMMddHHmmss" -> "${dateTime.year}${dateTime.month.number.toString().padStart(2, '0')}${dateTime.day.toString().padStart(2, '0')}" +
                "${dateTime.hour.toString().padStart(2, '0')}${dateTime.minute.toString().padStart(2, '0')}${dateTime.second.toString().padStart(2, '0')}"
        else -> dateTime.toString()
    }
}
