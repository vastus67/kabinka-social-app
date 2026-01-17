package app.kabinka.frontend.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import android.content.Intent
import android.net.Uri
import app.kabinka.frontend.auth.SessionStateManager
import app.kabinka.frontend.timeline.TimelineUiState
import app.kabinka.frontend.timeline.TimelineViewModel
import app.kabinka.frontend.timeline.TimelineType
import app.kabinka.social.model.Status
import app.kabinka.social.model.Attachment
import app.kabinka.frontend.components.ImageViewerDialog
import java.time.Duration
import java.time.Instant
import app.kabinka.frontend.ui.icons.TimelineRemixIcons
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



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTimelineScreen(
    sessionManager: SessionStateManager,
    onNavigateToLogin: () -> Unit = {},
    onOpenDrawer: () -> Unit = {},
    onNavigateToUser: (String) -> Unit = {},
    onNavigateToReply: (String) -> Unit = {},
    onNavigateToThread: (String) -> Unit = {}
) {
    val viewModel: TimelineViewModel = viewModel { TimelineViewModel(sessionManager) }
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Personal", "Local", "Beyond")
    val context = LocalContext.current
    
    // Image viewer state
    var showImageViewer by remember { mutableStateOf(false) }
    var imageViewerAttachments by remember { mutableStateOf<List<Attachment>>(emptyList()) }
    var imageViewerInitialIndex by remember { mutableStateOf(0) }
    
    // Check if user is logged in
    val isLoggedIn = sessionManager.getCurrentSession() != null
    
    // Handle tab changes - load appropriate timeline
    LaunchedEffect(selectedTab) {
        val timelineType = when (selectedTab) {
            0 -> TimelineType.HOME
            1 -> TimelineType.LOCAL
            2 -> TimelineType.FEDERATED
            else -> TimelineType.HOME
        }
        viewModel.loadTimeline(timelineType)
    }

    Scaffold(
        topBar = {
            Column {
                // Kabinka Top Bar with instance name
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "Kabinka",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "mastodon.social",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    navigationIcon = {
                        Box(
                            modifier = Modifier
                                .padding(start = 16.dp)
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "🧡",
                                style = MaterialTheme.typography.titleSmall
                            )
                        }
                    },
                    actions = {
                        // Menu button to open drawer
                        IconButton(onClick = onOpenDrawer) {
                            Icon(
                                imageVector = LineAwesomeIcons.EllipsisVSolid,
                                contentDescription = "Menu",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
                
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            icon = {
                                Icon(
                                    imageVector = when (index) {
                                        0 -> TimelineRemixIcons.home(selectedTab == index)
                                        1 -> TimelineRemixIcons.local(selectedTab == index)
                                        2 -> TimelineRemixIcons.federated(selectedTab == index)
                                        else -> TimelineRemixIcons.home(selectedTab == index)
                                    },
                                    contentDescription = title
                                )
                            },
                            text = { 
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(title)
                                    // Subtle indicator of timeline scope
                                    if (selectedTab == index) {
                                        Text(
                                            text = when (index) {
                                                0 -> "Your feed"
                                                1 -> "This instance"
                                                2 -> "All instances"
                                                else -> ""
                                            },
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val state = uiState) {
                is TimelineUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
                
                is TimelineUiState.Content -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.statuses) { status ->
                            app.kabinka.frontend.components.timeline.StatusCardComplete(
                                status = status,
                                onStatusClick = { onNavigateToThread(status.id) },
                                onProfileClick = onNavigateToUser,
                                onReply = { statusId -> onNavigateToReply(statusId) },
                                onBoost = { statusId -> viewModel.toggleReblog(statusId) },
                                onFavorite = { statusId -> viewModel.toggleFavorite(statusId) },
                                onBookmark = { statusId -> viewModel.toggleBookmark(statusId) },
                                onMore = { /* TODO: Show more options */ },
                                onLinkClick = { url ->
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        android.util.Log.e("HomeTimeline", "Failed to open URL: $url", e)
                                    }
                                },
                                onHashtagClick = { tag ->
                                    // TODO: Navigate to hashtag timeline
                                    android.util.Log.d("HomeTimeline", "Hashtag clicked: $tag")
                                },
                                onMentionClick = { userId ->
                                    onNavigateToUser(userId)
                                },
                                onMediaClick = { index ->
                                    val displayedStatus = status.reblog ?: status
                                    if (!displayedStatus.mediaAttachments.isNullOrEmpty()) {
                                        imageViewerAttachments = displayedStatus.mediaAttachments
                                        imageViewerInitialIndex = index
                                        showImageViewer = true
                                    }
                                }
                            )
                        }
                    }
                }
                
                is TimelineUiState.Empty -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Icon(
                                imageVector = when (selectedTab) {
                                    0 -> LineAwesomeIcons.HomeSolid
                                    1 -> LineAwesomeIcons.MapMarkerSolid
                                    2 -> LineAwesomeIcons.GlobeSolid
                                    else -> LineAwesomeIcons.InfoCircleSolid
                                },
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = state.message,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            if (state.isLoginRequired) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(onClick = onNavigateToLogin) {
                                    Text("Log In")
                                }
                            }
                        }
                    }
                }
                
                is TimelineUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            imageVector = LineAwesomeIcons.ExclamationTriangleSolid,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "Error loading timeline",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Button(
                            onClick = { viewModel.refresh() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(
                                imageVector = LineAwesomeIcons.SyncSolid,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Retry")
                        }
                    }
                }
            }
            }
        }
    }
    
    // Image viewer dialog
    if (showImageViewer && imageViewerAttachments.isNotEmpty()) {
        ImageViewerDialog(
            attachments = imageViewerAttachments,
            initialIndex = imageViewerInitialIndex,
            onDismiss = { showImageViewer = false }
        )
    }
}

@Composable
private fun ModernStatusCard(status: Status) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* TODO: Open status detail */ },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Author header
            Row(
                modifier = Modifier.fillMaxWidth(),
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
                        text = status.account.displayName.firstOrNull()?.toString() ?: "?",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = status.account.displayName,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "@${status.account.acct} · ${formatTimeAgo(status.createdAt.toString())}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = { /* TODO: More options */ }) {
                    Icon(
                        imageVector = LineAwesomeIcons.EllipsisVSolid,
                        contentDescription = "More options",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Content warning
            if (!status.spoilerText.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = LineAwesomeIcons.ExclamationTriangleSolid,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "CW: ${status.spoilerText}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            // Content
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = status.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Action buttons
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatusActionButton(
                    icon = LineAwesomeIcons.PhoneSolid,
                    count = status.repliesCount.toInt(),
                    onClick = { /* TODO: Reply */ }
                )

                StatusActionButton(
                    icon = LineAwesomeIcons.SyncSolid,
                    count = status.reblogsCount.toInt(),
                    isActive = status.reblogged,
                    onClick = { /* TODO: Boost */ }
                )

                StatusActionButton(
                    icon = if (status.favourited) LineAwesomeIcons.HeartSolid else LineAwesomeIcons.HeartSolid,
                    count = status.favouritesCount.toInt(),
                    isActive = status.favourited,
                    onClick = { /* TODO: Favorite */ }
                )

                IconButton(onClick = { /* TODO: Share */ }) {
                    Icon(
                        imageVector = LineAwesomeIcons.ShareSolid,
                        contentDescription = "Share",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    count: Int,
    isActive: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onClick) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (count > 0) {
            Text(
                text = formatCount(count),
                style = MaterialTheme.typography.bodySmall,
                color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun formatCount(count: Int): String {
    return when {
        count >= 1000000 -> "${count / 1000000}M"
        count >= 1000 -> "${count / 1000}K"
        else -> count.toString()
    }
}

private fun formatTimeAgo(createdAt: String): String {
    return try {
        val instant = Instant.parse(createdAt)
        val now = Instant.now()
        val duration = Duration.between(instant, now)
        
        when {
            duration.seconds < 60 -> "${duration.seconds}s"
            duration.toMinutes() < 60 -> "${duration.toMinutes()}m"
            duration.toHours() < 24 -> "${duration.toHours()}h"
            duration.toDays() < 7 -> "${duration.toDays()}d"
            else -> "${duration.toDays() / 7}w"
        }
    } catch (e: Exception) {
        "now"
    }
}
