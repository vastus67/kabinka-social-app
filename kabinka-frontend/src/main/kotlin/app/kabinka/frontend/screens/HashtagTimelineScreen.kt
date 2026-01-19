package app.kabinka.frontend.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.viewmodel.compose.viewModel
import app.kabinka.frontend.auth.SessionStateManager
import app.kabinka.frontend.timeline.TimelineViewModel
import app.kabinka.social.api.requests.timelines.GetHashtagTimeline
import app.kabinka.social.api.requests.tags.GetTag
import app.kabinka.social.api.requests.tags.SetTagFollowed
import app.kabinka.social.api.session.AccountSessionManager
import app.kabinka.social.model.Status
import app.kabinka.social.model.Hashtag
import app.kabinka.social.model.Attachment
import app.kabinka.frontend.components.ImageViewerDialog
import me.grishka.appkit.api.Callback
import me.grishka.appkit.api.ErrorResponse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HashtagTimelineScreen(
    hashtag: String,
    onNavigateBack: () -> Unit,
    onNavigateToUser: (String) -> Unit = {},
    onNavigateToReply: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionStateManager(context) }
    val timelineViewModel: TimelineViewModel = viewModel { TimelineViewModel(sessionManager) }
    
    var posts by remember { mutableStateOf<List<Status>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    
    // Hashtag info state
    var hashtagInfo by remember { mutableStateOf<Hashtag?>(null) }
    var isFollowing by remember { mutableStateOf(false) }
    var isFollowLoading by remember { mutableStateOf(false) }
    
    // Image viewer state
    var showImageViewer by remember { mutableStateOf(false) }
    var imageViewerAttachments by remember { mutableStateOf<List<Attachment>>(emptyList()) }
    var imageViewerInitialIndex by remember { mutableStateOf(0) }
    
    // Check if user is logged in
    val isLoggedIn = remember {
        try {
            AccountSessionManager.getInstance().lastActiveAccount != null
        } catch (e: Exception) {
            false
        }
    }

    LaunchedEffect(hashtag) {
        isLoading = true
        error = null
        
        // Load hashtag info
        if (isLoggedIn) {
            val accountId = AccountSessionManager.getInstance().lastActiveAccountID
            if (accountId != null) {
                GetTag(hashtag).setCallback(object : Callback<Hashtag> {
                    override fun onSuccess(result: Hashtag) {
                        hashtagInfo = result
                        isFollowing = result.following
                    }
                    override fun onError(errorResponse: ErrorResponse) {
                        // Ignore error, just don't show stats
                    }
                }).exec(accountId)
            }
        }
        
        val request = GetHashtagTimeline(hashtag, null, null, 40)
        
        if (isLoggedIn) {
            val accountId = AccountSessionManager.getInstance().lastActiveAccountID
            if (accountId != null) {
                request.setCallback(object : Callback<List<Status>> {
                    override fun onSuccess(result: List<Status>) {
                        posts = result
                        isLoading = false
                    }
                    override fun onError(errorResponse: ErrorResponse) {
                        error = "Failed to load posts"
                        isLoading = false
                    }
                }).exec(accountId)
            } else {
                isLoading = false
            }
        } else {
            request.setCallback(object : Callback<List<Status>> {
                override fun onSuccess(result: List<Status>) {
                    posts = result
                    isLoading = false
                }
                override fun onError(errorResponse: ErrorResponse) {
                    error = "Failed to load posts"
                    isLoading = false
                }
            }).execNoAuth("mastodon.social")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(
                            text = "#$hashtag",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                isLoading -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
                error != null -> Text(
                    error!!,
                    modifier = Modifier.align(Alignment.Center)
                )
                posts.isEmpty() -> Text(
                    "No posts found for #$hashtag",
                    modifier = Modifier.align(Alignment.Center)
                )
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(vertical = 8.dp, horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Hashtag header with stats and follow button
                        if (hashtagInfo != null && isLoggedIn) {
                            item {
                                HashtagHeader(
                                    hashtag = hashtagInfo!!,
                                    isFollowing = isFollowing,
                                    isFollowLoading = isFollowLoading,
                                    onFollowClick = {
                                        isFollowLoading = true
                                        val accountId = AccountSessionManager.getInstance().lastActiveAccountID
                                        if (accountId != null) {
                                            SetTagFollowed(hashtag, !isFollowing)
                                                .setCallback(object : Callback<Hashtag> {
                                                    override fun onSuccess(result: Hashtag) {
                                                        isFollowing = result.following
                                                        hashtagInfo = result
                                                        isFollowLoading = false
                                                    }
                                                    override fun onError(errorResponse: ErrorResponse) {
                                                        isFollowLoading = false
                                                    }
                                                })
                                                .exec(accountId)
                                        }
                                    }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                        
                        items(posts) { status ->
                            app.kabinka.frontend.components.timeline.StatusCardComplete(
                                status = status,
                                onStatusClick = { /* TODO: Navigate to status detail */ },
                                onProfileClick = onNavigateToUser,
                                onReply = { statusId -> onNavigateToReply(statusId) },
                                onBoost = { statusId -> timelineViewModel.toggleReblog(statusId) },
                                onFavorite = { statusId -> timelineViewModel.toggleFavorite(statusId) },
                                onBookmark = { statusId -> timelineViewModel.toggleBookmark(statusId) },
                                onMore = { /* Handled internally */ },
                                onLinkClick = { url ->
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        android.util.Log.e("HashtagTimeline", "Failed to open URL: $url", e)
                                    }
                                },
                                onHashtagClick = { tag ->
                                    android.util.Log.d("HashtagTimeline", "Hashtag clicked: $tag")
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
            }
            
            // Image viewer dialog
            if (showImageViewer) {
                ImageViewerDialog(
                    attachments = imageViewerAttachments,
                    initialIndex = imageViewerInitialIndex,
                    onDismiss = { showImageViewer = false }
                )
            }
        }
    }
}

@Composable
fun HashtagHeader(
    hashtag: Hashtag,
    isFollowing: Boolean,
    isFollowLoading: Boolean,
    onFollowClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Title and Follow button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "#${hashtag.name}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
                
                if (isFollowing) {
                    OutlinedButton(
                        onClick = onFollowClick,
                        enabled = !isFollowLoading
                    ) {
                        if (isFollowLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Unfollow")
                        }
                    }
                } else {
                    Button(
                        onClick = onFollowClick,
                        enabled = !isFollowLoading
                    ) {
                        if (isFollowLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text("Follow")
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Total posts
                StatItem(
                    label = "Posts",
                    value = formatNumber(getTotalPosts(hashtag))
                )
                
                // Participants (accounts)
                StatItem(
                    label = "Participants",
                    value = formatNumber(getTotalParticipants(hashtag))
                )
                
                // Posts today
                StatItem(
                    label = "Today",
                    value = formatNumber(getPostsToday(hashtag))
                )
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

fun getTotalPosts(hashtag: Hashtag): Int {
    return hashtag.history?.sumOf { it.uses } ?: 0
}

fun getTotalParticipants(hashtag: Hashtag): Int {
    return hashtag.history?.sumOf { it.accounts } ?: 0
}

fun getPostsToday(hashtag: Hashtag): Int {
    // The most recent history entry is today
    return hashtag.history?.firstOrNull()?.uses ?: 0
}

fun formatNumber(number: Int): String {
    return when {
        number >= 1000000 -> String.format("%.1fM", number / 1000000.0)
        number >= 1000 -> String.format("%.1fK", number / 1000.0)
        else -> number.toString()
    }
}
