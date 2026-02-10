package com.example.googlecalendarclone

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CalendarGrid(
    currentMonth: YearMonth,
    selectedDate: LocalDate,
    events: List<Event>,
    isExpanded: Boolean,
    expansionProgress: Float,
    onDateSelected: (LocalDate) -> Unit,
    onMonthChanged: (YearMonth) -> Unit,
    modifier: Modifier = Modifier
) {
    val initialPage = Int.MAX_VALUE / 2

    // Pager for Expanded Month Grid
    val monthPagerState = rememberPagerState(initialPage = initialPage) { Int.MAX_VALUE }

    // Pager for Compact Week View
    val weekPagerState = rememberPagerState(initialPage = initialPage) { Int.MAX_VALUE }

    // Sync monthPager with currentMonth
    LaunchedEffect(currentMonth) {
        val initialMonth = YearMonth.from(LocalDate.now())
        val diff = (currentMonth.year - initialMonth.year) * 12 + (currentMonth.monthValue - initialMonth.monthValue)
        val targetPage = initialPage + diff
        if (monthPagerState.currentPage != targetPage) {
            monthPagerState.scrollToPage(targetPage)
        }
    }

    // Sync currentMonth with monthPager swipe
    LaunchedEffect(monthPagerState.currentPage) {
        val diff = monthPagerState.currentPage - initialPage
        val newMonth = YearMonth.from(LocalDate.now()).plusMonths(diff.toLong())
        if (newMonth != currentMonth && monthPagerState.isScrollInProgress) {
            onMonthChanged(newMonth)
        }
    }

    // Sync weekPager with selectedDate
    LaunchedEffect(selectedDate) {
        val today = LocalDate.now()
        val startToday = today.minusDays((today.dayOfWeek.value % 7).toLong())
        val startSelected = selectedDate.minusDays((selectedDate.dayOfWeek.value % 7).toLong())
        val diff = (ChronoUnit.DAYS.between(startToday, startSelected) / 7).toInt()
        val targetPage = initialPage + diff
        if (weekPagerState.currentPage != targetPage) {
            weekPagerState.scrollToPage(targetPage)
        }
    }

    // Update selectedDate when weekPager swipes
    LaunchedEffect(weekPagerState.currentPage) {
        val diff = weekPagerState.currentPage - initialPage
        val today = LocalDate.now()
        val startToday = today.minusDays((today.dayOfWeek.value % 7).toLong())
        val newWeekStart = startToday.plusWeeks(diff.toLong())

        val isSelectedDateInWeek = !selectedDate.isBefore(newWeekStart) && !selectedDate.isAfter(newWeekStart.plusDays(6))
        if (!isSelectedDateInWeek && weekPagerState.isScrollInProgress) {
            onDateSelected(newWeekStart)
        }
    }

    Box(modifier = modifier.fillMaxWidth()) {

        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(animationSpec = tween(300)) + fadeIn(),
            exit = shrinkVertically(animationSpec = tween(300)) + fadeOut()
        ) {
            Column {
                WeekHeader()
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalPager(
                    state = monthPagerState,
                    modifier = Modifier.fillMaxWidth(),
                    beyondViewportPageCount = 1
                ) { page ->
                    val diff = page - initialPage
                    val month = YearMonth.from(LocalDate.now()).plusMonths(diff.toLong())

                    MonthGrid(
                        month = month,
                        selectedDate = selectedDate,
                        events = events,
                        onDateSelected = onDateSelected
                    )
                }
            }
        }

//        if (!isExpanded) {
//            HorizontalPager(
//                state = weekPagerState,
//                modifier = Modifier.fillMaxWidth(),
//                beyondViewportPageCount = 1
//            ) { page ->
//                val diff = page - initialPage
//                val today = LocalDate.now()
//                val startToday = today.minusDays((today.dayOfWeek.value % 7).toLong())
//                val weekStartDate = startToday.plusWeeks(diff.toLong())
//
//                CompactWeekRow(
//                    startDate = weekStartDate,
//                    selectedDate = selectedDate,
//                    events = events,
//                    onDateSelected = onDateSelected
//                )
//            }
//        }
    }
}

@Composable
fun MonthGrid(
    month: YearMonth,
    selectedDate: LocalDate,
    events: List<Event>,
    onDateSelected: (LocalDate) -> Unit
) {
    val firstDayOfMonth = month.atDay(1)
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7
    val daysInMonth = month.lengthOfMonth()

    val days = (1..daysInMonth).map { day -> month.atDay(day) }
    val leadingEmptyDays = List(firstDayOfWeek) { null }
    val allDays = leadingEmptyDays + days

    Box(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth()) {
            val weeks = allDays.chunked(7)
            weeks.forEachIndexed { index, week ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    week.forEach { date ->
                        if (date != null) {
                            val isSelected = date == selectedDate
                            val isToday = date == LocalDate.now()
                            val dayEvents = events.filter { it.startTime.toLocalDate() == date }

                            DayCell(
                                date = date,
                                isSelected = isSelected,
                                isToday = isToday,
                                events = dayEvents,
                                onClick = { onDateSelected(date) },
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                    if (week.size < 7) {
                        repeat(7 - week.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
                if (index < weeks.size - 1) {
                    HorizontalDivider(
                        modifier = Modifier.alpha(0.1f),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }
            }
        }
        
        // Vertical lines for the month grid
        Row(modifier = Modifier.matchParentSize()) {
            repeat(7) {
                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    VerticalDivider(
                        modifier = Modifier.align(Alignment.CenterEnd).alpha(0.1f),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun CompactWeekRow(
    startDate: LocalDate,
    selectedDate: LocalDate,
    events: List<Event>,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val weekDays = (0..6).map { startDate.plusDays(it.toLong()) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        weekDays.forEach { date ->
            val isSelected = date == selectedDate
            val isToday = date == LocalDate.now()
            val dayEvents = events.filter { it.startTime.toLocalDate() == date }

            CompactDayCell(
                date = date,
                isSelected = isSelected,
                isToday = isToday,
                hasEvents = dayEvents.isNotEmpty(),
                onClick = { onDateSelected(date) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun DayCell(
    date: LocalDate,
    isSelected: Boolean,
    isToday: Boolean,
    events: List<Event>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .aspectRatio(0.9f)
            .padding(1.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier.padding(top = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(
                        color = when {
                            isSelected -> MaterialTheme.colorScheme.primary
                            isToday -> MaterialTheme.colorScheme.primaryContainer
                            else -> Color.Transparent
                        },
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = date.dayOfMonth.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = when {
                        isSelected -> MaterialTheme.colorScheme.onPrimary
                        isToday -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )
            }

            if (events.isNotEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp),
                    verticalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    events.take(2).forEach { event ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.dp)
                                .background(
                                    color = Color(event.color.hexColor),
                                    shape = CircleShape
                                )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CompactDayCell(
    date: LocalDate,
    isSelected: Boolean,
    isToday: Boolean,
    hasEvents: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }

    Column(
        modifier = modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {


//        Text(
//            text = date.dayOfWeek.name.take(1),
//            style = MaterialTheme.typography.labelSmall,
//            color = MaterialTheme.colorScheme.onSurfaceVariant
//        )

        Spacer(modifier = Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .size(32.dp)
                .background(
                    color = when {
                        isSelected -> MaterialTheme.colorScheme.primary
                        isToday -> MaterialTheme.colorScheme.primaryContainer
                        else -> Color.Transparent
                    },
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
                color = when {
                    isSelected -> MaterialTheme.colorScheme.onPrimary
                    isToday -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )
        }

        Spacer(modifier = Modifier.height(2.dp))
        Box(
            modifier = Modifier
                .size(4.dp)
                .alpha(if (hasEvents) 1f else 0f)
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = CircleShape
                )
        )
    }
}
