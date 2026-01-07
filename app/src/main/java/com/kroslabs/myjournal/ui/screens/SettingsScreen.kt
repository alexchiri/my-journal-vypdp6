package com.kroslabs.myjournal.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.kroslabs.myjournal.notification.NotificationHelper
import com.kroslabs.myjournal.viewmodel.JournalViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: JournalViewModel,
    onNavigateBack: () -> Unit
) {
    val preferences by viewModel.userPreferences.collectAsState()
    val context = LocalContext.current

    var selectedDay by remember(preferences.reviewDayOfWeek) {
        mutableIntStateOf(preferences.reviewDayOfWeek)
    }
    var selectedHour by remember(preferences.reviewHour) {
        mutableIntStateOf(preferences.reviewHour)
    }
    var selectedMinute by remember(preferences.reviewMinute) {
        mutableIntStateOf(preferences.reviewMinute)
    }
    var textSize by remember(preferences.textSizeMultiplier) {
        mutableFloatStateOf(preferences.textSizeMultiplier)
    }

    var showDayPicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            NotificationHelper.scheduleWeeklyReview(
                context,
                selectedDay,
                selectedHour,
                selectedMinute
            )
        }
    }

    fun scheduleNotification() {
        viewModel.setReviewSchedule(selectedDay, selectedHour, selectedMinute)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    NotificationHelper.scheduleWeeklyReview(
                        context,
                        selectedDay,
                        selectedHour,
                        selectedMinute
                    )
                }
                else -> {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            NotificationHelper.scheduleWeeklyReview(
                context,
                selectedDay,
                selectedHour,
                selectedMinute
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Weekly Review Section
            Text(
                text = "Weekly Review Notifications",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Day picker
            Card(
                onClick = { showDayPicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Review Day",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = getDayName(selectedDay),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Time picker
            Card(
                onClick = { showTimePicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Review Time",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = formatTime(selectedHour, selectedMinute),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Text Size Section
            Text(
                text = "Display",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.TextFields,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "Text Size",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "A",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Slider(
                            value = textSize,
                            onValueChange = { textSize = it },
                            onValueChangeFinished = {
                                viewModel.setTextSizeMultiplier(textSize)
                            },
                            valueRange = 0.8f..1.5f,
                            steps = 6,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp)
                        )
                        Text(
                            text = "A",
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }

                    Text(
                        text = "Sample text preview",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = MaterialTheme.typography.bodyLarge.fontSize * textSize
                        ),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }

    if (showDayPicker) {
        DayPickerDialog(
            selectedDay = selectedDay,
            onDaySelected = { day ->
                selectedDay = day
                showDayPicker = false
                scheduleNotification()
            },
            onDismiss = { showDayPicker = false }
        )
    }

    if (showTimePicker) {
        TimePickerDialog(
            selectedHour = selectedHour,
            selectedMinute = selectedMinute,
            onTimeSelected = { hour, minute ->
                selectedHour = hour
                selectedMinute = minute
                showTimePicker = false
                scheduleNotification()
            },
            onDismiss = { showTimePicker = false }
        )
    }
}

@Composable
private fun DayPickerDialog(
    selectedDay: Int,
    onDaySelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val days = listOf(
        1 to "Monday",
        2 to "Tuesday",
        3 to "Wednesday",
        4 to "Thursday",
        5 to "Friday",
        6 to "Saturday",
        7 to "Sunday"
    )

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select day") },
        text = {
            Column {
                days.forEach { (dayNum, dayName) ->
                    Card(
                        onClick = { onDaySelected(dayNum) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = dayName,
                            modifier = Modifier.padding(16.dp),
                            color = if (dayNum == selectedDay) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    selectedHour: Int,
    selectedMinute: Int,
    onTimeSelected: (Int, Int) -> Unit,
    onDismiss: () -> Unit
) {
    val timePickerState = androidx.compose.material3.rememberTimePickerState(
        initialHour = selectedHour,
        initialMinute = selectedMinute
    )

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select time") },
        text = {
            androidx.compose.material3.TimePicker(state = timePickerState)
        },
        confirmButton = {
            androidx.compose.material3.TextButton(
                onClick = {
                    onTimeSelected(timePickerState.hour, timePickerState.minute)
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun getDayName(day: Int): String {
    return when (day) {
        1 -> "Monday"
        2 -> "Tuesday"
        3 -> "Wednesday"
        4 -> "Thursday"
        5 -> "Friday"
        6 -> "Saturday"
        7 -> "Sunday"
        else -> "Monday"
    }
}

private fun formatTime(hour: Int, minute: Int): String {
    val amPm = if (hour < 12) "AM" else "PM"
    val hour12 = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
    return String.format("%d:%02d %s", hour12, minute, amPm)
}
