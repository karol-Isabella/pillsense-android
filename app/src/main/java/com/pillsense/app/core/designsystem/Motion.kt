package com.pillsense.app.core.designsystem

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring

/**
 * PillSense motion tokens: smooth, spring-based animations inspired by iOS.
 * Used for UI feedback, transitions, and micro-interactions.
 */
object PillSenseMotion {
    // Easing curves for natural motion
    val easeInOutCubic: Easing = CubicBezierEasing(0.42f, 0f, 0.58f, 1f)
    val easeOutCubic: Easing = CubicBezierEasing(0.215f, 0.61f, 0.355f, 1f)
    val easeInCubic: Easing = CubicBezierEasing(0.55f, 0.055f, 0.675f, 0.19f)

    // Spring specs for button press, scale, and snap interactions
    val springDefault = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    )

    val springStiff = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium
    )

    // Animation durations (ms)
    const val durationShort = 150
    const val durationMedium = 300
    const val durationLong = 500
}
