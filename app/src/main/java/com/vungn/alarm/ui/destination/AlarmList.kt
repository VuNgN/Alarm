@file:OptIn(ExperimentalMaterial3Api::class)

package com.vungn.alarm.ui.destination

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vungn.alarm.model.entity.Alarm
import com.vungn.alarm.ui.theme.AlarmTheme
import com.vungn.alarm.util.AlarmState
import com.vungn.alarm.vm.AlarmListViewModel
import java.util.Calendar

@Composable
fun AlarmList(
    modifier: Modifier = Modifier,
    viewModel: AlarmListViewModel,
    navigateToAlarm: (Long?) -> Unit = {}
) {
    val alarms by viewModel.alarmList.collectAsState()
    Alarms(
        modifier = modifier,
        alarms = alarms,
        navigateToAlarm = navigateToAlarm,
        turnOnOffAlarm = viewModel::turnOnOffAlarm
    )
}

@Composable
fun Alarms(
    modifier: Modifier = Modifier,
    alarms: List<Alarm> = emptyList(),
    navigateToAlarm: (Long?) -> Unit = {},
    turnOnOffAlarm: (Context, Alarm) -> Unit = { _, _ -> }
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection), topBar = {
        LargeTopAppBar(title = { Text(text = "Alarm") }, scrollBehavior = scrollBehavior)
    }, floatingActionButton = {
        FloatingActionButton(onClick = { navigateToAlarm(null) }) {
            Icon(imageVector = Icons.Rounded.Add, contentDescription = "Add Alarm")
        }
    }) { paddingValues ->
        LazyColumn(modifier = Modifier.padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            content = {
                items(alarms) { alarm ->
                    AlarmItem(
                        modifier = Modifier.fillMaxWidth(),
                        alarm = alarm,
                        navigateToAlarm = navigateToAlarm,
                        turnOnOffAlarm = turnOnOffAlarm
                    )
                }
            })
    }
}

@Composable
fun AlarmItem(
    modifier: Modifier = Modifier,
    alarm: Alarm,
    navigateToAlarm: (Long?) -> Unit = {},
    turnOnOffAlarm: (Context, Alarm) -> Unit = { _, _ -> }
) {
    var checked by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val calendar = remember {
        Calendar.getInstance().apply {
            timeInMillis = alarm.date
        }
    }
    LaunchedEffect(key1 = alarm.state, block = {
        checked = alarm.state != AlarmState.OFF
    })
    Box(
        modifier = modifier.clickable(onClick = { navigateToAlarm(alarm.id) }),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier,
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "${calendar.get(Calendar.HOUR_OF_DAY)}:${calendar.get(Calendar.MINUTE)}",
                    style = MaterialTheme.typography.headlineLarge
                )
                Text(text = alarm.title, style = MaterialTheme.typography.titleMedium)
            }
            Column(
                modifier = Modifier,
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Switch(checked = checked, onCheckedChange = { turnOnOffAlarm(context, alarm) })
                Text(text = alarm.state.name, style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Preview
@Composable
fun AlarmsPreview() {
    AlarmTheme {
        Alarms()
    }
}
