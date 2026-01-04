package app.kabinka.frontend.components.timeline

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.kabinka.social.model.Poll
import java.time.Duration
import java.time.Instant

@Composable
fun PollView(
    poll: Poll,
    onVote: (List<Int>) -> Unit
) {
    var selectedOptions by remember { mutableStateOf(poll.ownVotes?.toSet() ?: emptySet()) }
    val hasVoted = poll.voted || !poll.ownVotes.isNullOrEmpty()
    val isExpired = poll.expiresAt?.let { Instant.now().isAfter(it) } ?: false
    val canVote = !hasVoted && !isExpired
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        poll.options.forEachIndexed { index, option ->
            PollOption(
                option = option,
                index = index,
                totalVotes = poll.votesCount.toInt(),
                isSelected = selectedOptions.contains(index),
                hasVoted = hasVoted,
                canSelect = canVote,
                onSelect = {
                    selectedOptions = if (poll.multiple) {
                        if (selectedOptions.contains(index)) {
                            selectedOptions - index
                        } else {
                            selectedOptions + index
                        }
                    } else {
                        setOf(index)
                    }
                }
            )
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${poll.votesCount} votes",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            if (poll.expiresAt != null) {
                Text(
                    text = if (isExpired) "Closed" else "Closes ${formatDuration(poll.expiresAt)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        if (canVote && selectedOptions.isNotEmpty()) {
            Button(
                onClick = { onVote(selectedOptions.toList()) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Vote")
            }
        }
    }
}

@Composable
private fun PollOption(
    option: Poll.Option,
    index: Int,
    totalVotes: Int,
    isSelected: Boolean,
    hasVoted: Boolean,
    canSelect: Boolean,
    onSelect: () -> Unit
) {
    val percentage = if (totalVotes > 0) {
        (option.votesCount ?: 0).toFloat() / totalVotes * 100
    } else 0f
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = if (hasVoted) {
            if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        } else {
            if (isSelected) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        },
        onClick = if (canSelect) onSelect else ({})
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            if (hasVoted) {
                // Show results bar
                Surface(
                    modifier = Modifier
                        .fillMaxWidth(percentage / 100f)
                        .matchParentSize(),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(8.dp)
                ) {}
            }
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = option.title,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                
                if (hasVoted) {
                    Text(
                        text = "${percentage.toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

private fun formatDuration(expiresAt: Instant): String {
    val duration = Duration.between(Instant.now(), expiresAt)
    return when {
        duration.toMinutes() < 60 -> "in ${duration.toMinutes()}m"
        duration.toHours() < 24 -> "in ${duration.toHours()}h"
        else -> "in ${duration.toDays()}d"
    }
}
