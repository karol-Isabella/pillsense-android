package com.pillsense.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.pillsense.app.core.designsystem.PillSenseTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PillSenseTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // NavHost irá aquí en Fase 3+5
                }
            }
        }
    }
}
