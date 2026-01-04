package com.kabinka.social.ui.screens.onboarding

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

data class MastodonInstance(
    val domain: String,
    val name: String,
    val description: String,
    val users: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerPickerScreen(
    onServerSelected: (String) -> Unit,
    onBack: () -> Unit
) {
    var customServer by remember { mutableStateOf("") }

    val recommendedServers = listOf(
        MastodonInstance(
            "mastodon.social",
            "Mastodon Social",
            "The original Mastodon instance",
            "500K+"
        ),
        MastodonInstance(
            "fosstodon.org",
            "Fosstodon",
            "Hub for free and open source software",
            "50K+"
        ),
        MastodonInstance(
            "mas.to",
            "Mas.to",
            "General purpose instance",
            "30K+"
        ),
        MastodonInstance(
            "mastodon.online",
            "Mastodon Online",
            "Another general purpose instance",
            "100K+"
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Choose Your Instance") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Recommended Instances",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(recommendedServers) { instance ->
                InstanceCard(
                    instance = instance,
                    onClick = { onServerSelected(instance.domain) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Custom Instance",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                OutlinedTextField(
                    value = customServer,
                    onValueChange = { customServer = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Instance domain") },
                    placeholder = { Text("e.g., mastodon.social") },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { onServerSelected(customServer) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = customServer.isNotBlank()
                ) {
                    Text("Continue")
                }
            }
        }
    }
}

@Composable
private fun InstanceCard(
    instance: MastodonInstance,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = instance.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = instance.users,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = instance.domain,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = instance.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
