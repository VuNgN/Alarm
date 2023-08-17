package com.vungn.alarm.util

import java.io.Serializable

data class Snooze(val enable: Boolean, val interval: Interval, val times: SnoozeTimes) :
    Serializable
