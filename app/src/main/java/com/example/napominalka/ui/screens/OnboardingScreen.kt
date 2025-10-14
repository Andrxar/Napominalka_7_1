package com.example.napominalka.ui.screens

import android.net.Uri
import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.napominalka.R
import com.example.napominalka.util.Permissions

@Composable
fun OnboardingScreen(
    onStart: () -> Unit,
    onPickBackground: (Uri?) -> Unit
) {
    val pickedImage = remember { mutableStateOf<Uri?>(null) }
    val ctx = LocalContext.current
    val activity = ctx as? Activity
    var notifGranted = remember { mutableStateOf(Permissions.hasPostNotifications(ctx)) }
    var overlayGranted = remember { mutableStateOf(Permissions.canDrawOverlays(ctx)) }

    LaunchedEffect(Unit) {
        notifGranted.value = Permissions.hasPostNotifications(ctx)
        overlayGranted.value = Permissions.canDrawOverlays(ctx)
    }
    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        pickedImage.value = uri
        onPickBackground(uri)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Отобразим выбранное изображение через AndroidView, чтобы не тянуть Coil
        pickedImage.value?.let { uri ->
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
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = stringResource(R.string.onboarding_title))
            Text(text = stringResource(R.string.onboarding_subtitle))
            Button(onClick = { pickImageLauncher.launch("image/*") }) {
                Text(text = stringResource(R.string.onboarding_pick_bg))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = stringResource(R.string.permissions_title))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {
                    if (activity != null && !notifGranted.value) {
                        Permissions.requestPostNotifications(activity)
                    }
                }) {
                    val label = if (notifGranted.value) stringResource(R.string.status_granted) else stringResource(R.string.action_allow_notifications)
                    Text(text = "${stringResource(R.string.perm_notifications)}: $label")
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {
                    if (!overlayGranted.value) {
                        Permissions.openOverlaySettings(ctx)
                    }
                }) {
                    val label = if (overlayGranted.value) stringResource(R.string.status_granted) else stringResource(R.string.action_allow_overlay)
                    Text(text = "${stringResource(R.string.perm_overlay)}: $label")
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onStart) {
                Text(text = stringResource(R.string.action_start))
            }
        }
    }
}