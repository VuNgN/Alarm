package com.vungn.alarm.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
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
class SnoozeReceiver : BroadcastReceiver() {
    @Inject
    lateinit var alarmDao: AlarmDao

    override fun onReceive(context: Context?, intent: Intent) = goAsync {
        Log.d(TAG, "Snooze alarm")
        val id = intent.getLongExtra("id", -1)
        updateDB(id)
        context?.stopService(Intent(context, AlarmService::class.java))
    }

    private suspend fun updateDB(id: Long) {
        withContext(Dispatchers.IO) {
            alarmDao.getById(id).let { alarm ->
                alarmDao.update(
                    alarm.copy(state = AlarmState.SNOOZE)
                )
            }
        }
    }

    companion object {
        private val TAG = this::class.simpleName
    }
}
