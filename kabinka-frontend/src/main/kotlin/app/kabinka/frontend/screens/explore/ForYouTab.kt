package app.kabinka.frontend.screens.explore

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import app.kabinka.social.api.requests.accounts.GetAccountRelationships
import app.kabinka.social.api.requests.accounts.SetAccountFollowed
import app.kabinka.social.api.requests.accounts.GetFollowSuggestions
import app.kabinka.social.api.session.AccountSessionManager
import app.kabinka.social.model.Account
import app.kabinka.social.model.FollowSuggestion
import app.kabinka.social.model.Relationship
import coil.compose.AsyncImage
import me.grishka.appkit.api.Callback
import me.grishka.appkit.api.ErrorResponse

@Composable
fun ForYouTab(isLoggedIn: Boolean, onNavigateToUser: (String) -> Unit = {}) {
    var suggestions by remember { mutableStateOf<List<Account>>(emptyList()) }
    var relationships by remember { mutableStateOf<Map<String, Relationship>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        if (isLoggedIn) {
            val accountId = AccountSessionManager.getInstance().lastActiveAccountID
            if (accountId != null) {
                GetFollowSuggestions(20).setCallback(object : Callback<List<FollowSuggestion>> {
                    override fun onSuccess(result: List<FollowSuggestion>) {
                        suggestions = result.map { it.account }
                        val ids = result.map { it.account.id }
                        GetAccountRelationships(ids).setCallback(object : Callback<List<Relationship>> {
                            override fun onSuccess(rels: List<Relationship>) {
                                relationships = rels.associateBy { it.id }
                                isLoading = false
                            }
                            override fun onError(errorResponse: ErrorResponse) {
                                isLoading = false
                            }
                        }).exec(accountId)
                    }
                    override fun onError(errorResponse: ErrorResponse) {
                        isLoading = false
                    }
                }).exec(accountId)
            } else {
                isLoading = false
            }
        } else {
            isLoading = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            !isLoggedIn -> Text("Login to see suggestions", modifier = Modifier.align(Alignment.Center))
            isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            suggestions.isEmpty() -> Text("No suggestions", modifier = Modifier.align(Alignment.Center))
            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(0.dp),
                    verticalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    items(suggestions) { account ->
                        SuggestionCard(
                            account = account,
                            relationship = relationships[account.id],
                            onFollowClick = { follow ->
                                val accountId = AccountSessionManager.getInstance().lastActiveAccountID
                                if (accountId != null) {
                                    SetAccountFollowed(account.id, follow, true, false).setCallback(object : Callback<Relationship> {
                                        override fun onSuccess(result: Relationship) {
                                            relationships = relationships + (account.id to result)
                                        }
                                        override fun onError(errorResponse: ErrorResponse) {}
                                    }).exec(accountId)
                                }
                            },
                            onNavigateToUser = onNavigateToUser
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SuggestionCard(
    account: Account,
    relationship: Relationship?,
    onFollowClick: (Boolean) -> Unit,
    onNavigateToUser: (String) -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onNavigateToUser(account.id) },
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = account.avatar,
                contentDescription = null,
                modifier = Modifier.size(48.dp).clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            
            Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                Text(account.displayName, style = MaterialTheme.typography.titleSmall)
                Text("@${account.acct}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                account.note?.let { note ->
                    if (note.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            note.replace(Regex("<[^>]*>"), ""),
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 2
                        )
                    }
                }
            }
            
            Button(
                onClick = { onFollowClick(relationship?.following != true) },
                colors = if (relationship?.following == true) {
                    ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                } else {
                    ButtonDefaults.buttonColors()
                }
            ) {
                Text(if (relationship?.following == true) "Unfollow" else "Follow")
            }
        }
    }
}
