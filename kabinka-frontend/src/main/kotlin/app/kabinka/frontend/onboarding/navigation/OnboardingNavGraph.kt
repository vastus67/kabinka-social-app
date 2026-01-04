package app.kabinka.frontend.onboarding.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import app.kabinka.frontend.onboarding.OnboardingViewModel
import app.kabinka.frontend.onboarding.data.ConnectionStatus
import app.kabinka.frontend.onboarding.ui.*

// NOTE: Phase 1 - Mastodon-only onboarding.
// Do not add future-proofing or abstractions for chat or media onboarding.

@Composable
fun OnboardingNavGraph(
    navController: NavHostController,
    startDestination: String,
    viewModel: OnboardingViewModel,
    sessionManager: app.kabinka.frontend.auth.SessionStateManager
) {
    val state by viewModel.state.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    
    // Show error messages
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            println("Error: $it")
            viewModel.clearError()
        }
    }
    
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Splash Screen
        composable(OnboardingRoute.Splash.route) {
            SplashScreen(
                onConnectMastodon = {
                    navController.navigate(OnboardingRoute.MastodonInstanceInput.route)
                },
                onBrowseWithoutAccount = {
                    viewModel.browsWithoutAccount()
                    navController.navigate(OnboardingRoute.AppShell.route) {
                        popUpTo(OnboardingRoute.Splash.route) { inclusive = true }
                    }
                }
            )
        }
        
        // Mastodon Instance Input
        composable(OnboardingRoute.MastodonInstanceInput.route) {
            MastodonInstanceInputScreen(
                defaultInstance = state.mastodonConnection.instanceUrl,
                onContinue = { instanceUrl ->
                    viewModel.setMastodonInstance(instanceUrl)
                    navController.navigate(OnboardingRoute.MastodonOAuthLogin.route)
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }
        
        // Mastodon OAuth Login
        composable(OnboardingRoute.MastodonOAuthLogin.route) {
            val isLoading = state.mastodonConnection.status == ConnectionStatus.CONNECTING
            
            // Auto-trigger OAuth when entering this screen
            LaunchedEffect(Unit) {
                if (!isLoading) {
                    viewModel.startMastodonOAuth(state.mastodonConnection.instanceUrl)
                }
            }
            
            MastodonOAuthLoginScreen(
                instanceUrl = state.mastodonConnection.instanceUrl,
                isLoading = isLoading,
                onStartOAuth = {
                    viewModel.startMastodonOAuth(state.mastodonConnection.instanceUrl)
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }
        
        // Mastodon OAuth Callback (deep link)
        composable(
            route = OnboardingRoute.MastodonOAuthCallback.route,
            deepLinks = listOf(
                navDeepLink { uriPattern = "kabinka://oauth/mastodon?code={code}" }
            )
        ) { backStackEntry ->
            val code = backStackEntry.arguments?.getString("code")
            
            LaunchedEffect(code) {
                if (code != null) {
                    android.util.Log.d("OAuth", "Processing OAuth code")
                    viewModel.handleMastodonOAuthCallback(code)
                }
            }
            
            // Navigate to AppShell when connected
            LaunchedEffect(state.mastodonConnection.status, state.onboardingCompleted) {
                if (state.mastodonConnection.status == ConnectionStatus.CONNECTED) {
                    kotlinx.coroutines.delay(500)
                    viewModel.completeOnboarding()
                }
                
                if (state.onboardingCompleted) {
                    kotlinx.coroutines.delay(500)
                    navController.navigate(OnboardingRoute.AppShell.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
            
            // Loading screen
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Completing login...",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
        
        // App Shell (Main App)
        composable(OnboardingRoute.AppShell.route) {
            AppShell(
                mastodonConnected = state.mastodonConnection.status == ConnectionStatus.CONNECTED,
                sessionManager = sessionManager,
                onNavigateToLogin = {
                    viewModel.resetOnboarding()
                    navController.navigate(OnboardingRoute.Splash.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
