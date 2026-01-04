package com.kabinka.social.navigation

import androidx.compose.runtime.*
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.kabinka.social.ui.screens.onboarding.*
import com.kabinka.social.ui.screens.home.*
import com.kabinka.social.ui.screens.compose.*
import com.kabinka.social.ui.screens.search.*
import com.kabinka.social.ui.screens.notifications.NotificationsScreen
import com.kabinka.social.ui.screens.profile.*
import com.kabinka.social.ui.screens.status.*
import com.kabinka.social.ui.screens.conversations.*
import com.kabinka.social.ui.screens.fluffychat.*
import com.kabinka.social.ui.screens.settings.*

@Composable
fun KabinkaNavigation(
    navController: NavHostController,
    startDestination: String = Screen.Home.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToWelcome = {
                    navController.navigate(Screen.Welcome.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Welcome.route) {
            WelcomeScreen(
                onGetStarted = { navController.navigate(Screen.ServerPicker.route) }
            )
        }

        composable(Screen.ServerPicker.route) {
            ServerPickerScreen(
                onServerSelected = { instance ->
                    navController.navigate(Screen.Login.createRoute(instance))
                },
                onBack = { navController.navigateUp() }
            )
        }

        composable(
            route = Screen.Login.route,
            arguments = listOf(navArgument("instance") { type = NavType.StringType })
        ) {
            val instance = it.arguments?.getString("instance") ?: ""
            LoginScreen(
                instance = instance,
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                },
                onBack = { navController.navigateUp() }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onNavigate = { route -> navController.navigate(route) }
            )
        }

        composable(Screen.LocalTimeline.route) {
            LocalTimelineScreen(
                onNavigate = { route -> navController.navigate(route) },
                onBack = { navController.navigateUp() }
            )
        }

        composable(Screen.FederatedTimeline.route) {
            FederatedTimelineScreen(
                onNavigate = { route -> navController.navigate(route) },
                onBack = { navController.navigateUp() }
            )
        }

        composable(Screen.Lists.route) {
            ListsScreen(
                onNavigate = { route -> navController.navigate(route) },
                onBack = { navController.navigateUp() }
            )
        }

        composable(
            route = Screen.ListTimeline.route,
            arguments = listOf(navArgument("listId") { type = NavType.StringType })
        ) {
            val listId = it.arguments?.getString("listId") ?: ""
            ListTimelineScreen(
                listId = listId,
                onNavigate = { route -> navController.navigate(route) },
                onBack = { navController.navigateUp() }
            )
        }

        composable(Screen.Bookmarks.route) {
            BookmarksScreen(
                onNavigate = { route -> navController.navigate(route) },
                onBack = { navController.navigateUp() }
            )
        }

        composable(Screen.Favorites.route) {
            FavoritesScreen(
                onNavigate = { route -> navController.navigate(route) },
                onBack = { navController.navigateUp() }
            )
        }

        composable(
            route = Screen.HashtagTimeline.route,
            arguments = listOf(navArgument("tag") { type = NavType.StringType })
        ) {
            val tag = it.arguments?.getString("tag") ?: ""
            HashtagTimelineScreen(
                tag = tag,
                onNavigate = { route -> navController.navigate(route) },
                onBack = { navController.navigateUp() }
            )
        }

        composable(Screen.Trending.route) {
            TrendingScreen(
                onNavigate = { route -> navController.navigate(route) },
                onBack = { navController.navigateUp() }
            )
        }

        composable(
            route = Screen.StatusDetail.route,
            arguments = listOf(navArgument("statusId") { type = NavType.StringType })
        ) {
            val statusId = it.arguments?.getString("statusId") ?: ""
            StatusDetailScreen(
                statusId = statusId,
                onNavigate = { route -> navController.navigate(route) },
                onBack = { navController.navigateUp() }
            )
        }

        composable(
            route = Screen.Thread.route,
            arguments = listOf(navArgument("statusId") { type = NavType.StringType })
        ) {
            val statusId = it.arguments?.getString("statusId") ?: ""
            ThreadScreen(
                statusId = statusId,
                onNavigate = { route -> navController.navigate(route) },
                onBack = { navController.navigateUp() }
            )
        }

        composable(Screen.Compose.route) {
            ComposeScreen(
                onBack = { navController.navigateUp() },
                onPostSuccess = { navController.navigateUp() }
            )
        }

        composable(
            route = Screen.ComposeReply.route,
            arguments = listOf(navArgument("statusId") { type = NavType.StringType })
        ) {
            val statusId = it.arguments?.getString("statusId") ?: ""
            ComposeReplyScreen(
                statusId = statusId,
                onBack = { navController.navigateUp() },
                onPostSuccess = { navController.navigateUp() }
            )
        }

        composable(Screen.Search.route) {
            SearchScreen(
                onNavigate = { route -> navController.navigate(route) }
            )
        }

        composable(
            route = Screen.SearchResults.route,
            arguments = listOf(navArgument("query") { type = NavType.StringType })
        ) {
            val query = it.arguments?.getString("query") ?: ""
            SearchResultsScreen(
                query = query,
                onNavigate = { route -> navController.navigate(route) },
                onBack = { navController.navigateUp() }
            )
        }

        composable(Screen.Explore.route) {
            ExploreScreen(
                onNavigate = { route -> navController.navigate(route) },
                onBack = { navController.navigateUp() }
            )
        }

        composable(Screen.Notifications.route) {
            NotificationsScreen(
                onNavigate = { route -> navController.navigate(route) }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                userId = "current",
                onNavigate = { route -> navController.navigate(route) }
            )
        }

        composable(
            route = Screen.ProfileDetail.route,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) {
            val userId = it.arguments?.getString("userId") ?: ""
            ProfileScreen(
                userId = userId,
                onNavigate = { route -> navController.navigate(route) },
                onBack = { navController.navigateUp() }
            )
        }

        composable(
            route = Screen.Followers.route,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) {
            val userId = it.arguments?.getString("userId") ?: ""
            FollowersScreen(
                userId = userId,
                onNavigate = { route -> navController.navigate(route) },
                onBack = { navController.navigateUp() }
            )
        }

        composable(
            route = Screen.Following.route,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) {
            val userId = it.arguments?.getString("userId") ?: ""
            FollowingScreen(
                userId = userId,
                onNavigate = { route -> navController.navigate(route) },
                onBack = { navController.navigateUp() }
            )
        }

        composable(Screen.Conversations.route) {
            ConversationsScreen(
                onNavigate = { route -> navController.navigate(route) },
                onBack = { navController.navigateUp() }
            )
        }

        composable(
            route = Screen.ConversationDetail.route,
            arguments = listOf(navArgument("conversationId") { type = NavType.StringType })
        ) {
            val conversationId = it.arguments?.getString("conversationId") ?: ""
            ConversationDetailScreen(
                conversationId = conversationId,
                onBack = { navController.navigateUp() }
            )
        }

        composable(Screen.NewConversation.route) {
            NewConversationScreen(
                onBack = { navController.navigateUp() },
                onConversationCreated = { conversationId ->
                    navController.navigate(Screen.ConversationDetail.createRoute(conversationId)) {
                        popUpTo(Screen.NewConversation.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.FluffyChat.route) {
            FluffyChatLandingScreen(
                onNavigate = { route -> navController.navigate(route) },
                onBack = { navController.navigateUp() }
            )
        }

        composable(Screen.FluffyChatServerSelection.route) {
            FluffyChatServerSelectionScreen(
                onServerSelected = {
                    navController.navigate(Screen.FluffyChatRoomList.route)
                },
                onBack = { navController.navigateUp() }
            )
        }

        composable(Screen.FluffyChatRoomList.route) {
            FluffyChatRoomListScreen(
                onNavigate = { route -> navController.navigate(route) },
                onBack = { navController.navigateUp() }
            )
        }

        composable(
            route = Screen.FluffyChatRoom.route,
            arguments = listOf(navArgument("roomId") { type = NavType.StringType })
        ) {
            val roomId = it.arguments?.getString("roomId") ?: ""
            FluffyChatRoomScreen(
                roomId = roomId,
                onBack = { navController.navigateUp() }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigate = { route -> navController.navigate(route) },
                onBack = { navController.navigateUp() }
            )
        }

        composable(Screen.AppearanceSettings.route) {
            AppearanceSettingsScreen(
                onBack = { navController.navigateUp() }
            )
        }

        composable(Screen.NotificationSettings.route) {
            NotificationSettingsScreen(
                onBack = { navController.navigateUp() }
            )
        }

        composable(Screen.PrivacySettings.route) {
            PrivacySettingsScreen(
                onBack = { navController.navigateUp() }
            )
        }

        composable(Screen.AccountManagement.route) {
            AccountManagementScreen(
                onBack = { navController.navigateUp() }
            )
        }

        composable(Screen.About.route) {
            AboutScreen(
                onBack = { navController.navigateUp() }
            )
        }

        composable(Screen.AccountSwitcher.route) {
            AccountSwitcherScreen(
                onAccountSelected = {
                    navController.navigateUp()
                },
                onAddAccount = {
                    navController.navigate(Screen.ServerPicker.route)
                },
                onBack = { navController.navigateUp() }
            )
        }
    }
}
