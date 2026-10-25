package com.pillsense.app.feature.debug

import androidx.compose.runtime.Composable

@Composable
fun DebugDataButtonsImpl(onDataGenerated: () -> Unit = {}, onDataCleared: () -> Unit = {}) {
    // No-op in release build
}
