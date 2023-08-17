@file:Suppress("DEPRECATION")

package com.vungn.alarm.ui.destination

import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ArrowForwardIos
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vungn.alarm.util.Interval
import com.vungn.alarm.util.Snooze
import com.vungn.alarm.util.SnoozeTimes
import com.vungn.alarm.util.getFileName
import com.vungn.alarm.util.getTimeFromCurrent
import com.vungn.alarm.util.toCalendar
import com.vungn.alarm.vm.AlarmViewModel
import kotlinx.coroutines.launch
import java.util.Calendar

@Composable
fun AlarmClock(modifier: Modifier = Modifier, viewModel: AlarmViewModel, goBack: () -> Unit) {
    val calendar by viewModel.date.collectAsState()
    val title by viewModel.title.collectAsState()
    val tone by viewModel.tone.collectAsState()
    val vibrate by viewModel.vibrate.collectAsState()
    val snooze by viewModel.snooze.collectAsState()
    RequestPermission(modifier = modifier) {
        Alarm(
            title = title,
            calendar = calendar,
            tone = tone,
            vibrate = vibrate,
            snooze = snooze,
            setTitle = viewModel::setTitle,
            setDate = viewModel::setDate,
            setTone = viewModel::setTone,
            setVibrate = viewModel::setVibrate,
            setSnooze = viewModel::setSnooze,
            startAlarm = viewModel::startAlarm,
            goBack = goBack
        )
    }
}

@Composable
fun RequestPermission(modifier: Modifier = Modifier, content: @Composable (Modifier) -> Unit) {
    val permissions = remember {
        mutableListOf<String>().also { permissions ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                permissions.add(
                    android.Manifest.permission.SCHEDULE_EXACT_ALARM,
                )
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissions.addAll(
                    arrayOf(
                        android.Manifest.permission.USE_EXACT_ALARM,
                        android.Manifest.permission.POST_NOTIFICATIONS,
                    )
                )
            } else {
                permissions.addAll(
                    arrayOf(
                        android.Manifest.permission.SET_ALARM,
                        android.Manifest.permission.WAKE_LOCK,
                    )
                )
            }
        }.toTypedArray()
    }
    val permissionsGranted = remember { mutableStateOf(false) }
    val requestPermission =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestMultiplePermissions(),
            onResult = { permissionList ->
                permissionsGranted.value = permissionList.values.all { it }
            })

    LaunchedEffect(key1 = true, block = {
        requestPermission.launch(permissions)
    })
    if (!permissionsGranted.value) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Button(onClick = { requestPermission.launch(permissions) }) {
                Text(text = "Request Permission")
            }
        }
    } else {
        content(modifier)
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun Alarm(
    modifier: Modifier = Modifier,
    title: String = "",
    calendar: Calendar = Calendar.getInstance(),
    tone: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM),
    vibrate: Boolean = true,
    snooze: Snooze = Snooze(true, Interval.FIVE_MINUTES, SnoozeTimes.THREE_TIMES),
    setTitle: (String) -> Unit = {},
    setDate: (Calendar) -> Unit = {},
    setTone: (Uri) -> Unit = {},
    setVibrate: (Boolean) -> Unit = {},
    setSnooze: (Snooze) -> Unit = {},
    startAlarm: () -> Unit = {},
    goBack: () -> Unit = {},
) {
    val state = rememberTimePickerState(
        initialHour = calendar.get(Calendar.HOUR_OF_DAY),
        initialMinute = calendar.get(Calendar.MINUTE),
    )
    val configuration = LocalConfiguration.current
    val snackBarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val bottomSheetState = rememberModalBottomSheetState()
    var openBottomSheet by remember { mutableStateOf(false) }
    val edgeToEdgeEnabled by remember { mutableStateOf(true) }
    val pickSong =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) {
            val toneUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                it.data?.getParcelableExtra(
                    RingtoneManager.EXTRA_RINGTONE_PICKED_URI, Uri::class.java
                )
            } else {
                it.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            }
            Log.d("", "Alarm: uri = $toneUri")
            setTone(toneUri ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
        }

    LaunchedEffect(key1 = state.hour, key2 = state.minute, block = {
        setDate(state.toCalendar())
    })

    Scaffold(modifier = modifier,
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) },
        topBar = {
            TopAppBar(title = { }, navigationIcon = {
                IconButton(onClick = goBack) {
                    Icon(
                        imageVector = Icons.Rounded.ArrowBack, contentDescription = "Back"
                    )
                }
            })
        },
        bottomBar = {
            BottomAppBar(actions = {}, floatingActionButton = {
                FloatingActionButton(onClick = {
                    coroutineScope.launch {
                        val cal = state.toCalendar()
                        startAlarm()
                        snackBarHostState.showSnackbar("Alarm will be ring after ${cal.timeInMillis.getTimeFromCurrent()} ms")
                    }
                }) {
                    Icon(imageVector = Icons.Rounded.Check, contentDescription = "Set Alarm")
                }
            })
        }) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                if (configuration.screenHeightDp > 400) {
                    TimePicker(state = state)
                } else {
                    TimeInput(state = state)
                }
            }
            item {
                OutlinedTextField(
                    value = title,
                    onValueChange = { setTitle(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    label = { Text(text = "Alarm name") },
                    singleLine = true,
                    keyboardActions = KeyboardActions(onDone = {
                        keyboardController?.hide()
                    }),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                )
            }
            item {
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER)
                        intent.putExtra(
                            RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM
                        )
                        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Tone")
                        intent.putExtra(
                            RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,
                            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                        )
                        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
                        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                        pickSong.launch(intent)
                    }) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Ringtone", style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = tone.getFileName(context),
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 1,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Icon(
                            imageVector = Icons.Rounded.ArrowForwardIos,
                            contentDescription = null,
                            modifier = Modifier.size(ButtonDefaults.IconSize)
                        )
                    }

                }
            }
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Vibrate", style = MaterialTheme.typography.titleMedium)
                    Switch(checked = vibrate, onCheckedChange = { setVibrate(it) })
                }
            }
            item {
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        coroutineScope.launch {
                            openBottomSheet = true
                        }
                    }) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Snooze", style = MaterialTheme.typography.titleMedium)
                        Icon(
                            imageVector = Icons.Rounded.ArrowForwardIos,
                            contentDescription = null,
                            modifier = Modifier.size(ButtonDefaults.IconSize)
                        )
                    }
                }
            }
            item {
                Spacer(modifier = Modifier.size(20.dp))
            }
        }
    }
    if (openBottomSheet) {
        val windowInsets =
            if (edgeToEdgeEnabled) WindowInsets(0) else BottomSheetDefaults.windowInsets

        ModalBottomSheet(
            onDismissRequest = { openBottomSheet = false },
            sheetState = bottomSheetState,
            windowInsets = windowInsets
        ) {
            SheetContent(snooze = snooze, setSnooze = setSnooze)
        }
    }
}

@Composable
fun SheetContent(snooze: Snooze, setSnooze: (Snooze) -> Unit) {
    var intervalExpanded by remember { mutableStateOf(false) }
    var timesExpanded by remember { mutableStateOf(false) }
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Snooze", style = MaterialTheme.typography.titleMedium)
                Switch(checked = snooze.enable,
                    onCheckedChange = { setSnooze(snooze.copy(enable = it)) })
            }
        }
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Interval", style = MaterialTheme.typography.titleMedium)
                Box(
                    modifier = Modifier.wrapContentSize(Alignment.TopStart)
                ) {
                    OutlinedButton(onClick = { intervalExpanded = true }) {
                        Text(
                            text = snooze.interval.minute.toString(),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    DropdownMenu(expanded = intervalExpanded,
                        onDismissRequest = { intervalExpanded = false }) {
                        Interval.values().forEach {
                            DropdownMenuItem(
                                text = { Text(it.minute.toString()) },
                                onClick = {
                                    setSnooze(snooze.copy(interval = it)); intervalExpanded = false
                                },
                            )
                        }
                    }
                }
            }
        }
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Times", style = MaterialTheme.typography.titleMedium)
                Box(
                    modifier = Modifier.wrapContentSize(Alignment.TopStart)
                ) {
                    OutlinedButton(onClick = { timesExpanded = true }) {
                        Text(
                            text = snooze.times.times.toString(),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    DropdownMenu(expanded = timesExpanded,
                        onDismissRequest = { timesExpanded = false }) {
                        SnoozeTimes.values().forEach {
                            DropdownMenuItem(
                                text = { Text(it.times.toString()) },
                                onClick = {
                                    setSnooze(snooze.copy(times = it)); intervalExpanded = false
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun AlarmPreview() {
    Alarm()
}
