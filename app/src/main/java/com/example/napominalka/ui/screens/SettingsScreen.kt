package com.example.napominalka.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.napominalka.R
import com.example.napominalka.ui.ThemeMode
import com.example.napominalka.viewmodel.ReminderViewModel
import com.example.napominalka.backup.BackupManager
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import com.example.napominalka.util.Permissions
import java.io.File

@Composable
fun SettingsScreen(
    themeMode: ThemeMode,
    onThemeChange: (ThemeMode) -> Unit,
    vm: ReminderViewModel
) {
    var vibrationEnabled by remember { mutableStateOf(true) }
    val ctx = LocalContext.current
    var notifGranted by remember { mutableStateOf(Permissions.hasPostNotifications(ctx)) }
    var overlayGranted by remember { mutableStateOf(Permissions.canDrawOverlays(ctx)) }

    Scaffold(topBar = { TopAppBar(title = { Text(text = stringResource(R.string.settings_title)) }) }) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Раздел Разрешения
            Text(text = stringResource(R.string.permissions_title))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {
                    val activity = (ctx as? android.app.Activity)
                    if (activity != null && !notifGranted) {
                        Permissions.requestPostNotifications(activity)
                        notifGranted = Permissions.hasPostNotifications(ctx)
                    }
                }) {
                    val label = if (notifGranted) stringResource(R.string.status_granted) else stringResource(R.string.action_allow_notifications)
                    Text(text = "${stringResource(R.string.perm_notifications)}: $label")
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {
                    if (!overlayGranted) {
                        Permissions.openOverlaySettings(ctx)
                        overlayGranted = Permissions.canDrawOverlays(ctx)
                    }
                }) {
                    val label = if (overlayGranted) stringResource(R.string.status_granted) else stringResource(R.string.action_allow_overlay)
                    Text(text = "${stringResource(R.string.perm_overlay)}: $label")
                }
            }

            Text(text = stringResource(R.string.settings_theme))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    RadioButton(selected = themeMode == ThemeMode.Light, onClick = { onThemeChange(ThemeMode.Light) })
                    Text(text = stringResource(R.string.theme_light))
                }
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    RadioButton(selected = themeMode == ThemeMode.Dark, onClick = { onThemeChange(ThemeMode.Dark) })
                    Text(text = stringResource(R.string.theme_dark))
                }
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    RadioButton(selected = themeMode == ThemeMode.System, onClick = { onThemeChange(ThemeMode.System) })
                    Text(text = stringResource(R.string.theme_system))
                }
            }

            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Switch(checked = vibrationEnabled, onCheckedChange = { vibrationEnabled = it })
                Spacer(Modifier.width(8.dp))
                Text(text = stringResource(R.string.settings_vibration))
            }

            Text(text = stringResource(R.string.settings_sound))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { /* TODO: Выбор звука */ }) { Text("Выбрать") }
                OutlinedButton(onClick = { /* TODO: Очистить звук */ }) { Text("Сбросить") }
            }

            Spacer(Modifier.height(8.dp))
            Text(text = stringResource(R.string.backup_export))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {
                    val file = BackupManager.exportToTxt(ctx, vm.reminders)
                    Toast.makeText(ctx, "Экспортировано: ${file.absolutePath}", Toast.LENGTH_LONG).show()
                }) { Text("Экспорт .txt") }
                OutlinedButton(onClick = {
                    val file = File(ctx.getExternalFilesDir(null), "backup/reminders.txt")
                    val list = BackupManager.importFromTxt(ctx, file)
                    list.forEach { r ->
                        vm.addReminder(r.title, r.description, r.timeMillis, r.repeat)
                    }
                    Toast.makeText(ctx, "Импортировано: ${list.size}", Toast.LENGTH_LONG).show()
                }) { Text(stringResource(R.string.backup_import)) }
            }
        }
    }
}