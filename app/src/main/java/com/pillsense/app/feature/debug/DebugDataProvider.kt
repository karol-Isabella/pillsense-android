package com.pillsense.app.feature.debug

import androidx.compose.runtime.Composable
import com.pillsense.app.BuildConfig

/**
 * Debug data buttons rendered only in DEBUG builds.
 * In release builds, this function renders nothing.
 */
@Composable
fun DebugDataButtons(onDataGenerated: () -> Unit = {}, onDataCleared: () -> Unit = {}) {
    if (BuildConfig.DEBUG) {
        DebugDataButtonsImpl(onDataGenerated, onDataCleared)
    }
}
