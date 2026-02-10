package com.example.googlecalendarclone

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.Duration
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WeekView(
    selectedDate: LocalDate,
    events: List<Event>,
    onEventClick: (String) -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    numberOfDays: Int = 7,
    modifier: Modifier = Modifier
) {
    val initialPage = Int.MAX_VALUE / 2
    val pagerState = rememberPagerState(initialPage = initialPage) { Int.MAX_VALUE }

    // Sync pager with selectedDate
    LaunchedEffect(selectedDate) {
        val diff = calculatePageDiff(selectedDate, numberOfDays)
        val targetPage = initialPage + diff
        if (pagerState.currentPage != targetPage) {
            pagerState.scrollToPage(targetPage)
        }
    }

    // Sync selectedDate with pager
    LaunchedEffect(pagerState.currentPage) {
        val diff = pagerState.currentPage - initialPage
        val newDate = calculateDateFromDiff(diff, numberOfDays)
        if (!isDateInRange(selectedDate, newDate, numberOfDays)) {
            onDateSelected(newDate)
        }
    }

    HorizontalPager(
        state = pagerState,
        modifier = modifier.fillMaxSize(),
        beyondViewportPageCount = 1
    ) { page ->
        val diff = page - initialPage
        val pageStartDate = calculateDateFromDiff(diff, numberOfDays)
        
        WeekGridContent(
            startDate = pageStartDate,
            events = events,
            onEventClick = onEventClick,
            numberOfDays = numberOfDays
        )
    }
}

private fun calculatePageDiff(selectedDate: LocalDate, numberOfDays: Int): Int {
    val today = LocalDate.now()
    return when (numberOfDays) {
        7 -> {
            val startSelected = selectedDate.minusDays((selectedDate.dayOfWeek.value % 7).toLong())
            val startToday = today.minusDays((today.dayOfWeek.value % 7).toLong())
            (ChronoUnit.DAYS.between(startToday, startSelected) / 7).toInt()
        }
        3 -> (ChronoUnit.DAYS.between(today, selectedDate) / 3).toInt()
        else -> ChronoUnit.DAYS.between(today, selectedDate).toInt()
    }
}

private fun calculateDateFromDiff(diff: Int, numberOfDays: Int): LocalDate {
    val today = LocalDate.now()
    return when (numberOfDays) {
        7 -> {
            val startToday = today.minusDays((today.dayOfWeek.value % 7).toLong())
            startToday.plusWeeks(diff.toLong())
        }
        3 -> today.plusDays(diff.toLong() * 3)
        else -> today.plusDays(diff.toLong())
    }
}

private fun isDateInRange(date: LocalDate, startDate: LocalDate, numberOfDays: Int): Boolean {
    val endDate = startDate.plusDays(numberOfDays.toLong() - 1)
    return !date.isBefore(startDate) && !date.isAfter(endDate)
}

@Composable
fun WeekGridContent(
    startDate: LocalDate,
    events: List<Event>,
    onEventClick: (String) -> Unit,
    numberOfDays: Int
) {
    val days = remember(startDate, numberOfDays) {
        (0 until numberOfDays).map { startDate.plusDays(it.toLong()) }
    }
    
    val hourHeight = 64.dp
    val timeColumnWidth = 56.dp
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        if (days.any { it == LocalDate.now() }) {
            val now = LocalTime.now()
            val scrollPosition = (now.hour * 60 + now.minute) * (hourHeight.value / 60f)
            scrollState.scrollTo(scrollPosition.toInt())
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Day Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(start = timeColumnWidth)
        ) {
            days.forEach { date ->
                DayHeaderItem(
                    date = date,
                    isToday = date == LocalDate.now(),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)

        // Grid
        Box(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            // Horizontal lines
            Column {
                repeat(24) {
                    Box(modifier = Modifier.height(hourHeight).fillMaxWidth()) {
                        HorizontalDivider(
                            modifier = Modifier.align(Alignment.TopStart).alpha(0.2f),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth()) {
                // Time labels
                Column(modifier = Modifier.width(timeColumnWidth)) {
                    repeat(24) { hour ->
                        val time = LocalTime.of(hour, 0)
                        Box(
                            modifier = Modifier.height(hourHeight).fillMaxWidth(),
                            contentAlignment = Alignment.TopEnd
                        ) {
                            if (hour > 0) {
                                Text(
                                    text = time.format(DateTimeFormatter.ofPattern("ha")),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(end = 8.dp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.End
                                )
                            }
                        }
                    }
                }

                // Grid and Events
                Box(modifier = Modifier.weight(1f)) {
                    // Vertical lines
                    Row(modifier = Modifier.fillMaxSize()) {
                        days.forEach { _ ->
                            Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                                VerticalDivider(
                                    modifier = Modifier.align(Alignment.CenterStart).alpha(0.1f),
                                    color = MaterialTheme.colorScheme.outlineVariant
                                )
                            }
                        }
                        VerticalDivider(modifier = Modifier.alpha(0.1f), color = MaterialTheme.colorScheme.outlineVariant)
                    }

                    // Events
                    Row(modifier = Modifier.fillMaxSize()) {
                        days.forEach { date ->
                            val dayEvents = events.filter { it.startTime.toLocalDate() == date }
                            Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                                    val columnWidth = maxWidth
                                    val positionedEvents = remember(dayEvents) {
                                        calculateEventPositions(dayEvents)
                                    }
                                    positionedEvents.forEach { (event, position) ->
                                        val eventWidth = columnWidth / position.totalInOverlap
                                        val xOffset = eventWidth * position.index
                                        WeekEventItem(
                                            event = event,
                                            hourHeight = hourHeight,
                                            onEventClick = onEventClick,
                                            modifier = Modifier
                                                .width(eventWidth)
                                                .offset(x = xOffset)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (days.any { it == LocalDate.now() }) {
                        val todayIndex = days.indexOf(LocalDate.now())
                        CurrentTimeIndicator(todayIndex, days.size, hourHeight)
                    }
                }
            }
        }
    }
}

data class EventPosition(val index: Int, val totalInOverlap: Int)

private fun calculateEventPositions(events: List<Event>): Map<Event, EventPosition> {
    if (events.isEmpty()) return emptyMap()
    
    val sortedEvents = events.sortedBy { it.startTime }
    val clusters = mutableListOf<MutableList<Event>>()
    
    // Group events into clusters that overlap directly or indirectly
    for (event in sortedEvents) {
        var addedToCluster = false
        for (cluster in clusters) {
            if (cluster.any { it.overlaps(event) }) {
                cluster.add(event)
                addedToCluster = true
                break
            }
        }
        if (!addedToCluster) {
            clusters.add(mutableListOf(event))
        }
    }
    
    val result = mutableMapOf<Event, EventPosition>()
    
    for (cluster in clusters) {
        val columns = mutableListOf<MutableList<Event>>()
        for (event in cluster) {
            var placed = false
            for (column in columns) {
                if (column.none { it.overlaps(event) }) {
                    column.add(event)
                    placed = true
                    break
                }
            }
            if (!placed) {
                columns.add(mutableListOf(event))
            }
        }
        
        for (columnIndex in columns.indices) {
            for (event in columns[columnIndex]) {
                result[event] = EventPosition(columnIndex, columns.size)
            }
        }
    }

    return result
}

private fun Event.overlaps(other: Event): Boolean {
    return startTime.isBefore(other.endTime) && other.startTime.isBefore(endTime)
}

@Composable
private fun DayHeaderItem(date: LocalDate, isToday: Boolean, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = date.dayOfWeek.name.take(3).lowercase().replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.labelMedium,
            color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
        )
        Spacer(modifier = Modifier.height(4.dp))
        Surface(
            shape = CircleShape,
            color = if (isToday) MaterialTheme.colorScheme.primary else Color.Transparent,
            modifier = Modifier.size(32.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = date.dayOfMonth.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isToday) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun WeekEventItem(
    event: Event,
    hourHeight: Dp,
    onEventClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val startMinute = event.startTime.hour * 60 + event.startTime.minute
    val durationMinutes = Duration.between(event.startTime, event.endTime).toMinutes()
    val topOffset = (startMinute / 60f) * hourHeight.value
    val eventHeight = (durationMinutes / 60f) * hourHeight.value

    Surface(
        modifier = modifier
            .padding(horizontal = 0.5.dp, vertical = 0.5.dp)
            .offset(y = topOffset.dp)
            .height(eventHeight.dp)
            .clickable { onEventClick(event.id) },
        color = Color(event.color.hexColor).copy(alpha = 0.85f),
        shape = RoundedCornerShape(4.dp)
    ) {
        Column(modifier = Modifier.padding(4.dp)) {
            Text(
                text = event.title,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                maxLines = 1,
                fontWeight = FontWeight.Bold,
                overflow = TextOverflow.Ellipsis
            )
            if (durationMinutes >= 30) {
                Text(
                    text = event.startTime.format(DateTimeFormatter.ofPattern("h:mm a")),
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 9.sp),
                    color = Color.White.copy(alpha = 0.9f),
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun CurrentTimeIndicator(dayIndex: Int, totalDays: Int, hourHeight: Dp) {
    var currentTime by remember { mutableStateOf(LocalTime.now()) }
    LaunchedEffect(Unit) {
        while(true) {
            delay(60000)
            currentTime = LocalTime.now()
        }
    }
    val minutesPassed = currentTime.hour * 60 + currentTime.minute
    val topOffset = (minutesPassed / 60f) * hourHeight.value
    Box(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxSize()) {
            repeat(totalDays) { index ->
                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    if (index == dayIndex) {
                        Box(modifier = Modifier.fillMaxWidth().offset(y = topOffset.dp).height(2.dp).background(Color.Red))
                        Box(modifier = Modifier.offset(y = (topOffset - 4).dp, x = (-4).dp).size(8.dp).background(Color.Red, shape = CircleShape))
                    }
                }
            }
        }
    }
}
