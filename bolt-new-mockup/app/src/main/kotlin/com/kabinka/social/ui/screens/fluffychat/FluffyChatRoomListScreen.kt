package com.kabinka.social.ui.screens.fluffychat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.kabinka.social.navigation.Screen
import com.kabinka.social.ui.components.KabinkaTopBar

data class ChatRoom(
    val id: String,
    val name: String,
    val lastMessage: String,
    val unread: Boolean = false
)

@Composable
fun FluffyChatRoomListScreen(
    onNavigate: (String) -> Unit,
    onBack: () -> Unit
) {
    val rooms = listOf(
        ChatRoom("1", "General", "Hey everyone!", true),
        ChatRoom("2", "Outdoor Adventures", "Anyone up for a hike?", false),
        ChatRoom("3", "Photography", "Check out this shot!", false)
    )

    Scaffold(
        topBar = {
            KabinkaTopBar(
                title = "FluffyChat",
                onNavigationClick = onBack,
                onChatClick = { }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { }
            ) {
                Icon(Icons.Outlined.Add, "New chat")
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
            items(rooms) { room ->
                RoomItem(
                    room = room,
                    onClick = { onNavigate(Screen.FluffyChatRoom.createRoute(room.id)) }
                )
            }
        }
    }
}

@Composable
private fun RoomItem(
    room: ChatRoom,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (room.unread) MaterialTheme.colorScheme.primaryContainer.copy(
                alpha = 0.3f
            )
            else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.tertiaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = room.name.first().toString(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = room.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = room.lastMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
    }
}
