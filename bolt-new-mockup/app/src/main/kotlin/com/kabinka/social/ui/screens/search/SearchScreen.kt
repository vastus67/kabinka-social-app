package com.kabinka.social.ui.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.kabinka.social.api.requests.trends.GetTrendingStatuses
import app.kabinka.social.api.requests.trends.GetTrendingHashtags
import app.kabinka.social.api.requests.trends.GetTrendingLinks
import app.kabinka.social.api.requests.accounts.GetFollowSuggestions
import app.kabinka.social.api.session.AccountSessionManager
import app.kabinka.social.model.Status
import app.kabinka.social.model.Hashtag
import app.kabinka.social.model.Card
import app.kabinka.social.model.FollowSuggestion
import com.kabinka.social.navigation.Screen
import com.kabinka.social.ui.components.KabinkaTopBar
import me.grishka.appkit.api.Callback
import me.grishka.appkit.api.ErrorResponse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onNavigate: (String) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("POSTS", "HASHTAGS", "NEWS", "FOR YOU")
    
    // Check if user is logged in
    val isLoggedIn = remember {
        try {
            AccountSessionManager.getInstance().lastActiveAccount != null
        } catch (e: Exception) {
            false
        }
    }
    
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            KabinkaTopBar(
                title = "Explore",
                onChatClick = if (isLoggedIn) {
                    { onNavigate(Screen.FluffyChat.route) }
                } else null,
                showAvatar = false
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search bar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = MaterialTheme.shapes.large,
                tonalElevation = 2.dp
            ) {
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search Mastodon") },
                    leadingIcon = {
                        Icon(Icons.Outlined.Search, "Search")
                    },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Outlined.Close, "Clear")
                            }
                        }
                    },
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                        unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent
                    ),
                    singleLine = true,
                    onKeyboardAction = {
                        if (searchQuery.isNotBlank()) {
                            onNavigate(Screen.SearchResults.createRoute(searchQuery))
                        }
                    }
                )
            }
            
            // Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { 
                            Text(
                                title,
                                style = MaterialTheme.typography.labelLarge
                            ) 
                        }
                    )
                }
            }

            // Tab content
            when (selectedTab) {
                0 -> PostsTab(onNavigate = onNavigate, isLoggedIn = isLoggedIn)
                1 -> HashtagsTab(onNavigate = onNavigate)
                2 -> NewsTab(isLoggedIn = isLoggedIn)
                3 -> ForYouTab(onNavigate = onNavigate, isLoggedIn = isLoggedIn)
            }
        }
    }
}

// POSTS TAB - Shows trending posts
@Composable
private fun PostsTab(onNavigate: (String) -> Unit, isLoggedIn: Boolean) {
    var posts by remember { mutableStateOf<List<Status>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

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
                        error = "Failed to load trending posts"
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
                    error = "Failed to load trending posts"
                    isLoading = false
                }
            }).execNoAuth("mastodon.social")
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            error != null -> {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Outlined.ErrorOutline,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(error!!, color = MaterialTheme.colorScheme.error)
                }
            }
            posts.isEmpty() -> {
                Text(
                    "No trending posts available",
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(posts) { status ->
                        TrendingPostCard(
                            status = status,
                            onClick = { onNavigate(Screen.StatusDetail.createRoute(status.id)) },
                            onUserClick = { onNavigate(Screen.ProfileDetail.createRoute(status.account.id)) },
                            isLoggedIn = isLoggedIn
                        )
                    }
                }
            }
        }
    }
}

// HASHTAGS TAB - Shows trending hashtags
@Composable
private fun HashtagsTab(onNavigate: (String) -> Unit) {
    var hashtags by remember { mutableStateOf<List<Hashtag>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        isLoading = true
        error = null
        
        GetTrendingHashtags(20)
            .setCallback(object : Callback<List<Hashtag>> {
                override fun onSuccess(result: List<Hashtag>) {
                    hashtags = result
                    isLoading = false
                }
                override fun onError(errorResponse: ErrorResponse) {
                    error = "Failed to load trending hashtags"
                    isLoading = false
                }
            }).execNoAuth("mastodon.social")
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            error != null -> {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Outlined.ErrorOutline,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(error!!, color = MaterialTheme.colorScheme.error)
                }
            }
            hashtags.isEmpty() -> {
                Text(
                    "No trending hashtags available",
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(hashtags) { hashtag ->
                        HashtagCard(
                            hashtag = hashtag,
                            onClick = { onNavigate(Screen.HashtagTimeline.createRoute(hashtag.name)) }
                        )
                    }
                }
            }
        }
    }
}

// NEWS TAB - Shows trending links/news
@Composable
private fun NewsTab(isLoggedIn: Boolean) {
    var newsLinks by remember { mutableStateOf<List<Card>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        isLoading = true
        error = null
        
        val request = GetTrendingLinks(20)
        
        if (isLoggedIn) {
            val accountId = AccountSessionManager.getInstance().lastActiveAccountID
            if (accountId != null) {
                request.setCallback(object : Callback<List<Card>> {
                    override fun onSuccess(result: List<Card>) {
                        newsLinks = result
                        isLoading = false
                    }
                    override fun onError(errorResponse: ErrorResponse) {
                        error = "Failed to load news"
                        isLoading = false
                    }
                }).exec(accountId)
            } else {
                isLoading = false
            }
        } else {
            request.setCallback(object : Callback<List<Card>> {
                override fun onSuccess(result: List<Card>) {
                    newsLinks = result
                    isLoading = false
                }
                override fun onError(errorResponse: ErrorResponse) {
                    error = "Failed to load news"
                    isLoading = false
                }
            }).execNoAuth("mastodon.social")
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            error != null -> {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Outlined.ErrorOutline,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(error!!, color = MaterialTheme.colorScheme.error)
                }
            }
            newsLinks.isEmpty() -> {
                Text(
                    "No news available",
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(newsLinks) { card ->
                        NewsCard(
                            card = card,
                            isLoggedIn = isLoggedIn
                        )
                    }
                }
            }
        }
    }
}

// FOR YOU TAB - Shows account suggestions (only for logged-in users)
@Composable
private fun ForYouTab(onNavigate: (String) -> Unit, isLoggedIn: Boolean) {
    var suggestions by remember { mutableStateOf<List<FollowSuggestion>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(isLoggedIn) {
        if (!isLoggedIn) {
            isLoading = false
            error = null
            return@LaunchedEffect
        }
        
        isLoading = true
        error = null
        
        val accountId = AccountSessionManager.getInstance().lastActiveAccountID
        if (accountId != null) {
            GetFollowSuggestions(20)
                .setCallback(object : Callback<List<FollowSuggestion>> {
                    override fun onSuccess(result: List<FollowSuggestion>) {
                        suggestions = result
                        isLoading = false
                    }
                    override fun onError(errorResponse: ErrorResponse) {
                        error = "Failed to load suggestions"
                        isLoading = false
                    }
                }).exec(accountId)
        } else {
            isLoading = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            !isLoggedIn -> {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Outlined.AccountCircle,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Sign in to see personalized suggestions",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Get recommendations for accounts to follow",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            error != null -> {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Outlined.ErrorOutline,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(error!!, color = MaterialTheme.colorScheme.error)
                }
            }
            suggestions.isEmpty() -> {
                Text(
                    "No suggestions available",
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(suggestions) { suggestion ->
                        SuggestionCard(
                            suggestion = suggestion,
                            onClick = { onNavigate(Screen.ProfileDetail.createRoute(suggestion.account.id)) },
                            onFollowClick = { /* TODO: Implement follow */ }
                        )
                    }
                }
            }
        }
    }
}

// UI Components

@Composable
private fun TrendingPostCard(
    status: Status,
    onClick: () -> Unit,
    onUserClick: () -> Unit,
    isLoggedIn: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.clickable(onClick = onUserClick),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = status.account.displayName.firstOrNull()?.toString() ?: "?",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = status.account.displayName,
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = "@${status.account.username}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = status.content.replace(Regex("<[^>]*>"), ""),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 6,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.ChatBubbleOutline,
                        contentDescription = "Replies",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "${status.repliesCount}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.Repeat,
                        contentDescription = "Boosts",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "${status.reblogsCount}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.FavoriteBorder,
                        contentDescription = "Favorites",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "${status.favouritesCount}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun HashtagCard(
    hashtag: Hashtag,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "#${hashtag.name}",
                    style = MaterialTheme.typography.titleMedium
                )
                hashtag.history?.firstOrNull()?.let { history ->
                    Text(
                        text = "${history.accounts} people are talking",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Icon(
                Icons.Outlined.TrendingUp,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun NewsCard(
    card: Card,
    isLoggedIn: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                card.authorName?.let { author ->
                    Text(
                        text = "$author Â· ${card.history?.firstOrNull()?.let { "${it.accounts}d ago" } ?: ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
                
                Text(
                    text = card.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                card.description?.let { desc ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = desc,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                card.authorName?.let { author ->
                    Text(
                        text = "By $author",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            card.image?.let { imageUrl ->
                Spacer(modifier = Modifier.width(12.dp))
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
            }
        }
    }
}

@Composable
private fun SuggestionCard(
    suggestion: FollowSuggestion,
    onClick: () -> Unit,
    onFollowClick: () -> Unit
) {
    val account = suggestion.account
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = account.displayName.firstOrNull()?.toString() ?: "?",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = account.displayName,
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = "@${account.username}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${account.followersCount} followers",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Button(
                onClick = onFollowClick,
                modifier = Modifier.height(36.dp)
            ) {
                Text("Follow")
            }
        }
    }
}

@Composable
private fun TrendingTagItem(
    tag: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Tag,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = tag,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
