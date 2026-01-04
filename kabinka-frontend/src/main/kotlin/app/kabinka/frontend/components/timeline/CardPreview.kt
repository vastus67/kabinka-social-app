package app.kabinka.frontend.components.timeline

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.kabinka.social.model.Card
import coil.compose.AsyncImage

@Composable
fun CardPreview(
    card: Card,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 1.dp
    ) {
        when (card.type?.name?.lowercase()) {
            "link" -> LinkCard(card)
            "photo" -> PhotoCard(card)
            "video" -> VideoCard(card)
            else -> LinkCard(card) // Default to link card
        }
    }
}

@Composable
private fun LinkCard(card: Card) {
    Column(modifier = Modifier.fillMaxWidth()) {
        if (card.image != null) {
            AsyncImage(
                model = card.image,
                contentDescription = card.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                contentScale = ContentScale.Crop
            )
        }
        
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            if (!card.providerName.isNullOrEmpty()) {
                Text(
                    text = card.providerName,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
            
            Text(
                text = card.title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            if (!card.description.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = card.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun PhotoCard(card: Card) {
    Box(modifier = Modifier.fillMaxWidth()) {
        AsyncImage(
            model = card.image ?: card.url,
            contentDescription = card.title,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 400.dp),
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
private fun VideoCard(card: Card) {
    Column(modifier = Modifier.fillMaxWidth()) {
        AsyncImage(
            model = card.image,
            contentDescription = card.title,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentScale = ContentScale.Crop
        )
        
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = card.title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
