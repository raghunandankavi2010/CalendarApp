package com.example.googlecalendarclone

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventRepository @Inject constructor() {
    private val _events = MutableStateFlow(generateSampleEvents())
    val events = _events.asStateFlow()

    fun getEventsForDate(date: LocalDate): Flow<List<Event>> {
        return events.map { eventList ->
            eventList.filter { event ->
                val eventDate = event.startTime.toLocalDate()
                eventDate == date ||
                        (event.isMultiDay() && date.isAfter(eventDate) && date.isBefore(event.endTime.toLocalDate()))
            }.sortedBy { it.startTime }
        }
    }

    fun getEventsForMonth(year: Int, month: Int): Flow<List<Event>> {
        return events.map { eventList ->
            eventList.filter { event ->
                val eventDate = event.startTime.toLocalDate()
                eventDate.year == year && eventDate.monthValue == month
            }
        }
    }

    fun addEvent(event: Event) {
        _events.value = _events.value + event
    }

    fun deleteEvent(eventId: String) {
        _events.value = _events.value.filter { it.id != eventId }
    }

    fun updateEvent(event: Event) {
        _events.value = _events.value.map { if (it.id == event.id) event else it }
    }

    private fun generateSampleEvents(): List<Event> {
        val today = LocalDate.now()
        return listOf(
            // Today overlaps
            Event(
                title = "Team Standup",
                startTime = LocalDateTime.of(today, LocalTime.of(9, 0)),
                endTime = LocalDateTime.of(today, LocalTime.of(9, 30)),
                color = EventColor.BLUE,
                location = "Conference Room A"
            ),
            Event(
                title = "Coding Session",
                startTime = LocalDateTime.of(today, LocalTime.of(9, 15)),
                endTime = LocalDateTime.of(today, LocalTime.of(11, 0)),
                color = EventColor.PINK,
                location = "Desk"
            ),
            Event(
                title = "Product Review",
                startTime = LocalDateTime.of(today, LocalTime.of(14, 0)),
                endTime = LocalDateTime.of(today, LocalTime.of(15, 30)),
                color = EventColor.GREEN,
                location = "Zoom"
            ),
            Event(
                title = "Brainstorming",
                startTime = LocalDateTime.of(today, LocalTime.of(14, 30)),
                endTime = LocalDateTime.of(today, LocalTime.of(16, 0)),
                color = EventColor.ORANGE,
                location = "Meeting Room 2"
            ),
            Event(
                title = "One-on-one",
                startTime = LocalDateTime.of(today, LocalTime.of(15, 0)),
                endTime = LocalDateTime.of(today, LocalTime.of(15, 45)),
                color = EventColor.PURPLE,
                location = "Office"
            ),
            // Yesterday overlaps (if within same month)
            Event(
                title = "Design Sync",
                startTime = LocalDateTime.of(today.minusDays(1), LocalTime.of(10, 0)),
                endTime = LocalDateTime.of(today.minusDays(1), LocalTime.of(11, 30)),
                color = EventColor.TEAL
            ),
            Event(
                title = "UI Feedback",
                startTime = LocalDateTime.of(today.minusDays(1), LocalTime.of(11, 0)),
                endTime = LocalDateTime.of(today.minusDays(1), LocalTime.of(12, 0)),
                color = EventColor.RED
            ),
            // Day after tomorrow overlaps
            Event(
                title = "Strategy Meeting",
                startTime = LocalDateTime.of(today.plusDays(2), LocalTime.of(13, 0)),
                endTime = LocalDateTime.of(today.plusDays(2), LocalTime.of(15, 0)),
                color = EventColor.BLUE
            ),
            Event(
                title = "Budget Planning",
                startTime = LocalDateTime.of(today.plusDays(2), LocalTime.of(14, 0)),
                endTime = LocalDateTime.of(today.plusDays(2), LocalTime.of(16, 0)),
                color = EventColor.YELLOW
            ),
            Event(
                title = "Lunch with Sarah",
                startTime = LocalDateTime.of(today.plusDays(1), LocalTime.of(12, 0)),
                endTime = LocalDateTime.of(today.plusDays(1), LocalTime.of(13, 0)),
                color = EventColor.YELLOW,
                location = "Downtown Cafe"
            ),
            Event(
                title = "All Hands Meeting",
                startTime = LocalDateTime.of(today.minusDays(2), LocalTime.of(10, 0)),
                endTime = LocalDateTime.of(today.minusDays(2), LocalTime.of(11, 0)),
                color = EventColor.PURPLE,
                location = "Main Auditorium"
            ),
            Event(
                title = "Project Deadline",
                description = "Final submission for Q4 project",
                startTime = LocalDateTime.of(today.plusDays(3), LocalTime.of(17, 0)),
                endTime = LocalDateTime.of(today.plusDays(3), LocalTime.of(18, 0)),
                color = EventColor.RED
            ),
            Event(
                title = "Multi-day Conference",
                startTime = LocalDateTime.of(today.plusDays(5), LocalTime.of(9, 0)),
                endTime = LocalDateTime.of(today.plusDays(7), LocalTime.of(18, 0)),
                color = EventColor.TEAL,
                location = "Convention Center",
                isAllDay = true
            )
        )
    }
}
