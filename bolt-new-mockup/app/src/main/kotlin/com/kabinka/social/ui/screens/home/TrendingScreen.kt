package com.kabinka.social.ui.screens.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kabinka.social.data.models.mockStatuses
import com.kabinka.social.navigation.Screen
import com.kabinka.social.ui.components.*

data class TrendingHashtag(
    val tag: String,
    val count: Int
)

@Composable
fun TrendingScreen(
    onNavigate: (String) -> Unit,
    onBack: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Posts", "Hashtags", "News")

    val trendingHashtags = listOf(
        TrendingHashtag("hiking", 1234),
        TrendingHashtag("photography", 892),
        TrendingHashtag("nature", 756),
        TrendingHashtag("outdoors", 645)
    )

    Scaffold(
        topBar = {
            Column {
                KabinkaTopBar(
                    title = "Trending",
                    onNavigationClick = onBack,
                    onChatClick = { onNavigate(Screen.FluffyChat.route) }
                )
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        when (selectedTab) {
            0 -> {
                val statuses = mockStatuses()
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
            1 -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(trendingHashtags) { hashtag ->
                        HashtagCard(
                            hashtag = hashtag,
                            onClick = { onNavigate(Screen.HashtagTimeline.createRoute(hashtag.tag)) }
                        )
                    }
                }
            }
            2 -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    EmptyState(
                        icon = androidx.compose.material.icons.Icons.Outlined.Newspaper,
                        title = "No News",
                        message = "Trending news will appear here"
                    )
                }
            }
        }
    }
}

@Composable
private fun HashtagCard(
    hashtag: TrendingHashtag,
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
                    text = "#${hashtag.tag}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${hashtag.count} posts",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
