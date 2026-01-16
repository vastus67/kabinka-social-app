package app.kabinka.frontend.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import android.net.Uri
import app.kabinka.frontend.screens.explore.PostsTab
import app.kabinka.frontend.screens.explore.HashtagsTab
import app.kabinka.frontend.screens.explore.NewsTab
import app.kabinka.frontend.screens.explore.ForYouTab
import app.kabinka.social.api.requests.search.GetSearchResults
import app.kabinka.social.api.session.AccountSessionManager
import app.kabinka.social.model.SearchResults
import app.kabinka.social.model.Account
import app.kabinka.social.model.Status
import app.kabinka.social.model.Hashtag
import app.kabinka.social.model.Attachment
import coil.compose.AsyncImage
import me.grishka.appkit.api.Callback
import me.grishka.appkit.api.ErrorResponse
import compose.icons.LineAwesomeIcons
import compose.icons.lineawesomeicons.SearchSolid
import compose.icons.lineawesomeicons.TimesSolid
import compose.icons.lineawesomeicons.FileAltSolid
import compose.icons.lineawesomeicons.HashtagSolid
import compose.icons.lineawesomeicons.NewspaperSolid
import compose.icons.lineawesomeicons.UserSolid
import compose.icons.lineawesomeicons.UsersSolid
import compose.icons.lineawesomeicons.CommentSolid



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    sessionManager: app.kabinka.frontend.auth.SessionStateManager,
    onNavigateToUser: (String) -> Unit = {},
    onNavigateToReply: (String) -> Unit = {}
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
    var searchResults by remember { mutableStateOf<SearchResults?>(null) }
    var isSearching by remember { mutableStateOf(false) }
    
    // Perform search when query changes
    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotBlank() && isLoggedIn) {
            isSearching = true
            val accountId = AccountSessionManager.getInstance().lastActiveAccountID
            if (accountId != null) {
                val request = GetSearchResults(searchQuery, null, false, null, 0, 20)
                request.setCallback(object : Callback<SearchResults> {
                    override fun onSuccess(result: SearchResults) {
                        searchResults = result
                        isSearching = false
                    }
                    override fun onError(errorResponse: ErrorResponse) {
                        isSearching = false
                    }
                }).exec(accountId)
            }
        } else {
            searchResults = null
            isSearching = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Explore") }
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
                        Icon(LineAwesomeIcons.SearchSolid, "Search")
                    },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(LineAwesomeIcons.TimesSolid, "Clear")
                            }
                        }
                    },
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                        unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent
                    ),
                    singleLine = true
                )
            }
            
            // Show search results if searching, otherwise show tabs
            if (searchQuery.isNotBlank()) {
                SearchResultsContent(
                    searchResults = searchResults,
                    isSearching = isSearching,
                    searchQuery = searchQuery
                )
            } else {
                // Tabs
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            icon = {
                                Icon(
                                    imageVector = when (index) {
                                        0 -> LineAwesomeIcons.FileAltSolid
                                        1 -> LineAwesomeIcons.HashtagSolid
                                        2 -> LineAwesomeIcons.NewspaperSolid
                                        3 -> LineAwesomeIcons.UserSolid
                                        else -> LineAwesomeIcons.FileAltSolid
                                    },
                                    contentDescription = title
                                )
                            },
                            text = { 
                                Text(
                                    title,
                                    style = MaterialTheme.typography.labelMedium,
                                    maxLines = 1,
                                    softWrap = false
                                ) 
                            }
                        )
                    }
                }

                // Tab content
                when (selectedTab) {
                    0 -> PostsTab(
                        isLoggedIn = isLoggedIn,
                        onNavigateToUser = onNavigateToUser,
                        onNavigateToReply = onNavigateToReply,
                        sessionManager = sessionManager
                    )
                    1 -> HashtagsTab()
                    2 -> NewsTab(isLoggedIn = isLoggedIn)
                    3 -> ForYouTab(isLoggedIn = isLoggedIn, onNavigateToUser = onNavigateToUser)
                }
            }
        }
    }
}

// SEARCH RESULTS
@Composable
private fun SearchResultsContent(
    searchResults: SearchResults?,
    isSearching: Boolean,
    searchQuery: String = ""
) {
    when {
        isSearching -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        searchResults != null -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                // Quick filter options at the top
                if (searchQuery.isNotBlank()) {
                    item {
                        SearchQuickFilters(searchQuery = searchQuery, searchResults = searchResults)
                        Spacer(modifier = Modifier.height(8.dp))
                        Divider()
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
                
                // People section
                if (!searchResults.accounts.isNullOrEmpty()) {
                    item {
                        Text(
                            text = "People",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                    items(searchResults.accounts) { account ->
                        SearchAccountItem(account = account)
                    }
                }
                
                // Posts section
                if (!searchResults.statuses.isNullOrEmpty()) {
                    item {
                        Text(
                            text = "Posts",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                    items(searchResults.statuses) { status ->
                        SearchPostItem(status = status)
                    }
                }
                
                // Hashtags section
                if (!searchResults.hashtags.isNullOrEmpty()) {
                    item {
                        Text(
                            text = "Hashtags",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                    items(searchResults.hashtags) { hashtag ->
                        SearchHashtagItem(hashtag = hashtag)
                    }
                }
            }
        }
        else -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Start typing to search",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SearchQuickFilters(
    searchQuery: String,
    searchResults: SearchResults
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        // Posts with "#searchQuery"
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { },
            color = MaterialTheme.colorScheme.surface
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "#",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Posts with \"#$searchQuery\"",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
        
        // Go to @account (if exact account match exists)
        searchResults.accounts?.firstOrNull()?.let { firstAccount ->
            if (firstAccount.acct.contains(searchQuery, ignoreCase = true)) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { },
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = LineAwesomeIcons.UserSolid,
                            contentDescription = "Account",
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "Go to @${firstAccount.acct}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
        
        // Posts with "searchQuery"
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { },
            color = MaterialTheme.colorScheme.surface
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = LineAwesomeIcons.SearchSolid,
                    contentDescription = "Search posts",
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Posts with \"$searchQuery\"",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
        
        // People with "searchQuery"
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { },
            color = MaterialTheme.colorScheme.surface
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = LineAwesomeIcons.UsersSolid,
                    contentDescription = "Search people",
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "People with \"$searchQuery\"",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Composable
private fun SearchAccountItem(account: Account) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 1.dp),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { }
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = LineAwesomeIcons.UserSolid,
                contentDescription = "Person",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(40.dp)
                    .padding(end = 12.dp)
            )
            
            AsyncImage(
                model = account.avatar,
                contentDescription = "Avatar",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = account.displayName.takeIf { it.isNotBlank() } ?: account.username,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "@${account.acct}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun SearchPostItem(status: Status) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 1.dp),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { }
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = LineAwesomeIcons.CommentSolid,
                contentDescription = "Post",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(40.dp)
                    .padding(end = 12.dp)
            )
            
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = status.account.avatar,
                        contentDescription = "Avatar",
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = status.account.displayName.takeIf { it.isNotBlank() } 
                            ?: status.account.username,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = status.content.replace(Regex("<[^>]*>"), ""),
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                // Show image if available
                status.mediaAttachments?.firstOrNull()?.let { attachment ->
                    if (attachment.type == Attachment.Type.IMAGE) {
                        Spacer(modifier = Modifier.height(8.dp))
                        AsyncImage(
                            model = attachment.previewUrl ?: attachment.url,
                            contentDescription = "Post image",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchHashtagItem(hashtag: Hashtag) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 1.dp),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { }
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .padding(end = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "#",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "#${hashtag.name}",
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                val usageText = buildString {
                    hashtag.history?.firstOrNull()?.let { history ->
                        val uses = try { history.uses?.toInt() ?: 0 } catch (e: Exception) { 0 }
                        val accounts = try { history.accounts?.toInt() ?: 0 } catch (e: Exception) { 0 }
                        append("$uses posts")
                        if (accounts > 0) {
                            append(" â€¢ $accounts people")
                        }
                    }
                }
                
                if (usageText.isNotBlank()) {
                    Text(
                        text = usageText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
