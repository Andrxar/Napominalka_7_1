package com.example.napominalka.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.Gravity
import android.media.MediaPlayer
import android.net.Uri
import android.os.IBinder
import android.os.SystemClock
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.Settings
import androidx.core.app.NotificationCompat
import com.example.napominalka.R
import com.example.napominalka.data.ReminderRepository
import com.example.napominalka.data.db.AppDatabase
import com.example.napominalka.data.prefs.Preferences
import com.example.napominalka.notifications.ReminderScheduler
import com.example.napominalka.MainActivity
import com.example.napominalka.util.Permissions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

class OverlayService : Service() {
    private val scope = CoroutineScope(Dispatchers.Default)
    private var job: Job? = null
    private var player: MediaPlayer? = null
    private var windowManager: WindowManager? = null
    private var overlayView: View? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val id = intent?.getLongExtra("id", -1L) ?: -1L
        val action = intent?.action

        // Обработка быстрых действий из уведомления
        if (action == ACTION_COMPLETE && id > 0) {
            scope.launch(Dispatchers.IO) {
                try {
                    val repo = ReminderRepository(AppDatabase.get(this@OverlayService).reminders())
                    repo.markCompleted(id)
                    ReminderScheduler.cancel(this@OverlayService, id)
                } catch (_: Exception) {}
                stopAllAndClose()
            }
            return START_NOT_STICKY
        } else if (action == ACTION_SNOOZE_5M && id > 0) {
            scope.launch(Dispatchers.IO) {
                try {
                    val repo = ReminderRepository(AppDatabase.get(this@OverlayService).reminders())
                    val r = repo.getById(id) ?: return@launch
                    val newTime = System.currentTimeMillis() + 5 * 60_000L
                    repo.update(id, r.title, r.description, newTime, r.repeat)
                    ReminderScheduler.schedule(this@OverlayService, id, newTime)
                } catch (_: Exception) {}
                stopAllAndClose()
            }
            return START_NOT_STICKY
        }

        createChannel()

        // Построим уведомление с экшенами
        val completeIntent = Intent(this, OverlayService::class.java).apply {
            action = ACTION_COMPLETE
            putExtra("id", id)
        }
        val completePi = PendingIntent.getService(
            this,
            (id * 10 + 1).toInt(),
            completeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val snooze5Intent = Intent(this, OverlayService::class.java).apply {
            action = ACTION_SNOOZE_5M
            putExtra("id", id)
        }
        val snooze5Pi = PendingIntent.getService(
            this,
            (id * 10 + 2).toInt(),
            snooze5Intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.overlay_title))
            .setContentText("Напоминание активно")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .addAction(android.R.drawable.checkbox_on_background, getString(R.string.action_complete), completePi)
            .addAction(android.R.drawable.ic_menu_recent_history, getString(R.string.snooze_5m), snooze5Pi)
            .build()
        startForeground(NOTIF_ID, notification)

        // Схема звука: 1 минута звука, 1 минута тишины, 5 циклов
        job?.cancel()
        job = scope.launch {
            repeat(5) {
                playSound(60_000L)
                delay(60_000L)
            }
            // По окончании 5 циклов помечаем как ПРОСРОЧЕНО, если пользователь не взаимодействовал
            if (id > 0) {
                try {
                    val repo = ReminderRepository(AppDatabase.get(this@OverlayService).reminders())
                    repo.markOverdue(id)
                } catch (_: Exception) { }
            }
            stopSelf()
        }

        // Показ системного оверлея поверх всех приложений
        showOverlay(intent)
        return START_STICKY
    }

    override fun onDestroy() {
        job?.cancel()
        player?.stop()
        player?.release()
        player = null
        removeOverlay()
        super.onDestroy()
    }

    private fun playSound(durationMs: Long) {
        try {
            player?.release()
            val uriStr = Preferences.getRingtoneUri(this)
            val uri: Uri = if (uriStr != null) Uri.parse(uriStr) else android.provider.Settings.System.DEFAULT_ALARM_ALERT_URI
            player = MediaPlayer.create(this, uri)
            player?.isLooping = true
            player?.start()
            // Вибрация при включении, если разрешено в настройках
            if (Preferences.isVibrationEnabled(this)) {
                val vib = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                val effect = VibrationEffect.createWaveform(longArrayOf(0, 1000, 500, 1000, 500, 1000), 0)
                vib.vibrate(effect)
            }
            SystemClock.sleep(durationMs)
            player?.pause()
            // Останавливаем вибрацию после минуты
            val vib = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vib.cancel()
        } catch (_: Exception) {}
    }

    private fun createChannel() {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(CHANNEL_ID, "Напоминания", NotificationManager.IMPORTANCE_HIGH)
        channel.enableVibration(Preferences.isVibrationEnabled(this))
        if (Preferences.isVibrationEnabled(this)) {
            channel.vibrationPattern = longArrayOf(0, 500, 250, 500)
        }
        nm.createNotificationChannel(channel)
    }

    private fun showOverlay(intent: Intent?) {
        // Если нет разрешения на оверлей — откроем настройки и не пытаемся добавить окно
        if (!Settings.canDrawOverlays(this)) {
            try { Permissions.openOverlaySettings(this) } catch (_: Exception) {}
            return
        }

        val id = intent?.getLongExtra("id", -1L) ?: -1L
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val inflater = LayoutInflater.from(this)
        val view = inflater.inflate(R.layout.overlay_view, null)

        // Заполняем заголовок и описание
        scope.launch(Dispatchers.IO) {
            try {
                val repo = ReminderRepository(AppDatabase.get(this@OverlayService).reminders())
                val reminder = if (id > 0) repo.getById(id) else null
                launch(Dispatchers.Main) {
                    view.findViewById<android.widget.TextView>(R.id.title).text = reminder?.title ?: getString(R.string.overlay_title)
                    view.findViewById<android.widget.TextView>(R.id.description).text = reminder?.description ?: ""
                }
            } catch (_: Exception) {}
        }

        // Обработчики кнопок
        view.findViewById<android.widget.Button>(R.id.btn_complete).setOnClickListener {
            scope.launch(Dispatchers.IO) {
                if (id > 0) {
                    val repo = ReminderRepository(AppDatabase.get(this@OverlayService).reminders())
                    repo.markCompleted(id)
                    ReminderScheduler.cancel(this@OverlayService, id)
                }
                stopAllAndClose()
            }
        }
        view.findViewById<android.widget.Button>(R.id.btn_edit).setOnClickListener {
            val editIntent = Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                putExtra(MainActivity.EXTRA_EDIT_ID, id)
            }
            startActivity(editIntent)
            stopAllAndClose()
        }

        fun snooze(minutes: Int) {
            scope.launch(Dispatchers.IO) {
                if (id > 0) {
                    val repo = ReminderRepository(AppDatabase.get(this@OverlayService).reminders())
                    val r = repo.getById(id) ?: return@launch
                    val newTime = System.currentTimeMillis() + minutes * 60_000L
                    repo.update(id, r.title, r.description, newTime, r.repeat)
                    ReminderScheduler.schedule(this@OverlayService, id, newTime)
                }
                stopAllAndClose()
            }
        }
        view.findViewById<android.widget.Button>(R.id.btn_5m).setOnClickListener { snooze(5) }
        view.findViewById<android.widget.Button>(R.id.btn_10m).setOnClickListener { snooze(10) }
        view.findViewById<android.widget.Button>(R.id.btn_15m).setOnClickListener { snooze(15) }
        view.findViewById<android.widget.Button>(R.id.btn_30m).setOnClickListener { snooze(30) }
        view.findViewById<android.widget.Button>(R.id.btn_1h).setOnClickListener { snooze(60) }
        view.findViewById<android.widget.Button>(R.id.btn_1d).setOnClickListener { snooze(24 * 60) }

        // Параметры окна оверлея
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            android.graphics.PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.TOP

        overlayView = view
        windowManager?.addView(view, params)
    }

    private fun removeOverlay() {
        overlayView?.let { v ->
            try { windowManager?.removeView(v) } catch (_: Exception) {}
        }
        overlayView = null
    }

    private fun stopAllAndClose() {
        try {
            player?.stop()
            player?.release()
            player = null
            val vib = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vib.cancel()
        } catch (_: Exception) {}
        removeOverlay()
        stopSelf()
    }

    companion object {
        private const val CHANNEL_ID = "reminder_overlay"
        private const val NOTIF_ID = 1001
        private const val ACTION_COMPLETE = "OverlayService.ACTION_COMPLETE"
        private const val ACTION_SNOOZE_5M = "OverlayService.ACTION_SNOOZE_5M"
    }
}