package com.kabinka.social.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kabinka.social.data.models.mockStatuses
import com.kabinka.social.navigation.Screen
import com.kabinka.social.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigate: (String) -> Unit
) {
    val statuses = mockStatuses()
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Home", "Local", "Federated")

    Scaffold(
        topBar = {
            Column {
                KabinkaTopBarWithInstance(
                    onDrawerOpen = { },
                    onChatClick = { onNavigate(Screen.FluffyChat.route) }
                )
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = {
                                selectedTab = index
                                when (index) {
                                    1 -> onNavigate(Screen.LocalTimeline.route)
                                    2 -> onNavigate(Screen.FederatedTimeline.route)
                                }
                            },
                            text = { Text(title) }
                        )
                    }
                }
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
            items(statuses) { status ->
                StatusCard(
                    status = status,
                    onStatusClick = { onNavigate(Screen.StatusDetail.createRoute(it)) },
                    onProfileClick = { onNavigate(Screen.ProfileDetail.createRoute(it)) },
                    onReply = { onNavigate(Screen.ComposeReply.createRoute(it)) },
                    onBoost = { },
                    onFavorite = { },
                    onMore = { }
                )
            }
        }
    }
}
