package app.kabinka.coreui.responsive

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Responsive utilities for adaptive layouts across different screen sizes
 */

/**
 * Screen size categories based on Android guidelines
 */
enum class WindowSize {
    COMPACT,    // < 600dp (phones in portrait)
    MEDIUM,     // 600dp - 840dp (tablets in portrait, phones in landscape)
    EXPANDED    // > 840dp (tablets in landscape, large screens)
}

/**
 * Get current window size category
 */
@Composable
fun rememberWindowSize(): WindowSize {
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp.dp
    
    return when {
        screenWidthDp < 600.dp -> WindowSize.COMPACT
        screenWidthDp < 840.dp -> WindowSize.MEDIUM
        else -> WindowSize.EXPANDED
    }
}

/**
 * Get responsive padding based on screen size
 * @param compact Padding for phones in portrait
 * @param medium Padding for tablets in portrait / phones in landscape
 * @param expanded Padding for tablets in landscape / large screens
 */
@Composable
fun responsivePadding(
    compact: Dp = 16.dp,
    medium: Dp = 24.dp,
    expanded: Dp = 32.dp
): Dp {
    return when (rememberWindowSize()) {
        WindowSize.COMPACT -> compact
        WindowSize.MEDIUM -> medium
        WindowSize.EXPANDED -> expanded
    }
}

/**
 * Get responsive grid columns based on screen size
 */
@Composable
fun responsiveGridColumns(
    compact: Int = 2,
    medium: Int = 3,
    expanded: Int = 4
): Int {
    return when (rememberWindowSize()) {
        WindowSize.COMPACT -> compact
        WindowSize.MEDIUM -> medium
        WindowSize.EXPANDED -> expanded
    }
}

/**
 * Get responsive spacing between items
 */
@Composable
fun responsiveSpacing(
    compact: Dp = 8.dp,
    medium: Dp = 12.dp,
    expanded: Dp = 16.dp
): Dp {
    return when (rememberWindowSize()) {
        WindowSize.COMPACT -> compact
        WindowSize.MEDIUM -> medium
        WindowSize.EXPANDED -> expanded
    }
}

/**
 * Get responsive content padding
 */
@Composable
fun responsiveContentPadding(
    compact: Dp = 12.dp,
    medium: Dp = 16.dp,
    expanded: Dp = 24.dp
): Dp {
    return when (rememberWindowSize()) {
        WindowSize.COMPACT -> compact
        WindowSize.MEDIUM -> medium
        WindowSize.EXPANDED -> expanded
    }
}

/**
 * Get responsive horizontal margin for centered content on large screens
 */
@Composable
fun responsiveHorizontalMargin(): Dp {
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp.dp
    
    // On very large screens, add horizontal margin to keep content readable
    return when {
        screenWidthDp > 1200.dp -> ((screenWidthDp - 1200.dp) / 2)
        screenWidthDp > 840.dp -> 48.dp
        else -> 0.dp
    }
}

/**
 * Get responsive max width for content to maintain readability
 */
@Composable
fun responsiveMaxWidth(
    compact: Dp = Dp.Infinity,
    medium: Dp = 720.dp,
    expanded: Dp = 1200.dp
): Dp {
    return when (rememberWindowSize()) {
        WindowSize.COMPACT -> compact
        WindowSize.MEDIUM -> medium
        WindowSize.EXPANDED -> expanded
    }
}

/**
 * Check if device is in landscape orientation
 */
@Composable
fun isLandscape(): Boolean {
    val configuration = LocalConfiguration.current
    return configuration.screenWidthDp > configuration.screenHeightDp
}

/**
 * Get screen width in dp
 */
@Composable
fun screenWidthDp(): Dp {
    val configuration = LocalConfiguration.current
    return configuration.screenWidthDp.dp
}

/**
 * Get screen height in dp
 */
@Composable
fun screenHeightDp(): Dp {
    val configuration = LocalConfiguration.current
    return configuration.screenHeightDp.dp
}

/**
 * Convert dp to pixels
 */
@Composable
fun Dp.toPx(): Float {
    return with(LocalDensity.current) { this@toPx.toPx() }
}

/**
 * Convert pixels to dp
 */
@Composable
fun Float.toDp(): Dp {
    return with(LocalDensity.current) { this@toDp.toDp() }
}

/**
 * Get responsive text size multiplier based on screen size
 * This can be used to slightly adjust text sizes on larger screens
 */
@Composable
fun responsiveTextScale(): Float {
    return when (rememberWindowSize()) {
        WindowSize.COMPACT -> 1.0f
        WindowSize.MEDIUM -> 1.05f
        WindowSize.EXPANDED -> 1.1f
    }
}

/**
 * Get responsive icon size
 */
@Composable
fun responsiveIconSize(
    compact: Dp = 24.dp,
    medium: Dp = 28.dp,
    expanded: Dp = 32.dp
): Dp {
    return when (rememberWindowSize()) {
        WindowSize.COMPACT -> compact
        WindowSize.MEDIUM -> medium
        WindowSize.EXPANDED -> expanded
    }
}
