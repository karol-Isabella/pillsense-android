package com.pillsense.app.feature.adherence.ui

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.unit.dp
import com.pillsense.app.BuildConfig

@Composable
fun DebugTestDataButton(modifier: Modifier = Modifier, onLoadTestData: () -> Unit = {}) {
    if (BuildConfig.DEBUG) {
        Button(modifier = modifier, onClick = onLoadTestData) {
            Text("Cargar datos de prueba")
        }
    }
}

@Composable
fun DebugDataButtons(
    modifier: Modifier = Modifier,
    onLoadHighAdherence: () -> Unit = {},
    onLoadMediumAdherence: () -> Unit = {},
    onLoadLowAdherence: () -> Unit = {},
    onClearTestData: () -> Unit = {}
) {
    if (BuildConfig.DEBUG) {
        Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(modifier = Modifier.fillMaxWidth(), onClick = onLoadHighAdherence) {
                Text("Test: Alto (~90%)")
            }
            Button(modifier = Modifier.fillMaxWidth(), onClick = onLoadMediumAdherence) {
                Text("Test: Medio (~65%)")
            }
            Button(modifier = Modifier.fillMaxWidth(), onClick = onLoadLowAdherence) {
                Text("Test: Bajo (~35%)")
            }
            Button(modifier = Modifier.fillMaxWidth(), onClick = onClearTestData) {
                Text("Borrar datos de prueba")
            }
        }
    }
}

