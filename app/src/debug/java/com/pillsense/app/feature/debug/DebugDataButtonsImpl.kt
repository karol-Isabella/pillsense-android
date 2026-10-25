package com.pillsense.app.feature.debug

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch

@Composable
fun DebugDataButtonsImpl(onDataGenerated: () -> Unit = {}, onDataCleared: () -> Unit = {}) {
    val scope = rememberCoroutineScope()
    val viewModel: DebugDataViewModel = hiltViewModel()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Button(
            onClick = {
                scope.launch {
                    viewModel.generateTestData("HIGH")
                    onDataGenerated()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            Text("Test: High Adherence (~90%)")
        }

        Button(
            onClick = {
                scope.launch {
                    viewModel.generateTestData("MEDIUM")
                    onDataGenerated()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            Text("Test: Medium Adherence (~65%)")
        }

        Button(
            onClick = {
                scope.launch {
                    viewModel.generateTestData("LOW")
                    onDataGenerated()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            Text("Test: Low Adherence (~35%)")
        }

        Button(
            onClick = {
                scope.launch {
                    viewModel.clearTestData()
                    onDataCleared()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Clear Test Data")
        }
    }
}
