package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// سمة الوضع الداكن المخصصة لمحرر الأكواد
private val DarkColorScheme = darkColorScheme(
    primary = ElectricCyan,
    onPrimary = DarkNavy900,
    primaryContainer = Cyan700.copy(alpha = 0.3f),
    onPrimaryContainer = ElectricCyan,
    secondary = ForgeAmber,
    onSecondary = DarkNavy900,
    secondaryContainer = ForgeAmber.copy(alpha = 0.2f),
    onSecondaryContainer = ForgeAmber,
    tertiary = PurpleAccent,
    background = DarkNavy900,
    onBackground = TextPrimary,
    surface = DarkNavy800,
    onSurface = TextPrimary,
    surfaceVariant = DarkNavy700,
    onSurfaceVariant = TextSecondary,
    error = ErrorRed,
    outline = DarkNavy600
)

// سمة الوضع الفاتح
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF0284C7),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0F2FE),
    onPrimaryContainer = Color(0xFF0369A1),
    secondary = Color(0xFFD97706),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFEF3C7),
    onSecondaryContainer = Color(0xFF92400E),
    tertiary = Color(0xFF7C3AED),
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF0F172A),
    surface = Color.White,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF475569),
    error = Color(0xFFDC2626),
    outline = Color(0xFFCBD5E1)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // نفضل الثيم الداكن افتراضياً لبيئات التطوير البرمجية
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
