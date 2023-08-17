package com.vungn.alarm.vm

import android.net.Uri
import com.vungn.alarm.util.Snooze
import kotlinx.coroutines.flow.StateFlow
import java.util.Calendar

interface AlarmViewModel {
    val id: StateFlow<Long?>
    val title: StateFlow<String>
    val date: StateFlow<Calendar>
    val tone: StateFlow<Uri>
    val vibrate: StateFlow<Boolean>
    val snooze: StateFlow<Snooze>
    fun setId(id: Long?)
    fun setDate(date: Calendar)
    fun setTitle(title: String)
    fun setTone(tone: Uri)
    fun setVibrate(vibrate: Boolean)
    fun setSnooze(snooze: Snooze)
    fun startAlarm()
}
