package com.example.napominalka

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.napominalka.ui.NapominalkaApp

class MainActivity : ComponentActivity() {
    companion object {
        const val EXTRA_EDIT_ID = "navigate_to_edit_id"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val editId = intent?.getLongExtra(EXTRA_EDIT_ID, -1L) ?: -1L
        setContent { NapominalkaApp(navigateToEditId = if (editId >= 0) editId else null) }
    }
}