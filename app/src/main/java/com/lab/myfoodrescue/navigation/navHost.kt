package com.lab.myfoodrescue.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.google.firebase.auth.FirebaseAuth
import com.lab.myfoodrescue.ui.screens.LoginScreen
import com.lab.myfoodrescue.ui.screens.SignUpScreen
import com.lab.myfoodrescue.ui.screens.main.MainAppScreen

@Composable
fun FoodRescueNavHost(
    navController: NavHostController
) {
    // Auth gate: start on Main if already logged in, otherwise on Login.
    val startDestination =
        if (FirebaseAuth.getInstance().currentUser != null)
            Screen.MAIN.route
        else
            Screen.LOGIN.route

    NavHost(
        navController = navController,
        startDestination = startDestination
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
                        popUpTo(Screen.LOGIN.route) { inclusive = true }
                    }
                },
                onNavigateBack = { navController.popBackStack() }
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