package com.vungn.alarm.vm.impl

import android.content.Context
import android.media.RingtoneManager
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vungn.alarm.model.entity.Alarm
import com.vungn.alarm.repo.GetAlarmByIdRepo
import com.vungn.alarm.repo.InsertAlarmRepo
import com.vungn.alarm.repo.SetExactAlarmRepo
import com.vungn.alarm.repo.UpdateAlarmRepo
import com.vungn.alarm.util.AlarmState
import com.vungn.alarm.util.Interval
import com.vungn.alarm.util.Snooze
import com.vungn.alarm.util.SnoozeTimes
import com.vungn.alarm.vm.AlarmViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class AlarmViewModelImpl @Inject constructor(
    @ApplicationContext context: Context,
    private val getAlarmByIdRepo: GetAlarmByIdRepo,
    private val insertAlarmRepo: InsertAlarmRepo,
    private val updateAlarmRepo: UpdateAlarmRepo,
    private val setExactAlarmRepo: SetExactAlarmRepo
) : ViewModel(), AlarmViewModel {
    private val defaultUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
    private val start: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val _id = MutableStateFlow<Long?>(null)
    private val _title = MutableStateFlow("")
    private val _date = MutableStateFlow(Calendar.getInstance())
    private val _tone = MutableStateFlow(defaultUri)
    private val _vibrate = MutableStateFlow(true)
    private val _snooze =
        MutableStateFlow(Snooze(true, Interval.FIVE_MINUTES, SnoozeTimes.THREE_TIMES))
    private val _goAction = MutableStateFlow(GoAction.CREATE)

    init {
        viewModelScope.launch {
            _id.collect { id ->
                if (id != null) {
                    withContext(Dispatchers.IO) {
                        getAlarmById(id)
                    }
                    _goAction.emit(GoAction.UPDATE)
                } else {
                    _goAction.emit(GoAction.CREATE)
                }
            }
        }
        viewModelScope.launch {
            start.collect {
                if (it) {
                    _goAction.value.apply {
                        when (this) {
                            GoAction.CREATE -> {
                                withContext(Dispatchers.IO) {
                                    insertAlarm()
                                }
                            }

                            GoAction.UPDATE -> {
                                withContext(Dispatchers.IO) {
                                    updateAlarm()
                                }
                            }
                        }
                    }
                    _id.value.apply {
                        if (this == null) return@apply
                        setExactAlarm(context)
                    }
                }
            }
        }
    }

    private suspend fun getAlarmById(id: Long) {
        getAlarmByIdRepo.getAlarmById(id).also {
            withContext(Dispatchers.Main) {
                _title.emit(it.title)
                _date.emit(Calendar.getInstance().also { cal ->
                    cal.timeInMillis = it.date
                })
                _tone.emit(it.tone)
                _vibrate.emit(it.vibrate)
                _snooze.emit(it.snooze)
            }
        }
    }

    private suspend fun updateAlarm() {
        updateAlarmRepo.updateAlarm(
            Alarm(
                id = _id.value,
                title = _title.value,
                date = _date.value.timeInMillis,
                tone = _tone.value,
                vibrate = _vibrate.value,
                snooze = _snooze.value,
                state = AlarmState.ON,
                snoozeTime = 0
            )
        )
    }

    private suspend fun insertAlarm() {
        insertAlarmRepo.insertAlarm(
            Alarm(
                title = _title.value,
                date = _date.value.timeInMillis,
                tone = _tone.value,
                vibrate = _vibrate.value,
                snooze = _snooze.value,
                state = AlarmState.ON,
                snoozeTime = 0
            )
        ).let { id ->
            _id.emit(id)
        }
    }

    private fun setExactAlarm(context: Context) {
        viewModelScope.launch {
            setExactAlarmRepo.setExactAlarm(context, _id.value ?: -1)
        }
    }

    override val id: StateFlow<Long?>
        get() = _id
    override val title: StateFlow<String>
        get() = _title
    override val date: StateFlow<Calendar>
        get() = _date
    override val tone: StateFlow<Uri>
        get() = _tone
    override val vibrate: StateFlow<Boolean>
        get() = _vibrate
    override val snooze: StateFlow<Snooze>
        get() = _snooze

    override fun setId(id: Long?) {
        viewModelScope.launch {
            _id.emit(id)
        }
    }

    override fun setTitle(title: String) {
        viewModelScope.launch {
            _title.emit(title)
        }
    }

    override fun setTone(tone: Uri) {
        viewModelScope.launch {
            _tone.emit(tone)
        }
    }

    override fun setVibrate(vibrate: Boolean) {
        viewModelScope.launch {
            _vibrate.emit(vibrate)
        }
    }

    override fun setSnooze(snooze: Snooze) {
        viewModelScope.launch {
            _snooze.emit(snooze)
        }
    }

    override fun setDate(date: Calendar) {
        viewModelScope.launch {
            _date.emit(date)
        }
    }

    override fun startAlarm() {
        viewModelScope.launch {
            start.emit(true)
        }
    }

    companion object {
        private val TAG = this::class.simpleName
    }

    enum class GoAction {
        CREATE, UPDATE
    }
}
