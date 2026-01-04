package com.kabinka.social.ui.screens.status

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kabinka.social.data.models.mockStatuses
import com.kabinka.social.navigation.Screen
import com.kabinka.social.ui.components.*

@Composable
fun StatusDetailScreen(
    statusId: String,
    onNavigate: (String) -> Unit,
    onBack: () -> Unit
) {
    val statuses = mockStatuses()
    val status = statuses.firstOrNull { it.id == statusId } ?: statuses.first()
    val replies = statuses.take(2)

    Scaffold(
        topBar = {
            KabinkaTopBar(
                title = "Post",
                onNavigationClick = onBack,
                onChatClick = { onNavigate(Screen.FluffyChat.route) }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                StatusCard(
                    status = status,
                    onStatusClick = { },
                    onProfileClick = { onNavigate(Screen.ProfileDetail.createRoute(it)) },
                    onReply = { onNavigate(Screen.ComposeReply.createRoute(it)) },
                    onBoost = { },
                    onFavorite = { },
                    onMore = { }
                )

                if (replies.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Replies",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }

            items(replies) { reply ->
                StatusCard(
                    status = reply,
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
