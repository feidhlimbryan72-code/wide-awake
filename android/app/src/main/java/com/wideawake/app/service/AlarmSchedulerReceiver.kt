package com.wideawake.app.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import androidx.core.content.ContextCompat
import com.wideawake.app.model.Alarm
import com.wideawake.app.ui.ActiveAlarmActivity
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.ZoneId

/**
 * Coordinates exact alarm triggering using AlarmManager.setAlarmClock().
 * Bypasses Android Doze mode and brings the lock-in disarm screen over the keyguard.
 */
class AlarmSchedulerReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Intent.ACTION_BOOT_COMPLETED || action == Intent.ACTION_LOCKED_BOOT_COMPLETED) {
            // Re-register alarms upon device reboot
            restoreScheduledAlarms(context)
            return
        }

        if (action == ACTION_TRIGGER_ALARM) {
            val alarmJson = intent.getStringExtra(EXTRA_ALARM_JSON) ?: return
            val alarm = try {
                Json.decodeFromString<Alarm>(alarmJson)
            } catch (e: Exception) {
                return
            }

            // Acquire temporary WakeLock to ensure screen turns on reliably
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            val wakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
                "WakeOrPay:AlarmWakeLock"
            )
            wakeLock.acquire(30_000) // 30 seconds hold

            // 1. Start persistent AlarmAudioService in foreground
            val serviceIntent = Intent(context, AlarmAudioService::class.java).apply {
                putExtra(AlarmAudioService.EXTRA_VOLUME, alarm.volume)
                putExtra(AlarmAudioService.EXTRA_ENABLE_PENALTY, alarm.isSnoozePenaltyEnabled)
            }
            ContextCompat.startForegroundService(context, serviceIntent)

            // 2. Launch full-screen lock-in disarm Activity
            val activityIntent = Intent(context, ActiveAlarmActivity::class.java).apply {
                putExtra(ActiveAlarmActivity.EXTRA_ALARM_JSON, alarmJson)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
            }
            context.startActivity(activityIntent)

            // 3. Reschedule next cycle if recurring
            if (alarm.repeatDays.isNotEmpty()) {
                scheduleAlarm(context, alarm)
            }
        }
    }

    companion object {
        const val ACTION_TRIGGER_ALARM = "com.wideawake.ALARM_TRIGGER"
        const val EXTRA_ALARM_JSON = "extra_alarm_json"
        private const val PREFS_NAME = "wideawake_alarms_store"
        private const val KEY_ALARMS = "alarms_list"

        fun scheduleAlarm(context: Context, alarm: Alarm) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (!alarmManager.canScheduleExactAlarms()) {
                    // Exact alarm permission not yet granted by user in settings
                    return
                }
            }

            val nextDateTime = alarm.nextTriggerDateTime()
            val epochMillis = nextDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

            val intent = Intent(context, AlarmSchedulerReceiver::class.java).apply {
                action = ACTION_TRIGGER_ALARM
                putExtra(EXTRA_ALARM_JSON, Json.encodeToString(alarm))
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                alarm.id.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Intent for show-on-lockscreen clock icon tap
            val clockIntent = Intent(context, ActiveAlarmActivity::class.java).apply {
                putExtra(ActiveAlarmActivity.EXTRA_ALARM_JSON, Json.encodeToString(alarm))
            }
            val clockPendingIntent = PendingIntent.getActivity(
                context,
                alarm.id.hashCode(),
                clockIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val clockInfo = AlarmManager.AlarmClockInfo(epochMillis, clockPendingIntent)
            alarmManager.setAlarmClock(clockInfo, pendingIntent)
        }

        fun cancelAlarm(context: Context, alarmId: String) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, AlarmSchedulerReceiver::class.java).apply {
                action = ACTION_TRIGGER_ALARM
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                alarmId.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
        }

        fun saveAlarms(context: Context, alarms: List<Alarm>) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val json = Json.encodeToString(alarms)
            prefs.edit().putString(KEY_ALARMS, json).apply()
        }

        fun loadAlarms(context: Context): List<Alarm> {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val json = prefs.getString(KEY_ALARMS, null) ?: return listOf(
                Alarm(
                    hour = 7,
                    minute = 0,
                    label = "Morning Awakening",
                    isEnabled = true,
                    repeatDays = setOf(1, 2, 3, 4, 5)
                )
            )
            return try {
                Json.decodeFromString<List<Alarm>>(json)
            } catch (e: Exception) {
                emptyList()
            }
        }

        private fun restoreScheduledAlarms(context: Context) {
            val alarms = loadAlarms(context)
            alarms.filter { it.isEnabled }.forEach { scheduleAlarm(context, it) }
        }
    }
}
