package com.kroslabs.myjournal.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kroslabs.myjournal.data.EntryType
import com.kroslabs.myjournal.data.JournalEntry
import com.kroslabs.myjournal.ui.components.EntryCard
import com.kroslabs.myjournal.viewmodel.JournalViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: JournalViewModel,
    onNavigateToEditor: (EntryType) -> Unit,
    onNavigateToSettings: () -> Unit,
    onEntryClick: (JournalEntry) -> Unit
) {
    val entries by viewModel.entries.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val preferences by viewModel.userPreferences.collectAsState()
    val deleteConfirmation by viewModel.showDeleteConfirmation.collectAsState()

    var isSearchActive by remember { mutableStateOf(false) }
    var showFabMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            if (!isSearchActive) {
                TopAppBar(
                    title = { Text("My Journal") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    actions = {
                        IconButton(onClick = { isSearchActive = true }) {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        }
                        IconButton(onClick = onNavigateToSettings) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings")
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End
            ) {
                AnimatedVisibility(visible = showFabMenu) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ExtendedFloatingActionButton(
                            onClick = {
                                showFabMenu = false
                                onNavigateToEditor(EntryType.WEEKLY_REVIEW)
                            },
                            icon = { Icon(Icons.Default.CalendarMonth, "Weekly Review") },
                            text = { Text("Weekly Review") },
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
                FloatingActionButton(
                    onClick = {
                        if (showFabMenu) {
                            showFabMenu = false
                            onNavigateToEditor(EntryType.DAILY)
                        } else {
                            showFabMenu = true
                        }
                    }
                ) {
                    Icon(
                        imageVector = if (showFabMenu) Icons.Default.Close else Icons.Default.Add,
                        contentDescription = if (showFabMenu) "Close menu" else "New entry"
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isSearchActive) {
                SearchBar(
                    inputField = {
                        SearchBarDefaults.InputField(
                            query = searchQuery,
                            onQueryChange = { viewModel.setSearchQuery(it) },
                            onSearch = { },
                            expanded = false,
                            onExpandedChange = { },
                            placeholder = { Text("Search entries...") },
                            leadingIcon = {
                                IconButton(onClick = {
                                    isSearchActive = false
                                    viewModel.setSearchQuery("")
                                }) {
                                    Icon(Icons.Default.Close, contentDescription = "Close search")
                                }
                            }
                        )
                    },
                    expanded = false,
                    onExpandedChange = { },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) { }
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (preferences.hasWeeklyReviewPending) {
                WeeklyReviewBanner(
                    onClick = { onNavigateToEditor(EntryType.WEEKLY_REVIEW) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            if (entries.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (searchQuery.isNotBlank()) {
                            "No entries found for \"$searchQuery\""
                        } else {
                            "No entries yet.\nTap + to create your first entry!"
                        },
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = MaterialTheme.typography.bodyLarge.fontSize * preferences.textSizeMultiplier
                        ),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                EntryList(
                    entries = entries,
                    textSizeMultiplier = preferences.textSizeMultiplier,
                    onEntryClick = onEntryClick
                )
            }
        }
    }

    deleteConfirmation?.let { entry ->
        AlertDialog(
            onDismissRequest = { viewModel.cancelDelete() },
            title = { Text("Delete Entry?") },
            text = { Text("Are you sure you want to delete this entry? This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmDelete() }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelDelete() }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun WeeklyReviewBanner(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CalendarMonth,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Time for your weekly review",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "Tap to reflect on your past week",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun EntryList(
    entries: List<JournalEntry>,
    textSizeMultiplier: Float,
    onEntryClick: (JournalEntry) -> Unit
) {
    val groupedEntries = entries.groupBy { entry ->
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = entry.createdAt
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        calendar.timeInMillis
    }

    val dateFormatter = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        groupedEntries.forEach { (dateMillis, dateEntries) ->
            item {
                Text(
                    text = dateFormatter.format(Date(dateMillis)),
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontSize = MaterialTheme.typography.titleSmall.fontSize * textSizeMultiplier
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            items(dateEntries, key = { it.id }) { entry ->
                EntryCard(
                    entry = entry,
                    textSizeMultiplier = textSizeMultiplier,
                    onClick = { onEntryClick(entry) }
                )
            }
        }
    }
}
