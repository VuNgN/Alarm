package com.vungn.alarm.repo

import com.vungn.alarm.model.dao.AlarmDao
import com.vungn.alarm.model.entity.Alarm
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar
import javax.inject.Inject

class UpdateAlarmRepo @Inject constructor(private val alarmDao: AlarmDao) {
    suspend fun updateAlarm(alarm: Alarm): Int {
        return withContext(Dispatchers.IO) {
            val calendar = Calendar.getInstance().also { cal ->
                cal.timeInMillis = alarm.date
                if (cal.before(Calendar.getInstance())) {
                    val currentCalendar = Calendar.getInstance()
                    cal.set(Calendar.DATE, currentCalendar.get(Calendar.DATE) + 1)
                }
                cal.isLenient = false
            }
            alarmDao.update(alarm.copy(date = calendar.timeInMillis))
        }
    }
}
