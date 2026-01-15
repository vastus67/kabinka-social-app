package app.kabinka.frontend

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import app.kabinka.frontend.auth.SessionState
import app.kabinka.frontend.auth.SessionStateManager
import app.kabinka.frontend.navigation.Screen
import app.kabinka.frontend.screens.ComposeScreen
import app.kabinka.frontend.screens.HomeTimelineScreen
import app.kabinka.frontend.screens.LoginScreen
import app.kabinka.frontend.screens.LoginChooserScreen
import app.kabinka.frontend.screens.ExploreScreen
import app.kabinka.frontend.screens.NotificationsScreen
import app.kabinka.frontend.screens.ProfileScreen
import app.kabinka.frontend.screens.UserProfileScreen
import app.kabinka.frontend.screens.SettingsScreen
import app.kabinka.frontend.screens.AboutServerScreen
import app.kabinka.frontend.screens.BookmarksScreen
import app.kabinka.frontend.screens.FavoritesScreen
import app.kabinka.frontend.settings.ui.BehaviourSettingsScreen
import app.kabinka.frontend.settings.ui.DisplaySettingsScreen
import app.kabinka.frontend.settings.ui.PrivacySettingsScreen
import app.kabinka.frontend.settings.ui.FiltersSettingsScreen
import app.kabinka.frontend.settings.ui.NotificationsSettingsScreen
import app.kabinka.frontend.settings.ui.PostingDefaultsSettingsScreen
import app.kabinka.frontend.settings.ui.EditFilterScreen
import app.kabinka.coreui.components.KabinkaBottomNav
import app.kabinka.coreui.components.KabinkaDrawer
import app.kabinka.social.api.session.AccountSessionManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KabinkaApp(
    sessionManager: SessionStateManager,
    onNavigateToLogin: () -> Unit = {}
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: ""
    
    val sessionState by sessionManager.sessionState.collectAsState()
    
    // Don't check session state automatically - just start with LoggedOut
    // Session check will happen after user tries to login
    
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    // Get current account for drawer
    val currentAccountSession = remember {
        try {
            AccountSessionManager.getInstance().lastActiveAccount
        } catch (e: Exception) {
            null
        }
    }
    
    val currentAccount = currentAccountSession?.self
    
    // Get domain from account session
    val accountDomain = remember(currentAccountSession, currentAccount) {
        currentAccount?.let { account ->
            val domain = currentAccountSession?.domain ?: account.getDomain()
            if (domain != null) {
                "@${account.username}@$domain"
            } else {
                "@${account.username}"
            }
        }
    }

    // Always start at Home - login/logout is handled by the onboarding flow
    val startDestination = Screen.Home.route
    
    // Always show bottom bar since we're inside the app shell
    val showBottomBar = true

    val bottomNavRoutes = listOf(
        Screen.Home.route,
        Screen.Search.route,
        Screen.Compose.route,
        Screen.Notifications.route,
        Screen.Profile.route
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            KabinkaDrawer(
                onNavigate = { route ->
                    navController.navigate(route)
                    scope.launch {
                        drawerState.close()
                    }
                },
                onDismiss = {
                    scope.launch {
                        drawerState.close()
                    }
                },
                profileAvatarUrl = currentAccount?.avatar,
                profileDisplayName = currentAccount?.displayName,
                profileUsername = accountDomain,
                isLoggedIn = currentAccount != null,
                onLogout = {
                    // Handle logout
                    if (currentAccount != null) {
                        try {
                            currentAccountSession?.let {
                                AccountSessionManager.getInstance().removeAccount(it.getID())
                            }
                            sessionManager.setAnonymousMode(true)
                        } catch (e: Exception) {
                            android.util.Log.e("KabinkaApp", "Error logging out", e)
                        }
                    }
                    onNavigateToLogin()
                }
            )
        }
    ) {
        Scaffold(
            bottomBar = {
                if (showBottomBar) {
                    KabinkaBottomNav(
                        currentRoute = currentRoute,
                        onNavigate = { route ->
                            // Special handling for Home and Profile navigation - clear user_profile backstack
                            if ((route == Screen.Home.route || route == Screen.Profile.route) && currentRoute.startsWith("user_profile/")) {
                                navController.navigate(route) {
                                    popUpTo(route) {
                                        inclusive = false
                                    }
                                    launchSingleTop = true
                                }
                            } else {
                                navController.navigate(route) {
                                    popUpTo(bottomNavRoutes.first()) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )
                }
            }
        ) { paddingValues ->
            NavHost(
                navController = navController,
                startDestination = startDestination,
                modifier = Modifier.padding(paddingValues)
            ) {
                composable(Screen.Home.route) {
                    HomeTimelineScreen(
                        sessionManager = sessionManager,
                        onNavigateToLogin = onNavigateToLogin,
                        onOpenDrawer = {
                            scope.launch {
                                drawerState.open()
                            }
                        },
                        onNavigateToUser = { userId ->
                            navController.navigate("user_profile/$userId")
                        },
                        onNavigateToReply = { statusId ->
                            navController.navigate(Screen.ComposeReply.createRoute(statusId))
                        }
                    )
                }
                
                // Placeholder screens for other navigation items
                composable(Screen.Search.route) {
                    ExploreScreen(
                        onNavigateToUser = { userId ->
                            navController.navigate("user_profile/$userId")
                        }
                    )
                }
                
                composable(Screen.Compose.route) {
                    ComposeScreen(
                        sessionManager = sessionManager,
                        onNavigateBack = {
                            navController.popBackStack()
                        }
                    )
                }
                
                composable(
                    route = Screen.ComposeReply.route,
                    arguments = listOf(navArgument("statusId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val statusId = backStackEntry.arguments?.getString("statusId") ?: return@composable
                    
                    // Load the status to reply to
                    val replyToStatus = remember { mutableStateOf<app.kabinka.social.model.Status?>(null) }
                    val isLoading = remember { mutableStateOf(true) }
                    val errorMessage = remember { mutableStateOf<String?>(null) }
                    
                    LaunchedEffect(statusId) {
                        try {
                            isLoading.value = true
                            errorMessage.value = null
                            val session = sessionManager.getCurrentSession()
                            if (session != null) {
                                val statusRequest = app.kabinka.social.api.requests.statuses.GetStatusByID(statusId)
                                statusRequest.setCallback(object : me.grishka.appkit.api.Callback<app.kabinka.social.model.Status> {
                                    override fun onSuccess(result: app.kabinka.social.model.Status) {
                                        replyToStatus.value = result
                                        isLoading.value = false
                                    }
                                    
                                    override fun onError(err: me.grishka.appkit.api.ErrorResponse) {
                                        errorMessage.value = "Failed to load status"
                                        isLoading.value = false
                                    }
                                }).exec(session.getID())
                            } else {
                                errorMessage.value = "No active session"
                                isLoading.value = false
                            }
                        } catch (e: Exception) {
                            errorMessage.value = e.message ?: "Unknown error"
                            isLoading.value = false
                        }
                    }
                    
                    when {
                        isLoading.value -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                        errorMessage.value != null -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Error: ${errorMessage.value}")
                                    Button(onClick = { navController.popBackStack() }) {
                                        Text("Go Back")
                                    }
                                }
                            }
                        }
                        replyToStatus.value != null -> {
                            ComposeScreen(
                                sessionManager = sessionManager,
                                replyToStatus = replyToStatus.value,
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }
                    }
                }
                
                composable(Screen.Notifications.route) {
                    NotificationsScreen(
                        onNavigateToUser = { userId ->
                            navController.navigate("user_profile/$userId")
                        }
                    )
                }
                
                composable(Screen.Profile.route) {
                    ProfileScreen(
                        onNavigateToUser = { userId ->
                            navController.navigate("user_profile/$userId")
                        },
                        onNavigateToReply = { statusId ->
                            navController.navigate(Screen.ComposeReply.createRoute(statusId))
                        }
                    )
                }
                
                composable(Screen.Bookmarks.route) {
                    BookmarksScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToUser = { userId ->
                            navController.navigate("user_profile/$userId")
                        },
                        onNavigateToReply = { statusId ->
                            navController.navigate(Screen.ComposeReply.createRoute(statusId))
                        }
                    )
                }
                
                composable(Screen.Favorites.route) {
                    FavoritesScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToUser = { userId ->
                            navController.navigate("user_profile/$userId")
                        },
                        onNavigateToReply = { statusId ->
                            navController.navigate(Screen.ComposeReply.createRoute(statusId))
                        }
                    )
                }
                
                composable(Screen.Settings.route) {
                    SettingsScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToBehaviour = { navController.navigate(Screen.BehaviourSettings.route) },
                        onNavigateToDisplay = { navController.navigate(Screen.DisplaySettings.route) },
                        onNavigateToPrivacy = { navController.navigate(Screen.PrivacySettings.route) },
                        onNavigateToFilters = { navController.navigate(Screen.FiltersSettings.route) },
                        onNavigateToNotifications = { navController.navigate(Screen.NotificationSettings.route) },
                        onNavigateToPostingDefaults = { navController.navigate(Screen.PostingDefaultsSettings.route) },
                        onNavigateToAboutServer = { navController.navigate(Screen.AboutServer.route) },
                        onNavigateToDonate = { navController.navigate(Screen.DonateToServer.route) },
                        onNavigateToDeleteAccount = { navController.navigate(Screen.DeleteAccount.route) }
                    )
                }
                
                // Settings sub-screens
                composable(Screen.BehaviourSettings.route) {
                    BehaviourSettingsScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
                
                composable(Screen.DisplaySettings.route) {
                    DisplaySettingsScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
                
                composable(Screen.PrivacySettings.route) {
                    PrivacySettingsScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
                
                composable(Screen.FiltersSettings.route) {
                    FiltersSettingsScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToAddFilter = { navController.navigate(Screen.AddFilter.route) },
                        onNavigateToEditFilter = { filterId -> navController.navigate(Screen.EditFilter.createRoute(filterId)) }
                    )
                }
                
                composable(Screen.AddFilter.route) {
                    EditFilterScreen(
                        filterId = null,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
                
                composable(Screen.EditFilter.route) { backStackEntry ->
                    val filterId = backStackEntry.arguments?.getString("filterId")
                    EditFilterScreen(
                        filterId = filterId,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
                
                composable(Screen.NotificationSettings.route) {
                    NotificationsSettingsScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
                
                composable(Screen.PostingDefaultsSettings.route) {
                    PostingDefaultsSettingsScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
                
                composable(Screen.AboutServer.route) {
                    AboutServerScreen(
                        onBack = { navController.popBackStack() }
                    )
                }
                
                composable(Screen.DonateToServer.route) {
                    PlaceholderScreen("Donate")
                }
                
                composable(Screen.DeleteAccount.route) {
                    PlaceholderScreen("Delete Account")
                }
                
                // User profile screen
                composable(
                    route = "user_profile/{userId}",
                    arguments = listOf(navArgument("userId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val userId = backStackEntry.arguments?.getString("userId") ?: return@composable
                    UserProfileScreen(
                        userId = userId,
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToUser = { targetUserId ->
                            navController.navigate("user_profile/$targetUserId")
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun PlaceholderScreen(title: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$title - Coming Soon",
            style = MaterialTheme.typography.headlineSmall
        )
    }
}
