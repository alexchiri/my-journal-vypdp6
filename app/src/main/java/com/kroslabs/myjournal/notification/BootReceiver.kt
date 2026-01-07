package com.kroslabs.myjournal.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.kroslabs.myjournal.data.UserPreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val preferencesRepository = UserPreferencesRepository(context)

            CoroutineScope(Dispatchers.IO).launch {
                val preferences = preferencesRepository.userPreferences.first()

                if (preferences.hasCompletedOnboarding) {
                    NotificationHelper.scheduleWeeklyReview(
                        context,
                        preferences.reviewDayOfWeek,
                        preferences.reviewHour,
                        preferences.reviewMinute
                    )

                    if (preferences.hasWeeklyReviewPending) {
                        NotificationHelper.showWeeklyReviewNotification(context)
                    }
                }
            }
        }
    }
}
