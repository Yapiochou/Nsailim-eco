package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = EcoPrimaryContainer,
    onPrimary = EcoOnPrimaryContainer,
    primaryContainer = EcoPrimary,
    onPrimaryContainer = EcoOnPrimary,
    secondary = EcoSecondaryContainer,
    onSecondary = EcoOnSecondaryContainer,
    secondaryContainer = EcoSecondary,
    onSecondaryContainer = EcoOnSecondary,
    tertiary = EcoTertiaryContainer,
    onTertiary = EcoOnTertiaryContainer,
    tertiaryContainer = EcoTertiary,
    onTertiaryContainer = EcoOnTertiary,
    background = EcoOnBackground,
    onBackground = EcoBackground,
    surface = EcoOnBackground,
    onSurface = EcoBackground,
    surfaceVariant = EcoOnSurfaceVariant,
    onSurfaceVariant = EcoSurfaceVariant,
    outline = EcoOutline,
    outlineVariant = EcoOutlineVariant
  )

private val LightColorScheme =
  lightColorScheme(
    primary = EcoPrimary,
    onPrimary = EcoOnPrimary,
    primaryContainer = EcoPrimaryContainer,
    onPrimaryContainer = EcoOnPrimaryContainer,
    secondary = EcoSecondary,
    onSecondary = EcoOnSecondary,
    secondaryContainer = EcoSecondaryContainer,
    onSecondaryContainer = EcoOnSecondaryContainer,
    tertiary = EcoTertiary,
    onTertiary = EcoOnTertiary,
    tertiaryContainer = EcoTertiaryContainer,
    onTertiaryContainer = EcoOnTertiaryContainer,
    background = EcoBackground,
    onBackground = EcoOnBackground,
    surface = EcoSurface,
    onSurface = EcoOnSurface,
    surfaceVariant = EcoSurfaceVariant,
    onSurfaceVariant = EcoOnSurfaceVariant,
    outline = EcoOutline,
    outlineVariant = EcoOutlineVariant
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is available on Android 12+
  dynamicColor: Boolean = true,
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
