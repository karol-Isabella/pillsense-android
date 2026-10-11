package com.pillsense.app.feature.adherence.ui

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.pillsense.app.BuildConfig

@Composable
fun DebugTestDataButton(modifier: Modifier = Modifier, onLoadTestData: () -> Unit = {}) {
    if (BuildConfig.DEBUG) {
        Button(modifier = modifier, onClick = onLoadTestData) {
            Text("Cargar datos de prueba")
        }
    }
}
