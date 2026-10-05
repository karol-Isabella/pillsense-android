package com.pillsense.app.core.designsystem

import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color

// ==================== LIGHT MODE ====================
// Brand colors
val PrimaryLight = Color(0xFF007AFF)
val SecondaryLight = Color(0xFF30B0C7)
val TertiaryLight = Color(0xFF34C759)

// Foreground / Text
val PrimaryForegroundLight = Color(0xFFFFFFFF)
val SecondaryForegroundLight = Color(0xFF1C1C1E)
val MutedForegroundLight = Color(0xFF8E8E93)

// Backgrounds (layered iOS style)
val BackgroundLight = Color(0xFFF2F2F7)           // Grouped background (gray)
val SurfaceLight = Color(0xFFFFFFFF)              // Card / Surface (white)
val SurfaceVariantLight = Color(0xFFF9F9FB)       // Secondary surface (off-white)

// Accent / Semantic
val AccentLight = Color(0xFFE5F0FF)
val AccentForegroundLight = Color(0xFF007AFF)
val DestructiveLight = Color(0xFFFF3B30)
val SuccessLight = Color(0xFF34C759)
val SuccessStrongLight = Color(0xFF248A3D)
val WarningLight = Color(0xFFFF9500)
val WarningStrongLight = Color(0xFFC76F00)

// Borders and inputs
val BorderLight = Color(0x1F3C3C43)
val InputLight = Color(0x1F3C3C43)
val RingLight = Color(0xFF007AFF)

// ==================== DARK MODE ====================
// Brand colors
val PrimaryDark = Color(0xFF0A84FF)
val SecondaryDark = Color(0xFF30B0C7)
val TertiaryDark = Color(0xFF30D158)

// Foreground / Text
val PrimaryForegroundDark = Color(0xFFFFFFFF)
val SecondaryForegroundDark = Color(0xFFFFFFFF)
val MutedForegroundDark = Color(0xFF98989F)

// Backgrounds (layered iOS style)
val BackgroundDark = Color(0xFF000000)             // Grouped background (black)
val SurfaceDark = Color(0xFF1C1C1E)                // Card / Surface (dark gray)
val SurfaceVariantDark = Color(0xFF2C2C2E)         // Secondary surface (medium gray)

// Accent / Semantic
val AccentDark = Color(0xFF0A2540)
val AccentForegroundDark = Color(0xFF0A84FF)
val DestructiveDark = Color(0xFFFF453A)
val SuccessDark = Color(0xFF30D158)
val SuccessStrongDark = Color(0xFF30D158)
val WarningDark = Color(0xFFFF9F0A)
val WarningStrongDark = Color(0xFFFF9F0A)

// Borders and inputs
val BorderDark = Color(0x80545458)
val InputDark = Color(0x80545458)
val RingDark = Color(0xFF0A84FF)

// ==================== MATERIAL 3 COLOR SCHEMES ====================
val LightColorScheme = lightColorScheme(
    primary = PrimaryLight,
    onPrimary = PrimaryForegroundLight,
    secondary = SecondaryLight,
    onSecondary = PrimaryForegroundLight,
    tertiary = TertiaryLight,
    onTertiary = PrimaryForegroundLight,
    background = BackgroundLight,
    onBackground = SecondaryForegroundLight,
    surface = SurfaceLight,
    onSurface = SecondaryForegroundLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = MutedForegroundLight,
    error = DestructiveLight,
    onError = Color.White,
    outline = BorderLight,
    outlineVariant = InputLight
)

val DarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = PrimaryForegroundDark,
    secondary = SecondaryDark,
    onSecondary = SecondaryForegroundDark,
    tertiary = TertiaryDark,
    onTertiary = PrimaryForegroundDark,
    background = BackgroundDark,
    onBackground = PrimaryForegroundDark,
    surface = SurfaceDark,
    onSurface = PrimaryForegroundDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = MutedForegroundDark,
    error = DestructiveDark,
    onError = Color.Black,
    outline = BorderDark,
    outlineVariant = InputDark
)

// ==================== ALIASES FOR EASY ACCESS ====================
val PillSenseColorLight = LightColorScheme
val PillSenseColorDark = DarkColorScheme
