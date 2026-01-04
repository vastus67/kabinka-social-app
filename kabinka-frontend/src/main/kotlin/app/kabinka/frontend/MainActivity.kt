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
import app.kabinka.frontend.onboarding.OnboardingViewModel
import app.kabinka.frontend.onboarding.data.OnboardingRepository
import app.kabinka.frontend.onboarding.navigation.OnboardingNavGraph
import app.kabinka.frontend.onboarding.navigation.OnboardingRoute
import app.kabinka.frontend.ui.theme.KabinkaTheme

// NOTE: Phase 1 - Mastodon-only onboarding.
// Do not add future-proofing or abstractions for chat or media onboarding.

class MainActivity : ComponentActivity() {
    
    private lateinit var repository: OnboardingRepository
    private val sessionManager by lazy { SessionStateManager(applicationContext) }
    private val oauthCodeState = mutableStateOf<String?>(null)
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
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
                
                // Handle OAuth code if available (from intent)
                LaunchedEffect(oauthCode) {
                    oauthCode?.let { code ->
                        android.util.Log.d("MainActivity", "Processing OAuth code: ${code.take(10)}...")
                        viewModel.handleMastodonOAuthCallback(code)
                        
                        // Wait for session to be created
                        kotlinx.coroutines.delay(1000)
                        
                        viewModel.completeOnboarding()
                        
                        // Wait for state to update
                        kotlinx.coroutines.delay(500)
                        
                        android.util.Log.d("MainActivity", "OAuth complete, navigating to AppShell")
                        navController.navigate(OnboardingRoute.AppShell.route) {
                            popUpTo(0) { inclusive = true }
                        }
                        
                        oauthCodeState.value = null // Clear after processing
                    }
                }
                
                // Set anonymous mode if user is browsing without account
                LaunchedEffect(state.mode, state.mastodonConnection.status) {
                    if (state.mode == app.kabinka.frontend.onboarding.data.OnboardingMode.BROWSE_ONLY) {
                        sessionManager.setAnonymousMode(true)
                    } else if (state.mastodonConnection.status == app.kabinka.frontend.onboarding.data.ConnectionStatus.CONNECTED) {
                        // User is logged in, disable anonymous mode
                        sessionManager.setAnonymousMode(false)
                    } else {
                        sessionManager.setAnonymousMode(false)
                    }
                }
                
                val startDestination = if (state.onboardingCompleted) {
                    OnboardingRoute.AppShell.route
                } else {
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
        setIntent(intent)
        handleOAuthIntent(intent)
    }
    
    private fun handleOAuthIntent(intent: Intent) {
        val data = intent.data
        android.util.Log.d("MainActivity", "Handling intent with data: $data")
        
        if (data != null && data.scheme == "kabinka" && data.host == "oauth") {
            val code = data.getQueryParameter("code")
            if (code != null) {
                android.util.Log.d("MainActivity", "OAuth code received: ${code.take(10)}...")
                oauthCodeState.value = code
            }
        }
    }
}
