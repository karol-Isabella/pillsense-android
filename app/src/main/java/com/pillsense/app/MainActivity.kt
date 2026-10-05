package com.pillsense.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pillsense.app.core.designsystem.PillSenseTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PillSenseTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) {
                    PillSenseNavHost()
                }
            }
        }
    }
}

@Composable
private fun PillSenseNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "auth",
        modifier = modifier,
    ) {
        composable("auth") {
            // TODO: Implement auth screen
        }
        composable("medication/scan") {
            // TODO: Implement medication scan screen
        }
        composable("medication/confirm") {
            // TODO: Implement medication confirm screen
        }
        composable("medication/list") {
            // TODO: Implement medication list screen
        }
        composable("reminders") {
            // TODO: Implement reminders screen
        }
        composable("intake/history") {
            // TODO: Implement intake history screen
        }
        composable("adherence/insights") {
            // TODO: Implement adherence insights screen
        }
        composable("emergency") {
            // TODO: Implement emergency screen
        }
    }
}