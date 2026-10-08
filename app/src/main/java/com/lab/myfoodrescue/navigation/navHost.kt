package com.lab.myfoodrescue.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.lab.myfoodrescue.ui.screens.LoginScreen
import com.lab.myfoodrescue.ui.screens.SignUpScreen
import com.lab.myfoodrescue.ui.screens.main.MainAppScreen

@Composable
fun FoodRescueNavHost(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = Screen.LOGIN.route
    ) {
        composable(Screen.LOGIN.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.MAIN.route) {
                        popUpTo(Screen.LOGIN.route) { inclusive = true }
                    }
                },
                onNavigateToSignUp = {
                    navController.navigate(Screen.SIGN_UP.route)
                }
            )
        }

        composable(Screen.SIGN_UP.route) {
            SignUpScreen(
                onSignUpSuccess = {
                    navController.navigate(Screen.MAIN.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onBackToLogin = { navController.popBackStack() }
            )
        }

        composable(Screen.MAIN.route) {
            MainAppScreen(
                onLoggedOut = {
                    navController.navigate(Screen.LOGIN.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}