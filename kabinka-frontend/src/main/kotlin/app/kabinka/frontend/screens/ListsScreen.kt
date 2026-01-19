package app.kabinka.frontend.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.kabinka.social.api.requests.lists.GetLists
import app.kabinka.social.api.session.AccountSessionManager
import me.grishka.appkit.api.Callback
import me.grishka.appkit.api.ErrorResponse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCreateList: () -> Unit,
    onNavigateToEditList: (String) -> Unit
) {
    var lists by remember { mutableStateOf<List<app.kabinka.social.model.FollowList>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Load lists from API
    LaunchedEffect(Unit) {
        try {
            val session = AccountSessionManager.getInstance().lastActiveAccount
            if (session != null) {
                GetLists()
                    .setCallback(object : Callback<List<app.kabinka.social.model.FollowList>> {
                        override fun onSuccess(result: List<app.kabinka.social.model.FollowList>) {
                            lists = result
                            isLoading = false
                        }

                        override fun onError(error: ErrorResponse?) {
                            errorMessage = error?.toString() ?: "Failed to load lists"
                            isLoading = false
                        }
                    })
                    .exec(session.getID())
            } else {
                errorMessage = "Please log in to view lists"
                isLoading = false
            }
        } catch (e: Exception) {
            errorMessage = e.message ?: "An error occurred"
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lists") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToCreateList) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Create list"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            errorMessage != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = errorMessage!!,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onNavigateBack) {
                            Text("Go Back")
                        }
                    }
                }
            }
            lists.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No lists yet",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Create a list to get started",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onNavigateToCreateList) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Create list")
                        }
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    items(lists) { list ->
                        ListItem(
                            headlineContent = { Text(list.title) },
                            supportingContent = {
                                val policyText = when (list.repliesPolicy) {
                                    app.kabinka.social.model.FollowList.RepliesPolicy.LIST -> "Members only"
                                    app.kabinka.social.model.FollowList.RepliesPolicy.FOLLOWED -> "People you follow"
                                    app.kabinka.social.model.FollowList.RepliesPolicy.NONE -> "No replies"
                                    else -> ""
                                }
                                Text(policyText)
                            },
                            trailingContent = {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = null
                                )
                            },
                            modifier = Modifier.clickable {
                                onNavigateToEditList(list.id)
                            }
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}
