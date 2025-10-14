package com.example.napominalka.ui.screens

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.napominalka.R
import com.example.napominalka.data.Reminder
import com.example.napominalka.data.ReminderStatus
import com.example.napominalka.util.formatDateTime
import com.example.napominalka.viewmodel.ReminderViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    vm: ReminderViewModel,
    backgroundImageUri: Uri?,
    onAdd: () -> Unit,
    onEdit: (Long) -> Unit,
    onSettings: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Активные, 1: Выполненные
    var search by remember { mutableStateOf(TextFieldValue("")) }
    var showOverdueOnly by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) { vm.refreshOverdue(System.currentTimeMillis()) }

    val active = vm.reminders.filter { it.status == ReminderStatus.ACTIVE }
    val done = vm.reminders.filter { it.status == ReminderStatus.COMPLETED }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.app_name)) },
                actions = {
                    IconButton(onClick = onSettings) {
                        Icon(Icons.Default.Settings, contentDescription = null)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAdd) {
                Icon(Icons.Default.Add, contentDescription = null)
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            backgroundImageUri?.let { uri ->
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx -> android.widget.ImageView(ctx) },
                    update = { imageView ->
                        imageView.scaleType = android.widget.ImageView.ScaleType.CENTER_CROP
                        imageView.setImageURI(uri)
                    }
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(12.dp),
            ) {
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("${stringResource(R.string.tab_active)} (${active.size})") }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("${stringResource(R.string.tab_done)} (${done.size})") }
                    )
                }

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = search,
                    onValueChange = { search = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(R.string.search_placeholder)) }
                )

                Spacer(Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = showOverdueOnly, onCheckedChange = { showOverdueOnly = it })
                    Text(text = stringResource(R.string.filter_overdue))
                }

                Spacer(Modifier.height(8.dp))

                val list = when (selectedTab) {
                    0 -> active
                        .filter { !showOverdueOnly || it.status == ReminderStatus.OVERDUE }
                        .filter { it.matches(search.text) }
                    else -> done.filter { it.matches(search.text) }
                }

                if (selectedTab == 0 && list.isEmpty()) {
                    Text(text = stringResource(R.string.empty_active))
                }

                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(list) { item ->
                        ReminderRow(
                            item = item,
                            onEdit = { onEdit(item.id) },
                            onDelete = { scope.launch { vm.deleteReminder(item.id) } },
                            onComplete = { scope.launch { vm.markCompleted(item.id) } }
                        )
                    }
                }
            }
        }
    }
}

private fun Reminder.matches(query: String): Boolean {
    if (query.isBlank()) return true
    val q = query.trim().lowercase()
    return title.lowercase().contains(q) || description.lowercase().contains(q)
}

@Composable
private fun ReminderRow(
    item: Reminder,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onComplete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(text = stringResource(R.string.delete_confirm_title)) },
            text = { Text(text = stringResource(R.string.delete_confirm_message)) },
            confirmButton = {
                TextButton(onClick = {
                    onDelete()
                    showDeleteDialog = false
                }) { Text(text = stringResource(R.string.yes)) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text(text = stringResource(R.string.no)) }
            }
        )
    }

    Card { 
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = item.title, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.weight(1f))
                if (item.status == ReminderStatus.OVERDUE) {
                    AssistChip(onClick = {}, label = { Text(text = stringResource(R.string.status_overdue)) })
                }
            }
            if (item.description.isNotBlank()) {
                Text(text = item.description, style = MaterialTheme.typography.bodyMedium)
            }
            Text(text = formatDateTime(item.timeMillis), style = MaterialTheme.typography.bodySmall)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, contentDescription = null) }
                IconButton(onClick = { showDeleteDialog = true }) { Icon(Icons.Default.Delete, contentDescription = null) }
                if (item.status == ReminderStatus.ACTIVE) {
                    IconButton(onClick = onComplete) { Icon(Icons.Default.Check, contentDescription = null) }
                }
            }
        }
    }
}