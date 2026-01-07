package com.kroslabs.myjournal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.kroslabs.myjournal.data.EntryType
import com.kroslabs.myjournal.navigation.NavGraph
import com.kroslabs.myjournal.navigation.Screen
import com.kroslabs.myjournal.notification.NotificationHelper
import com.kroslabs.myjournal.ui.theme.MyJournalTheme
import com.kroslabs.myjournal.viewmodel.JournalViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val openWeeklyReview = intent.getBooleanExtra("open_weekly_review", false)

        setContent {
            val app = application as MyJournalApp
            val viewModel: JournalViewModel = viewModel(
                factory = JournalViewModel.Factory(app.repository, app.preferencesRepository)
            )

            val navController = rememberNavController()
            var hasHandledDeepLink by remember { mutableStateOf(false) }

            MyJournalTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavGraph(
                        navController = navController,
                        viewModel = viewModel
                    )
                }
            }

            // Handle the weekly review intent
            LaunchedEffect(openWeeklyReview, hasHandledDeepLink) {
                if (openWeeklyReview && !hasHandledDeepLink) {
                    hasHandledDeepLink = true
                    viewModel.startNewEntry(EntryType.WEEKLY_REVIEW)
                    navController.navigate(Screen.Editor.createRoute(EntryType.WEEKLY_REVIEW))
                    NotificationHelper.cancelWeeklyReviewNotification(this@MainActivity)
                }
            }
        }
    }
}
