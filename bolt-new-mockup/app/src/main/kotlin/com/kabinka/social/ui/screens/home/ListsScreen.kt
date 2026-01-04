package com.kabinka.social.ui.screens.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kabinka.social.navigation.Screen
import com.kabinka.social.ui.components.KabinkaTopBar

data class UserList(
    val id: String,
    val name: String,
    val membersCount: Int
)

@Composable
fun ListsScreen(
    onNavigate: (String) -> Unit,
    onBack: () -> Unit
) {
    val lists = listOf(
        UserList("1", "Close Friends", 12),
        UserList("2", "Tech News", 45),
        UserList("3", "Outdoor Enthusiasts", 28)
    )

    Scaffold(
        topBar = {
            KabinkaTopBar(
                title = "Lists",
                onNavigationClick = onBack,
                onChatClick = { onNavigate(Screen.FluffyChat.route) }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { }
            ) {
                Icon(Icons.Outlined.Add, "Create list")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(lists) { list ->
                ListCard(
                    list = list,
                    onClick = { onNavigate(Screen.ListTimeline.createRoute(list.id)) }
                )
            }
        }
    }
}

@Composable
private fun ListCard(
    list: UserList,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = list.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${list.membersCount} members",
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
