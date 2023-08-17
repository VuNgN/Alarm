package com.vungn.alarm.receiver

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.ALARM_SERVICE
import android.content.Intent
import android.os.Build
import android.util.Log
import com.vungn.alarm.model.dao.AlarmDao
import com.vungn.alarm.service.AlarmService
import com.vungn.alarm.util.AlarmState
import com.vungn.alarm.util.goAsync
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class StopReceiver : BroadcastReceiver() {
    @Inject
    lateinit var alarmDao: AlarmDao

    override fun onReceive(context: Context, intent: Intent) = goAsync {
        Log.d(TAG, "Stop alarm")
        val id = intent.getLongExtra("id", -1)
        updateDB(id)
        cancelAlarm(context, id)
    }

    private suspend fun updateDB(id: Long) {
        withContext(Dispatchers.IO) {
            alarmDao.getById(id).let { alarm ->
                alarmDao.update(
                    alarm.copy(state = AlarmState.OFF)
                )
            }
        }
    }

    private fun cancelAlarm(context: Context, id: Long) {
        Log.d(TAG, "cancelAlarm: alarm id = $id")
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
        context.stopService(Intent(context, AlarmService::class.java))
    }

    companion object {
        private val TAG = this::class.simpleName
    }
}
