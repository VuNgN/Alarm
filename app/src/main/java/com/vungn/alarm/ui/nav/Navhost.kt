package com.vungn.alarm.ui.nav

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.vungn.alarm.ui.destination.AlarmClock
import com.vungn.alarm.ui.destination.AlarmList
import com.vungn.alarm.util.Routes
import com.vungn.alarm.vm.AlarmListViewModel
import com.vungn.alarm.vm.AlarmViewModel
import com.vungn.alarm.vm.impl.AlarmListViewModelImpl
import com.vungn.alarm.vm.impl.AlarmViewModelImpl

@Composable
fun MyNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    NavHost(
        modifier = modifier, navController = navController, startDestination = Routes.ALARM.name
    ) {
        navigation(route = Routes.ALARM.name, startDestination = Routes.ALARM_LIST.name) {
            composable(route = Routes.ALARM_LIST.name) {
                val viewModel: AlarmListViewModel = hiltViewModel<AlarmListViewModelImpl>()
                AlarmList(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    navigateToAlarm = {
                        if (it != null) navController.navigate("${Routes.ALARM_DETAIL.name}/$it")
                        else navController.navigate(Routes.ALARM_DETAIL.name)
                    })
            }
            composable(
                route = "${Routes.ALARM_DETAIL.name}/{id}",
                arguments = listOf(navArgument("id") { type = NavType.LongType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getLong("id")
                val viewModel: AlarmViewModel = hiltViewModel<AlarmViewModelImpl>()
                viewModel.setId(id)
                AlarmClock(modifier = Modifier.fillMaxSize(), viewModel = viewModel, goBack = {
                    navController.popBackStack()
                })
            }
            composable(
                route = Routes.ALARM_DETAIL.name,
            ) {
                val viewModel: AlarmViewModel = hiltViewModel<AlarmViewModelImpl>()
                AlarmClock(modifier = Modifier.fillMaxSize(), viewModel = viewModel, goBack = {
                    navController.popBackStack()
                })
            }
        }
    }
}
