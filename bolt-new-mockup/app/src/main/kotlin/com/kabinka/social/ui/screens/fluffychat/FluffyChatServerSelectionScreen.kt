package com.kabinka.social.ui.screens.fluffychat

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kabinka.social.ui.components.KabinkaTopBar

data class MatrixServer(
    val name: String,
    val domain: String,
    val description: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FluffyChatServerSelectionScreen(
    onServerSelected: () -> Unit,
    onBack: () -> Unit
) {
    var customServer by remember { mutableStateOf("") }

    val servers = listOf(
        MatrixServer(
            "Matrix.org",
            "matrix.org",
            "The flagship Matrix homeserver"
        ),
        MatrixServer(
            "Beeper",
            "beeper.com",
            "All-in-one messaging platform"
        ),
        MatrixServer(
            "Element.io",
            "element.io",
            "Premium Matrix hosting"
        )
    )

    Scaffold(
        topBar = {
            KabinkaTopBar(
                title = "Choose Matrix Server",
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Recommended Servers",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(servers) { server ->
                ServerCard(
                    server = server,
                    onClick = onServerSelected
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Custom Server",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = customServer,
                    onValueChange = { customServer = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Server address") },
                    placeholder = { Text("e.g., matrix.org") },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onServerSelected,
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
private fun ServerCard(
    server: MatrixServer,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = server.name,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = server.domain,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = server.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
