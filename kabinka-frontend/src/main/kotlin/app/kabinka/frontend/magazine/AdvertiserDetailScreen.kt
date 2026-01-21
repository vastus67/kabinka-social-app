package app.kabinka.frontend.magazine

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.kabinka.coreui.responsive.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvertiserDetailScreen(
    adId: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val repository = remember { MagazineRepository.getInstance() }
    val ad = remember { repository.getAdById(adId) }
    val context = LocalContext.current
    
    // Responsive values
    val contentPadding = responsivePadding()
    val itemSpacing = responsiveSpacing(compact = 20.dp, medium = 24.dp, expanded = 28.dp)
    val cornerRadius = responsiveSpacing(compact = 16.dp, medium = 20.dp, expanded = 24.dp)
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Ad Details",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
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
        if (ad == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Ad not found")
            }
        } else {
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(paddingValues),
                contentPadding = PaddingValues(contentPadding),
                verticalArrangement = Arrangement.spacedBy(itemSpacing)
            ) {
                // Paid placement disclosure
                item {
                    Surface(
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        shape = RoundedCornerShape(responsiveSpacing(compact = 8.dp, medium = 10.dp, expanded = 12.dp))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ℹ️",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "Paid placement",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                }
                
                // Category badge
                item {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(responsiveSpacing(compact = 8.dp, medium = 10.dp, expanded = 12.dp))
                    ) {
                        Text(
                            text = ad.category.displayName,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
                
                // Headline
                item {
                    Text(
                        text = ad.headline,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                // Hero image
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f)
                            .clip(RoundedCornerShape(cornerRadius))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                                        MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (ad.heroImageUrl != null) {
                            coil.compose.AsyncImage(
                                model = ad.heroImageUrl,
                                contentDescription = ad.headline,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                        } else {
                            Text(
                                text = "📸",
                                style = MaterialTheme.typography.displayLarge
                            )
                        }
                    }
                }
                
                // Sponsor name
                item {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Presented by",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = ad.sponsorName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                // Full description
                item {
                    Text(
                        text = ad.fullDescription ?: ad.bodyCopy,
                        style = MaterialTheme.typography.bodyLarge,
                        lineHeight = MaterialTheme.typography.bodyLarge.lineHeight.times(1.5f),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                // Additional images (if any)
                if (ad.additionalImages.isNotEmpty()) {
                    item {
                        Text(
                            text = "Gallery",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    items(ad.additionalImages.size) { index ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Image ${index + 1}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
                
                // CTA Button
                if (!ad.destinationUrl.isNullOrBlank()) {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Button(
                            onClick = {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(ad.destinationUrl))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    // Handle error
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = ad.ctaText,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Icon(
                                    imageVector = Icons.Filled.ArrowForward,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
                
                // Disclaimer
                item {
                    Text(
                        text = "Kabinka does not endorse or verify the claims made by advertisers. Please research before making any decisions.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                
                // Spacing at bottom
                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}
