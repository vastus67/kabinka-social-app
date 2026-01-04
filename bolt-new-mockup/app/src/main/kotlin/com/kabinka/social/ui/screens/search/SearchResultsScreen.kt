package com.kabinka.social.ui.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.kabinka.social.data.models.mockStatuses
import com.kabinka.social.data.models.mockUsers
import com.kabinka.social.navigation.Screen
import com.kabinka.social.ui.components.*

@Composable
fun SearchResultsScreen(
    query: String,
    onNavigate: (String) -> Unit,
    onBack: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Posts", "People", "Hashtags")

    Scaffold(
        topBar = {
            Column {
                KabinkaTopBar(
                    title = "Search: $query",
                    onNavigationClick = onBack,
                    onChatClick = { onNavigate(Screen.FluffyChat.route) }
                )
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        when (selectedTab) {
            0 -> {
                val statuses = mockStatuses()
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
            1 -> {
                val users = mockUsers()
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(users) { user ->
                        UserSearchResultItem(
                            user = user,
                            onClick = { onNavigate(Screen.ProfileDetail.createRoute(user.id)) }
                        )
                    }
                }
            }
            2 -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    EmptyState(
                        icon = androidx.compose.material.icons.Icons.Outlined.Tag,
                        title = "No hashtags found",
                        message = "Try searching for something else"
                    )
                }
            }
        }
    }
}

@Composable
private fun UserSearchResultItem(
    user: com.kabinka.social.data.models.User,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = user.displayName.first().toString(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.displayName,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = user.username,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (user.bio.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = user.bio,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }
            }
        }
    }
}
