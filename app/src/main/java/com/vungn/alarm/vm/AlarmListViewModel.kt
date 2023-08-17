package com.vungn.alarm.vm

import android.content.Context
import com.vungn.alarm.model.entity.Alarm
import kotlinx.coroutines.flow.StateFlow

interface AlarmListViewModel {
    val alarmList: StateFlow<List<Alarm>>
    fun turnOnOffAlarm(context: Context, alarm: Alarm)
}
