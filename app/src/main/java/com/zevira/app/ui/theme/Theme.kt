package com.zevira.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = ElectricBlue,
    onPrimary = SurfaceWhite,
    primaryContainer = SurfaceTint,
    onPrimaryContainer = PrimaryDark,
    secondary = StarYellow,
    onSecondary = TextPrimary,
    tertiary = MintGreen,
    onTertiary = TextPrimary,
    background = SurfaceWhite,
    onBackground = TextPrimary,
    surface = SurfaceWhite,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceTint,
    onSurfaceVariant = TextSecondary,
    outline = StickerBorder,
    error = CoralRed,
    onError = SurfaceWhite
)

private val DarkColorScheme = darkColorScheme(
    primary = SkyBlue,
    onPrimary = NightNavy,
    primaryContainer = SurfaceDarkTint,
    onPrimaryContainer = SkyBlue,
    secondary = StarYellow,
    onSecondary = NightNavy,
    tertiary = MintGreen,
    onTertiary = NightNavy,
    background = NightNavy,
    onBackground = TextLightPrimary,
    surface = SurfaceDark,
    onSurface = TextLightPrimary,
    surfaceVariant = SurfaceDarkTint,
    onSurfaceVariant = TextLightSecondary,
    outline = StickerBorderDark,
    error = CoralRed,
    onError = SurfaceWhite
)

@Composable
fun ZeviraTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
