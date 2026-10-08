package com.lab.myfoodrescue.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.lab.myfoodrescue.ui.screens.LoginScreen
import com.lab.myfoodrescue.ui.screens.RoleSelectionScreen
import com.lab.myfoodrescue.ui.screens.RoleSession
import com.lab.myfoodrescue.ui.screens.SignUpScreen
import com.lab.myfoodrescue.ui.screens.UserRole
import com.lab.myfoodrescue.ui.screens.courier.CourierAppScreen
import com.lab.myfoodrescue.ui.screens.donor.DonorAppScreen
import com.lab.myfoodrescue.ui.screens.main.MainAppScreen

/** Maps a role to its home screen route. */
private fun routeForRole(role: UserRole): String = when (role) {
    UserRole.COURIER -> Screen.COURIER.route
    UserRole.DONOR -> Screen.DONOR.route
    else -> Screen.MAIN.route
}

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
                // role comes from the signed-in user's Firestore profile,
                // so existing users land straight on their role's screen.
                // Null for guests (no account) -> Recipient by default.
                onLoginSuccess = { role ->
                    val userRole = role?.let { saved ->
                        runCatching { UserRole.valueOf(saved) }.getOrNull()
                    } ?: UserRole.RECIPIENT
                    RoleSession.currentRole = userRole
                    navController.navigate(routeForRole(userRole)) {
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
                // Only brand-new accounts pick a role
                onSignUpSuccess = {
                    navController.navigate(Screen.ROLE_SELECTION.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onBackToLogin = { navController.popBackStack() }
            )
        }

        composable(Screen.ROLE_SELECTION.route) {
            RoleSelectionScreen(
                onRoleSelected = { role ->
                    RoleSession.currentRole = role
                    navController.navigate(routeForRole(role)) {
                        popUpTo(Screen.ROLE_SELECTION.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.MAIN.route) {
            MainAppScreen(
                onLoggedOut = {
                    navController.navigate(Screen.LOGIN.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onSwitchRole = { role ->
                    RoleSession.currentRole = role
                    navController.navigate(routeForRole(role)) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.COURIER.route) {
            CourierAppScreen(
                onLoggedOut = {
                    navController.navigate(Screen.LOGIN.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onSwitchRole = { role ->
                    RoleSession.currentRole = role
                    navController.navigate(routeForRole(role)) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.DONOR.route) {
            DonorAppScreen(
                onLoggedOut = {
                    navController.navigate(Screen.LOGIN.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onSwitchRole = { role ->
                    RoleSession.currentRole = role
                    navController.navigate(routeForRole(role)) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
