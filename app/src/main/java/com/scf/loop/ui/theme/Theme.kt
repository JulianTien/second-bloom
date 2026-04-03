package com.scf.loop.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LoopLightColorScheme = lightColorScheme(
    primary = LoopGreenPrimary,
    onPrimary = LoopGreenOnPrimary,
    primaryContainer = LoopGreenPrimaryContainer,
    onPrimaryContainer = LoopGreenOnPrimaryContainer,
    secondary = LoopGreenSecondary,
    onSecondary = LoopGreenOnSecondary,
    secondaryContainer = LoopGreenSecondaryContainer,
    onSecondaryContainer = LoopGreenOnSecondaryContainer,
    tertiary = LoopTertiary,
    onTertiary = LoopGreenOnTertiary,
    tertiaryContainer = LoopTertiaryContainer,
    onTertiaryContainer = LoopGreenOnTertiaryContainer,
    background = LoopBackground,
    onBackground = LoopOnBackground,
    surface = LoopSurface,
    onSurface = LoopOnSurface,
    surfaceVariant = LoopSurfaceVariant,
    onSurfaceVariant = LoopOnSurfaceVariant,
    surfaceTint = LoopSurfaceTint,
    outline = LoopOutline,
    outlineVariant = LoopOutlineVariant,
    inverseSurface = LoopInverseSurface,
    inverseOnSurface = LoopInverseOnSurface,
    inversePrimary = LoopInversePrimary,
    scrim = LoopScrim,
    error = LoopError,
    onError = LoopOnError,
    errorContainer = LoopErrorContainer,
    onErrorContainer = LoopOnErrorContainer,
    surfaceContainerLowest = LoopSurfaceContainerLowest,
    surfaceContainerLow = LoopSurfaceContainerLow,
    surfaceContainer = LoopSurfaceContainer,
    surfaceContainerHigh = LoopSurfaceContainerHigh,
    surfaceContainerHighest = LoopSurfaceContainerHighest
)

private val LoopDarkColorScheme = darkColorScheme(
    primary = LoopGreenPrimaryDark,
    onPrimary = LoopGreenOnPrimaryDark,
    primaryContainer = LoopGreenPrimaryContainerDark,
    onPrimaryContainer = LoopGreenOnPrimaryContainerDark,
    secondary = LoopGreenSecondaryDark,
    onSecondary = LoopGreenOnSecondaryDark,
    secondaryContainer = LoopGreenSecondaryContainerDark,
    onSecondaryContainer = LoopGreenOnSecondaryContainerDark,
    tertiary = LoopTertiaryDark,
    onTertiary = LoopGreenOnTertiaryDark,
    tertiaryContainer = LoopTertiaryContainerDark,
    onTertiaryContainer = LoopGreenOnTertiaryContainerDark,
    background = LoopBackgroundDark,
    onBackground = LoopOnBackgroundDark,
    surface = LoopSurfaceDark,
    onSurface = LoopOnSurfaceDark,
    surfaceVariant = LoopSurfaceVariantDark,
    onSurfaceVariant = LoopOnSurfaceVariantDark,
    surfaceTint = LoopSurfaceTintDark,
    outline = LoopOutlineDark,
    outlineVariant = LoopOutlineVariantDark,
    inverseSurface = LoopInverseSurfaceDark,
    inverseOnSurface = LoopInverseOnSurfaceDark,
    inversePrimary = LoopInversePrimaryDark,
    scrim = LoopScrimDark,
    error = LoopErrorDark,
    onError = LoopOnErrorDark,
    errorContainer = LoopErrorContainerDark,
    onErrorContainer = LoopOnErrorContainerDark,
    surfaceContainerLowest = LoopSurfaceContainerLowestDark,
    surfaceContainerLow = LoopSurfaceContainerLowDark,
    surfaceContainer = LoopSurfaceContainerDark,
    surfaceContainerHigh = LoopSurfaceContainerHighDark,
    surfaceContainerHighest = LoopSurfaceContainerHighestDark
)

@Composable
fun LoopTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> LoopDarkColorScheme
        else -> LoopLightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
