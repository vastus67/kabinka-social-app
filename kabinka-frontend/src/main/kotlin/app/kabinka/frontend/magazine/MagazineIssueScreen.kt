package app.kabinka.frontend.magazine

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MagazineIssueScreen(
    issueId: String,
    onNavigateBack: () -> Unit,
    onNavigateToAdDetail: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MagazineViewModel = viewModel()
) {
    val issue = remember { viewModel.getIssueById(issueId) }
    val savedAds by viewModel.savedAds.collectAsState()
    val dismissedAds by viewModel.dismissedAds.collectAsState()
    
    // Filter visible ads (not dismissed)
    val visibleAds = remember(issue, dismissedAds) {
        issue?.ads?.filter { ad -> ad.id !in dismissedAds } ?: emptyList()
    }
    
    val gridState = rememberLazyGridState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            issue?.title ?: "Magazine",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Issue #${issue?.issueNumber?.toString()?.padStart(2, '0')}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
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
        if (issue == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Issue not found")
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                state = gridState,
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = visibleAds,
                    key = { it.id }
                ) { ad ->
                    MagazineInteractiveAdCard(
                        ad = ad,
                        isSaved = ad.id in savedAds,
                        onOpenDetail = { onNavigateToAdDetail(it.id) },
                        onSave = { viewModel.saveAd(it.id) },
                        onDismiss = { viewModel.dismissAd(it.id) }
                    ) {
                        AdCardContent(ad = ad)
                    }
                }
                
                // End message
                if (visibleAds.isNotEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    "End of Issue #${issue.issueNumber.toString().padStart(2, '0')}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    "Check back next month for new selections",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Content for each ad card
 */
@Composable
private fun AdCardContent(ad: MagazineAd) {
    val isDark = isSystemInDarkTheme()
    val cardColor = if (isDark) {
        Color(0xFF2D2D32) // Slate/charcoal
    } else {
        Color(0xFFFFFBF5) // Paper white
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        // Hero image
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.75f)
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "📸",
                style = MaterialTheme.typography.displayMedium
            )
        }
    }
}
