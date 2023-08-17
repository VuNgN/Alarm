package com.vungn.alarm.util

import android.content.BroadcastReceiver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TimePickerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext


fun Long.getTimeFromCurrent(): String {
    //milliseconds
    val current = Calendar.getInstance().timeInMillis
    var different: Long = this - current
    println("different : $different")
    val secondsInMilli: Long = 1000
    val minutesInMilli = secondsInMilli * 60
    val hoursInMilli = minutesInMilli * 60
    val daysInMilli = hoursInMilli * 24

    //long elapsedDays = different / daysInMilli;
    different %= daysInMilli;
    val elapsedHours = different / hoursInMilli
    different %= hoursInMilli
    val elapsedMinutes = different / minutesInMilli
    different %= minutesInMilli
    val elapsedSeconds = different / secondsInMilli
    return "$elapsedHours hours $elapsedMinutes minutes $elapsedSeconds seconds"
}

@OptIn(ExperimentalMaterial3Api::class)
fun TimePickerState.toCalendar(): Calendar {
    val cal = Calendar.getInstance()
    cal.set(Calendar.HOUR_OF_DAY, this.hour)
    cal.set(Calendar.MINUTE, this.minute)
    cal.isLenient = false
    return cal
}

fun BroadcastReceiver.goAsync(
    context: CoroutineContext = EmptyCoroutineContext, block: suspend CoroutineScope.() -> Unit
) {
    val pendingResult = goAsync()
    @OptIn(DelicateCoroutinesApi::class) // Must run globally; there's no teardown callback.
    GlobalScope.launch(context) {
        try {
            block()
        } finally {
            pendingResult.finish()
        }
    }
}

fun Uri.getFileName(context: Context): String {
    var result: String? = null
    if (this.scheme.equals("content")) {
        val cursor: Cursor? = context.contentResolver.query(this, null, null, null, null)
        try {
            if (cursor != null && cursor.moveToFirst()) {
                result =
                    cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
            }
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        } finally {
            cursor?.close()
        }
    }
    if (result == null) {
        result = this.path
        val cut = result?.lastIndexOf('/')
        if (cut != -1) {
            if (cut != null) {
                result = result?.substring(cut + 1)
            }
        }
    }
    return result ?: ""
}
