package com.kabinka.social.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
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
fun ProfileScreen(
    userId: String,
    onNavigate: (String) -> Unit,
    onBack: (() -> Unit)? = null
) {
    val users = mockUsers()
    val user = if (userId == "current") users[0] else users.firstOrNull { it.id == userId } ?: users[0]
    val statuses = mockStatuses()
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Posts", "Replies", "Media")

    Scaffold(
        topBar = {
            if (onBack != null) {
                KabinkaTopBar(
                    title = user.displayName,
                    onNavigationClick = onBack,
                    onChatClick = { onNavigate(Screen.FluffyChat.route) },
                    actions = {
                        IconButton(onClick = { }) {
                            Icon(Icons.Outlined.MoreVert, "More options")
                        }
                    }
                )
            } else {
                KabinkaTopBar(
                    title = "Profile",
                    onChatClick = { onNavigate(Screen.FluffyChat.route) },
                    showAvatar = false
                )
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            item {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = user.displayName.first().toString(),
                                style = MaterialTheme.typography.displaySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        if (userId != "current") {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(onClick = { }) {
                                    Text(if (user.isFollowing) "Unfollow" else "Follow")
                                }
                                IconButton(onClick = { }) {
                                    Icon(Icons.Outlined.MoreVert, "More")
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = user.displayName,
                        style = MaterialTheme.typography.headlineSmall
                    )

                    Text(
                        text = user.username,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (user.bio.isNotBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = user.bio,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        ProfileStat(
                            count = user.postsCount,
                            label = "Posts",
                            onClick = { }
                        )
                        ProfileStat(
                            count = user.followersCount,
                            label = "Followers",
                            onClick = { onNavigate(Screen.Followers.createRoute(user.id)) }
                        )
                        ProfileStat(
                            count = user.followingCount,
                            label = "Following",
                            onClick = { onNavigate(Screen.Following.createRoute(user.id)) }
                        )
                    }
                }

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

            when (selectedTab) {
                0 -> {
                    items(statuses) { status ->
                        StatusCard(
                            status = status,
                            onStatusClick = { onNavigate(Screen.StatusDetail.createRoute(it)) },
                            onProfileClick = { onNavigate(Screen.ProfileDetail.createRoute(it)) },
                            onReply = { onNavigate(Screen.ComposeReply.createRoute(it)) },
                            onBoost = { },
                            onFavorite = { },
                            onMore = { },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
                1, 2 -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            EmptyState(
                                icon = if (selectedTab == 1) Icons.Outlined.ChatBubbleOutline else Icons.Outlined.Image,
                                title = "Nothing here",
                                message = "No ${tabs[selectedTab].lowercase()} to show"
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileStat(
    count: Int,
    label: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
