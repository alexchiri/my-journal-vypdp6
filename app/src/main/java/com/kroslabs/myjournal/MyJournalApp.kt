package com.kroslabs.myjournal

import android.app.Application
import com.kroslabs.myjournal.data.JournalDatabase
import com.kroslabs.myjournal.data.JournalRepository
import com.kroslabs.myjournal.data.UserPreferencesRepository
import com.kroslabs.myjournal.notification.NotificationHelper

class MyJournalApp : Application() {
    val database by lazy { JournalDatabase.getDatabase(this) }
    val repository by lazy { JournalRepository(database.journalDao()) }
    val preferencesRepository by lazy { UserPreferencesRepository(this) }

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannel(this)
    }
}
