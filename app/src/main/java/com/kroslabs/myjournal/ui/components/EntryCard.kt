package com.kroslabs.myjournal.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kroslabs.myjournal.data.EntryType
import com.kroslabs.myjournal.data.JournalEntry
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun EntryCard(
    entry: JournalEntry,
    textSizeMultiplier: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormatter = SimpleDateFormat("MMM dd, yyyy 'at' h:mm a", Locale.getDefault())
    val isWeeklyReview = entry.entryType == EntryType.WEEKLY_REVIEW

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isWeeklyReview) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
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
                    imageVector = if (isWeeklyReview) Icons.Default.CalendarMonth else Icons.Default.Edit,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = if (isWeeklyReview) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isWeeklyReview) "Weekly Review" else "Daily Entry",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontSize = MaterialTheme.typography.labelMedium.fontSize * textSizeMultiplier
                    ),
                    color = if (isWeeklyReview) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = dateFormatter.format(Date(entry.createdAt)),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = MaterialTheme.typography.labelSmall.fontSize * textSizeMultiplier
                    ),
                    color = if (isWeeklyReview) {
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = entry.content,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = MaterialTheme.typography.bodyMedium.fontSize * textSizeMultiplier
                ),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                color = if (isWeeklyReview) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}
