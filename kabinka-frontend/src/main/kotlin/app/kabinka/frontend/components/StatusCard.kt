package app.kabinka.frontend.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import compose.icons.LineAwesomeIcons
import compose.icons.lineawesomeicons.HomeSolid
import compose.icons.lineawesomeicons.SearchSolid
import compose.icons.lineawesomeicons.EditSolid
import compose.icons.lineawesomeicons.BellSolid
import compose.icons.lineawesomeicons.UserSolid
import compose.icons.lineawesomeicons.ShareSolid
import compose.icons.lineawesomeicons.CogSolid
import compose.icons.lineawesomeicons.CommentSolid
import compose.icons.lineawesomeicons.EllipsisVSolid
import compose.icons.lineawesomeicons.GlobeSolid
import compose.icons.lineawesomeicons.HeartSolid
import compose.icons.lineawesomeicons.RetweetSolid
import compose.icons.lineawesomeicons.StarSolid
import compose.icons.lineawesomeicons.FileAltSolid
import compose.icons.lineawesomeicons.HashtagSolid
import compose.icons.lineawesomeicons.FileSolid
import compose.icons.lineawesomeicons.UsersSolid
import compose.icons.lineawesomeicons.RssSolid
import compose.icons.lineawesomeicons.AtSolid
import compose.icons.lineawesomeicons.UserPlusSolid
import compose.icons.lineawesomeicons.ChartBarSolid
import compose.icons.lineawesomeicons.BookmarkSolid
import compose.icons.lineawesomeicons.MapMarkerSolid
import compose.icons.lineawesomeicons.InfoCircleSolid
import compose.icons.lineawesomeicons.ExclamationTriangleSolid
import compose.icons.lineawesomeicons.SyncSolid
import compose.icons.lineawesomeicons.PhoneSolid
import compose.icons.lineawesomeicons.ReplySolid
import compose.icons.lineawesomeicons.PlaySolid
import compose.icons.lineawesomeicons.QrcodeSolid
import compose.icons.lineawesomeicons.TimesSolid


import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.kabinka.social.model.Status
import coil.compose.AsyncImage
import java.time.Duration
import java.time.Instant

@Composable
fun StatusCard(
    status: Status,
    onStatusClick: (String) -> Unit = {},
    onProfileClick: (String) -> Unit = {},
    onReply: (String) -> Unit = {},
    onBoost: (String) -> Unit = {},
    onFavorite: (String) -> Unit = {},
    onMore: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onStatusClick(status.id) },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Author header - bolt-new-mockup style
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar with Coil for real images
                if (status.account.avatar != null) {
                    AsyncImage(
                        model = status.account.avatar,
                        contentDescription = "${status.account.displayName} avatar",
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .clickable { onProfileClick(status.account.id) },
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .clickable { onProfileClick(status.account.id) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = status.account.displayName.firstOrNull()?.toString() ?: "?",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = status.account.displayName.ifEmpty { status.account.username },
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${status.account.username} Â· ${formatTimeAgo(status.createdAt)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = { onMore(status.id) }) {
                    Icon(
                        imageVector = LineAwesomeIcons.EllipsisVSolid,
                        contentDescription = "More options",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Content warning - bolt-new-mockup style
            if (!status.spoilerText.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = LineAwesomeIcons.ExclamationTriangleSolid,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "CW: ${status.spoilerText}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Content text - bolt-new-mockup style
            Text(
                text = stripHtml(status.content),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Media attachments - bolt-new-mockup style with real images
            if (!status.mediaAttachments.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                MediaAttachmentsGrid(attachments = status.mediaAttachments)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action buttons - bolt-new-mockup style with kabinka-social data
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatusActionButton(
                    icon = LineAwesomeIcons.PhoneSolid,
                    count = status.repliesCount.toInt(),
                    onClick = { onReply(status.id) }
                )

                StatusActionButton(
                    icon = LineAwesomeIcons.SyncSolid,
                    count = status.reblogsCount.toInt(),
                    isActive = status.reblogged,
                    onClick = { onBoost(status.id) }
                )

                StatusActionButton(
                    icon = if (status.favourited) LineAwesomeIcons.HeartSolid else LineAwesomeIcons.HeartSolid,
                    count = status.favouritesCount.toInt(),
                    isActive = status.favourited,
                    onClick = { onFavorite(status.id) }
                )

                IconButton(onClick = { onMore(status.id) }) {
                    Icon(
                        imageVector = LineAwesomeIcons.ShareSolid,
                        contentDescription = "Share",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun MediaAttachmentsGrid(
    attachments: List<app.kabinka.social.model.Attachment>
) {
    when (attachments.size) {
        1 -> {
            // Single image - full width
            AsyncImage(
                model = attachments[0].url ?: attachments[0].previewUrl,
                contentDescription = attachments[0].description,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
        }
        2 -> {
            // Two images side by side
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                attachments.forEach { attachment ->
                    AsyncImage(
                        model = attachment.url ?: attachment.previewUrl,
                        contentDescription = attachment.description,
                        modifier = Modifier
                            .weight(1f)
                            .height(200.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
        3 -> {
            // Three images - first full, others split
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                AsyncImage(
                    model = attachments[0].url ?: attachments[0].previewUrl,
                    contentDescription = attachments[0].description,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    attachments.drop(1).forEach { attachment ->
                        AsyncImage(
                            model = attachment.url ?: attachment.previewUrl,
                            contentDescription = attachment.description,
                            modifier = Modifier
                                .weight(1f)
                                .height(150.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }
        else -> {
            // 4+ images - 2x2 grid
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                attachments.chunked(2).take(2).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        row.forEach { attachment ->
                            AsyncImage(
                                model = attachment.url ?: attachment.previewUrl,
                                contentDescription = attachment.description,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(150.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusActionButton(
    icon: ImageVector,
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

private fun formatTimeAgo(instant: Instant): String {
    val duration = Duration.between(instant, Instant.now())

    return when {
        duration.toMinutes() < 1 -> "just now"
        duration.toMinutes() < 60 -> "${duration.toMinutes()}m"
        duration.toHours() < 24 -> "${duration.toHours()}h"
        duration.toDays() < 7 -> "${duration.toDays()}d"
        else -> "${duration.toDays() / 7}w"
    }
}

private fun formatCount(count: Int): String {
    return when {
        count < 1000 -> count.toString()
        count < 10000 -> String.format("%.1fk", count / 1000.0)
        count < 1000000 -> "${count / 1000}k"
        else -> String.format("%.1fM", count / 1000000.0)
    }
}

private fun stripHtml(html: String): String {
    return html
        .replace("<br\\s*/?>".toRegex(), "\n")
        .replace("<p>", "\n")
        .replace("</p>", "")
        .replace("<[^>]*>".toRegex(), "")
        .replace("&nbsp;", " ")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&amp;", "&")
        .trim()
}
