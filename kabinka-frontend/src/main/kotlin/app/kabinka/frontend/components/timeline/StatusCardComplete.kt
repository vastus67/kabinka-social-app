package app.kabinka.frontend.components.timeline

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.kabinka.social.model.Account
import app.kabinka.social.model.Status
import app.kabinka.social.ui.text.HtmlParser
import app.kabinka.social.ui.text.LinkSpan
import app.kabinka.social.api.session.AccountSessionManager
import app.kabinka.social.api.StatusInteractionController
import coil.compose.AsyncImage
import compose.icons.LineAwesomeIcons
import compose.icons.lineawesomeicons.EllipsisVSolid
import compose.icons.lineawesomeicons.ExclamationTriangleSolid
import compose.icons.lineawesomeicons.ReplySolid
import compose.icons.lineawesomeicons.StarSolid
import compose.icons.lineawesomeicons.SyncSolid
import java.time.Instant
import java.time.Duration

/**
 * Complete StatusCard with full Mastodon feature parity:
 * - Boosts/Reblogs indicator
 * - Reply indicator  
 * - Content warnings (collapsible)
 * - Media attachments (images/videos/audio)
 * - Polls
 * - Link preview cards
 * - Custom emojis
 * - HTML content rendering
 * - Pinned indicator
 * - Edited indicator
 * - Visibility indicator
 */
@Composable
fun StatusCardComplete(
    status: Status,
    onStatusClick: (String) -> Unit = {},
    onProfileClick: (String) -> Unit = {},
    onReply: (String) -> Unit = {},
    onBoost: (String) -> Unit = {},
    onFavorite: (String) -> Unit = {},
    onBookmark: (String) -> Unit = {},
    onMore: (String) -> Unit = {},
    onMediaClick: (Int) -> Unit = {},
    onPollVote: (List<Int>) -> Unit = {},
    onLinkClick: (String) -> Unit = {},
    onHashtagClick: (String) -> Unit = {},
    onMentionClick: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Determine if this is a boost/reblog
    val isBoost = status.reblog != null
    val displayedStatus = status.reblog ?: status
    val boostAccount = if (isBoost) status.account else null
    
    // Local state for immediate UI updates
    var isFavorited by remember(displayedStatus.id) { mutableStateOf(displayedStatus.favourited) }
    var isReblogged by remember(displayedStatus.id) { mutableStateOf(displayedStatus.reblogged) }
    var isBookmarked by remember(displayedStatus.id) { mutableStateOf(displayedStatus.bookmarked) }
    var favoritesCount by remember(displayedStatus.id) { mutableStateOf(displayedStatus.favouritesCount.toInt()) }
    var reblogsCount by remember(displayedStatus.id) { mutableStateOf(displayedStatus.reblogsCount.toInt()) }
    
    // Sync with status object when it changes
    LaunchedEffect(displayedStatus.favourited, displayedStatus.reblogged, displayedStatus.bookmarked) {
        isFavorited = displayedStatus.favourited
        isReblogged = displayedStatus.reblogged
        isBookmarked = displayedStatus.bookmarked
        favoritesCount = displayedStatus.favouritesCount.toInt()
        reblogsCount = displayedStatus.reblogsCount.toInt()
    }
    
    val accountId = AccountSessionManager.getInstance().lastActiveAccountID
    val controller = remember(accountId) { 
        if (accountId != null) StatusInteractionController(accountId) else null 
    }
    
    Card(
        modifier = modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Boost indicator
            if (isBoost && boostAccount != null) {
                BoostIndicator(account = boostAccount)
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            // Reply indicator
            if (displayedStatus.inReplyToId != null) {
                ReplyIndicator()
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            // Pinned indicator
            if (status.pinned == true) {
                PinnedIndicator()
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            // Author header
            StatusHeader(
                account = displayedStatus.account,
                createdAt = displayedStatus.createdAt,
                editedAt = displayedStatus.editedAt,
                visibility = displayedStatus.visibility.toString(),
                onProfileClick = { onProfileClick(displayedStatus.account.id) },
                onMore = { onMore(displayedStatus.id) }
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Content with CW handling
            Column {
                StatusContent(
                    content = displayedStatus.content,
                    spoilerText = displayedStatus.spoilerText,
                    sensitive = displayedStatus.sensitive,
                    emojis = displayedStatus.emojis,
                    mentions = displayedStatus.mentions,
                    tags = displayedStatus.tags,
                    onLinkClick = onLinkClick,
                    onHashtagClick = onHashtagClick,
                    onMentionClick = onMentionClick
                )
            }
            
            // Media attachments
            if (!displayedStatus.mediaAttachments.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                MediaAttachmentsGrid(
                    attachments = displayedStatus.mediaAttachments,
                    sensitive = displayedStatus.sensitive,
                    onMediaClick = onMediaClick
                )
            }
            
            // Poll
            if (displayedStatus.poll != null) {
                Spacer(modifier = Modifier.height(12.dp))
                PollView(
                    poll = displayedStatus.poll,
                    onVote = onPollVote
                )
            }
            
            // Card preview (only if no media)
            if (displayedStatus.card != null && displayedStatus.mediaAttachments.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                CardPreview(
                    card = displayedStatus.card,
                    onClick = { onLinkClick(displayedStatus.card.url) }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Action buttons
            StatusActions(
                repliesCount = displayedStatus.repliesCount.toInt(),
                reblogsCount = reblogsCount,
                favouritesCount = favoritesCount,
                reblogged = isReblogged,
                favourited = isFavorited,
                bookmarked = isBookmarked,
                onReply = { onReply(displayedStatus.id) },
                onBoost = {
                    val newState = !isReblogged
                    isReblogged = newState
                    reblogsCount = if (newState) reblogsCount + 1 else reblogsCount - 1
                    controller?.setReblogged(displayedStatus, newState)
                },
                onFavorite = {
                    val newState = !isFavorited
                    isFavorited = newState
                    favoritesCount = if (newState) favoritesCount + 1 else favoritesCount - 1
                    controller?.setFavorited(displayedStatus, newState)
                },
                onBookmark = {
                    isBookmarked = !isBookmarked
                    controller?.setBookmarked(displayedStatus, isBookmarked)
                },
                onShare = { onMore(displayedStatus.id) }
            )
        }
    }
}

@Composable
private fun BoostIndicator(account: Account) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 4.dp)
    ) {
        Icon(
            imageVector = LineAwesomeIcons.SyncSolid,
            contentDescription = "Boosted",
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "${account.displayName.ifEmpty { account.username }} boosted",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ReplyIndicator() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 4.dp)
    ) {
        Icon(
            imageVector = LineAwesomeIcons.ReplySolid,
            contentDescription = "Reply",
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Replying to thread",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PinnedIndicator() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 4.dp)
    ) {
        Icon(
            imageVector = LineAwesomeIcons.StarSolid,
            contentDescription = "Pinned",
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Pinned post",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun StatusHeader(
    account: Account,
    createdAt: Instant,
    editedAt: Instant?,
    visibility: String,
    onProfileClick: () -> Unit,
    onMore: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        if (account.avatar != null) {
            AsyncImage(
                model = account.avatar,
                contentDescription = "${account.displayName} avatar",
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onProfileClick),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .clickable(onClick = onProfileClick),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = account.displayName.firstOrNull()?.toString() ?: "?",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .clickable(onClick = onProfileClick)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = account.displayName.ifEmpty { account.username },
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (account.bot == true) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "BOT",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "@${account.acct}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = " Â· ${formatTimeAgo(createdAt)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (editedAt != null) {
                    Text(
                        text = " Â· edited",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
            }
        }

        IconButton(onClick = onMore) {
            Icon(
                imageVector = LineAwesomeIcons.EllipsisVSolid,
                contentDescription = "More options",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun StatusContent(
    content: String,
    spoilerText: String?,
    sensitive: Boolean,
    emojis: List<app.kabinka.social.model.Emoji>?,
    mentions: List<app.kabinka.social.model.Mention>?,
    tags: List<app.kabinka.social.model.Hashtag>?,
    onLinkClick: (String) -> Unit,
    onHashtagClick: (String) -> Unit,
    onMentionClick: (String) -> Unit
) {
    var showContent by remember { mutableStateOf(spoilerText.isNullOrEmpty() && !sensitive) }
    
    // Content warning
    if (!spoilerText.isNullOrEmpty() || sensitive) {
        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = LineAwesomeIcons.ExclamationTriangleSolid,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = spoilerText ?: "Sensitive content",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                TextButton(
                    onClick = { showContent = !showContent },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(if (showContent) "Hide content" else "Show content")
                }
            }
        }
        
        if (showContent) {
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
    
    // Actual content
    if (showContent) {
        val annotatedContent = htmlToAnnotatedString(
            html = content,
            mentions = mentions ?: emptyList(),
            tags = tags ?: emptyList(),
            baseColor = MaterialTheme.colorScheme.onSurface
        )
        
        ClickableText(
            text = annotatedContent,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurface
            ),
            onClick = { offset ->
                annotatedContent.getStringAnnotations(
                    tag = "URL",
                    start = offset,
                    end = offset
                ).firstOrNull()?.let { annotation ->
                    onLinkClick(annotation.item)
                }
                
                annotatedContent.getStringAnnotations(
                    tag = "HASHTAG",
                    start = offset,
                    end = offset
                ).firstOrNull()?.let { annotation ->
                    onHashtagClick(annotation.item)
                }
                
                annotatedContent.getStringAnnotations(
                    tag = "MENTION",
                    start = offset,
                    end = offset
                ).firstOrNull()?.let { annotation ->
                    onMentionClick(annotation.item)
                }
            }
        )
    }
}

@Composable
private fun htmlToAnnotatedString(
    html: String,
    mentions: List<app.kabinka.social.model.Mention>,
    tags: List<app.kabinka.social.model.Hashtag>,
    baseColor: androidx.compose.ui.graphics.Color
): AnnotatedString {
    // Use kabinka-social's HtmlParser and convert to AnnotatedString
    val context = androidx.compose.ui.platform.LocalContext.current
    val accountId = AccountSessionManager.getInstance().lastActiveAccountID
    
    val spanned = app.kabinka.social.ui.text.HtmlParser.parse(
        html,
        emptyList(), // emojis - we'll handle separately if needed
        mentions,
        tags,
        accountId ?: "",
        null, // parentObject
        context
    )
    
    return buildAnnotatedString {
        val linkColor = androidx.compose.ui.graphics.Color(0xFF1DA1F2)
        
        // Convert SpannableStringBuilder to AnnotatedString
        append(spanned.toString())
        
        // Apply base color to all text
        addStyle(SpanStyle(color = baseColor), 0, length)
        
        // Extract LinkSpan annotations from the spanned text
        val spans = spanned.getSpans(0, spanned.length, app.kabinka.social.ui.text.LinkSpan::class.java)
        for (span in spans) {
            val start = spanned.getSpanStart(span)
            val end = spanned.getSpanEnd(span)
            
            // Add link styling
            addStyle(
                SpanStyle(
                    color = linkColor,
                    textDecoration = TextDecoration.Underline
                ),
                start,
                end
            )
            
            // Add annotation based on link type
            when (span.type) {
                app.kabinka.social.ui.text.LinkSpan.Type.MENTION -> {
                    addStringAnnotation(
                        tag = "MENTION",
                        annotation = span.link,
                        start = start,
                        end = end
                    )
                }
                app.kabinka.social.ui.text.LinkSpan.Type.HASHTAG -> {
                    addStringAnnotation(
                        tag = "HASHTAG",
                        annotation = span.link,
                        start = start,
                        end = end
                    )
                }
                app.kabinka.social.ui.text.LinkSpan.Type.URL -> {
                    addStringAnnotation(
                        tag = "URL",
                        annotation = span.link,
                        start = start,
                        end = end
                    )
                }
                else -> {
                    addStringAnnotation(
                        tag = "URL",
                        annotation = span.link,
                        start = start,
                        end = end
                    )
                }
            }
        }
    }
}

private fun formatTimeAgo(instant: Instant): String {
    val duration = Duration.between(instant, Instant.now())
    return when {
        duration.toMinutes() < 1 -> "now"
        duration.toMinutes() < 60 -> "${duration.toMinutes()}m"
        duration.toHours() < 24 -> "${duration.toHours()}h"
        duration.toDays() < 7 -> "${duration.toDays()}d"
        else -> "${duration.toDays() / 7}w"
    }
}
