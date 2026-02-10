package com.example.googlecalendarclone

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

enum class ViewMode {
    Schedule, Day, ThreeDay, Week, Month
}

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val repository: EventRepository
) : ViewModel() {

    private val _viewMode = MutableStateFlow(ViewMode.Schedule)
    val viewMode: StateFlow<ViewMode> = _viewMode

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate

    private val _currentMonth = MutableStateFlow(YearMonth.now())
    val currentMonth: StateFlow<YearMonth> = _currentMonth

    val eventsForSelectedDate = _selectedDate
        .flatMapLatest { date ->
            repository.getEventsForDate(date)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val eventsForMonth = combine(
        _currentMonth,
        repository.events
    ) { month, events ->
        events.filter { event ->
            val eventDate = event.startTime.toLocalDate()
            eventDate.year == month.year && eventDate.monthValue == month.monthValue
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setViewMode(mode: ViewMode) {
        _viewMode.value = mode
    }

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
        // Update current month if date is in different month
        val newMonth = YearMonth.of(date.year, date.month)
        if (newMonth != _currentMonth.value) {
            _currentMonth.value = newMonth
        }
    }

    fun changeMonth(month: YearMonth) {
        _currentMonth.value = month
        // Removed auto-selection of first day of month
    }

    fun addEvent(event: Event) {
        viewModelScope.launch {
            repository.addEvent(event)
        }
    }

    fun deleteEvent(eventId: String) {
        viewModelScope.launch {
            repository.deleteEvent(eventId)
        }
    }

    fun updateEvent(event: Event) {
        viewModelScope.launch {
            repository.updateEvent(event)
        }
    }

    fun getEventById(eventId: String): StateFlow<Event?> {
        return repository.events
            .map { events -> events.find { it.id == eventId } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    }
}
