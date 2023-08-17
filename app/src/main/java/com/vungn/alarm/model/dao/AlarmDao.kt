package com.vungn.alarm.model.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.vungn.alarm.model.entity.Alarm
import kotlinx.coroutines.flow.Flow

@Dao
interface AlarmDao {
    @Query("SELECT * FROM alarm")
    fun getAll(): Flow<List<Alarm>>

    @Query("SELECT * FROM alarm WHERE id = :id")
    fun getById(id: Long): Alarm

    @Insert
    fun insert(alarm: Alarm): Long

    @Update
    fun update(alarm: Alarm): Int

    @Delete
    fun delete(alarm: Alarm)
}
