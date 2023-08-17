package com.vungn.alarm.service

import android.app.AlarmManager
import android.app.Notification
import android.app.Notification.Action
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.VibrationAttributes
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import com.vungn.alarm.MyApplication.Companion.CHANNEL_ID
import com.vungn.alarm.R
import com.vungn.alarm.model.dao.AlarmDao
import com.vungn.alarm.receiver.SnoozeReceiver
import com.vungn.alarm.receiver.StopReceiver
import com.vungn.alarm.util.AlarmState
import com.vungn.alarm.util.Interval
import com.vungn.alarm.util.Snooze
import com.vungn.alarm.util.SnoozeTimes
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import javax.inject.Inject

@Suppress("DEPRECATION")
@AndroidEntryPoint
class AlarmService : Service() {
    @Inject
    lateinit var alarmDao: AlarmDao

    @Inject
    @ApplicationContext
    lateinit var context: Context
    private lateinit var ringtone: Ringtone
    private lateinit var vibrator: Vibrator
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand: start service")
        val bundle = intent?.extras
        val id = bundle?.getLong("id", -1) ?: -1
        val title = bundle?.getString("title").let {
            val defaultTitle = "Alarm"
            if (it.isNullOrBlank()) {
                Log.d(TAG, "on service: title -> $defaultTitle")
                defaultTitle
            } else {
                Log.d(TAG, "on service: title -> $it")
                it
            }
        }
        val tone = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            bundle?.getParcelable("tone", Uri::class.java) ?: RingtoneManager.getDefaultUri(
                RingtoneManager.TYPE_ALARM
            )
        } else {
            bundle?.getParcelable("tone")
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        }
        val vibrate = bundle?.getBoolean("vibrate", true) ?: true
        val snooze = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            bundle?.getSerializable("snooze", Snooze::class.java) ?: Snooze(
                true, Interval.FIVE_MINUTES, SnoozeTimes.THREE_TIMES
            )
        } else {
            bundle?.getSerializable("snooze") as Snooze
        }
        val snoozeTime = bundle?.getInt("snoozeTime", 0) ?: 0
        val notification = getNotification(title, id, snooze.enable)
        ringtone(tone)
        vibrate(vibrate)
        if (snooze.enable && snoozeTime < snooze.times.times) {
            startAlarm(id, title, vibrate, tone, snooze)
        }
        scope.launch {
            updateDB(id, AlarmState.RINGING)
            startForeground(ONGOING_NOTIFICATION_ID, notification)
            delay(1000 * 60 * 1)
            stopSelf()
            if (snooze.times.times == snoozeTime) {
                updateDB(id, AlarmState.OFF)
            } else {
                updateDB(id, AlarmState.SNOOZE)
            }
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        ringtone.stop()
        vibrator.cancel()
        job.cancel()
    }

    private suspend fun updateDB(id: Long, state: AlarmState) {
        withContext(Dispatchers.IO) {
            alarmDao.getById(id).let { alarm ->
                alarmDao.update(
                    alarm.copy(
                        state = state, snoozeTime = alarm.snoozeTime + 1
                    )
                )
            }
        }
    }

    private fun vibrate(vibrate: Boolean) {
        if (!vibrate) return
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            getSystemService(VIBRATOR_SERVICE) as Vibrator
        }
        val pattern = longArrayOf(0, 3000, 1000)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                vibrator.vibrate(
                    VibrationEffect.createWaveform(pattern, 0),
                    VibrationAttributes.createForUsage(VibrationAttributes.USAGE_ALARM)
                )
            } else {
                vibrator.vibrate(VibrationEffect.createWaveform(pattern, 0))
            }
        } else {
            vibrator.vibrate(pattern, 0)
        }
    }

    private fun ringtone(tone: Uri) {
        ringtone = RingtoneManager.getRingtone(
            this, tone
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ringtone.isLooping = true
        }
        ringtone.play()
    }

    private fun startAlarm(id: Long, title: String, vibrate: Boolean, tone: Uri, snooze: Snooze) {
        val intent = Intent(context, AlarmService::class.java).let {
            val bundle = Bundle()
            bundle.putLong("id", id)
            bundle.putString("title", title)
            bundle.putBoolean("vibrate", vibrate)
            bundle.putParcelable("tone", tone)
            it.putExtras(bundle)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                PendingIntent.getForegroundService(
                    context,
                    id.toInt(),
                    it,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            } else {
                PendingIntent.getService(
                    context,
                    id.toInt(),
                    it,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            }
        }
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        val triggerAtMillis = Calendar.getInstance().let {
            it.add(Calendar.MINUTE, snooze.interval.minute)
            it.timeInMillis
        }

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP, triggerAtMillis, intent
        )
    }

    private fun getNotification(title: String, id: Long, isSnooze: Boolean): Notification {
        val stopPendingIntent = Intent(this, StopReceiver::class.java).let {
            it.putExtra("id", id)
            PendingIntent.getBroadcast(this, id.toInt(), it, PendingIntent.FLAG_MUTABLE)
        }
        val snoozePendingIntent = Intent(this, SnoozeReceiver::class.java).let {
            it.putExtra("id", id)
            PendingIntent.getBroadcast(this, id.toInt(), it, PendingIntent.FLAG_MUTABLE)
        }
        val stopAction = Action.Builder(
            Icon.createWithResource(this, R.drawable.round_stop_24), "Stop", stopPendingIntent
        ).build()

        val snoozeAction = Action.Builder(
            Icon.createWithResource(this, R.drawable.round_snooze_24), "Snooze", snoozePendingIntent
        ).build()

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID).setContentTitle("Alarm").setContentText(title)
                .setSmallIcon(R.drawable.round_alarm_24).setTicker("").addAction(stopAction).also {
                    if (isSnooze) {
                        it.addAction(snoozeAction)
                    }
                }.build()
        } else {
            Notification.Builder(this).setContentTitle("Alarm").setContentText(title)
                .setSmallIcon(R.drawable.round_alarm_24).setTicker("").addAction(stopAction).also {
                    if (isSnooze) {
                        it.addAction(snoozeAction)
                    }
                }.build()
        }
    }

    companion object {
        private const val TAG = "AlarmService"
        private const val ONGOING_NOTIFICATION_ID = 1
    }
}
