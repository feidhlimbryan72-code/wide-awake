package com.wideawake.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.*
import androidx.core.app.NotificationCompat
import com.wideawake.app.R
import kotlinx.coroutines.*

/**
 * Foreground Service running audio playback with USAGE_ALARM to bypass DND and mute settings.
 * Ramps audio volume and initiates haptic shockwaves if disarm takes over 90 seconds.
 */
class AlarmAudioService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var penaltyJob: Job? = null
    private var hapticJob: Job? = null
    private var secondsElapsed = 0
    private var isPenaltyActive = false

    override fun onCreate() {
        super.onCreate()
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        if (action == ACTION_STOP_ALARM) {
            stopAlarm()
            stopSelf()
            return START_NOT_STICKY
        }

        val initialVolume = intent?.getFloatExtra(EXTRA_VOLUME, 0.85f) ?: 0.85f
        val enablePenalty = intent?.getBooleanExtra(EXTRA_ENABLE_PENALTY, true) ?: true

        startForeground(NOTIFICATION_ID, createForegroundNotification())
        startAudioPlayback(initialVolume)

        if (enablePenalty) {
            startPenaltyEscalation(initialVolume)
        }

        return START_STICKY
    }

    private fun startAudioPlayback(initialVolume: Float) {
        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED)
                        .build()
                )
                val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                setDataSource(applicationContext, alarmUri)
                isLooping = true
                setVolume(initialVolume, initialVolume)
                prepare()
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun startPenaltyEscalation(baseVolume: Float) {
        penaltyJob?.cancel()
        penaltyJob = serviceScope.launch {
            secondsElapsed = 0
            while (isActive) {
                delay(1000)
                secondsElapsed++

                if (secondsElapsed <= 90) {
                    val progress = secondsElapsed / 90.0f
                    val ramped = baseVolume + ((1.0f - baseVolume) * progress)
                    mediaPlayer?.setVolume(ramped, ramped)
                }

                if (secondsElapsed >= 90 && !isPenaltyActive) {
                    isPenaltyActive = true
                    mediaPlayer?.setVolume(1.0f, 1.0f)
                    startAggressiveHaptics()
                }
            }
        }
    }

    private fun startAggressiveHaptics() {
        hapticJob?.cancel()
        hapticJob = serviceScope.launch {
            while (isActive && isPenaltyActive) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 200, 100, 300), -1))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(longArrayOf(0, 200, 100, 300), -1)
                }
                delay(1000)
            }
        }
    }

    private fun stopAlarm() {
        penaltyJob?.cancel()
        hapticJob?.cancel()
        vibrator?.cancel()
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        isPenaltyActive = false
    }

    override fun onDestroy() {
        stopAlarm()
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createForegroundNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.alarm_active_title))
            .setContentText(getString(R.string.alarm_active_desc))
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .build()
    }

    companion object {
        const val CHANNEL_ID = "alarm_audio_channel"
        const val NOTIFICATION_ID = 1001
        const val ACTION_STOP_ALARM = "com.wideawake.ACTION_STOP_ALARM"
        const val EXTRA_VOLUME = "extra_volume"
        const val EXTRA_ENABLE_PENALTY = "extra_enable_penalty"
    }
}
