package app.kabinka.frontend.components.timeline

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
            icon = LineAwesomeIcons.ReplySolid,
            count = repliesCount,
            onClick = onReply
        )

        ActionButton(
            icon = LineAwesomeIcons.SyncSolid,
            count = reblogsCount,
            isActive = reblogged,
            onClick = onBoost
        )

        ActionButton(
            icon = if (favourited) LineAwesomeIcons.HeartSolid else LineAwesomeIcons.HeartSolid,
            count = favouritesCount,
            isActive = favourited,
            onClick = onFavorite
        )
        
        IconButton(onClick = onBookmark) {
            Icon(
                imageVector = if (bookmarked) LineAwesomeIcons.StarSolid else LineAwesomeIcons.StarSolid,
                contentDescription = "Bookmark",
                tint = if (bookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        IconButton(onClick = onShare) {
            Icon(
                imageVector = LineAwesomeIcons.ShareSolid,
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
