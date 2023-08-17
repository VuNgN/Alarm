package com.vungn.alarm.di

import android.content.Context
import androidx.room.Room
import com.vungn.alarm.db.AppDatabase
import com.vungn.alarm.model.dao.AlarmDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, "alarm.db")
            .fallbackToDestructiveMigration().build()
    }

    @Provides
    @Singleton
    fun provideAlarmDao(database: AppDatabase): AlarmDao = database.alarmDao()
}
