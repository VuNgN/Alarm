package com.vungn.alarm.model.entity

import android.media.RingtoneManager
import android.net.Uri
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.vungn.alarm.util.AlarmState
import com.vungn.alarm.util.Interval
import com.vungn.alarm.util.Snooze
import com.vungn.alarm.util.SnoozeTimes

@Entity
data class Alarm(
    @PrimaryKey(autoGenerate = true) val id: Long? = null,
    val title: String,
    val date: Long,
    val tone: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM),
    val vibrate: Boolean = true,
    val snooze: Snooze = Snooze(true, Interval.FIVE_MINUTES, SnoozeTimes.THREE_TIMES),
    val state: AlarmState = AlarmState.OFF,
    val snoozeTime: Int = 0
)
