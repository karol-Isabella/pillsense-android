package com.pillsense.app.core.designsystem

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

/**
 * PillSense shape tokens inspired by iOS: clean, consistent radii.
 * - small: 10dp (small components, toggles)
 * - medium: 14dp (text fields, small cards)
 * - large: 16dp (buttons, medium cards)
 * - extraLarge: 20dp (large cards, groups)
 * - xxLarge: 28dp (hero cards, prominent elements)
 */
object PillSenseShape {
    val small = RoundedCornerShape(10.dp)
    val medium = RoundedCornerShape(14.dp)
    val large = RoundedCornerShape(16.dp)
    val extraLarge = RoundedCornerShape(20.dp)
    val xxLarge = RoundedCornerShape(28.dp)
}
