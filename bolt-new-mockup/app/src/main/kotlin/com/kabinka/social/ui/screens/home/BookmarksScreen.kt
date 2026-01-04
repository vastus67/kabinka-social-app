package com.kabinka.social.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kabinka.social.data.models.mockStatuses
import com.kabinka.social.navigation.Screen
import com.kabinka.social.ui.components.*

@Composable
fun BookmarksScreen(
    onNavigate: (String) -> Unit,
    onBack: () -> Unit
) {
    val statuses = mockStatuses()

    Scaffold(
        topBar = {
            KabinkaTopBar(
                title = "Bookmarks",
                onNavigationClick = onBack,
                onChatClick = { onNavigate(Screen.FluffyChat.route) }
            )
        }
    ) { padding ->
        if (statuses.isEmpty()) {
            EmptyState(
                icon = Icons.Outlined.BookmarkBorder,
                title = "No Bookmarks",
                message = "Bookmark posts to save them for later"
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(statuses) { status ->
                    StatusCard(
                        status = status,
                        onStatusClick = { onNavigate(Screen.StatusDetail.createRoute(it)) },
                        onProfileClick = { onNavigate(Screen.ProfileDetail.createRoute(it)) },
                        onReply = { onNavigate(Screen.ComposeReply.createRoute(it)) },
                        onBoost = { },
                        onFavorite = { },
                        onMore = { }
                    )
                }
            }
        }
    }
}
