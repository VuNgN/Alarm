package com.vungn.alarm.repo

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.ALARM_SERVICE
import android.content.Intent
import android.os.Build
import com.vungn.alarm.service.AlarmService
import javax.inject.Inject

class CancelExactAlarmRepo @Inject constructor() {
    fun cancelExactAlarm(context: Context, id: Long) {
        val intent = Intent(context, AlarmService::class.java).let {
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
        val alarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(intent)
        intent.cancel()
    }
}
