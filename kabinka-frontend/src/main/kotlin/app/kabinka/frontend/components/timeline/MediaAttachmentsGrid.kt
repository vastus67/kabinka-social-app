package app.kabinka.frontend.components.timeline

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import app.kabinka.social.model.Attachment
import coil.compose.AsyncImage

@Composable
fun MediaAttachmentsGrid(
    attachments: List<Attachment>,
    sensitive: Boolean,
    onMediaClick: (Int) -> Unit
) {
    var showSensitive by remember { mutableStateOf(!sensitive) }
    
    Box {
        when (attachments.size) {
            1 -> SingleMedia(attachments[0], showSensitive, onMediaClick, 0)
            2 -> TwoMediaGrid(attachments, showSensitive, onMediaClick)
            3 -> ThreeMediaGrid(attachments, showSensitive, onMediaClick)
            else -> FourMediaGrid(attachments, showSensitive, onMediaClick)
        }
        
        if (sensitive && !showSensitive) {
            SensitiveOverlay(onClick = { showSensitive = true })
        }
    }
}

@Composable
private fun SingleMedia(
    attachment: Attachment,
    show: Boolean,
    onClick: (Int) -> Unit,
    index: Int
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 400.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick(index) }
    ) {
        AsyncImage(
            model = attachment.previewUrl ?: attachment.url,
            contentDescription = attachment.description,
            modifier = Modifier
                .fillMaxWidth()
                .then(if (!show) Modifier.blur(20.dp) else Modifier),
            contentScale = ContentScale.Crop
        )
        
        val typeName = attachment.type?.name?.lowercase()
        if (typeName == "video" || typeName == "gifv") {
            VideoIndicator()
        }
    }
}

@Composable
private fun TwoMediaGrid(
    attachments: List<Attachment>,
    show: Boolean,
    onClick: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        attachments.forEachIndexed { index, attachment ->
            MediaItem(
                attachment = attachment,
                show = show,
                modifier = Modifier
                    .weight(1f)
                    .height(200.dp),
                onClick = { onClick(index) }
            )
        }
    }
}

@Composable
private fun ThreeMediaGrid(
    attachments: List<Attachment>,
    show: Boolean,
    onClick: (Int) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        MediaItem(
            attachment = attachments[0],
            show = show,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            onClick = { onClick(0) }
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            attachments.drop(1).forEachIndexed { index, attachment ->
                MediaItem(
                    attachment = attachment,
                    show = show,
                    modifier = Modifier
                        .weight(1f)
                        .height(150.dp),
                    onClick = { onClick(index + 1) }
                )
            }
        }
    }
}

@Composable
private fun FourMediaGrid(
    attachments: List<Attachment>,
    show: Boolean,
    onClick: (Int) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        attachments.chunked(2).take(2).forEachIndexed { rowIndex, row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                row.forEachIndexed { colIndex, attachment ->
                    MediaItem(
                        attachment = attachment,
                        show = show,
                        modifier = Modifier
                            .weight(1f)
                            .height(150.dp),
                        onClick = { onClick(rowIndex * 2 + colIndex) }
                    )
                }
            }
        }
    }
}

@Composable
private fun MediaItem(
    attachment: Attachment,
    show: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    ) {
        AsyncImage(
            model = attachment.previewUrl ?: attachment.url,
            contentDescription = attachment.description,
            modifier = Modifier
                .fillMaxSize()
                .then(if (!show) Modifier.blur(20.dp) else Modifier),
            contentScale = ContentScale.Crop
        )
        
        val typeName = attachment.type?.name?.lowercase()
        if (typeName == "video" || typeName == "gifv") {
            VideoIndicator()
        }
    }
}

@Composable
private fun BoxScope.VideoIndicator() {
    Surface(
        modifier = Modifier
            .align(Alignment.Center)
            .size(48.dp),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = "Video",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun BoxScope.SensitiveOverlay(onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Warning,
                contentDescription = "Sensitive content",
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Sensitive content",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Click to view",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
