package com.vungn.alarm.vm.impl

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vungn.alarm.model.entity.Alarm
import com.vungn.alarm.repo.CancelExactAlarmRepo
import com.vungn.alarm.repo.GetAllAlarmsRepo
import com.vungn.alarm.repo.SetExactAlarmRepo
import com.vungn.alarm.repo.UpdateAlarmRepo
import com.vungn.alarm.util.AlarmState
import com.vungn.alarm.vm.AlarmListViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class AlarmListViewModelImpl @Inject constructor(
    private val alarmListRepo: GetAllAlarmsRepo,
    private val updateAlarmRepo: UpdateAlarmRepo,
    private val setExactAlarmRepo: SetExactAlarmRepo,
    private val cancelExactAlarmRepo: CancelExactAlarmRepo
) : AlarmListViewModel, ViewModel() {
    private val _alarmList = MutableStateFlow(emptyList<Alarm>())

    init {
        viewModelScope.launch(Dispatchers.IO) {
            alarmListRepo.getAlarmList().onEach { alarms ->
                _alarmList.emit(alarms.sortedBy { alarm ->
                    Calendar.getInstance().let {
                        val cal =
                            Calendar.getInstance().also { cal -> cal.timeInMillis = alarm.date }
                        it.set(Calendar.HOUR_OF_DAY, cal.get(Calendar.HOUR_OF_DAY))
                        it.set(Calendar.MINUTE, cal.get(Calendar.MINUTE))
                        it.time
                    }
                })
            }.stateIn(viewModelScope)
        }
    }

    override val alarmList: StateFlow<List<Alarm>>
        get() = _alarmList

    override fun turnOnOffAlarm(context: Context, alarm: Alarm) {
        Log.d(TAG, "turnOnOffAlarm: alarm id = ${alarm.id}")
        when (alarm.state) {
            AlarmState.OFF -> {
                viewModelScope.launch(Dispatchers.IO) {
                    updateAlarmRepo.updateAlarm(alarm.copy(state = AlarmState.ON))
                    setExactAlarmRepo.setExactAlarm(context, alarm.id ?: -1)
                }
            }

            AlarmState.ON, AlarmState.RINGING, AlarmState.SNOOZE -> {
                viewModelScope.launch(Dispatchers.IO) {
                    updateAlarmRepo.updateAlarm(alarm.copy(state = AlarmState.OFF))
                    cancelExactAlarmRepo.cancelExactAlarm(context, alarm.id ?: -1)
                }
            }
        }
    }

    companion object {
        private const val TAG = "AlarmListViewModelImpl"
    }
}
