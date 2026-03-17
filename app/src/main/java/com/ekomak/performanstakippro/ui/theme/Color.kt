package com.ekomak.performanstakippro.ui.theme

import androidx.compose.ui.graphics.Color

// Base Colors
val Primary = Color(0xFF1A1F36)        // Deep Navy
val PrimaryLight = Color(0xFF2A3050)
val PrimaryDark = Color(0xFF0F1425)

val Accent = Color(0xFF00C2A8)          // Teal
val AccentLight = Color(0xFF33D4BE)
val AccentDark = Color(0xFF009E89)

val Accent2 = Color(0xFFF5A623)         // Amber
val Accent2Light = Color(0xFFFFC148)

// Status Base Colors
val Success = Color(0xFF10B981)
val SuccessLight = Color(0xFFD1FAE5)
val Danger = Color(0xFFEF4444)
val DangerLight = Color(0xFFFEE2E2)
val WarningBase = Color(0xFFF5A623)
val WarningLight = Color(0xFFFEF3C7)

// Dark Theme Colors
val DarkBackground = Color(0xFF0F1425)
val DarkSurface = Color(0xFF1A1F36)
val DarkSurfaceVariant = Color(0xFF252B45)
val DarkCardBackground = Color(0xFF1E2540)
val DarkTextPrimary = Color(0xFFF4F6FA)
val DarkTextSecondary = Color(0xFF9CA3AF)
val DarkBorder = Color(0xFF374151)

// Dynamic Color Provider
object AppColors {
    var isDark = false

    val Background: Color get() = if (isDark) Color(0xFF0F1425) else Color(0xFFF4F6FA)
    val SurfaceColor: Color get() = if (isDark) Color(0xFF1A1F36) else Color(0xFFFFFFFF)
    val SurfaceVariant: Color get() = if (isDark) Color(0xFF252B45) else Color(0xFFF0F2F6)
    val CardBackground: Color get() = if (isDark) Color(0xFF1E2540) else Color(0xFFFFFFFF)
    
    val TextPrimary: Color get() = if (isDark) Color(0xFFF4F6FA) else Color(0xFF1A1F36)
    val TextSecondary: Color get() = if (isDark) Color(0xFF9CA3AF) else Color(0xFF6B7280)
    val TextOnPrimary: Color get() = Color(0xFFFFFFFF)
    val TextOnAccent: Color get() = Color(0xFFFFFFFF)
    
    val Border: Color get() = if (isDark) Color(0xFF374151) else Color(0xFFE5E7EB)
    val Divider: Color get() = if (isDark) Color(0xFF374151) else Color(0xFFF0F0F0)
}

// Aliases for backward compatibility in the app code
val Background get() = AppColors.Background
val Surface get() = AppColors.SurfaceColor
val SurfaceVariant get() = AppColors.SurfaceVariant
val CardBackground get() = AppColors.CardBackground
val TextPrimary get() = AppColors.TextPrimary
val TextSecondary get() = AppColors.TextSecondary
val TextOnPrimary get() = AppColors.TextOnPrimary
val TextOnAccent get() = AppColors.TextOnAccent
val Border get() = AppColors.Border
val Divider get() = AppColors.Divider
val Warning get() = WarningBase

// Gradient colors for dashboard cards
val GradientTeal = listOf(Color(0xFF00C2A8), Color(0xFF00E5C7))
val GradientAmber = listOf(Color(0xFFF5A623), Color(0xFFFFD166))
val GradientPurple = listOf(Color(0xFF8B5CF6), Color(0xFFA78BFA))
val GradientNavy = listOf(Color(0xFF1A1F36), Color(0xFF2A3050))
