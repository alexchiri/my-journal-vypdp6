package com.kroslabs.myjournal.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

data class UserPreferencesData(
    val hasCompletedOnboarding: Boolean = false,
    val reviewDayOfWeek: Int = 1, // 1 = Monday, 7 = Sunday
    val reviewHour: Int = 18,
    val reviewMinute: Int = 0,
    val textSizeMultiplier: Float = 1.0f,
    val hasWeeklyReviewPending: Boolean = false
)

class UserPreferencesRepository(private val context: Context) {
    private val hasCompletedOnboardingKey = booleanPreferencesKey("has_completed_onboarding")
    private val reviewDayOfWeekKey = intPreferencesKey("review_day_of_week")
    private val reviewHourKey = intPreferencesKey("review_hour")
    private val reviewMinuteKey = intPreferencesKey("review_minute")
    private val textSizeMultiplierKey = floatPreferencesKey("text_size_multiplier")
    private val hasWeeklyReviewPendingKey = booleanPreferencesKey("has_weekly_review_pending")

    val userPreferences: Flow<UserPreferencesData> = context.dataStore.data.map { preferences ->
        UserPreferencesData(
            hasCompletedOnboarding = preferences[hasCompletedOnboardingKey] ?: false,
            reviewDayOfWeek = preferences[reviewDayOfWeekKey] ?: 1,
            reviewHour = preferences[reviewHourKey] ?: 18,
            reviewMinute = preferences[reviewMinuteKey] ?: 0,
            textSizeMultiplier = preferences[textSizeMultiplierKey] ?: 1.0f,
            hasWeeklyReviewPending = preferences[hasWeeklyReviewPendingKey] ?: false
        )
    }

    suspend fun setOnboardingCompleted() {
        context.dataStore.edit { preferences ->
            preferences[hasCompletedOnboardingKey] = true
        }
    }

    suspend fun setReviewSchedule(dayOfWeek: Int, hour: Int, minute: Int) {
        context.dataStore.edit { preferences ->
            preferences[reviewDayOfWeekKey] = dayOfWeek
            preferences[reviewHourKey] = hour
            preferences[reviewMinuteKey] = minute
        }
    }

    suspend fun setTextSizeMultiplier(multiplier: Float) {
        context.dataStore.edit { preferences ->
            preferences[textSizeMultiplierKey] = multiplier
        }
    }

    suspend fun setWeeklyReviewPending(pending: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[hasWeeklyReviewPendingKey] = pending
        }
    }
}
