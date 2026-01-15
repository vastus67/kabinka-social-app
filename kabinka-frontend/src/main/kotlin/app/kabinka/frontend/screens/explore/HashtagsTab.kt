package app.kabinka.frontend.screens.explore

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.kabinka.social.api.requests.trends.GetTrendingHashtags
import app.kabinka.social.model.Hashtag
import me.grishka.appkit.api.Callback
import me.grishka.appkit.api.ErrorResponse

@Composable
fun HashtagsTab() {
    var hashtags by remember { mutableStateOf<List<Hashtag>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        GetTrendingHashtags(20)
            .setCallback(object : Callback<List<Hashtag>> {
                override fun onSuccess(result: List<Hashtag>) {
                    hashtags = result
                    isLoading = false
                }
                override fun onError(errorResponse: ErrorResponse) {
                    isLoading = false
                }
            }).execNoAuth("mastodon.social")
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            hashtags.isEmpty() -> Text("No hashtags", modifier = Modifier.align(Alignment.Center))
            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(0.dp),
                    verticalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    items(hashtags) { hashtag ->
                        HashtagCard(hashtag)
                    }
                }
            }
        }
    }
}

@Composable
private fun HashtagCard(hashtag: Hashtag) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("#${hashtag.name}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                hashtag.history?.firstOrNull()?.let {
                    Text("${it.accounts} people talking", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Text("#", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
        }
    }
}
