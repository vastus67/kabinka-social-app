package com.kabinka.social.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kabinka.social.ui.components.KabinkaTopBar

@Composable
fun NotificationSettingsScreen(
    onBack: () -> Unit
) {
    var pushEnabled by remember { mutableStateOf(true) }
    var soundEnabled by remember { mutableStateOf(true) }
    var vibrationEnabled by remember { mutableStateOf(true) }
    var mentions by remember { mutableStateOf(true) }
    var follows by remember { mutableStateOf(true) }
    var boosts by remember { mutableStateOf(true) }
    var favorites by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            KabinkaTopBar(
                title = "Notifications",
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "General",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            item {
                NotificationToggle(
                    title = "Push Notifications",
                    subtitle = "Receive notifications",
                    checked = pushEnabled,
                    onCheckedChange = { pushEnabled = it }
                )
            }

            item {
                NotificationToggle(
                    title = "Sound",
                    subtitle = "Play sound for notifications",
                    checked = soundEnabled,
                    onCheckedChange = { soundEnabled = it },
                    enabled = pushEnabled
                )
            }

            item {
                NotificationToggle(
                    title = "Vibration",
                    subtitle = "Vibrate on notifications",
                    checked = vibrationEnabled,
                    onCheckedChange = { vibrationEnabled = it },
                    enabled = pushEnabled
                )
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Notification Types",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            item {
                NotificationToggle(
                    title = "Mentions",
                    subtitle = "When someone mentions you",
                    checked = mentions,
                    onCheckedChange = { mentions = it },
                    enabled = pushEnabled
                )
            }

            item {
                NotificationToggle(
                    title = "Follows",
                    subtitle = "When someone follows you",
                    checked = follows,
                    onCheckedChange = { follows = it },
                    enabled = pushEnabled
                )
            }

            item {
                NotificationToggle(
                    title = "Boosts",
                    subtitle = "When someone boosts your post",
                    checked = boosts,
                    onCheckedChange = { boosts = it },
                    enabled = pushEnabled
                )
            }

            item {
                NotificationToggle(
                    title = "Favorites",
                    subtitle = "When someone favorites your post",
                    checked = favorites,
                    onCheckedChange = { favorites = it },
                    enabled = pushEnabled
                )
            }
        }
    }
}

@Composable
private fun NotificationToggle(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (enabled) MaterialTheme.colorScheme.onSurface
                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                    alpha = if (enabled) 1f else 0.6f
                )
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled
        )
    }
}
