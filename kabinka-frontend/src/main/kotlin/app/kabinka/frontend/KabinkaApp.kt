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
import app.kabinka.frontend.auth.SessionState
import app.kabinka.frontend.auth.SessionStateManager
import app.kabinka.frontend.navigation.Screen
import app.kabinka.frontend.screens.HomeTimelineScreen
import app.kabinka.frontend.screens.LoginScreen
import app.kabinka.frontend.screens.LoginChooserScreen
import app.kabinka.frontend.screens.ExploreScreen
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
                profileUsername = accountDomain
            )
        }
    ) {
        Scaffold(
            bottomBar = {
                if (showBottomBar) {
                    KabinkaBottomNav(
                        currentRoute = currentRoute,
                        onNavigate = { route ->
                            navController.navigate(route) {
                                popUpTo(bottomNavRoutes.first()) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
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
                        onNavigateToLogin = onNavigateToLogin
                    )
                }
                
                // Placeholder screens for other navigation items
                composable(Screen.Search.route) {
                    ExploreScreen()
                }
                
                composable(Screen.Compose.route) {
                    PlaceholderScreen("Compose")
                }
                
                composable(Screen.Notifications.route) {
                    PlaceholderScreen("Notifications")
                }
                
                composable(Screen.Profile.route) {
                    PlaceholderScreen("Profile")
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
