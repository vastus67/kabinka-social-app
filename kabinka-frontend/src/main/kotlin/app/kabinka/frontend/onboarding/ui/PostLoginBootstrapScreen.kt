package app.kabinka.frontend.onboarding.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.kabinka.frontend.auth.SessionState
import app.kabinka.frontend.auth.SessionStateManager

/**
 * Post-login bootstrap screen.
 * 
 * This screen is shown immediately after OAuth callback completes.
 * It waits for the session to be loaded from storage and only navigates
 * to the authenticated home when SessionState == Authenticated.
 * 
 * This prevents race conditions where the UI bootstraps before the session
 * is persisted, causing a fallback to anonymous mode.
 */
@Composable
fun PostLoginBootstrapScreen(
    sessionManager: SessionStateManager,
    onSessionReady: () -> Unit,
    onSessionFailed: () -> Unit
) {
    val sessionState by sessionManager.sessionState.collectAsState()
    
    // Monitor session state and navigate when ready
    LaunchedEffect(sessionState) {
        android.util.Log.d("OAuth:Bootstrap", "Session state: ${sessionState::class.simpleName}")
        
        when (sessionState) {
            is SessionState.Authenticated -> {
                android.util.Log.d("OAuth:Bootstrap", "Session ready, navigating to home")
                onSessionReady()
            }
            is SessionState.Anonymous -> {
                android.util.Log.e("OAuth:Bootstrap", "Session failed - no account found")
                // Give it a few retries in case of race condition
                kotlinx.coroutines.delay(500)
                sessionManager.checkSessionState()
            }
            is SessionState.Loading -> {
                android.util.Log.d("OAuth:Bootstrap", "Session loading...")
            }
        }
    }
    
    // Check session state on first composition
    LaunchedEffect(Unit) {
        android.util.Log.d("OAuth:Bootstrap", "Checking session state")
        sessionManager.checkSessionState()
    }
    
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Completing sign-in…",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Please wait while we set up your session",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
        }
    }
}
