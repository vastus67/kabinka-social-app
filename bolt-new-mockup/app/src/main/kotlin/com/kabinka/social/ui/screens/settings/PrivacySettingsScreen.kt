package com.kabinka.social.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kabinka.social.ui.components.KabinkaTopBar

@Composable
fun PrivacySettingsScreen(
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            KabinkaTopBar(
                title = "Privacy & Safety",
                onNavigationClick = onBack,
                onChatClick = { }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text(
                    text = "Content Preferences",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            item {
                PrivacyItem(
                    icon = Icons.Outlined.FilterList,
                    title = "Filtered Words",
                    subtitle = "Manage filtered content",
                    onClick = { }
                )
            }

            item {
                PrivacyItem(
                    icon = Icons.Outlined.Block,
                    title = "Blocked Accounts",
                    subtitle = "View and manage blocks",
                    onClick = { }
                )
            }

            item {
                PrivacyItem(
                    icon = Icons.Outlined.VolumeOff,
                    title = "Muted Accounts",
                    subtitle = "View and manage mutes",
                    onClick = { }
                )
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Profile Privacy",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            item {
                PrivacyItem(
                    icon = Icons.Outlined.Lock,
                    title = "Private Account",
                    subtitle = "Require approval for followers",
                    onClick = { }
                )
            }

            item {
                PrivacyItem(
                    icon = Icons.Outlined.SearchOff,
                    title = "Discoverable",
                    subtitle = "Allow discovery in search",
                    onClick = { }
                )
            }
        }
    }
}

@Composable
private fun PrivacyItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
