package com.pillsense.app.core.designsystem

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.pillsense.app.R

// Inter font family from res/font/
val interRegular = Font(R.font.inter_regular, FontWeight.Normal)
val interMedium = Font(R.font.inter_medium, FontWeight.Medium)
val interSemiBold = Font(R.font.inter_semibold, FontWeight.SemiBold)
val interBold = Font(R.font.inter_bold, FontWeight.Bold)

val interFontFamily = FontFamily(
    interRegular,
    interMedium,
    interSemiBold,
    interBold
)

/**
 * PillSense typography: iOS-inspired scale with Inter font.
 * - Large Title: 34sp/700 (bold), -2% tracking (tight)
 * - Title: 28sp/600 (semibold), -1% tracking
 * - Headline: 20sp/600 (semibold)
 * - Body: 17sp/400 (regular)
 * - Caption: 13sp/400 (regular)
 * Comfortable line heights and spacing for readability.
 */
val PillSenseTypography = Typography(
    // Large Title (hero heading)
    displayLarge = TextStyle(
        fontFamily = interFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 34.sp,
        lineHeight = 41.sp,
        letterSpacing = (-0.68).sp // -2% tracking
    ),
    // Title
    displayMedium = TextStyle(
        fontFamily = interFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 34.sp,
        letterSpacing = (-0.28).sp // -1% tracking
    ),
    displaySmall = TextStyle(
        fontFamily = interFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    // Headline
    headlineLarge = TextStyle(
        fontFamily = interFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 26.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = interFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = interFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    // Subheading
    titleLarge = TextStyle(
        fontFamily = interFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp,
        lineHeight = 22.sp
    ),
    titleMedium = TextStyle(
        fontFamily = interFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 20.sp
    ),
    titleSmall = TextStyle(
        fontFamily = interFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        lineHeight = 18.sp
    ),
    // Body
    bodyLarge = TextStyle(
        fontFamily = interFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 17.sp,
        lineHeight = 24.sp,
        letterSpacing = (-0.41).sp // -0.05em subtle
    ),
    bodyMedium = TextStyle(
        fontFamily = interFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp
    ),
    bodySmall = TextStyle(
        fontFamily = interFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp
    ),
    // Caption
    labelLarge = TextStyle(
        fontFamily = interFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        lineHeight = 20.sp
    ),
    labelMedium = TextStyle(
        fontFamily = interFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 18.sp
    ),
    labelSmall = TextStyle(
        fontFamily = interFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp
    )
)
