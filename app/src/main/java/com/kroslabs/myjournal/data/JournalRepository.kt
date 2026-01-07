package com.kroslabs.myjournal.data

import kotlinx.coroutines.flow.Flow

class JournalRepository(private val journalDao: JournalDao) {

    fun getAllEntries(): Flow<List<JournalEntry>> = journalDao.getAllEntries()

    fun getEntriesByType(type: EntryType): Flow<List<JournalEntry>> =
        journalDao.getEntriesByType(type)

    fun searchEntries(query: String): Flow<List<JournalEntry>> =
        journalDao.searchEntries(query)

    fun getEntriesInRange(startTime: Long, endTime: Long): Flow<List<JournalEntry>> =
        journalDao.getEntriesInRange(startTime, endTime)

    suspend fun getEntryById(id: Long): JournalEntry? = journalDao.getEntryById(id)

    suspend fun getDraft(type: EntryType): JournalEntry? = journalDao.getDraft(type)

    suspend fun getLatestWeeklyReview(): JournalEntry? = journalDao.getLatestWeeklyReview()

    suspend fun saveEntry(entry: JournalEntry): Long = journalDao.insert(entry)

    suspend fun updateEntry(entry: JournalEntry) = journalDao.update(entry)

    suspend fun deleteEntry(entry: JournalEntry) = journalDao.delete(entry)

    suspend fun clearDrafts() = journalDao.clearDrafts()
}
