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
import app.kabinka.social.model.Instance
import kotlinx.coroutines.launch
import app.kabinka.social.api.requests.accounts.RegisterAccount
import app.kabinka.social.api.requests.oauth.CreateOAuthApp
import app.kabinka.social.api.requests.oauth.GetOauthToken
import app.kabinka.social.model.Token
import app.kabinka.social.model.Application
import me.grishka.appkit.api.Callback
import me.grishka.appkit.api.ErrorResponse
import android.util.Log
import java.util.Locale
import java.util.TimeZone

// NOTE: Phase 1 - Mastodon-only onboarding with complete registration flow.

@Composable
fun OnboardingNavGraph(
    navController: NavHostController,
    startDestination: String,
    viewModel: OnboardingViewModel,
    sessionManager: app.kabinka.frontend.auth.SessionStateManager
) {
    val state by viewModel.state.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    
    // Shared state for registration flow
    var selectedServer by remember { mutableStateOf("") }
    var selectedInstance by remember { mutableStateOf<Instance?>(null) }
    var registeredEmail by remember { mutableStateOf("") }
    var registeredUsername by remember { mutableStateOf("") }
    var registeredDisplayName by remember { mutableStateOf("") }
    
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
                onRegister = {
                    navController.navigate(OnboardingRoute.ServerSelection.route)
                },
                onBrowseWithoutAccount = {
                    viewModel.browsWithoutAccount()
                    navController.navigate(OnboardingRoute.AppShell.route) {
                        popUpTo(OnboardingRoute.Splash.route) { inclusive = true }
                    }
                }
            )
        }
        
        // REGISTRATION FLOW
        
        // Server Selection
        composable(OnboardingRoute.ServerSelection.route) {
            ServerSelectionScreen(
                onServerSelected = { serverDomain ->
                    selectedServer = serverDomain
                    navController.navigate(OnboardingRoute.ServerRules.route)
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }
        
        // Server Rules
        composable(OnboardingRoute.ServerRules.route) {
            ServerRulesScreen(
                serverDomain = selectedServer,
                onAgree = { instance ->
                    selectedInstance = instance
                    navController.navigate(OnboardingRoute.PrivacyPolicy.route)
                },
                onDisagree = {
                    navController.popBackStack()
                }
            )
        }
        
        // Privacy Policy
        composable(OnboardingRoute.PrivacyPolicy.route) {
            selectedInstance?.let { instance ->
                PrivacyPolicyScreen(
                    instance = instance,
                    onAgree = {
                        navController.navigate(OnboardingRoute.MastodonRegister.route)
                    },
                    onDisagree = {
                        navController.popBackStack()
                    }
                )
            }
        }
        
        // Registration Form
        composable(OnboardingRoute.MastodonRegister.route) {
            var isLoading by remember { mutableStateOf(false) }
            var errorMsg by remember { mutableStateOf<String?>(null) }
            val scope = rememberCoroutineScope()
            
            selectedInstance?.let { instance ->
                MastodonRegisterScreen(
                    instance = instance,
                    onRegister = { displayName, username, email, password, birthDate ->
                        isLoading = true
                        errorMsg = null
                        registeredEmail = email
                        registeredUsername = username
                        registeredDisplayName = displayName
                        
                        val locale = Locale.getDefault().toString()
                        val timezone = TimeZone.getDefault().id
                        val domain = instance.domain
                        
                        Log.d("Registration", "Step 1: Creating OAuth app for $domain")
                        
                        // Step 1: Create OAuth app
                        CreateOAuthApp()
                            .setCallback(object : Callback<Application> {
                                override fun onSuccess(app: Application) {
                                    Log.d("Registration", "Step 2: Getting client credentials token")
                                    
                                    // Step 2: Get client credentials token
                                    GetOauthToken(
                                        app.clientId,
                                        app.clientSecret,
                                        null,
                                        GetOauthToken.GrantType.CLIENT_CREDENTIALS
                                    )
                                    .setCallback(object : Callback<Token> {
                                        override fun onSuccess(token: Token) {
                                            Log.d("Registration", "Step 3: Registering account $username")
                                            
                                            // Step 3: Register account with client token
                                            RegisterAccount(
                                                username,
                                                email,
                                                password,
                                                locale,
                                                null, // reason
                                                timezone,
                                                null, // inviteCode
                                                birthDate
                                            )
                                            .setCallback(object : Callback<Token> {
                                                override fun onSuccess(result: Token) {
                                                    Log.d("Registration", "Success! Account created, email sent")
                                                    isLoading = false
                                                    navController.navigate(OnboardingRoute.EmailConfirmation.route)
                                                }
                                                
                                                override fun onError(error: ErrorResponse) {
                                                    Log.e("Registration", "Step 3 failed: Account registration", error as? Throwable)
                                                    isLoading = false
                                                    errorMsg = if (error is app.kabinka.social.api.MastodonErrorResponse) {
                                                        error.error ?: "Registration failed"
                                                    } else {
                                                        "Registration failed. Please try again."
                                                    }
                                                }
                                            })
                                            .exec(domain, token)
                                        }
                                        
                                        override fun onError(error: ErrorResponse) {
                                            Log.e("Registration", "Step 2 failed: Get token", error as? Throwable)
                                            isLoading = false
                                            errorMsg = "Failed to get authorization. Please try again."
                                        }
                                    })
                                    .execNoAuth(domain)
                                }
                                
                                override fun onError(error: ErrorResponse) {
                                    Log.e("Registration", "Step 1 failed: Create OAuth app", error as? Throwable)
                                    isLoading = false
                                    errorMsg = "Failed to initialize registration. Please try again."
                                }
                            })
                            .execNoAuth(domain)
                    },
                    onBack = {
                        navController.popBackStack()
                    },
                    isLoading = isLoading,
                    errorMessage = errorMsg
                )
            }
        }
        
        // Email Confirmation
        composable(OnboardingRoute.EmailConfirmation.route) {
            EmailConfirmationScreen(
                email = registeredEmail,
                onContinue = {
                    navController.navigate(OnboardingRoute.FeedPersonalization.route)
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }
        
        // Feed Personalization
        composable(OnboardingRoute.FeedPersonalization.route) {
            FeedPersonalizationScreen(
                accountId = registeredUsername, // In real app, use actual account ID from registration
                onContinue = {
                    navController.navigate(OnboardingRoute.ProfileSetup.route)
                },
                onSkip = {
                    navController.navigate(OnboardingRoute.ProfileSetup.route)
                }
            )
        }
        
        // Profile Setup
        composable(OnboardingRoute.ProfileSetup.route) {
            ProfileSetupScreen(
                initialDisplayName = registeredDisplayName,
                onComplete = {
                    // Complete onboarding and go to app
                    viewModel.completeOnboarding()
                    navController.navigate(OnboardingRoute.AppShell.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onSkip = {
                    // Skip and go to app
                    viewModel.completeOnboarding()
                    navController.navigate(OnboardingRoute.AppShell.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        
        // LOGIN FLOW (Existing accounts)
        
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
