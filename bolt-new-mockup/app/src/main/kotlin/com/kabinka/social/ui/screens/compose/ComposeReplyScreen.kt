package com.kabinka.social.ui.screens.compose

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kabinka.social.data.models.Visibility
import com.kabinka.social.data.models.mockStatuses
import com.kabinka.social.ui.components.KabinkaTopBar
import com.kabinka.social.ui.components.StatusCard
import com.kabinka.social.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComposeReplyScreen(
    statusId: String,
    onBack: () -> Unit,
    onPostSuccess: () -> Unit
) {
    var content by remember { mutableStateOf("") }
    val statuses = mockStatuses()
    val replyingTo = statuses.firstOrNull { it.id == statusId } ?: statuses.first()

    Scaffold(
        topBar = {
            KabinkaTopBar(
                title = "Reply",
                onNavigationClick = onBack,
                onChatClick = { },
                actions = {
                    TextButton(
                        onClick = onPostSuccess,
                        enabled = content.isNotBlank()
                    ) {
                        Text("Reply")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Replying to ${replyingTo.author.username}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = replyingTo.content,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 3
                    )
                }
            }

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 150.dp),
                    placeholder = { Text("Write your reply...") },
                    maxLines = 8
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(onClick = { }) {
                        Icon(Icons.Outlined.Image, "Add media")
                    }

                    IconButton(onClick = { }) {
                        Icon(Icons.Outlined.EmojiEmotions, "Add emoji")
                    }
                }
            }
        }
    }
}
