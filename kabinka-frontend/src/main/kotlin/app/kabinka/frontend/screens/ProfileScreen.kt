package app.kabinka.frontend.screens

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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.kabinka.social.api.requests.accounts.GetAccountStatuses
import app.kabinka.social.api.requests.accounts.GetOwnAccount
import app.kabinka.social.api.session.AccountSessionManager
import app.kabinka.social.model.Account
import app.kabinka.social.model.Status
import coil.compose.AsyncImage
import compose.icons.FeatherIcons
import compose.icons.feathericons.*
import me.grishka.appkit.api.Callback
import me.grishka.appkit.api.ErrorResponse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen() {
    var account by remember { mutableStateOf<Account?>(null) }
    var statuses by remember { mutableStateOf<List<Status>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var selectedTab by remember { mutableStateOf(0) }
    var selectedFilter by remember { mutableStateOf(0) }
    
    val tabs = listOf("Featured", "Timeline", "About", "Saved")
    val filters = listOf("Posts", "Replies", "Media")
    
    // Check if user is logged in
    val isLoggedIn = remember {
        try {
            AccountSessionManager.getInstance().lastActiveAccount != null
        } catch (e: Exception) {
            false
        }
    }
    
    // Load own account
    LaunchedEffect(selectedFilter) {
        if (isLoggedIn) {
            isLoading = true
            val accountId = AccountSessionManager.getInstance().lastActiveAccountID
            if (accountId != null) {
                val accountRequest = GetOwnAccount()
                accountRequest.setCallback(object : Callback<Account> {
                    override fun onSuccess(result: Account) {
                        account = result
                        
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
                actions = {
                    IconButton(onClick = { /* TODO: Share */ }) {
                        Icon(
                            imageVector = FeatherIcons.Share2,
                            contentDescription = "Share",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = { /* TODO: Settings */ }) {
                        Icon(
                            imageVector = FeatherIcons.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        }
    ) { padding ->


        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = error!!,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            account != null -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    // Profile Header
                    item {
                        ProfileHeader(account = account!!, postsCount = statuses.size)
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
                                    text = {
                                        Text(
                                            text = title,
                                            style = MaterialTheme.typography.labelLarge
                                        )
                                    }
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
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                filters.forEachIndexed { index, title ->
                                    FilterChip(
                                        selected = selectedFilter == index,
                                        onClick = { selectedFilter = index },
                                        label = { Text(title) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                        )
                                    )
                                }
                            }
                        }
                    }
                    
                    // Content based on selected tab
                    when (selectedTab) {
                        0 -> {
                            // Featured
                            item {
                                EmptyContentCard(
                                    icon = FeatherIcons.Star,
                                    title = "No featured posts",
                                    message = "Pin your best posts to feature them here"
                                )
                            }
                        }
                        1 -> {
                            // Timeline
                            if (statuses.isEmpty() && !isLoading) {
                                item {
                                    EmptyContentCard(
                                        icon = FeatherIcons.FileText,
                                        title = "No posts yet",
                                        message = "Start sharing your thoughts"
                                    )
                                }
                            } else {
                                items(statuses) { status ->
                                    StatusCard(status)
                                }
                            }
                        }
                        2 -> {
                            // About
                            item {
                                AboutSection(account!!)
                            }
                        }
                        3 -> {
                            // Saved
                            item {
                                EmptyContentCard(
                                    icon = FeatherIcons.Bookmark,
                                    title = "No saved posts",
                                    message = "Bookmark posts to see them here"
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileHeader(account: Account, postsCount: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Banner area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                    )
            ) {
                // TODO: Add banner image support
                // For now, just a colored background
            }
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    // Avatar (overlapping banner)
                    Box(
                        modifier = Modifier.offset(y = (-40).dp)
                    ) {
                        if (account.avatar != null) {
                            AsyncImage(
                                model = account.avatar,
                                contentDescription = "Profile picture",
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surface),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
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
                    }
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    // Action buttons
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { /* TODO: QR Code */ },
                            modifier = Modifier.size(40.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(
                                imageVector = FeatherIcons.Maximize,
                                contentDescription = "QR Code",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        
                        Button(
                            onClick = { /* TODO: Edit profile */ },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = FeatherIcons.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Edit", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
                
                // Name and username (moved up, no duplication)
                Column(
                    modifier = Modifier.offset(y = (-24).dp)
                ) {
                    Text(
                        text = account.displayName,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Text(
                        text = "@${account.username}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Bio (if present)
                if (!account.note.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = account.note.replace(Regex("<[^>]*>"), ""),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Compact stats row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CompactStatItem(
                        count = postsCount.toLong(),
                        label = "Posts"
                    )
                    CompactStatItem(
                        count = account.followersCount,
                        label = "Followers"
                    )
                    CompactStatItem(
                        count = account.followingCount,
                        label = "Following"
                    )
                }
            }
        }
    }
}

@Composable
private fun CompactStatItem(
    count: Long,
    label: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun EmptyContentCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    message: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AboutSection(account: Account) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "About",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            
            if (!account.note.isNullOrEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Bio",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = account.note.replace(Regex("<[^>]*>"), ""),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Joined",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = account.createdAt.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            if (!account.url.isNullOrEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Profile URL",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = account.url,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusCard(status: Status) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Status content
            Text(
                text = status.content.replace(Regex("<[^>]*>"), ""),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 5,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Status metadata
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = FeatherIcons.Repeat,
                        contentDescription = "Boosts",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${status.reblogsCount}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = FeatherIcons.Heart,
                        contentDescription = "Favorites",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${status.favouritesCount}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
