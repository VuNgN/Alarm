package com.vungn.alarm.util

import android.net.Uri
import androidx.room.TypeConverter
import com.google.gson.Gson

object TypeConverter {
    @TypeConverter
    fun toSnooze(value: String): Snooze {
        return Gson().fromJson(value, Snooze::class.java)
    }

    @TypeConverter
    fun fromSnooze(value: Snooze): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toAlarmState(value: String): AlarmState {
        return Gson().fromJson(value, AlarmState::class.java)
    }

    @TypeConverter
    fun fromAlarmState(value: AlarmState): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toUri(value: String): Uri {
        return Uri.parse(value)
    }

    @TypeConverter
    fun fromUri(value: Uri): String {
        return value.toString()
    }
}
