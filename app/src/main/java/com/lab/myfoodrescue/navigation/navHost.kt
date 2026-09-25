package com.lab.myfoodrescue.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost

@Composable
fun NavigationLogic(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = Screen.REGISTER.route
    ) {

    }
}