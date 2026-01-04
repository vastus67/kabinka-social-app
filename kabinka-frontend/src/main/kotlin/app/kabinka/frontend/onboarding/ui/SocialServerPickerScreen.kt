package app.kabinka.frontend.onboarding.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

// NOTE: Phase 1 - Mastodon-only onboarding.
// Do not add future-proofing or abstractions for chat or media onboarding.

@Composable
fun MastodonInstanceInputScreen(
    defaultInstance: String = "https://mastodon.social",
    onContinue: (String) -> Unit,
    onBack: () -> Unit
) {
    var instanceUrl by remember { mutableStateOf(defaultInstance) }
    val recommendedInstances = listOf(
        "mastodon.social",
        "mastodon.online",
        "mstdn.social"
    )
    
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            IconButton(onClick = onBack) {
                Text("←", style = MaterialTheme.typography.headlineMedium)
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Choose Your Server",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Enter your Mastodon instance",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            OutlinedTextField(
                value = instanceUrl,
                onValueChange = { instanceUrl = it },
                label = { Text("Instance URL") },
                placeholder = { Text("mastodon.social") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Popular instances",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            recommendedInstances.forEach { instance ->
                AssistChip(
                    onClick = { instanceUrl = instance },
                    label = { Text(instance) },
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            Button(
                onClick = { 
                    val finalUrl = if (instanceUrl.startsWith("http")) {
                        instanceUrl
                    } else {
                        "https://$instanceUrl"
                    }
                    onContinue(finalUrl) 
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = instanceUrl.isNotBlank()
            ) {
                Text("Continue", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
