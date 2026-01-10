package app.kabinka.frontend.components.timeline

import compose.icons.FeatherIcons
import compose.icons.feathericons.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun StatusActions(
    repliesCount: Int,
    reblogsCount: Int,
    favouritesCount: Int,
    reblogged: Boolean,
    favourited: Boolean,
    bookmarked: Boolean,
    onReply: () -> Unit,
    onBoost: () -> Unit,
    onFavorite: () -> Unit,
    onBookmark: () -> Unit,
    onShare: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        ActionButton(
            icon = FeatherIcons.CornerUpLeft,
            count = repliesCount,
            onClick = onReply
        )

        ActionButton(
            icon = FeatherIcons.RefreshCw,
            count = reblogsCount,
            isActive = reblogged,
            onClick = onBoost
        )

        ActionButton(
            icon = if (favourited) FeatherIcons.Heart else FeatherIcons.Heart,
            count = favouritesCount,
            isActive = favourited,
            onClick = onFavorite
        )
        
        IconButton(onClick = onBookmark) {
            Icon(
                imageVector = if (bookmarked) FeatherIcons.Star else FeatherIcons.Star,
                contentDescription = "Bookmark",
                tint = if (bookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        IconButton(onClick = onShare) {
            Icon(
                imageVector = FeatherIcons.Share2,
                contentDescription = "Share",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ActionButton(
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

private fun formatCount(count: Int): String {
    return when {
        count < 1000 -> count.toString()
        count < 10000 -> String.format("%.1fk", count / 1000.0)
        count < 1000000 -> "${count / 1000}k"
        else -> String.format("%.1fM", count / 1000000.0)
    }
}
