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
import app.kabinka.social.api.session.AccountSessionManager
import app.kabinka.social.model.Status
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
