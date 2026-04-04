package com.scf.secondbloom.ui.theme

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

private val SecondBloomLightColorScheme = lightColorScheme(
    primary = SecondBloomGreenPrimary,
    onPrimary = SecondBloomGreenOnPrimary,
    primaryContainer = SecondBloomGreenPrimaryContainer,
    onPrimaryContainer = SecondBloomGreenOnPrimaryContainer,
    secondary = SecondBloomGreenSecondary,
    onSecondary = SecondBloomGreenOnSecondary,
    secondaryContainer = SecondBloomGreenSecondaryContainer,
    onSecondaryContainer = SecondBloomGreenOnSecondaryContainer,
    tertiary = SecondBloomTertiary,
    onTertiary = SecondBloomGreenOnTertiary,
    tertiaryContainer = SecondBloomTertiaryContainer,
    onTertiaryContainer = SecondBloomGreenOnTertiaryContainer,
    background = SecondBloomBackground,
    onBackground = SecondBloomOnBackground,
    surface = SecondBloomSurface,
    onSurface = SecondBloomOnSurface,
    surfaceVariant = SecondBloomSurfaceVariant,
    onSurfaceVariant = SecondBloomOnSurfaceVariant,
    surfaceTint = SecondBloomSurfaceTint,
    outline = SecondBloomOutline,
    outlineVariant = SecondBloomOutlineVariant,
    inverseSurface = SecondBloomInverseSurface,
    inverseOnSurface = SecondBloomInverseOnSurface,
    inversePrimary = SecondBloomInversePrimary,
    scrim = SecondBloomScrim,
    error = SecondBloomError,
    onError = SecondBloomOnError,
    errorContainer = SecondBloomErrorContainer,
    onErrorContainer = SecondBloomOnErrorContainer,
    surfaceContainerLowest = SecondBloomSurfaceContainerLowest,
    surfaceContainerLow = SecondBloomSurfaceContainerLow,
    surfaceContainer = SecondBloomSurfaceContainer,
    surfaceContainerHigh = SecondBloomSurfaceContainerHigh,
    surfaceContainerHighest = SecondBloomSurfaceContainerHighest
)

private val SecondBloomDarkColorScheme = darkColorScheme(
    primary = SecondBloomGreenPrimaryDark,
    onPrimary = SecondBloomGreenOnPrimaryDark,
    primaryContainer = SecondBloomGreenPrimaryContainerDark,
    onPrimaryContainer = SecondBloomGreenOnPrimaryContainerDark,
    secondary = SecondBloomGreenSecondaryDark,
    onSecondary = SecondBloomGreenOnSecondaryDark,
    secondaryContainer = SecondBloomGreenSecondaryContainerDark,
    onSecondaryContainer = SecondBloomGreenOnSecondaryContainerDark,
    tertiary = SecondBloomTertiaryDark,
    onTertiary = SecondBloomGreenOnTertiaryDark,
    tertiaryContainer = SecondBloomTertiaryContainerDark,
    onTertiaryContainer = SecondBloomGreenOnTertiaryContainerDark,
    background = SecondBloomBackgroundDark,
    onBackground = SecondBloomOnBackgroundDark,
    surface = SecondBloomSurfaceDark,
    onSurface = SecondBloomOnSurfaceDark,
    surfaceVariant = SecondBloomSurfaceVariantDark,
    onSurfaceVariant = SecondBloomOnSurfaceVariantDark,
    surfaceTint = SecondBloomSurfaceTintDark,
    outline = SecondBloomOutlineDark,
    outlineVariant = SecondBloomOutlineVariantDark,
    inverseSurface = SecondBloomInverseSurfaceDark,
    inverseOnSurface = SecondBloomInverseOnSurfaceDark,
    inversePrimary = SecondBloomInversePrimaryDark,
    scrim = SecondBloomScrimDark,
    error = SecondBloomErrorDark,
    onError = SecondBloomOnErrorDark,
    errorContainer = SecondBloomErrorContainerDark,
    onErrorContainer = SecondBloomOnErrorContainerDark,
    surfaceContainerLowest = SecondBloomSurfaceContainerLowestDark,
    surfaceContainerLow = SecondBloomSurfaceContainerLowDark,
    surfaceContainer = SecondBloomSurfaceContainerDark,
    surfaceContainerHigh = SecondBloomSurfaceContainerHighDark,
    surfaceContainerHighest = SecondBloomSurfaceContainerHighestDark
)

@Composable
fun SecondBloomTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> SecondBloomDarkColorScheme
        else -> SecondBloomLightColorScheme
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
