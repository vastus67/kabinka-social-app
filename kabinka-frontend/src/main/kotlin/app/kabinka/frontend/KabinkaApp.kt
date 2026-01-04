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
import app.kabinka.coreui.components.KabinkaBottomNav
import app.kabinka.coreui.components.KabinkaDrawer
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KabinkaApp(
    sessionManager: SessionStateManager
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: ""
    
    val sessionState by sessionManager.sessionState.collectAsState()
    
    // Don't check session state automatically - just start with LoggedOut
    // Session check will happen after user tries to login
    
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Determine start destination based on session state
    // Default to Login since we start with LoggedOut state
    val startDestination = when (sessionState) {
        is SessionState.LoggedIn -> Screen.Home.route
        is SessionState.LoggedOut -> Screen.Login.route
    }
    
    // Show bottom bar only when logged in and on main screens
    val showBottomBar = when {
        sessionState !is SessionState.LoggedIn -> false
        currentRoute.startsWith("login") -> false
        else -> true
    }

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
                composable(Screen.Login.route) {
                    LoginScreen(
                        onLoginSuccess = {
                            // Refresh session state and navigate to home
                            sessionManager.checkSessionState()
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        },
                        onNavigateToLoginChooser = {
                            navController.navigate("login_chooser")
                        }
                    )
                }
                
                composable("login_chooser") {
                    LoginChooserScreen(
                        onBack = {
                            navController.popBackStack()
                        }
                    )
                }
                
                composable(Screen.Home.route) {
                    HomeTimelineScreen(sessionManager = sessionManager)
                }
                
                // Placeholder screens for other navigation items
                composable(Screen.Search.route) {
                    PlaceholderScreen("Search")
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
private fun PlaceholderScreen(title: String) {
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
