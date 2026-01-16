package app.kabinka.frontend.screens.explore

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.kabinka.social.api.requests.trends.GetTrendingStatuses
import app.kabinka.social.api.session.AccountSessionManager
import app.kabinka.social.model.Status
import app.kabinka.social.model.Attachment
import me.grishka.appkit.api.Callback
import me.grishka.appkit.api.ErrorResponse
import app.kabinka.frontend.components.timeline.StatusCardComplete
import app.kabinka.frontend.components.ImageViewerDialog
import androidx.lifecycle.viewmodel.compose.viewModel
import app.kabinka.frontend.timeline.TimelineViewModel
import app.kabinka.frontend.timeline.TimelineType
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import app.kabinka.frontend.auth.SessionStateManager

@Composable
fun PostsTab(
    isLoggedIn: Boolean, 
    onNavigateToUser: (String) -> Unit,
    onNavigateToReply: (String) -> Unit = {},
    sessionManager: SessionStateManager
) {
    var posts by remember { mutableStateOf<List<Status>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    
    // Image viewer state
    var showImageViewer by remember { mutableStateOf(false) }
    var imageViewerAttachments by remember { mutableStateOf<List<Attachment>>(emptyList()) }
    var imageViewerInitialIndex by remember { mutableStateOf(0) }
    
    // Get view model for post interactions
    val timelineViewModel: TimelineViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return TimelineViewModel(sessionManager, TimelineType.FEDERATED) as T
            }
        }
    )
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        isLoading = true
        error = null
        
        val request = GetTrendingStatuses(0, 20)
        
        if (isLoggedIn) {
            val accountId = AccountSessionManager.getInstance().lastActiveAccountID
            if (accountId != null) {
                request.setCallback(object : Callback<List<Status>> {
                    override fun onSuccess(result: List<Status>) {
                        posts = result
                        isLoading = false
                    }
                    override fun onError(errorResponse: ErrorResponse) {
                        error = "Failed to load"
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
                    error = "Failed to load"
                    isLoading = false
                }
            }).execNoAuth("mastodon.social")
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            error != null -> Text(error!!, modifier = Modifier.align(Alignment.Center))
            posts.isEmpty() -> Text("No posts", modifier = Modifier.align(Alignment.Center))
            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(vertical = 8.dp, horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(posts) { status ->
                        StatusCardComplete(
                            status = status,
                            onStatusClick = { /* TODO: Navigate to status detail */ },
                            onProfileClick = onNavigateToUser,
                            onReply = { statusId -> onNavigateToReply(statusId) },
                            onBoost = { statusId -> timelineViewModel.toggleReblog(statusId) },
                            onFavorite = { statusId -> timelineViewModel.toggleFavorite(statusId) },
                            onBookmark = { statusId -> timelineViewModel.toggleBookmark(statusId) },
                            onMore = { /* TODO: Show more options */ },
                            onLinkClick = { url ->
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    android.util.Log.e("PostsTab", "Failed to open URL: $url", e)
                                }
                            },
                            onHashtagClick = { tag ->
                                android.util.Log.d("PostsTab", "Hashtag clicked: $tag")
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
