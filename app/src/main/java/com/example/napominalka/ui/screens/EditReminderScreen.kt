package com.example.napominalka.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.napominalka.R
import com.example.napominalka.data.RepeatOption
import com.example.napominalka.util.formatDateTime
import com.example.napominalka.viewmodel.ReminderViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditReminderScreen(
    existingId: Long?,
    vm: ReminderViewModel,
    onSaved: () -> Unit,
    onCancel: () -> Unit
) {
    val existing = existingId?.let { vm.getById(it) }
    var title by remember { mutableStateOf(existing?.title ?: "") }
    var description by remember { mutableStateOf(existing?.description ?: "") }
    var repeat by remember { mutableStateOf(existing?.repeat ?: RepeatOption.Once) }

    val zone = ZoneId.systemDefault()
    val defaultMillis = existing?.timeMillis ?: (System.currentTimeMillis() + 10 * 60 * 1000)
    var dateTime by remember {
        mutableStateOf(LocalDateTime.ofInstant(Instant.ofEpochMilli(defaultMillis), zone))
    }

    val ctx = LocalContext.current

    fun openDatePicker() {
        val d = dateTime.toLocalDate()
        DatePickerDialog(ctx, { _, y, m, day ->
            dateTime = LocalDateTime.of(LocalDate.of(y, m + 1, day), dateTime.toLocalTime())
        }, d.year, d.monthValue - 1, d.dayOfMonth).show()
    }

    fun openTimePicker() {
        val t = dateTime.toLocalTime()
        TimePickerDialog(ctx, { _, h, min ->
            dateTime = LocalDateTime.of(dateTime.toLocalDate(), LocalTime.of(h, min))
        }, t.hour, t.minute, true).show()
    }

    var dropdownExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(text = if (existingId == null) stringResource(R.string.action_save) else stringResource(R.string.action_edit)) })
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.edit_title)) },
                singleLine = true
            )
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.edit_description)) }
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { openDatePicker() }) {
                    Text(text = stringResource(R.string.edit_pick_date))
                }
                OutlinedButton(onClick = { openTimePicker() }) {
                    Text(text = stringResource(R.string.edit_pick_time))
                }
            }
            Text(text = formatDateTime(dateTime.atZone(zone).toInstant().toEpochMilli()))

            ExposedDropdownMenuBox(expanded = dropdownExpanded, onExpandedChange = { dropdownExpanded = it }) {
                OutlinedTextField(
                    readOnly = true,
                    value = when (repeat) {
                        RepeatOption.Once -> stringResource(R.string.repeat_once)
                        RepeatOption.Daily -> stringResource(R.string.repeat_daily)
                        RepeatOption.Weekly -> stringResource(R.string.repeat_weekly)
                        RepeatOption.Monthly -> stringResource(R.string.repeat_monthly)
                        RepeatOption.Yearly -> stringResource(R.string.repeat_yearly)
                    },
                    onValueChange = {},
                    label = { Text(stringResource(R.string.repeat)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = dropdownExpanded, onDismissRequest = { dropdownExpanded = false }) {
                    DropdownMenuItem(text = { Text(stringResource(R.string.repeat_once)) }, onClick = { repeat = RepeatOption.Once; dropdownExpanded = false })
                    DropdownMenuItem(text = { Text(stringResource(R.string.repeat_daily)) }, onClick = { repeat = RepeatOption.Daily; dropdownExpanded = false })
                    DropdownMenuItem(text = { Text(stringResource(R.string.repeat_weekly)) }, onClick = { repeat = RepeatOption.Weekly; dropdownExpanded = false })
                    DropdownMenuItem(text = { Text(stringResource(R.string.repeat_monthly)) }, onClick = { repeat = RepeatOption.Monthly; dropdownExpanded = false })
                    DropdownMenuItem(text = { Text(stringResource(R.string.repeat_yearly)) }, onClick = { repeat = RepeatOption.Yearly; dropdownExpanded = false })
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {
                    val scope = rememberCoroutineScope()
                    scope.launch {
                        val millis = dateTime.atZone(zone).toInstant().toEpochMilli()
                        if (title.isBlank()) return@launch
                        if (existingId == null) {
                            val id = vm.addReminder(title, description, millis, repeat)
                            com.example.napominalka.notifications.ReminderScheduler.schedule(ctx, id, millis)
                        } else {
                            vm.updateReminder(existingId, title, description, millis, repeat)
                            com.example.napominalka.notifications.ReminderScheduler.schedule(ctx, existingId, millis)
                        }
                        onSaved()
                    }
                }) { Text(text = stringResource(R.string.action_save)) }

                OutlinedButton(onClick = onCancel) { Text(text = stringResource(android.R.string.cancel)) }
            }
        }
    }
}