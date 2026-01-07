package com.kroslabs.myjournal.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface JournalDao {
    @Query("SELECT * FROM journal_entries WHERE isDraft = 0 ORDER BY createdAt DESC")
    fun getAllEntries(): Flow<List<JournalEntry>>

    @Query("SELECT * FROM journal_entries WHERE isDraft = 0 AND entryType = :type ORDER BY createdAt DESC")
    fun getEntriesByType(type: EntryType): Flow<List<JournalEntry>>

    @Query("SELECT * FROM journal_entries WHERE id = :id")
    suspend fun getEntryById(id: Long): JournalEntry?

    @Query("SELECT * FROM journal_entries WHERE isDraft = 1 AND entryType = :type LIMIT 1")
    suspend fun getDraft(type: EntryType): JournalEntry?

    @Query("SELECT * FROM journal_entries WHERE isDraft = 0 AND content LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    fun searchEntries(query: String): Flow<List<JournalEntry>>

    @Query("SELECT * FROM journal_entries WHERE isDraft = 0 AND createdAt >= :startTime AND createdAt < :endTime ORDER BY createdAt DESC")
    fun getEntriesInRange(startTime: Long, endTime: Long): Flow<List<JournalEntry>>

    @Query("SELECT * FROM journal_entries WHERE isDraft = 0 AND entryType = 'WEEKLY_REVIEW' ORDER BY createdAt DESC LIMIT 1")
    suspend fun getLatestWeeklyReview(): JournalEntry?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: JournalEntry): Long

    @Update
    suspend fun update(entry: JournalEntry)

    @Delete
    suspend fun delete(entry: JournalEntry)

    @Query("DELETE FROM journal_entries WHERE isDraft = 1")
    suspend fun clearDrafts()
}
