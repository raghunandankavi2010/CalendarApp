package com.example.googlecalendarclone


import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.UUID

data class Event(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String = "",
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val location: String = "",
    val color: EventColor = EventColor.values().random(),
    val isAllDay: Boolean = false,
    val recurrence: RecurrenceType = RecurrenceType.NONE,
    val attendees: List<String> = emptyList()
)

enum class EventColor(val hexColor: Long) {
    BLUE(0xFF4285F4),
    RED(0xFFEA4335),
    YELLOW(0xFFFBBC04),
    GREEN(0xFF34A853),
    PURPLE(0xFF9C27B0),
    TEAL(0xFF009688),
    ORANGE(0xFFFF9800),
    PINK(0xFFE91E63)
}

enum class RecurrenceType {
    NONE, DAILY, WEEKLY, MONTHLY, YEARLY
}


fun Event.getDuration(): String {
    return if (isAllDay) {
        "All day"
    } else {
        val start = startTime.toLocalTime()
        val end = endTime.toLocalTime()
        "${start.format(java.time.format.DateTimeFormatter.ofPattern("h:mm a"))} - " +
                "${end.format(java.time.format.DateTimeFormatter.ofPattern("h:mm a"))}"
    }
}


fun Event.isMultiDay(): Boolean {
    return startTime.toLocalDate() != endTime.toLocalDate()
}