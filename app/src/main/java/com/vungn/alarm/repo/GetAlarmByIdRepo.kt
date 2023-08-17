package com.vungn.alarm.repo

import com.vungn.alarm.model.dao.AlarmDao
import com.vungn.alarm.model.entity.Alarm
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetAlarmByIdRepo @Inject constructor(private val alarmDao: AlarmDao) {
    suspend fun getAlarmById(id: Long): Alarm {
        return withContext(Dispatchers.IO) { alarmDao.getById(id) }
    }
}
