package com.kroslabs.myjournal.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.kroslabs.myjournal.data.UserPreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WeeklyReviewReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val preferencesRepository = UserPreferencesRepository(context)

        CoroutineScope(Dispatchers.IO).launch {
            preferencesRepository.setWeeklyReviewPending(true)
        }

        NotificationHelper.showWeeklyReviewNotification(context)
    }
}
