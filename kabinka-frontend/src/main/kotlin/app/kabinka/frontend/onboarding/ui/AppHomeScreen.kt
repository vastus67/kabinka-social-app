package app.kabinka.frontend.onboarding.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.kabinka.frontend.auth.SessionStateManager
import app.kabinka.frontend.KabinkaApp

// NOTE: Phase 1 - Mastodon-only onboarding.
// Do not add future-proofing or abstractions for chat or media onboarding.

@Composable
fun AppShell(
    mastodonConnected: Boolean,
    sessionManager: SessionStateManager,
    onNavigateToLogin: () -> Unit = {}
) {
    // Show the full app with bottom navigation
    KabinkaApp(
        sessionManager = sessionManager,
        onNavigateToLogin = onNavigateToLogin
    )
}
