package app.kabinka.frontend.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.kabinka.social.api.requests.notifications.GetNotificationsV1
import app.kabinka.social.api.session.AccountSessionManager
import app.kabinka.social.model.Notification
import app.kabinka.social.model.NotificationType
import coil.compose.AsyncImage
import compose.icons.FeatherIcons
import compose.icons.feathericons.*
import me.grishka.appkit.api.Callback
import me.grishka.appkit.api.ErrorResponse
import java.util.EnumSet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen() {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("ALL", "MENTIONS")
    
    var allNotifications by remember { mutableStateOf<List<Notification>>(emptyList()) }
    var mentionNotifications by remember { mutableStateOf<List<Notification>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    
    // Check if user is logged in
    val isLoggedIn = remember {
        try {
            AccountSessionManager.getInstance().lastActiveAccount != null
        } catch (e: Exception) {
            false
        }
    }
    
    // Fetch all notifications
    LaunchedEffect(Unit) {
        if (isLoggedIn) {
            isLoading = true
            val accountId = AccountSessionManager.getInstance().lastActiveAccountID
            if (accountId != null) {
                // Fetch all notifications
                val allRequest = GetNotificationsV1(null, 40, EnumSet.allOf(NotificationType::class.java))
                allRequest.setCallback(object : Callback<List<Notification>> {
                    override fun onSuccess(result: List<Notification>) {
                        allNotifications = result
                        isLoading = false
                    }
                    override fun onError(errorResponse: ErrorResponse) {
                        error = "Failed to load notifications"
                        isLoading = false
                    }
                }).exec(accountId)
                
                // Fetch mentions only
                val mentionsRequest = GetNotificationsV1(null, 40, EnumSet.of(NotificationType.MENTION))
                mentionsRequest.setCallback(object : Callback<List<Notification>> {
                    override fun onSuccess(result: List<Notification>) {
                        mentionNotifications = result
                    }
                    override fun onError(errorResponse: ErrorResponse) {
                        // Silently fail for mentions
                    }
                }).exec(accountId)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Tab Row
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelLarge
                            )
                        },
                        icon = {
                            Icon(
                                imageVector = when (index) {
                                    0 -> FeatherIcons.Bell
                                    1 -> FeatherIcons.AtSign
                                    else -> FeatherIcons.Bell
                                },
                                contentDescription = title
                            )
                        }
                    )
                }
            }

            // Content based on selected tab
            when {
                isLoading -> LoadingContent()
                error != null -> ErrorContent(error!!)
                selectedTab == 0 -> NotificationsList(allNotifications)
                selectedTab == 1 -> NotificationsList(mentionNotifications)
            }
        }
    }
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorContent(errorMessage: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Error: $errorMessage",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error
        )
    }
}

@Composable
private fun NotificationsList(notifications: List<Notification>) {
    if (notifications.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No notifications",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(notifications) { notification ->
                NotificationItem(notification)
            }
        }
    }
}

@Composable
private fun NotificationItem(notification: Notification) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
                NotificationType.FOLLOW, NotificationType.FOLLOW_REQUEST -> FeatherIcons.UserPlus to MaterialTheme.colorScheme.primary
                NotificationType.FAVORITE -> FeatherIcons.Heart to MaterialTheme.colorScheme.error
                NotificationType.REBLOG -> FeatherIcons.Repeat to MaterialTheme.colorScheme.tertiary
                NotificationType.MENTION -> FeatherIcons.AtSign to MaterialTheme.colorScheme.secondary
                NotificationType.POLL -> FeatherIcons.BarChart2 to MaterialTheme.colorScheme.primary
                NotificationType.STATUS -> FeatherIcons.Bell to MaterialTheme.colorScheme.primary
                NotificationType.QUOTE -> FeatherIcons.MessageSquare to MaterialTheme.colorScheme.secondary
                else -> FeatherIcons.Bell to MaterialTheme.colorScheme.primary
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
                    if (notification.account?.avatar != null) {
                        AsyncImage(
                            model = notification.account.avatar,
                            contentDescription = null,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Text(
                        text = notification.account?.displayName ?: notification.account?.username ?: "Unknown",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = when (notification.type) {
                        NotificationType.FOLLOW -> "followed you"
                        NotificationType.FAVORITE -> "favorited your post"
                        NotificationType.REBLOG -> "boosted your post"
                        NotificationType.MENTION -> "mentioned you"
                        NotificationType.POLL -> "poll ended"
                        NotificationType.FOLLOW_REQUEST -> "requested to follow you"
                        NotificationType.STATUS -> "posted"
                        NotificationType.QUOTE -> "quoted your post"
                        else -> "notification"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (notification.status != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = notification.status.content.replace(Regex("<[^>]*>"), ""), // Strip HTML
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
