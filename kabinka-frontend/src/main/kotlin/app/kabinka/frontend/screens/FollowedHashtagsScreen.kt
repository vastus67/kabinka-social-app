package app.kabinka.frontend.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.kabinka.social.api.requests.tags.GetFollowedTags
import app.kabinka.social.api.session.AccountSessionManager
import app.kabinka.social.model.Hashtag
import app.kabinka.social.model.HeaderPaginationList
import me.grishka.appkit.api.Callback
import me.grishka.appkit.api.ErrorResponse
import compose.icons.LineAwesomeIcons
import compose.icons.lineawesomeicons.HashtagSolid

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FollowedHashtagsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToManageHashtags: () -> Unit,
    onNavigateToHashtag: (String) -> Unit
) {
    var hashtags by remember { mutableStateOf<List<Hashtag>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(Unit) {
        val session = AccountSessionManager.getInstance().lastActiveAccount
        if (session != null) {
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
        } else {
            errorMessage = "Not logged in"
            isLoading = false
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Followed hashtags") },
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
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "You're not following any hashtags yet",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(onClick = onNavigateToManageHashtags) {
                            Text("Manage hashtags")
                        }
                    }
                }
                
                else -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Manage button at top
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.surface,
                            tonalElevation = 1.dp
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.End
                            ) {
                                OutlinedButton(onClick = onNavigateToManageHashtags) {
                                    Text("Manage hashtags")
                                }
                            }
                        }
                        
                        // Hashtags list
                        LazyColumn(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(hashtags) { hashtag ->
                                HashtagItem(
                                    hashtag = hashtag,
                                    onClick = { onNavigateToHashtag(hashtag.name) }
                                )
                                HorizontalDivider()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HashtagItem(
    hashtag: Hashtag,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
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
        }
    }
}
