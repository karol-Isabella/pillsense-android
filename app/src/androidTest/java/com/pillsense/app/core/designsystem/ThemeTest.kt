package com.pillsense.app.core.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import androidx.compose.ui.test.junit4.createComposeRule

@RunWith(AndroidJUnit4::class)
class ThemeTest {
    @get:org.junit.Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testPillSenseThemeApplies() {
        composeTestRule.setContent {
            PillSenseTheme {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
        }

        composeTestRule.onRoot().assertExists()
    }
}
