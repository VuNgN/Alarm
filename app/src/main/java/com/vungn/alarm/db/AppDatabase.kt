package com.vungn.alarm.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.vungn.alarm.model.dao.AlarmDao
import com.vungn.alarm.model.entity.Alarm
import com.vungn.alarm.util.TypeConverter

@Database(entities = [Alarm::class], version = 1)
@TypeConverters(TypeConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun alarmDao(): AlarmDao
}
