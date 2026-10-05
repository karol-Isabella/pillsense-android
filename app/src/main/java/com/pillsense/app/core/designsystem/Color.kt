package com.pillsense.app.core.designsystem

import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color

// Light mode colors
val PrimaryLight = Color(0xFF007AFF)
val PrimaryForegroundLight = Color(0xFFFFFFFF)
val SecondaryLight = Color(0xFFE9E9EE)
val SecondaryForegroundLight = Color(0xFF1C1C1E)
val MutedLight = Color(0xFFE9E9EE)
val MutedForegroundLight = Color(0xFF8E8E93)
val AccentLight = Color(0xFFE5F0FF)
val AccentForegroundLight = Color(0xFF007AFF)
val DestructiveLight = Color(0xFFFF3B30)
val BackgroundLight = Color(0xFFF2F2F7)
val ForegroundLight = Color(0xFF1C1C1E)
val CardLight = Color(0xFFFFFFFF)
val CardForegroundLight = Color(0xFF1C1C1E)
val PopoverLight = Color(0xFFFFFFFF)
val PopoverForegroundLight = Color(0xFF1C1C1E)
val SuccessLight = Color(0xFF34C759)
val SuccessStrongLight = Color(0xFF248A3D)
val WarningLight = Color(0xFFFF9500)
val WarningStrongLight = Color(0xFFC76F00)
val BorderLight = Color(0x1F3C3C43)
val InputLight = Color(0x1F3C3C43)
val RingLight = Color(0xFF007AFF)

// Dark mode colors
val PrimaryDark = Color(0xFF0A84FF)
val PrimaryForegroundDark = Color(0xFFFFFFFF)
val SecondaryDark = Color(0xFF2C2C2E)
val SecondaryForegroundDark = Color(0xFFFFFFFF)
val MutedDark = Color(0xFF2C2C2E)
val MutedForegroundDark = Color(0xFF98989F)
val AccentDark = Color(0xFF0A2540)
val AccentForegroundDark = Color(0xFF0A84FF)
val DestructiveDark = Color(0xFFFF453A)
val BackgroundDark = Color(0xFF000000)
val ForegroundDark = Color(0xFFFFFFFF)
val CardDark = Color(0xFF1C1C1E)
val CardForegroundDark = Color(0xFFFFFFFF)
val PopoverDark = Color(0xFF1C1C1E)
val PopoverForegroundDark = Color(0xFFFFFFFF)
val SuccessDark = Color(0xFF30D158)
val SuccessStrongDark = Color(0xFF30D158)
val WarningDark = Color(0xFFFF9F0A)
val WarningStrongDark = Color(0xFFFF9F0A)
val BorderDark = Color(0x80545458)
val InputDark = Color(0x80545458)
val RingDark = Color(0xFF0A84FF)

val LightColorScheme = lightColorScheme(
    primary = PrimaryLight,
    onPrimary = PrimaryForegroundLight,
    secondary = SecondaryLight,
    onSecondary = SecondaryForegroundLight,
    tertiary = AccentLight,
    onTertiary = AccentForegroundLight,
    background = BackgroundLight,
    onBackground = ForegroundLight,
    surface = CardLight,
    onSurface = CardForegroundLight,
    error = DestructiveLight,
    onError = Color.White,
    outline = BorderLight,
)

val DarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = PrimaryForegroundDark,
    secondary = SecondaryDark,
    onSecondary = SecondaryForegroundDark,
    tertiary = AccentDark,
    onTertiary = AccentForegroundDark,
    background = BackgroundDark,
    onBackground = ForegroundDark,
    surface = CardDark,
    onSurface = CardForegroundDark,
    error = DestructiveDark,
    onError = Color.Black,
    outline = BorderDark,
)
