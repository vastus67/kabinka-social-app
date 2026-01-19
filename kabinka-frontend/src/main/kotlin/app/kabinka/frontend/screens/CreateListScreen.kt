package app.kabinka.frontend.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.kabinka.social.api.requests.lists.CreateList
import app.kabinka.social.api.session.AccountSessionManager
import me.grishka.appkit.api.Callback
import me.grishka.appkit.api.ErrorResponse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateListScreen(
    onNavigateBack: () -> Unit,
    onNavigateToManageMembers: (listName: String, showRepliesTo: ShowRepliesToOption, hideMembers: Boolean) -> Unit
) {
    var listName by remember { mutableStateOf("") }
    var showRepliesTo by remember { mutableStateOf(ShowRepliesToOption.MEMBERS) }
    var hideMembers by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    var isCreating by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create list") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                // List Name TextField
                OutlinedTextField(
                    value = listName,
                    onValueChange = { 
                        listName = it
                        errorMessage = null
                    },
                    label = { Text("List name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = errorMessage != null
                )
                
                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Show replies to dropdown
                Text(
                    text = "Show replies to",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = showRepliesTo.displayName,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        ShowRepliesToOption.values().forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.displayName) },
                                onClick = {
                                    showRepliesTo = option
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Hide members toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Hide members in Following",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    FilterChip(
                        selected = hideMembers,
                        onClick = { hideMembers = !hideMembers },
                        label = {
                            Text(if (hideMembers) "Hidden" else "Visible")
                        }
                    )
                }
            }

            // Create Button
            Button(
                onClick = {
                    if (listName.isNotBlank()) {
                        isCreating = true
                        errorMessage = null
                        
                        try {
                            val session = AccountSessionManager.getInstance().lastActiveAccount
                            if (session != null) {
                                val repliesPolicy = when (showRepliesTo) {
                                    ShowRepliesToOption.MEMBERS -> app.kabinka.social.model.FollowList.RepliesPolicy.LIST
                                    ShowRepliesToOption.FOLLOWING -> app.kabinka.social.model.FollowList.RepliesPolicy.FOLLOWED
                                    ShowRepliesToOption.NO_ONE -> app.kabinka.social.model.FollowList.RepliesPolicy.NONE
                                }
                                
                                CreateList(listName, repliesPolicy, hideMembers)
                                    .setCallback(object : Callback<app.kabinka.social.model.FollowList> {
                                        override fun onSuccess(result: app.kabinka.social.model.FollowList) {
                                            isCreating = false
                                            onNavigateToManageMembers(listName, showRepliesTo, hideMembers)
                                        }
                                        
                                        override fun onError(error: ErrorResponse?) {
                                            isCreating = false
                                            errorMessage = error?.toString() ?: "Failed to create list"
                                        }
                                    })
                                    .exec(session.getID())
                            } else {
                                isCreating = false
                                errorMessage = "Please log in to create lists"
                            }
                        } catch (e: Exception) {
                            isCreating = false
                            errorMessage = e.message ?: "An error occurred"
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = listName.isNotBlank() && !isCreating
            ) {
                if (isCreating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Create")
                }
            }
        }
    }
}
