package com.lab.myfoodrescue

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.lab.myfoodrescue.navigation.FoodRescueNavHost
import com.lab.myfoodrescue.ui.theme.MyFoodRescueTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyFoodRescueTheme {
                FoodRescueNavHost(navController = rememberNavController())
            }
        }
    }
}