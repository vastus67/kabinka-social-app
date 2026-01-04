package app.kabinka.coreui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = KabinkaOrange,
    onPrimary = Color.White,
    primaryContainer = KabinkaOrangeLight,
    onPrimaryContainer = Color(0xFF3E1500),

    secondary = KabinkaAmber,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFDCC5),
    onSecondaryContainer = Color(0xFF4A2800),

    tertiary = Color(0xFF6B5E52),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFF4E2D4),
    onTertiaryContainer = Color(0xFF251A10),

    error = ErrorRed,
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF690005),

    background = LightBackground,
    onBackground = LightText,

    surface = LightSurface,
    onSurface = LightText,
    surfaceVariant = Color(0xFFF3F4F6),
    onSurfaceVariant = LightTextSecondary,

    outline = LightOutline,
    outlineVariant = Color(0xFFD1D5DB),

    scrim = Color.Black,
    inverseSurface = DarkSurface,
    inverseOnSurface = DarkText,
    inversePrimary = KabinkaOrangeLight
)

private val DarkColorScheme = darkColorScheme(
    primary = KabinkaOrange,
    onPrimary = Color(0xFF4A2200),
    primaryContainer = KabinkaOrangeDark,
    onPrimaryContainer = KabinkaOrangeLight,

    secondary = KabinkaAmber,
    onSecondary = Color(0xFF4A2800),
    secondaryContainer = Color(0xFF6A3D00),
    onSecondaryContainer = Color(0xFFFFDCC5),

    tertiary = Color(0xFFD7C6B7),
    onTertiary = Color(0xFF3B2F24),
    tertiaryContainer = Color(0xFF534439),
    onTertiaryContainer = Color(0xFFF4E2D4),

    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),

    background = DarkBackground,
    onBackground = DarkText,

    surface = DarkSurface,
    onSurface = DarkText,
    surfaceVariant = DarkElevatedSurface,
    onSurfaceVariant = DarkTextSecondary,

    outline = DarkOutline,
    outlineVariant = Color(0xFF1F2937),

    scrim = Color.Black,
    inverseSurface = LightSurface,
    inverseOnSurface = LightText,
    inversePrimary = KabinkaOrangeDark
)

@Composable
fun KabinkaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

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
        typography = KabinkaTypography,
        content = content
    )
}
