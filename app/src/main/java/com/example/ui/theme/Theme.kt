package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
  primary = NovaCyanGlow,
  onPrimary = NavyDark,
  primaryContainer = NovaBlue,
  onPrimaryContainer = Color.White,
  secondary = NovaVioletLight,
  onSecondary = Color.White,
  secondaryContainer = NavyCard,
  onSecondaryContainer = NovaVioletLight,
  tertiary = NovaCyan,
  onTertiary = NavyDark,
  background = NavyDark,
  onBackground = Color(0xFFF1F5F9),
  surface = NavySurface,
  onSurface = Color(0xFFF8FAFC),
  surfaceVariant = NavyCard,
  onSurfaceVariant = Color(0xFF94A3B8),
  outline = NavyBorder,
  error = ErrorRed
)

private val LightColorScheme = lightColorScheme(
  primary = NovaBlue,
  onPrimary = Color.White,
  primaryContainer = Color(0xFFDBEAFE),
  onPrimaryContainer = NovaBlue,
  secondary = NovaViolet,
  onSecondary = Color.White,
  secondaryContainer = NovaVioletContainer,
  onSecondaryContainer = NovaViolet,
  tertiary = NovaCyan,
  onTertiary = Color.White,
  background = Slate50,
  onBackground = Slate900,
  surface = Color.White,
  onSurface = Slate900,
  surfaceVariant = Slate100,
  onSurfaceVariant = Slate500,
  outline = Slate200,
  error = ErrorRed
)

@Composable
fun StudyNovaTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
