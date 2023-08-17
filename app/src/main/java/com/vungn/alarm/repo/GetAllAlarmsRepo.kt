package com.vungn.alarm.repo

import com.vungn.alarm.model.dao.AlarmDao
import com.vungn.alarm.model.entity.Alarm
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetAllAlarmsRepo @Inject constructor(
    private val dao: AlarmDao
) {
    suspend fun getAlarmList(): Flow<List<Alarm>> {
        return withContext(Dispatchers.IO) { dao.getAll() }
    }
}
