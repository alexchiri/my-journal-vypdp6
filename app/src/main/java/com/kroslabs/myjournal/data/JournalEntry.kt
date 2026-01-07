package com.kroslabs.myjournal.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class EntryType {
    DAILY,
    WEEKLY_REVIEW
}

@Entity(tableName = "journal_entries")
data class JournalEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val content: String,
    val entryType: EntryType,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isDraft: Boolean = false
)
