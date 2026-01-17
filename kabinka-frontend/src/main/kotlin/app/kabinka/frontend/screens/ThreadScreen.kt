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
import app.kabinka.social.api.requests.statuses.GetStatusByID
import app.kabinka.social.api.requests.statuses.GetStatusContext
import app.kabinka.social.api.session.AccountSessionManager
import app.kabinka.social.model.Status
import app.kabinka.social.model.StatusContext
import app.kabinka.social.model.Attachment
import app.kabinka.frontend.components.ImageViewerDialog
import me.grishka.appkit.api.Callback
import me.grishka.appkit.api.ErrorResponse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThreadScreen(
    statusId: String,
    onNavigateBack: () -> Unit,
    onNavigateToUser: (String) -> Unit = {},
    onNavigateToReply: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionStateManager(context) }
    val timelineViewModel: TimelineViewModel = viewModel { TimelineViewModel(sessionManager) }
    
    var mainStatus by remember { mutableStateOf<Status?>(null) }
    var ancestors by remember { mutableStateOf<List<Status>>(emptyList()) }
    var descendants by remember { mutableStateOf<List<Status>>(emptyList()) }
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

    LaunchedEffect(statusId) {
        isLoading = true
        error = null
        
        if (isLoggedIn) {
            val accountId = AccountSessionManager.getInstance().lastActiveAccountID
            if (accountId != null) {
                // Load main status
                val statusRequest = GetStatusByID(statusId)
                statusRequest.setCallback(object : Callback<Status> {
                    override fun onSuccess(result: Status) {
                        mainStatus = result
                        
                        // Load context (ancestors and descendants)
                        val contextRequest = GetStatusContext(statusId)
                        contextRequest.setCallback(object : Callback<StatusContext> {
                            override fun onSuccess(context: StatusContext) {
                                ancestors = context.ancestors ?: emptyList()
                                descendants = context.descendants ?: emptyList()
                                isLoading = false
                            }
                            override fun onError(errorResponse: ErrorResponse) {
                                isLoading = false
                            }
                        }).exec(accountId)
                    }
                    override fun onError(errorResponse: ErrorResponse) {
                        error = "Failed to load status"
                        isLoading = false
                    }
                }).exec(accountId)
            } else {
                error = "Not logged in"
                isLoading = false
            }
        } else {
            // For non-logged in users
            val statusRequest = GetStatusByID(statusId)
            statusRequest.setCallback(object : Callback<Status> {
                override fun onSuccess(result: Status) {
                    mainStatus = result
                    
                    val contextRequest = GetStatusContext(statusId)
                    contextRequest.setCallback(object : Callback<StatusContext> {
                        override fun onSuccess(context: StatusContext) {
                            ancestors = context.ancestors ?: emptyList()
                            descendants = context.descendants ?: emptyList()
                            isLoading = false
                        }
                        override fun onError(errorResponse: ErrorResponse) {
                            isLoading = false
                        }
                    }).execNoAuth("mastodon.social")
                }
                override fun onError(errorResponse: ErrorResponse) {
                    error = "Failed to load status"
                    isLoading = false
                }
            }).execNoAuth("mastodon.social")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thread") },
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
                mainStatus == null -> Text(
                    "Status not found",
                    modifier = Modifier.align(Alignment.Center)
                )
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(vertical = 8.dp, horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Ancestors (parent posts in the thread)
                        if (ancestors.isNotEmpty()) {
                            item {
                                Text(
                                    "Thread context",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            }
                            items(ancestors) { status ->
                                app.kabinka.frontend.components.timeline.StatusCardComplete(
                                    status = status,
                                    onStatusClick = { /* Already in thread */ },
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
                                            android.util.Log.e("Thread", "Failed to open URL: $url", e)
                                        }
                                    },
                                    onHashtagClick = { tag ->
                                        android.util.Log.d("Thread", "Hashtag clicked: $tag")
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
                        
                        // Main status (highlighted)
                        item {
                            if (ancestors.isNotEmpty()) {
                                Text(
                                    "Original post",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                )
                            ) {
                                app.kabinka.frontend.components.timeline.StatusCardComplete(
                                    status = mainStatus!!,
                                    onStatusClick = { /* Already viewing */ },
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
                                            android.util.Log.e("Thread", "Failed to open URL: $url", e)
                                        }
                                    },
                                    onHashtagClick = { tag ->
                                        android.util.Log.d("Thread", "Hashtag clicked: $tag")
                                    },
                                    onMentionClick = { userId ->
                                        onNavigateToUser(userId)
                                    },
                                    onMediaClick = { index ->
                                        val displayedStatus = mainStatus?.reblog ?: mainStatus
                                        if (displayedStatus?.mediaAttachments?.isNotEmpty() == true) {
                                            imageViewerAttachments = displayedStatus.mediaAttachments
                                            imageViewerInitialIndex = index
                                            showImageViewer = true
                                        }
                                    }
                                )
                            }
                        }
                        
                        // Descendants (replies)
                        if (descendants.isNotEmpty()) {
                            item {
                                Text(
                                    "${descendants.size} ${if (descendants.size == 1) "reply" else "replies"}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                            items(descendants) { status ->
                                app.kabinka.frontend.components.timeline.StatusCardComplete(
                                    status = status,
                                    onStatusClick = { /* Could navigate to nested thread */ },
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
                                            android.util.Log.e("Thread", "Failed to open URL: $url", e)
                                        }
                                    },
                                    onHashtagClick = { tag ->
                                        android.util.Log.d("Thread", "Hashtag clicked: $tag")
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
