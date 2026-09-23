package com.example.ui.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = DrBenamDarkPrimary,
    onPrimary = Color(0xFF072725),
    primaryContainer = Color(0xFF142E2C),
    onPrimaryContainer = DrBenamDarkPrimary,
    secondary = DrBenamDarkPrimaryDark,
    onSecondary = Color.White,
    background = DrBenamDarkBg,
    onBackground = DrBenamDarkText,
    surface = DrBenamDarkCard,
    onSurface = DrBenamDarkText,
    surfaceVariant = Color(0xFF162530),
    onSurfaceVariant = DrBenamDarkTextMuted,
    outline = DrBenamDarkLine,
    outlineVariant = Color(0xFF1E3A4B),
    error = DrBenamDanger,
    onError = Color.White,
  )

private val LightColorScheme =
  lightColorScheme(
    primary = DrBenamPrimary,
    onPrimary = Color.White,
    primaryContainer = DrBenamPrimarySoft,
    onPrimaryContainer = DrBenamPrimaryDark,
    secondary = DrBenamPrimaryDark,
    onSecondary = Color.White,
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF0F172A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF64748B),
    outline = Color(0xFFE2E8F0),
    outlineVariant = Color(0xFFE2E8F0),
    error = DrBenamDanger,
    onError = Color.White,
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = false, // Default is strictly Light as requested
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }
      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
