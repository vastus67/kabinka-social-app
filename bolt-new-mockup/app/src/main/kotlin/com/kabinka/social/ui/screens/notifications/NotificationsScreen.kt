package com.kabinka.social.ui.screens.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.kabinka.social.data.models.*
import com.kabinka.social.navigation.Screen
import com.kabinka.social.ui.components.KabinkaTopBar

@Composable
fun NotificationsScreen(
    onNavigate: (String) -> Unit
) {
    val notifications = mockNotifications()

    Scaffold(
        topBar = {
            KabinkaTopBar(
                title = "Notifications",
                onChatClick = { onNavigate(Screen.FluffyChat.route) },
                showAvatar = false
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(notifications) { notification ->
                NotificationItem(
                    notification = notification,
                    onClick = {
                        when (notification.type) {
                            NotificationType.FOLLOW, NotificationType.FOLLOW_REQUEST -> {
                                onNavigate(Screen.ProfileDetail.createRoute(notification.account.id))
                            }
                            else -> {
                                notification.status?.let {
                                    onNavigate(Screen.StatusDetail.createRoute(it.id))
                                }
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun NotificationItem(
    notification: Notification,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val (icon, iconColor) = when (notification.type) {
                NotificationType.FOLLOW -> Icons.Outlined.PersonAdd to MaterialTheme.colorScheme.primary
                NotificationType.FAVORITE -> Icons.Outlined.Favorite to MaterialTheme.colorScheme.error
                NotificationType.BOOST -> Icons.Outlined.Repeat to MaterialTheme.colorScheme.tertiary
                NotificationType.MENTION -> Icons.Outlined.AlternateEmail to MaterialTheme.colorScheme.secondary
                NotificationType.POLL -> Icons.Outlined.Poll to MaterialTheme.colorScheme.primary
                NotificationType.FOLLOW_REQUEST -> Icons.Outlined.PersonAdd to MaterialTheme.colorScheme.primary
            }

            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = iconColor
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = notification.account.displayName.first().toString(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Text(
                        text = notification.account.displayName,
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = when (notification.type) {
                        NotificationType.FOLLOW -> "followed you"
                        NotificationType.FAVORITE -> "favorited your post"
                        NotificationType.BOOST -> "boosted your post"
                        NotificationType.MENTION -> "mentioned you"
                        NotificationType.POLL -> "poll ended"
                        NotificationType.FOLLOW_REQUEST -> "requested to follow you"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (notification.status != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = notification.status.content,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (notification.type == NotificationType.FOLLOW_REQUEST) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Accept")
                        }
                        OutlinedButton(
                            onClick = { },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Decline")
                        }
                    }
                }
            }
        }
    }
}
