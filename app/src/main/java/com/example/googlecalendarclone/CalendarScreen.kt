package com.example.googlecalendarclone


import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CalendarViewDay
import androidx.compose.material.icons.filled.CalendarViewWeek
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel,
    onEventClick: (String) -> Unit,
    onAddEvent: (LocalDate?) -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val currentMonth by viewModel.currentMonth.collectAsStateWithLifecycle()
    val events by viewModel.eventsForSelectedDate.collectAsStateWithLifecycle()
    val allEvents by viewModel.eventsForMonth.collectAsStateWithLifecycle()
    val viewMode by viewModel.viewMode.collectAsStateWithLifecycle()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var isCalendarExpanded by remember { mutableStateOf(false) }
    var expansionProgress by remember { mutableFloatStateOf(0f) }
    var isSearchVisible by remember { mutableStateOf(false) }

    val animatedProgress by animateFloatAsState(
        targetValue = if (isCalendarExpanded) 1f else 0f,
        animationSpec = tween(300),
        label = "expansion_progress"
    )
    expansionProgress = animatedProgress

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (available.y < -10 && isCalendarExpanded && source == NestedScrollSource.Drag) {
                    isCalendarExpanded = false
                    return Offset(0f, available.y)
                }
                return Offset.Zero
            }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            CalendarDrawerContent(
                currentViewMode = viewMode,
                onViewModeSelected = { mode ->
                    viewModel.setViewMode(mode)
                    scope.launch { drawerState.close() }
                }
            )
        }
    ) {
        Scaffold(
            modifier = modifier.nestedScroll(nestedScrollConnection),
            topBar = {
                Column(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surface)
                        .statusBarsPadding()
                ) {
                    if (!isSearchVisible) {
                        MonthHeader(
                            currentDate = currentMonth.atDay(1),
                            isExpanded = isCalendarExpanded,
                            expansionProgress = expansionProgress,
                            onToggleExpansion = { isCalendarExpanded = !isCalendarExpanded },
                            onTodayClick = { viewModel.selectDate(LocalDate.now()) },
                            onMenuClick = { scope.launch { drawerState.open() } },
                            onSearchClick = { isSearchVisible = true },
                            onMonthChanged = { date ->
                                viewModel.changeMonth(YearMonth.from(date))
                            }
                        )
                    } else {
                        AnimatedSearchBar(
                            isVisible = isSearchVisible,
                            onClose = { isSearchVisible = false }
                        )
                    }

                    if (viewMode != ViewMode.Month) {
                        CalendarGrid(
                            currentMonth = currentMonth,
                            selectedDate = selectedDate,
                            events = allEvents,
                            isExpanded = isCalendarExpanded,
                            expansionProgress = expansionProgress,
                            onDateSelected = { date ->
                                viewModel.selectDate(date)
                                if (isCalendarExpanded) {
                                    isCalendarExpanded = false
                                }
                            },
                            onMonthChanged = { viewModel.changeMonth(it) }
                        )
                        HorizontalDivider(
                            modifier = Modifier.alpha(if (isCalendarExpanded) expansionProgress else 0.2f),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background)
            ) {

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectVerticalDragGestures { change, dragAmount ->
                                if (dragAmount > 20 && !isCalendarExpanded && viewMode != ViewMode.Month) {
                                    change.consume()
                                    isCalendarExpanded = true
                                }
                            }
                        }
                ) {
                    when (viewMode) {
                        ViewMode.Schedule -> {
                            ScheduleList(
                                selectedDate = selectedDate,
                                events = events,
                                onEventClick = onEventClick,
                                onAddEvent = { onAddEvent(selectedDate) },
                                onScrollDirectionChange = { }
                            )
                        }
                        ViewMode.Day -> {
                            WeekView(
                                selectedDate = selectedDate,
                                events = allEvents,
                                onEventClick = onEventClick,
                                onDateSelected = { viewModel.selectDate(it) },
                                numberOfDays = 1
                            )
                        }
                        ViewMode.ThreeDay -> {
                            WeekView(
                                selectedDate = selectedDate,
                                events = allEvents,
                                onEventClick = onEventClick,
                                onDateSelected = { viewModel.selectDate(it) },
                                numberOfDays = 3
                            )
                        }
                        ViewMode.Week -> {
                            WeekView(
                                selectedDate = selectedDate,
                                events = allEvents,
                                onEventClick = onEventClick,
                                onDateSelected = { viewModel.selectDate(it) },
                                numberOfDays = 7
                            )
                        }
                        ViewMode.Month -> {
                            CalendarGrid(
                                currentMonth = currentMonth,
                                selectedDate = selectedDate,
                                events = allEvents,
                                isExpanded = true,
                                expansionProgress = 1f,
                                onDateSelected = { viewModel.selectDate(it) },
                                onMonthChanged = { viewModel.changeMonth(it) },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarDrawerContent(
    currentViewMode: ViewMode,
    onViewModeSelected: (ViewMode) -> Unit
) {
    ModalDrawerSheet {
        Spacer(Modifier.height(12.dp))
        Text(
            "Google Calendar",
            modifier = Modifier.padding(horizontal = 28.dp, vertical = 16.dp),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )
        HorizontalDivider()
        Spacer(Modifier.height(12.dp))

        DrawerItem(
            label = "Schedule",
            icon = Icons.Default.Schedule,
            isSelected = currentViewMode == ViewMode.Schedule,
            onClick = { onViewModeSelected(ViewMode.Schedule) }
        )
        DrawerItem(
            label = "Day",
            icon = Icons.Default.CalendarViewDay,
            isSelected = currentViewMode == ViewMode.Day,
            onClick = { onViewModeSelected(ViewMode.Day) }
        )
        DrawerItem(
            label = "3 days",
            icon = Icons.Default.CalendarToday,
            isSelected = currentViewMode == ViewMode.ThreeDay,
            onClick = { onViewModeSelected(ViewMode.ThreeDay) }
        )
        DrawerItem(
            label = "Week",
            icon = Icons.Default.CalendarViewWeek,
            isSelected = currentViewMode == ViewMode.Week,
            onClick = { onViewModeSelected(ViewMode.Week) }
        )
        DrawerItem(
            label = "Month",
            icon = Icons.Default.CalendarMonth,
            isSelected = currentViewMode == ViewMode.Month,
            onClick = { onViewModeSelected(ViewMode.Month) }
        )
    }
}

@Composable
fun DrawerItem(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    NavigationDrawerItem(
        label = { Text(label) },
        icon = { Icon(icon, contentDescription = null) },
        selected = isSelected,
        onClick = onClick,
        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
    )
}
