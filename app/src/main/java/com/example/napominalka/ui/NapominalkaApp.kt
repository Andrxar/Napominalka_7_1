package com.example.napominalka.ui

import android.net.Uri
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.intl.Locale
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.napominalka.ui.theme.NapominalkaTheme
import com.example.napominalka.ui.screens.EditReminderScreen
import com.example.napominalka.ui.screens.MainScreen
import com.example.napominalka.ui.screens.OnboardingScreen
import com.example.napominalka.ui.screens.SettingsScreen
import com.example.napominalka.viewmodel.ReminderViewModel
import com.example.napominalka.data.ReminderRepository
import com.example.napominalka.data.db.AppDatabase

enum class ThemeMode { Light, Dark, System }

@Composable
fun NapominalkaApp(navigateToEditId: Long? = null) {
    // Простой контроллер темы (Светлая/Тёмная/Системная)
    var themeMode by remember { mutableStateOf(ThemeMode.System) }
    val isDark = when (themeMode) {
        ThemeMode.System -> androidx.compose.foundation.isSystemInDarkTheme()
        ThemeMode.Dark -> true
        ThemeMode.Light -> false
    }

    val navController = rememberNavController()
    val ctx = LocalContext.current
    val vm = remember {
        val dao = AppDatabase.get(ctx).reminders()
        val repo = ReminderRepository(dao)
        ReminderViewModel(repo)
    }
    var backgroundImageUri by remember { mutableStateOf<Uri?>(null) }

    NapominalkaTheme(darkTheme = isDark) {
        Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
            // Если приложение запущено из системного оверлея с конкретным ID,
            // сразу перенаправим на экран редактирования
            LaunchedEffect(navigateToEditId) {
                if (navigateToEditId != null && navigateToEditId >= 0) {
                    navController.navigate("edit?id=${navigateToEditId}")
                }
            }

            NavHost(navController = navController, startDestination = "onboarding") {
                composable("onboarding") {
                    OnboardingScreen(
                        onStart = { navController.navigate("main") },
                        onPickBackground = { uri -> backgroundImageUri = uri }
                    )
                }
                composable("main") {
                    MainScreen(
                        vm = vm,
                        backgroundImageUri = backgroundImageUri,
                        onAdd = { navController.navigate("edit") },
                        onEdit = { id -> navController.navigate("edit?id=$id") },
                        onSettings = { navController.navigate("settings") }
                    )
                }
                composable(
                    route = "edit?id={id}",
                    arguments = listOf(navArgument("id") { type = NavType.LongType; defaultValue = -1L })
                ) { backStackEntry ->
                    val id = backStackEntry.arguments?.getLong("id") ?: -1L
                    EditReminderScreen(
                        existingId = if (id >= 0) id else null,
                        vm = vm,
                        onSaved = { navController.popBackStack() },
                        onCancel = { navController.popBackStack() }
                    )
                }
                composable("settings") {
                    SettingsScreen(
                        themeMode = themeMode,
                        onThemeChange = { themeMode = it },
                        vm = vm
                    )
                }
            }
        }
    }
}