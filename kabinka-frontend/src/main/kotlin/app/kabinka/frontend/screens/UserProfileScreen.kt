package app.kabinka.frontend.screens

import android.net.Uri
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.kabinka.social.api.requests.accounts.GetAccountByID
import app.kabinka.social.api.requests.accounts.GetAccountStatuses
import app.kabinka.social.api.requests.accounts.GetAccountRelationships
import app.kabinka.social.api.requests.accounts.SetAccountFollowed
import app.kabinka.social.api.session.AccountSessionManager
import app.kabinka.social.model.Account
import app.kabinka.social.model.Relationship
import app.kabinka.social.model.Status
import coil.compose.AsyncImage
import compose.icons.LineAwesomeIcons
import compose.icons.lineawesomeicons.*
import me.grishka.appkit.api.Callback
import me.grishka.appkit.api.ErrorResponse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    userId: String,
    onNavigateBack: () -> Unit = {},
    onNavigateToUser: (String) -> Unit = {}
) {
    var account by remember { mutableStateOf<Account?>(null) }
    var relationship by remember { mutableStateOf<Relationship?>(null) }
    var statuses by remember { mutableStateOf<List<Status>>(emptyList()) }
    var pinnedStatuses by remember { mutableStateOf<List<Status>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var selectedTab by remember { mutableStateOf(0) }
    var selectedFilter by remember { mutableStateOf(0) }
    var refreshTrigger by remember { mutableStateOf(0) }
    var isFollowing by remember { mutableStateOf(false) }
    var isFollowLoading by remember { mutableStateOf(false) }
    
    val tabs = listOf("Featured", "Timeline", "About")
    val filters = listOf("Posts", "Replies", "Media")
    
    // Check if user is logged in
    val isLoggedIn = remember {
        try {
            AccountSessionManager.getInstance().lastActiveAccount != null
        } catch (e: Exception) {
            false
        }
    }
    
    // Load user account and relationship
    LaunchedEffect(userId, selectedFilter, refreshTrigger) {
        if (isLoggedIn) {
            isLoading = true
            val accountId = AccountSessionManager.getInstance().lastActiveAccountID
            if (accountId != null) {
                // Get user account
                val accountRequest = GetAccountByID(userId)
                accountRequest.setCallback(object : Callback<Account> {
                    override fun onSuccess(result: Account) {
                        account = result
                        
                        // Load pinned posts for Featured tab
                        val pinnedRequest = GetAccountStatuses(result.id, null, null, 20, GetAccountStatuses.Filter.PINNED, null)
                        pinnedRequest.setCallback(object : Callback<List<Status>> {
                            override fun onSuccess(pinnedResult: List<Status>) {
                                pinnedStatuses = pinnedResult
                            }
                            override fun onError(errorResponse: ErrorResponse) {
                                // Pinned posts are optional, don't show error
                            }
                        }).exec(accountId)
                        
                        // Get relationship
                        val relationshipRequest = GetAccountRelationships(listOf(userId))
                        relationshipRequest.setCallback(object : Callback<List<Relationship>> {
                            override fun onSuccess(relationships: List<Relationship>) {
                                if (relationships.isNotEmpty()) {
                                    relationship = relationships[0]
                                    isFollowing = relationships[0].following
                                }
                            }
                            override fun onError(errorResponse: ErrorResponse) {
                                // Relationship fetch failed, continue without it
                            }
                        }).exec(accountId)
                        
                        // Load account statuses based on filter
                        val statusFilter = when (selectedFilter) {
                            0 -> GetAccountStatuses.Filter.DEFAULT
                            1 -> GetAccountStatuses.Filter.INCLUDE_REPLIES
                            2 -> GetAccountStatuses.Filter.MEDIA
                            else -> GetAccountStatuses.Filter.DEFAULT
                        }
                        
                        val statusRequest = GetAccountStatuses(result.id, null, null, 20, statusFilter, null)
                        statusRequest.setCallback(object : Callback<List<Status>> {
                            override fun onSuccess(statusResult: List<Status>) {
                                statuses = statusResult
                                isLoading = false
                            }
                            override fun onError(errorResponse: ErrorResponse) {
                                error = "Failed to load posts"
                                isLoading = false
                            }
                        }).exec(accountId)
                    }
                    override fun onError(errorResponse: ErrorResponse) {
                        error = "Failed to load profile"
                        isLoading = false
                    }
                }).exec(accountId)
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = account?.displayName ?: "Profile",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = LineAwesomeIcons.ArrowLeftSolid,
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
    ) { padding ->
        if (isLoading && account == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (error != null && account == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = LineAwesomeIcons.ExclamationTriangleSolid,
                        contentDescription = "Error",
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = error ?: "Unknown error",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                    Button(onClick = { refreshTrigger++ }) {
                        Text("Retry")
                    }
                }
            }
        } else if (account != null) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Profile Header
                item {
                    UserProfileHeader(
                        account = account!!,
                        postsCount = statuses.size,
                        relationship = relationship,
                        isFollowing = isFollowing,
                        isFollowLoading = isFollowLoading,
                        onFollowClick = {
                            isFollowLoading = true
                            val accountId = AccountSessionManager.getInstance().lastActiveAccountID
                            if (accountId != null) {
                                val followRequest = SetAccountFollowed(userId, !isFollowing, true, false)
                                followRequest.setCallback(object : Callback<Relationship> {
                                    override fun onSuccess(result: Relationship) {
                                        relationship = result
                                        isFollowing = result.following
                                        isFollowLoading = false
                                    }
                                    override fun onError(errorResponse: ErrorResponse) {
                                        isFollowLoading = false
                                    }
                                }).exec(accountId)
                            }
                        },
                        onRefresh = { refreshTrigger++ }
                    )
                }
                
                // Tabs
                item {
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.primary
                    ) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTab == index,
                                onClick = { selectedTab = index },
                                text = { Text(title) }
                            )
                        }
                    }
                }
                
                // Filter chips (only for Timeline tab)
                if (selectedTab == 1) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            filters.forEachIndexed { index, filter ->
                                FilterChip(
                                    selected = selectedFilter == index,
                                    onClick = { selectedFilter = index },
                                    label = { Text(filter) }
                                )
                            }
                        }
                    }
                }
                
                // Tab content
                when (selectedTab) {
                    0 -> {
                        // Featured - show pinned posts
                        if (pinnedStatuses.isEmpty() && !isLoading) {
                            item {
                                EmptyContentCard(
                                    icon = LineAwesomeIcons.StarSolid,
                                    title = "No featured posts",
                                    message = "This user hasn't pinned any posts"
                                )
                            }
                        } else {
                            items(pinnedStatuses) { status ->
                                StatusCard(
                                    status = status,
                                    onUserClick = onNavigateToUser
                                )
                            }
                        }
                    }
                    1 -> {
                        // Timeline
                        if (statuses.isEmpty() && !isLoading) {
                            item {
                                EmptyContentCard(
                                    icon = LineAwesomeIcons.FileAltSolid,
                                    title = "No posts yet",
                                    message = "This user hasn't posted anything"
                                )
                            }
                        } else {
                            items(statuses) { status ->
                                StatusCard(
                                    status = status,
                                    onUserClick = onNavigateToUser
                                )
                            }
                        }
                    }
                    2 -> {
                        // About
                        item {
                            AboutSection(account!!)
                        }
                        
                        // Profile fields
                        if (!account!!.fields.isNullOrEmpty()) {
                            items(account!!.fields!!) { field ->
                                ProfileFieldCard(field)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UserProfileHeader(
    account: Account,
    postsCount: Int,
    relationship: Relationship?,
    isFollowing: Boolean,
    isFollowLoading: Boolean,
    onFollowClick: () -> Unit,
    onRefresh: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column {
            // Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            ) {
                if (!account.header.isNullOrEmpty()) {
                    AsyncImage(
                        model = account.header,
                        contentDescription = "Profile banner",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                    )
                }
            }
            
            // Profile info section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    // Avatar
                    if (!account.avatar.isNullOrEmpty()) {
                        AsyncImage(
                            model = account.avatar,
                            contentDescription = "Profile picture",
                            modifier = Modifier
                                .size(80.dp)
                                .offset(y = (-40).dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .offset(y = (-40).dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = account.displayName.firstOrNull()?.toString() ?: "?",
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    // Follow button
                    Button(
                        onClick = onFollowClick,
                        enabled = !isFollowLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isFollowing) 
                                MaterialTheme.colorScheme.surfaceVariant 
                            else 
                                MaterialTheme.colorScheme.primary
                        )
                    ) {
                        if (isFollowLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = if (isFollowing) 
                                    LineAwesomeIcons.UserMinusSolid 
                                else 
                                    LineAwesomeIcons.UserPlusSolid,
                                contentDescription = if (isFollowing) "Unfollow" else "Follow",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (isFollowing) "Unfollow" else "Follow")
                        }
                    }
                }
                
                // Name and username
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = (-20).dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = account.displayName,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "@${account.acct}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Bio
                if (!account.note.isNullOrEmpty()) {
                    Text(
                        text = account.note.replace(Regex("<[^>]*>"), ""),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                
                // Stats
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    CompactStatItem(
                        count = postsCount.toLong(),
                        label = "Posts"
                    )
                    CompactStatItem(
                        count = account.followingCount,
                        label = "Following"
                    )
                    CompactStatItem(
                        count = account.followersCount,
                        label = "Followers"
                    )
                }
            }
        }
    }
}
