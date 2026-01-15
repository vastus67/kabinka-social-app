package app.kabinka.frontend.screens.explore

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.kabinka.social.api.requests.trends.GetTrendingStatuses
import app.kabinka.social.api.session.AccountSessionManager
import app.kabinka.social.api.StatusInteractionController
import app.kabinka.social.model.Status
import app.kabinka.social.model.Attachment
import app.kabinka.social.model.Relationship
import coil.compose.AsyncImage
import me.grishka.appkit.api.Callback
import me.grishka.appkit.api.ErrorResponse
import compose.icons.LineAwesomeIcons
import compose.icons.lineawesomeicons.CommentSolid
import compose.icons.lineawesomeicons.RetweetSolid
import compose.icons.lineawesomeicons.HeartSolid
import compose.icons.lineawesomeicons.BookmarkSolid

@Composable
fun PostsTab(isLoggedIn: Boolean, onNavigateToUser: (String) -> Unit) {
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
                    contentPadding = PaddingValues(0.dp),
                    verticalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    items(posts) { status ->
                        PostCard(status, onNavigateToUser)
                    }
                }
            }
        }
    }
}

@Composable
private fun PostCard(status: Status, onNavigateToUser: (String) -> Unit = {}) {
    var isFavorited by remember(status.id) { mutableStateOf(status.favourited) }
    var isReblogged by remember(status.id) { mutableStateOf(status.reblogged) }
    var isBookmarked by remember(status.id) { mutableStateOf(status.bookmarked) }
    var favoritesCount by remember(status.id) { mutableStateOf(status.favouritesCount.toInt()) }
    var reblogsCount by remember(status.id) { mutableStateOf(status.reblogsCount.toInt()) }
    
    // Sync with status object when it changes
    LaunchedEffect(status.favourited, status.reblogged, status.bookmarked) {
        isFavorited = status.favourited
        isReblogged = status.reblogged
        isBookmarked = status.bookmarked
        favoritesCount = status.favouritesCount.toInt()
        reblogsCount = status.reblogsCount.toInt()
    }
    
    val accountId = AccountSessionManager.getInstance().lastActiveAccountID
    val controller = remember(accountId) { 
        if (accountId != null) StatusInteractionController(accountId) else null 
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onNavigateToUser(status.account.id) }
            ) {
                // Avatar
                AsyncImage(
                    model = status.account.avatar,
                    contentDescription = "Avatar",
                    modifier = Modifier.size(40.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(status.account.displayName, style = MaterialTheme.typography.titleSmall)
                    Text("@${status.account.acct}", style = MaterialTheme.typography.bodySmall)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(status.content.replace(Regex("<[^>]*>"), ""), maxLines = 6, overflow = TextOverflow.Ellipsis)
            
            // Media attachments
            if (status.mediaAttachments.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                val imageAttachments = status.mediaAttachments.filter { it.type == Attachment.Type.IMAGE }
                if (imageAttachments.isNotEmpty()) {
                    AsyncImage(
                        model = imageAttachments.first().url,
                        contentDescription = "Post image",
                        modifier = Modifier.fillMaxWidth().heightIn(max = 200.dp).clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }
            
            // Link preview card
            status.card?.let { card ->
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(modifier = Modifier.padding(8.dp)) {
                        card.image?.let { imageUrl ->
                            AsyncImage(
                                model = imageUrl,
                                contentDescription = null,
                                modifier = Modifier.size(60.dp).clip(RoundedCornerShape(4.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Column {
                            Text(card.title, style = MaterialTheme.typography.bodyMedium, maxLines = 2)
                            card.description?.let {
                                Text(it, style = MaterialTheme.typography.bodySmall, maxLines = 1)
                            }
                        }
                    }
                }
            }
            
            // Action buttons
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Reply
                StatusActionButton(
                    icon = LineAwesomeIcons.CommentSolid,
                    count = status.repliesCount.toInt(),
                    onClick = { /* Reply functionality */ }
                )
                
                // Boost/Reblog
                StatusActionButton(
                    icon = LineAwesomeIcons.RetweetSolid,
                    count = reblogsCount,
                    isActive = isReblogged,
                    onClick = {
                        val newState = !isReblogged
                        isReblogged = newState
                        reblogsCount = if (newState) reblogsCount + 1 else reblogsCount - 1
                        controller?.setReblogged(status, newState)
                    }
                )
                
                // Favorite/Like
                StatusActionButton(
                    icon = LineAwesomeIcons.HeartSolid,
                    count = favoritesCount,
                    isActive = isFavorited,
                    onClick = {
                        val newState = !isFavorited
                        isFavorited = newState
                        favoritesCount = if (newState) favoritesCount + 1 else favoritesCount - 1
                        controller?.setFavorited(status, newState)
                    }
                )
                
                // Bookmark
                IconButton(onClick = {
                    isBookmarked = !isBookmarked
                    controller?.setBookmarked(status, isBookmarked)
                }) {
                    Icon(
                        imageVector = LineAwesomeIcons.BookmarkSolid,
                        contentDescription = "Bookmark",
                        tint = if (isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    count: Int,
    isActive: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onClick) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (count > 0) {
            Text(
                text = formatCount(count),
                style = MaterialTheme.typography.bodySmall,
                color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun formatCount(count: Int): String {
    return when {
        count >= 1000000 -> "${count / 1000000}M"
        count >= 1000 -> "${count / 1000}K"
        else -> count.toString()
    }
}
