package com.pillsense.app.feature.adherence.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun ProgressScreen(viewModel: ProgressViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadProgress()
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Adherencia Semanal", style = MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.height(16.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("${state.weekPercentage.toInt()}%", style = MaterialTheme.typography.headlineLarge)
                Text(if (state.trend > 0) "+${state.trend.toInt()}%" else "${state.trend.toInt()}%", style = MaterialTheme.typography.bodyMedium)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (state.recommendations.isNotEmpty()) {
            Text("Recomendaciones (${state.source})", style = MaterialTheme.typography.titleMedium)
            state.recommendations.forEachIndexed { idx, rec ->
                Button(modifier = Modifier.fillMaxWidth(), onClick = { viewModel.applyRecommendation(idx) }) {
                    Text(rec)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Esta app no sustituye a un profesional de salud.", style = MaterialTheme.typography.bodySmall)

        if (state.isLoading) {
            CircularProgressIndicator()
        }
    }
}
