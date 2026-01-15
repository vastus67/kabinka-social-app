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
import androidx.lifecycle.viewmodel.compose.viewModel
import android.content.Intent
import android.net.Uri
import app.kabinka.frontend.auth.SessionStateManager
import app.kabinka.frontend.timeline.TimelineUiState
import app.kabinka.frontend.timeline.TimelineViewModel
import app.kabinka.frontend.timeline.TimelineType
import app.kabinka.frontend.components.timeline.StatusCardComplete
import app.kabinka.social.model.Attachment
import app.kabinka.frontend.components.ImageViewerDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToUser: (String) -> Unit = {},
    onNavigateToReply: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionStateManager(context) }
    val viewModel: TimelineViewModel = viewModel { 
        TimelineViewModel(sessionManager, TimelineType.FAVORITES) 
    }
    val uiState by viewModel.uiState.collectAsState()
    
    // Image viewer state
    var showImageViewer by remember { mutableStateOf(false) }
    var imageViewerAttachments by remember { mutableStateOf<List<Attachment>>(emptyList()) }
    var imageViewerInitialIndex by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Favorites",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is TimelineUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is TimelineUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Error loading favorites",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadTimeline(TimelineType.FAVORITES) }) {
                            Text("Retry")
                        }
                    }
                }
                is TimelineUiState.Content -> {
                    if (state.statuses.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "No favorites yet",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Favorited posts will appear here",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(
                                items = state.statuses,
                                key = { it.id }
                            ) { status ->
                                StatusCardComplete(
                                    status = status,
                                    onStatusClick = { /* TODO: Navigate to status detail */ },
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
                                            android.util.Log.e("Favorites", "Failed to open URL: $url", e)
                                        }
                                    },
                                    onHashtagClick = { tag ->
                                        android.util.Log.d("Favorites", "Hashtag clicked: $tag")
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
                                HorizontalDivider()
                            }
                        }
                    }
                }
                is TimelineUiState.Empty -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
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
