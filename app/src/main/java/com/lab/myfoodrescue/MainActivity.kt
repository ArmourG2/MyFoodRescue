package com.lab.myfoodrescue

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.lab.myfoodrescue.data.repository.AppSync
import com.lab.myfoodrescue.navigation.FoodRescueNavHost
import com.lab.myfoodrescue.ui.theme.MyFoodRescueTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Cross-device sync: anonymous auth for guests, one-time demo
        // seeding, and live Firestore listeners for posts/listings/
        // reservations.
        AppSync.start()

        setContent {
            MyFoodRescueTheme {
                FoodRescueNavHost(navController = rememberNavController())
            }
        }
    }
}