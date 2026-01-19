package app.kabinka.frontend.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.kabinka.social.api.requests.tags.GetFollowedTags
import app.kabinka.social.api.requests.tags.SetTagFollowed
import app.kabinka.social.api.session.AccountSessionManager
import app.kabinka.social.model.Hashtag
import app.kabinka.social.model.HeaderPaginationList
import me.grishka.appkit.api.Callback
import me.grishka.appkit.api.ErrorResponse
import compose.icons.LineAwesomeIcons
import compose.icons.lineawesomeicons.HashtagSolid

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageHashtagsScreen(
    onNavigateBack: () -> Unit
) {
    var hashtags by remember { mutableStateOf<List<Hashtag>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var hashtagToDelete by remember { mutableStateOf<Hashtag?>(null) }
    
    fun loadHashtags() {
        val session = AccountSessionManager.getInstance().lastActiveAccount
        if (session != null) {
            isLoading = true
            GetFollowedTags(null, 100)
                .setCallback(object : Callback<HeaderPaginationList<Hashtag>> {
                    override fun onSuccess(result: HeaderPaginationList<Hashtag>) {
                        hashtags = result
                        isLoading = false
                    }
                    
                    override fun onError(error: ErrorResponse?) {
                        errorMessage = error?.toString() ?: "Failed to load followed hashtags"
                        isLoading = false
                    }
                })
                .exec(session.getID())
        }
    }
    
    LaunchedEffect(Unit) {
        loadHashtags()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage hashtags") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                
                errorMessage != null -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = errorMessage ?: "Unknown error",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                
                hashtags.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = LineAwesomeIcons.HashtagSolid,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No followed hashtags",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(hashtags) { hashtag ->
                            ManageHashtagItem(
                                hashtag = hashtag,
                                onUnfollow = { hashtagToDelete = hashtag }
                            )
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
    
    // Unfollow confirmation dialog
    hashtagToDelete?.let { hashtag ->
        AlertDialog(
            onDismissRequest = { hashtagToDelete = null },
            title = { Text("Unfollow hashtag?") },
            text = { Text("Do you want to stop following #${hashtag.name}?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val session = AccountSessionManager.getInstance().lastActiveAccount
                        if (session != null) {
                            SetTagFollowed(hashtag.name, false)
                                .setCallback(object : Callback<Hashtag> {
                                    override fun onSuccess(result: Hashtag) {
                                        hashtags = hashtags.filter { it.name != hashtag.name }
                                        hashtagToDelete = null
                                    }
                                    
                                    override fun onError(error: ErrorResponse?) {
                                        errorMessage = error?.toString() ?: "Failed to unfollow hashtag"
                                        hashtagToDelete = null
                                    }
                                })
                                .exec(session.getID())
                        }
                    }
                ) {
                    Text("Unfollow")
                }
            },
            dismissButton = {
                TextButton(onClick = { hashtagToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ManageHashtagItem(
    hashtag: Hashtag,
    onUnfollow: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = LineAwesomeIcons.HashtagSolid,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "#${hashtag.name}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                if (hashtag.history?.isNotEmpty() == true) {
                    val totalUses = hashtag.history.sumOf { it.uses }
                    Text(
                        text = "$totalUses uses this week",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            IconButton(onClick = onUnfollow) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Unfollow",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
