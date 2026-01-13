package app.kabinka.frontend.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import app.kabinka.frontend.ui.theme.ThemePreferencesObserver

@Composable
fun KabinkaTheme(
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val themePreferences by ThemePreferencesObserver.preferences.collectAsState()
    
    // Determine dark theme based on preferences
    val systemDarkTheme = isSystemInDarkTheme()
    val darkTheme = when (themePreferences.themeMode) {
        0 -> systemDarkTheme  // AUTO - follow system
        1 -> false            // LIGHT
        2 -> true             // DARK
        else -> systemDarkTheme
    }
    
    // Use dynamic colors if enabled and available (Android 12+)
    val colorScheme = when {
        themePreferences.useDynamicColors && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) {
                dynamicDarkColorScheme(context)
            } else {
                dynamicLightColorScheme(context)
            }
        }
        darkTheme -> KabinkaDarkColorScheme
        else -> KabinkaLightColorScheme
    }
    
    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
