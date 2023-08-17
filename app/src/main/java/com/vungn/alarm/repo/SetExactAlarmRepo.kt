package com.vungn.alarm.repo

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import com.vungn.alarm.model.dao.AlarmDao
import com.vungn.alarm.service.AlarmService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SetExactAlarmRepo @Inject constructor(private val alarmDao: AlarmDao) {
    suspend fun setExactAlarm(context: Context, id: Long) {
        Log.d(TAG, "setExactAlarm: alarm id = $id")
        withContext(Dispatchers.IO) {
            val alarm = alarmDao.getById(id)
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val alarmIntent = Intent(context, AlarmService::class.java).also { intent ->
                val bundle = Bundle()
                bundle.putLong("id", id)
                bundle.putString("title", alarm.title)
                bundle.putBoolean("vibrate", alarm.vibrate)
                bundle.putParcelable("tone", alarm.tone)
                bundle.putSerializable("snooze", alarm.snooze)
                bundle.putInt("snoozeTime", alarm.snoozeTime)
                intent.putExtras(bundle)
            }.let { intent: Intent ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    PendingIntent.getForegroundService(
                        context,
                        id.toInt(),
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                } else {
                    PendingIntent.getService(
                        context,
                        id.toInt(),
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                }
            }

            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP, alarm.date, alarmIntent
            )
        }
    }

    companion object {
        private const val TAG = "SetExactAlarmRepo"
    }
}
