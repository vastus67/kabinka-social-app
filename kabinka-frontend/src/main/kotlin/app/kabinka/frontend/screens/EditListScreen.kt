package app.kabinka.frontend.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import app.kabinka.social.api.requests.lists.GetListAccounts
import app.kabinka.social.api.requests.lists.UpdateList
import app.kabinka.social.api.requests.lists.DeleteList
import app.kabinka.social.api.requests.lists.GetLists
import app.kabinka.social.api.session.AccountSessionManager
import me.grishka.appkit.api.Callback
import me.grishka.appkit.api.ErrorResponse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditListScreen(
    listId: String,
    onNavigateBack: () -> Unit,
    onListDeleted: () -> Unit
) {
    var listName by remember { mutableStateOf("") }
    var showRepliesTo by remember { mutableStateOf(ShowRepliesToOption.MEMBERS) }
    var hideMembers by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    var members by remember { mutableStateOf<List<app.kabinka.social.model.Account>>(emptyList()) }
    var showAddMemberDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    
    // Load list data and members from API
    LaunchedEffect(listId) {
        try {
            val session = AccountSessionManager.getInstance().lastActiveAccount
            if (session != null) {
                // Load all lists to find this one
                GetLists()
                    .setCallback(object : Callback<List<app.kabinka.social.model.FollowList>> {
                        override fun onSuccess(result: List<app.kabinka.social.model.FollowList>) {
                            val list = result.find { it.id == listId }
                            if (list != null) {
                                listName = list.title
                                showRepliesTo = when (list.repliesPolicy) {
                                    app.kabinka.social.model.FollowList.RepliesPolicy.LIST -> ShowRepliesToOption.MEMBERS
                                    app.kabinka.social.model.FollowList.RepliesPolicy.FOLLOWED -> ShowRepliesToOption.FOLLOWING
                                    app.kabinka.social.model.FollowList.RepliesPolicy.NONE -> ShowRepliesToOption.NO_ONE
                                    else -> ShowRepliesToOption.MEMBERS
                                }
                                hideMembers = list.exclusive
                            }
                            isLoading = false
                        }
                        
                        override fun onError(error: ErrorResponse?) {
                            isLoading = false
                        }
                    })
                    .exec(session.getID())
                
                // Load list members
                GetListAccounts(listId, null, 100)
                    .setCallback(object : Callback<app.kabinka.social.model.HeaderPaginationList<app.kabinka.social.model.Account>> {
                        override fun onSuccess(result: app.kabinka.social.model.HeaderPaginationList<app.kabinka.social.model.Account>) {
                            members = result
                        }
                        
                        override fun onError(error: ErrorResponse?) {
                            // Handle error
                        }
                    })
                    .exec(session.getID())
            }
        } catch (e: Exception) {
            isLoading = false
        }
    }
    
    // Function to save list changes
    fun saveListChanges() {
        isSaving = true
        try {
            val session = AccountSessionManager.getInstance().lastActiveAccount
            if (session != null) {
                val repliesPolicy = when (showRepliesTo) {
                    ShowRepliesToOption.MEMBERS -> app.kabinka.social.model.FollowList.RepliesPolicy.LIST
                    ShowRepliesToOption.FOLLOWING -> app.kabinka.social.model.FollowList.RepliesPolicy.FOLLOWED
                    ShowRepliesToOption.NO_ONE -> app.kabinka.social.model.FollowList.RepliesPolicy.NONE
                }
                
                UpdateList(listId, listName, repliesPolicy, hideMembers)
                    .setCallback(object : Callback<app.kabinka.social.model.FollowList> {
                        override fun onSuccess(result: app.kabinka.social.model.FollowList) {
                            isSaving = false
                        }
                        
                        override fun onError(error: ErrorResponse?) {
                            isSaving = false
                        }
                    })
                    .exec(session.getID())
            }
        } catch (e: Exception) {
            isSaving = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit list") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete list"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Settings Section
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = "List Settings",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // List Name TextField
                    OutlinedTextField(
                        value = listName,
                        onValueChange = { listName = it },
                        label = { Text("List name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

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

                    Spacer(modifier = Modifier.height(16.dp))

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
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Save button
                    Button(
                        onClick = { saveListChanges() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = listName.isNotBlank() && !isSaving
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text("Save Changes")
                        }
                    }
                }
            }

            // Members Section
            item {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider()
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Members (${members.size})",
                            style = MaterialTheme.typography.titleMedium
                        )
                        IconButton(onClick = { showAddMemberDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add member"
                            )
                        }
                    }
                }
            }

            if (members.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No members yet",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Tap + to add members to this list",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(members) { member ->
                    MemberItem(
                        member = member,
                        onRemove = {
                            members = members.filter { it.id != member.id }
                        }
                    )
                }
            }
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete list?") },
            text = { Text("This will permanently delete \"$listName\" and cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val session = AccountSessionManager.getInstance().lastActiveAccount
                        if (session != null) {
                            DeleteList(listId)
                                .setCallback(object : Callback<Void> {
                                    override fun onSuccess(result: Void?) {
                                        showDeleteDialog = false
                                        onListDeleted()
                                    }
                                    
                                    override fun onError(error: ErrorResponse?) {
                                        showDeleteDialog = false
                                    }
                                })
                                .exec(session.getID())
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Note: Add member functionality would require account search API
    // For now, we'll keep the dialog simple or remove it
}

@Composable
private fun MemberItem(
    member: app.kabinka.social.model.Account,
    onRemove: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            if (member.avatar != null) {
                AsyncImage(
                    model = member.avatar,
                    contentDescription = null,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = member.displayName?.firstOrNull()?.toString()?.uppercase() ?: "?",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = member.displayName ?: member.username,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "@${member.acct}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove member"
                )
            }
        }
    }
    HorizontalDivider()
}
