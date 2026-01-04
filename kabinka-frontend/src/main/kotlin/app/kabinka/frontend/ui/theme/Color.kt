package app.kabinka.frontend.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Kabinka Orange Theme Colors
val KabinkaOrange = Color(0xFFFF6B35)
val KabinkaOrangeDark = Color(0xFFE55A28)
val KabinkaOrangeLight = Color(0xFFFF8C5F)

val KabinkaBackground = Color(0xFFFFFBF7)
val KabinkaSurface = Color(0xFFFFFFFF)
val KabinkaOnPrimary = Color(0xFFFFFFFF)
val KabinkaOnSecondary = Color(0xFF000000)

val KabinkaBackgroundDark = Color(0xFF1A1410)
val KabinkaSurfaceDark = Color(0xFF2A1F1A)

val KabinkaLightColorScheme = lightColorScheme(
    primary = KabinkaOrange,
    onPrimary = KabinkaOnPrimary,
    primaryContainer = KabinkaOrangeLight,
    onPrimaryContainer = Color(0xFF4A1A00),
    
    secondary = Color(0xFFFFB380),
    onSecondary = KabinkaOnSecondary,
    secondaryContainer = Color(0xFFFFDCC6),
    onSecondaryContainer = Color(0xFF5A2800),
    
    tertiary = Color(0xFFFF9F66),
    onTertiary = Color.White,
    
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    
    background = KabinkaBackground,
    onBackground = Color(0xFF1F1B16),
    
    surface = KabinkaSurface,
    onSurface = Color(0xFF1F1B16),
    surfaceVariant = Color(0xFFF0E0D0),
    onSurfaceVariant = Color(0xFF4F4539),
    
    outline = Color(0xFF817567),
    outlineVariant = Color(0xFFD3C4B4),
)

val KabinkaDarkColorScheme = darkColorScheme(
    primary = KabinkaOrange,
    onPrimary = Color(0xFF4A1A00),
    primaryContainer = KabinkaOrangeDark,
    onPrimaryContainer = Color(0xFFFFDCC6),
    
    secondary = Color(0xFFFFB380),
    onSecondary = Color(0xFF5A2800),
    secondaryContainer = Color(0xFF7A3A00),
    onSecondaryContainer = Color(0xFFFFDCC6),
    
    tertiary = Color(0xFFFF9F66),
    onTertiary = Color(0xFF3A1A00),
    
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    
    background = KabinkaBackgroundDark,
    onBackground = Color(0xFFEAE1D9),
    
    surface = KabinkaSurfaceDark,
    onSurface = Color(0xFFEAE1D9),
    surfaceVariant = Color(0xFF4F4539),
    onSurfaceVariant = Color(0xFFD3C4B4),
    
    outline = Color(0xFF9C8F80),
    outlineVariant = Color(0xFF4F4539),
)
