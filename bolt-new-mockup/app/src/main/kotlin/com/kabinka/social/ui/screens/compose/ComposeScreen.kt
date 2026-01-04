package com.kabinka.social.ui.screens.compose

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kabinka.social.data.models.Visibility
import com.kabinka.social.ui.components.KabinkaTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComposeScreen(
    onBack: () -> Unit,
    onPostSuccess: () -> Unit
) {
    var content by remember { mutableStateOf("") }
    var contentWarning by remember { mutableStateOf("") }
    var showCWField by remember { mutableStateOf(false) }
    var visibility by remember { mutableStateOf(Visibility.PUBLIC) }
    var showVisibilityMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            KabinkaTopBar(
                title = "Compose",
                onNavigationClick = onBack,
                onChatClick = { },
                actions = {
                    TextButton(
                        onClick = onPostSuccess,
                        enabled = content.isNotBlank()
                    ) {
                        Text("Post")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (showCWField) {
                OutlinedTextField(
                    value = contentWarning,
                    onValueChange = { contentWarning = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Content warning") },
                    placeholder = { Text("Write your warning here") },
                    singleLine = true
                )
            }

            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 200.dp),
                placeholder = { Text("What's on your mind?") },
                maxLines = 10
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = { }) {
                    Icon(Icons.Outlined.Image, "Add media")
                }

                IconButton(onClick = { }) {
                    Icon(Icons.Outlined.Poll, "Create poll")
                }

                IconButton(onClick = { }) {
                    Icon(Icons.Outlined.EmojiEmotions, "Add emoji")
                }

                IconButton(onClick = { showCWField = !showCWField }) {
                    Icon(
                        Icons.Outlined.Warning,
                        "Content warning",
                        tint = if (showCWField) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Box {
                    TextButton(onClick = { showVisibilityMenu = true }) {
                        Icon(
                            imageVector = when (visibility) {
                                Visibility.PUBLIC -> Icons.Outlined.Public
                                Visibility.UNLISTED -> Icons.Outlined.LockOpen
                                Visibility.FOLLOWERS -> Icons.Outlined.Group
                                Visibility.DIRECT -> Icons.Outlined.Email
                            },
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(visibility.name.lowercase().replaceFirstChar { it.uppercase() })
                    }

                    DropdownMenu(
                        expanded = showVisibilityMenu,
                        onDismissRequest = { showVisibilityMenu = false }
                    ) {
                        Visibility.entries.forEach { vis ->
                            DropdownMenuItem(
                                text = { Text(vis.name.lowercase().replaceFirstChar { it.uppercase() }) },
                                onClick = {
                                    visibility = vis
                                    showVisibilityMenu = false
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = when (vis) {
                                            Visibility.PUBLIC -> Icons.Outlined.Public
                                            Visibility.UNLISTED -> Icons.Outlined.LockOpen
                                            Visibility.FOLLOWERS -> Icons.Outlined.Group
                                            Visibility.DIRECT -> Icons.Outlined.Email
                                        },
                                        contentDescription = null
                                    )
                                }
                            )
                        }
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Preview",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = content.ifBlank { "Your post will appear here..." },
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (content.isBlank()) MaterialTheme.colorScheme.onSurfaceVariant.copy(
                            alpha = 0.6f
                        ) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
