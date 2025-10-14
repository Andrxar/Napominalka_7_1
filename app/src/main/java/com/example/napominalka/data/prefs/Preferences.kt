package com.example.napominalka.data.prefs

import android.content.Context
import android.net.Uri

object Preferences {
    private const val NAME = "napominalka_prefs"
    private const val KEY_RINGTONE = "ringtone_uri"
    private const val KEY_VIBRATION = "vibration_enabled"

    private fun prefs(ctx: Context) = ctx.getSharedPreferences(NAME, Context.MODE_PRIVATE)

    fun getRingtoneUri(ctx: Context): String? = prefs(ctx).getString(KEY_RINGTONE, null)
    fun setRingtoneUri(ctx: Context, uri: Uri?) {
        prefs(ctx).edit().putString(KEY_RINGTONE, uri?.toString()).apply()
    }

    fun isVibrationEnabled(ctx: Context): Boolean = prefs(ctx).getBoolean(KEY_VIBRATION, true)
    fun setVibrationEnabled(ctx: Context, enabled: Boolean) {
        prefs(ctx).edit().putBoolean(KEY_VIBRATION, enabled).apply()
    }
}