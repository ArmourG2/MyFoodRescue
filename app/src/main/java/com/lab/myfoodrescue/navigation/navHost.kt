package com.lab.myfoodrescue.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

@Composable
fun FoodRescueNavHost(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = Screen.ROLE_SELECTION.route
    ) {
        composable(Screen.ROLE_SELECTION.route) {
            // TODO: Show RoleSelectionScreen
        }

        composable(Screen.HOME.route) {
            // TODO: Show HomeScreen
        }

        composable(Screen.SEARCH.route) {
            // TODO: Show SearchScreen
        }

        composable(Screen.LISTING_DETAIL.route) {
            // TODO: Show ListingDetailScreen
        }

        composable(Screen.RESERVATION.route) {
            // TODO: Show ReservationScreen
        }

        composable(Screen.PICKUP_SCHEDULE.route) {
            // TODO: Show PickupScheduleScreen
        }

        composable(Screen.STATUS.route) {
            // TODO: Show StatusScreen
        }

        composable(Screen.HISTORY.route) {
            // TODO: Show HistoryScreen
        }

        composable(Screen.PROFILE.route) {
            // TODO: Show ProfileScreen
        }

        composable(Screen.SETTINGS.route) {
            // TODO: Show SettingsScreen
        }

        composable(Screen.CREATE_DONATION.route) {
            // TODO: Show CreateDonationScreen
        }
    }
}