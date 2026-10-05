package com.pillsense.app.core.designsystem

import androidx.compose.material3.Shapes
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

object PillSenseSpacing {
    val minTouchTarget = 48.dp
    
    val spacing_0 = 0.dp
    val spacing_2 = 2.dp
    val spacing_4 = 4.dp
    val spacing_8 = 8.dp
    val spacing_12 = 12.dp
    val spacing_16 = 16.dp
    val spacing_20 = 20.dp
    val spacing_24 = 24.dp
    val spacing_32 = 32.dp
    val spacing_40 = 40.dp
    val spacing_48 = 48.dp
    val spacing_56 = 56.dp
    val spacing_64 = 64.dp
}

object PillSenseRadius {
    val small = 6.dp // calc(var(--radius) * 0.6) where radius = 14dp
    val medium = 11.dp // calc(var(--radius) * 0.8)
    val large = 14.dp // var(--radius)
    val xLarge = 20.dp // calc(var(--radius) * 1.4)
    val twoXL = 25.dp // calc(var(--radius) * 1.8)
    val threeXL = 31.dp // calc(var(--radius) * 2.2)
    val fourXL = 36.dp // calc(var(--radius) * 2.6)
}

val PillSenseShapes = Shapes(
    small = RoundedCornerShape(PillSenseRadius.small),
    medium = RoundedCornerShape(PillSenseRadius.medium),
    large = RoundedCornerShape(PillSenseRadius.large),
)
