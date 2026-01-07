package com.kroslabs.myjournal.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.kroslabs.myjournal.data.EntryType
import com.kroslabs.myjournal.ui.screens.EditorScreen
import com.kroslabs.myjournal.ui.screens.HomeScreen
import com.kroslabs.myjournal.ui.screens.OnboardingScreen
import com.kroslabs.myjournal.ui.screens.SettingsScreen
import com.kroslabs.myjournal.viewmodel.JournalViewModel

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Home : Screen("home")
    object Editor : Screen("editor/{entryType}") {
        fun createRoute(entryType: EntryType) = "editor/${entryType.name}"
    }
    object Settings : Screen("settings")
}

@Composable
fun NavGraph(
    navController: NavHostController,
    viewModel: JournalViewModel,
    startWithWeeklyReview: Boolean = false
) {
    val preferences by viewModel.userPreferences.collectAsState()

    val startDestination = if (preferences.hasCompletedOnboarding) {
        Screen.Home.route
    } else {
        Screen.Onboarding.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                viewModel = viewModel,
                onComplete = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                viewModel = viewModel,
                onNavigateToEditor = { entryType ->
                    viewModel.startNewEntry(entryType)
                    navController.navigate(Screen.Editor.createRoute(entryType))
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                },
                onEntryClick = { entry ->
                    viewModel.editEntry(entry)
                    navController.navigate(Screen.Editor.createRoute(entry.entryType))
                }
            )
        }

        composable(Screen.Editor.route) { backStackEntry ->
            EditorScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }

    // Handle deep link to weekly review
    if (startWithWeeklyReview && preferences.hasCompletedOnboarding) {
        viewModel.startNewEntry(EntryType.WEEKLY_REVIEW)
        navController.navigate(Screen.Editor.createRoute(EntryType.WEEKLY_REVIEW))
    }
}
