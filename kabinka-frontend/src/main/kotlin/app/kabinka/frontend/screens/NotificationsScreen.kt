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
import compose.icons.LineAwesomeIcons
import compose.icons.lineawesomeicons.HomeSolid
import compose.icons.lineawesomeicons.SearchSolid
import compose.icons.lineawesomeicons.EditSolid
import compose.icons.lineawesomeicons.BellSolid
import compose.icons.lineawesomeicons.UserSolid
import compose.icons.lineawesomeicons.ShareSolid
import compose.icons.lineawesomeicons.CogSolid
import compose.icons.lineawesomeicons.CommentSolid
import compose.icons.lineawesomeicons.EllipsisVSolid
import compose.icons.lineawesomeicons.GlobeSolid
import compose.icons.lineawesomeicons.HeartSolid
import compose.icons.lineawesomeicons.RetweetSolid
import compose.icons.lineawesomeicons.StarSolid
import compose.icons.lineawesomeicons.FileAltSolid
import compose.icons.lineawesomeicons.HashtagSolid
import compose.icons.lineawesomeicons.FileSolid
import compose.icons.lineawesomeicons.UsersSolid
import compose.icons.lineawesomeicons.RssSolid
import compose.icons.lineawesomeicons.AtSolid
import compose.icons.lineawesomeicons.UserPlusSolid
import compose.icons.lineawesomeicons.ChartBarSolid
import compose.icons.lineawesomeicons.BookmarkSolid
import compose.icons.lineawesomeicons.MapMarkerSolid
import compose.icons.lineawesomeicons.InfoCircleSolid
import compose.icons.lineawesomeicons.ExclamationTriangleSolid
import compose.icons.lineawesomeicons.SyncSolid
import compose.icons.lineawesomeicons.PhoneSolid
import compose.icons.lineawesomeicons.ReplySolid
import compose.icons.lineawesomeicons.PlaySolid
import compose.icons.lineawesomeicons.QrcodeSolid
import compose.icons.lineawesomeicons.TimesSolid

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
                                    0 -> LineAwesomeIcons.BellSolid
                                    1 -> LineAwesomeIcons.AtSolid
                                    else -> LineAwesomeIcons.BellSolid
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
                NotificationType.FOLLOW, NotificationType.FOLLOW_REQUEST -> LineAwesomeIcons.UserPlusSolid to MaterialTheme.colorScheme.primary
                NotificationType.FAVORITE -> LineAwesomeIcons.HeartSolid to MaterialTheme.colorScheme.error
                NotificationType.REBLOG -> LineAwesomeIcons.RetweetSolid to MaterialTheme.colorScheme.tertiary
                NotificationType.MENTION -> LineAwesomeIcons.AtSolid to MaterialTheme.colorScheme.secondary
                NotificationType.POLL -> LineAwesomeIcons.ChartBarSolid to MaterialTheme.colorScheme.primary
                NotificationType.STATUS -> LineAwesomeIcons.BellSolid to MaterialTheme.colorScheme.primary
                NotificationType.QUOTE -> LineAwesomeIcons.CommentSolid to MaterialTheme.colorScheme.secondary
                else -> LineAwesomeIcons.BellSolid to MaterialTheme.colorScheme.primary
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
