package app.kabinka.frontend.onboarding.ui

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
import app.kabinka.social.api.requests.accounts.SetAccountFollowed
import app.kabinka.social.model.Account
import app.kabinka.social.model.Relationship
import compose.icons.LineAwesomeIcons
import compose.icons.lineawesomeicons.UserPlusSolid
import compose.icons.lineawesomeicons.UserCheckSolid
import kotlinx.coroutines.launch
import me.grishka.appkit.api.Callback
import me.grishka.appkit.api.ErrorResponse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedPersonalizationScreen(
    accountId: String,
    onContinue: () -> Unit,
    onSkip: () -> Unit
) {
    var isLoading by remember { mutableStateOf(true) }
    var suggestedAccounts by remember { mutableStateOf<List<Account>>(emptyList()) }
    var followedAccounts by remember { mutableStateOf<Set<String>>(emptySet()) }
    val scope = rememberCoroutineScope()
    
    // Load suggested accounts
    LaunchedEffect(Unit) {
        // In a real implementation, this would fetch suggestions from the API
        // For now, we'll just set loading to false
        isLoading = false
    }
    
    fun toggleFollow(account: Account) {
        scope.launch {
            if (followedAccounts.contains(account.id)) {
                // Unfollow
                SetAccountFollowed(account.id, false, true, false)
                    .setCallback(object : Callback<Relationship> {
                        override fun onSuccess(result: Relationship) {
                            followedAccounts = followedAccounts - account.id
                        }
                        override fun onError(error: ErrorResponse) {
                            // Handle error
                        }
                    })
            } else {
                // Follow
                SetAccountFollowed(account.id, true, true, false)
                    .setCallback(object : Callback<Relationship> {
                        override fun onSuccess(result: Relationship) {
                            followedAccounts = followedAccounts + account.id
                        }
                        override fun onError(error: ErrorResponse) {
                            // Handle error
                        }
                    })
            }
        }
    }
    
    fun followAll() {
        suggestedAccounts.forEach { account ->
            if (!followedAccounts.contains(account.id)) {
                scope.launch {
                    SetAccountFollowed(account.id, true, true, false)
                        .setCallback(object : Callback<Relationship> {
                            override fun onSuccess(result: Relationship) {
                                followedAccounts = followedAccounts + account.id
                            }
                            override fun onError(error: ErrorResponse) {}
                        })
                }
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Personalize your feed") },
                navigationIcon = {
                    IconButton(onClick = onSkip) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    TextButton(onClick = onSkip) {
                        Text("Skip")
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                shadowElevation = 8.dp,
                tonalElevation = 3.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    if (suggestedAccounts.isNotEmpty()) {
                        OutlinedButton(
                            onClick = { followAll() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(LineAwesomeIcons.UserPlusSolid, null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Follow all")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    
                    Button(
                        onClick = onContinue,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Continue")
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
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (suggestedAccounts.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = LineAwesomeIcons.UserPlusSolid,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Start exploring",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Your feed is ready! Start following people you're interested in to see their posts.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    item {
                        Text(
                            text = "Suggested accounts",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Follow some accounts to get started. You can always change this later.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                    
                    items(suggestedAccounts) { account ->
                        AccountSuggestionCard(
                            account = account,
                            isFollowing = followedAccounts.contains(account.id),
                            onToggleFollow = { toggleFollow(account) }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun AccountSuggestionCard(
    account: Account,
    isFollowing: Boolean,
    onToggleFollow: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = account.displayName.ifBlank { account.username },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "@${account.acct}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (!account.note.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = account.note!!,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Button(
                onClick = onToggleFollow,
                colors = if (isFollowing) {
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                } else {
                    ButtonDefaults.buttonColors()
                }
            ) {
                Icon(
                    imageVector = if (isFollowing) LineAwesomeIcons.UserCheckSolid else LineAwesomeIcons.UserPlusSolid,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (isFollowing) "Following" else "Follow")
            }
        }
    }
}
