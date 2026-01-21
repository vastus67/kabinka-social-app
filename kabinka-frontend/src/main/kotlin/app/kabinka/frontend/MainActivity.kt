package app.kabinka.frontend

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import app.kabinka.frontend.auth.SessionStateManager
import app.kabinka.frontend.auth.SessionState
import app.kabinka.frontend.onboarding.OnboardingViewModel
import app.kabinka.frontend.onboarding.data.OnboardingRepository
import app.kabinka.frontend.onboarding.navigation.OnboardingNavGraph
import app.kabinka.frontend.onboarding.navigation.OnboardingRoute
import app.kabinka.frontend.ui.theme.KabinkaTheme
import kotlinx.coroutines.delay

// NOTE: Phase 1 - Mastodon-only onboarding.
// Do not add future-proofing or abstractions for chat or media onboarding.

class MainActivity : ComponentActivity() {
    
    private lateinit var repository: OnboardingRepository
    private val sessionManager by lazy { SessionStateManager(applicationContext) }
    private val oauthCodeState = mutableStateOf<String?>(null)
    private val shouldNavigateToBootstrap = mutableStateOf(false)
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        android.util.Log.d("OAuth:MainActivity", "onCreate called")
        
        repository = OnboardingRepository(applicationContext)
        
        // Check if this is an OAuth callback
        handleOAuthIntent(intent)
        
        setContent {
            KabinkaTheme {
                val viewModel: OnboardingViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            @Suppress("UNCHECKED_CAST")
                            return OnboardingViewModel(repository, applicationContext) as T
                        }
                    }
                )
                
                val state by viewModel.state.collectAsState()
                val navController = rememberNavController()
                val oauthCode by oauthCodeState
                val navigateToBootstrap by shouldNavigateToBootstrap
                
                // Handle OAuth code if available (from intent)
                LaunchedEffect(oauthCode) {
                    oauthCode?.let { code ->
                        android.util.Log.d("OAuth:MainActivity", "Processing OAuth code: ${code.take(10)}...")
                        
                        // Handle the OAuth callback (this persists the session)
                        viewModel.handleMastodonOAuthCallback(code)
                        
                        // Wait for session to be persisted (blocking)
                        android.util.Log.d("OAuth:MainActivity", "Waiting for session persistence")
                        delay(1500)
                        
                        // Complete onboarding
                        viewModel.completeOnboarding()
                        delay(200)
                        
                        // Signal to navigate to bootstrap
                        shouldNavigateToBootstrap.value = true
                        
                        // Clear code after processing
                        oauthCodeState.value = null
                    }
                }
                
                // Navigate to PostLoginBootstrap when ready
                LaunchedEffect(navigateToBootstrap) {
                    if (navigateToBootstrap) {
                        android.util.Log.d("OAuth:MainActivity", "Navigating to PostLoginBootstrap")
                        navController.navigate("post_login_bootstrap") {
                            popUpTo(0) { inclusive = true }
                        }
                        shouldNavigateToBootstrap.value = false
                    }
                }
                
                // Set anonymous mode if user is browsing without account
                LaunchedEffect(state.mode, state.mastodonConnection.status) {
                    if (state.mode == app.kabinka.frontend.onboarding.data.OnboardingMode.BROWSE_ONLY) {
                        android.util.Log.d("OAuth:MainActivity", "Setting anonymous mode: true (BROWSE_ONLY)")
                        sessionManager.setAnonymousMode(true)
                    } else if (state.mastodonConnection.status == app.kabinka.frontend.onboarding.data.ConnectionStatus.CONNECTED) {
                        // User is logged in, disable anonymous mode and check session
                        android.util.Log.d("OAuth:MainActivity", "Setting anonymous mode: false (CONNECTED)")
                        sessionManager.setAnonymousMode(false)
                        // Force session check after a short delay to ensure session is saved
                        delay(500)
                        sessionManager.checkSessionState()
                    } else if (state.mastodonConnection.status == app.kabinka.frontend.onboarding.data.ConnectionStatus.DISCONNECTED) {
                        android.util.Log.d("OAuth:MainActivity", "Setting anonymous mode: false (DISCONNECTED)")
                        sessionManager.setAnonymousMode(false)
                    }
                }
                
                // Check if user has an active session to determine start destination
                val hasActiveSession = remember {
                    try {
                        val session = app.kabinka.social.api.session.AccountSessionManager.getInstance().lastActiveAccount
                        android.util.Log.d("OAuth:MainActivity", "Active session check: ${session != null}")
                        session != null
                    } catch (e: Exception) {
                        android.util.Log.e("OAuth:MainActivity", "Session check failed", e)
                        false
                    }
                }
                
                val startDestination = if (hasActiveSession) {
                    android.util.Log.d("OAuth:MainActivity", "Start destination: AppShell (has session)")
                    OnboardingRoute.AppShell.route
                } else {
                    android.util.Log.d("OAuth:MainActivity", "Start destination: Splash (no session)")
                    OnboardingRoute.Splash.route
                }
                
                OnboardingNavGraph(
                    navController = navController,
                    startDestination = startDestination,
                    viewModel = viewModel,
                    sessionManager = sessionManager
                )
            }
        }
    }
    
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        android.util.Log.d("OAuth:MainActivity", "onNewIntent called")
        setIntent(intent)
        handleOAuthIntent(intent)
    }
    
    private fun handleOAuthIntent(intent: Intent) {
        val data = intent.data
        android.util.Log.d("OAuth:MainActivity", "Handling intent with data: $data")
        
        if (data != null && data.scheme == "kabinka" && data.host == "oauth") {
            val code = data.getQueryParameter("code")
            if (code != null) {
                android.util.Log.d("OAuth:MainActivity", "OAuth code received: ${code.take(10)}...")
                oauthCodeState.value = code
            }
        }
    }
}
