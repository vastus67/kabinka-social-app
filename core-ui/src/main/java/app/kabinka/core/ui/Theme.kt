package app.kabinka.core.ui

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Orange = Color(0xFFFF7A45)
val Peach  = Color(0xFFFF9E66)
val Amber  = Color(0xFFFFB878)
val LightBg = Color(0xFFFFF6F1)
val LightPanel = Color(0xFFFFEDE4)
val DarkBg = Color(0xFF1E1B19)
val DarkPanel = Color(0xFF2B2725)

private val LightScheme = lightColorScheme(
  primary = Orange, secondary = Peach, tertiary = Amber,
  background = LightBg, surface = LightPanel,
  onBackground = Color(0xFF2E2F32), onSurface = Color(0xFF2E2F32)
)
private val DarkScheme = darkColorScheme(
  primary = Orange, secondary = Peach, tertiary = Amber,
  background = DarkBg, surface = DarkPanel,
  onBackground = Color.White, onSurface = Color.White
)

@Composable fun KabinkaTheme(dark: Boolean = false, content: @Composable () -> Unit) {
  MaterialTheme(colorScheme = if (dark) DarkScheme else LightScheme, content = content)
}
