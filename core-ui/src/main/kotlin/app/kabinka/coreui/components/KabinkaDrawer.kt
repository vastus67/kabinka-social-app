package app.kabinka.coreui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import app.kabinka.coreui.responsive.*
import coil.compose.AsyncImage
import kotlinx.coroutines.launch

@Composable
fun KabinkaDrawer(
    onNavigate: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    profileAvatarUrl: String? = null,
    profileDisplayName: String? = null,
    profileUsername: String? = null,
    isLoggedIn: Boolean = false,
    onLogout: () -> Unit = {}
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    // Responsive padding
    val horizontalPadding = responsivePadding()
    val verticalSpacing = responsiveSpacing()
    
    ModalDrawerSheet(
        modifier = modifier,
        drawerContainerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(verticalSpacing * 1.5f))

            // Profile Avatar
            if (profileAvatarUrl != null) {
                AsyncImage(
                    model = profileAvatarUrl,
                    contentDescription = "Profile avatar",
                    modifier = Modifier
                        .padding(horizontal = horizontalPadding)
                        .size(64.dp)
                        .clip(CircleShape)
                        .clickable {
                            onNavigate("profile")
                            onDismiss()
                        },
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .padding(horizontal = horizontalPadding)
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .clickable {
                            onNavigate("profile")
                            onDismiss()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = profileDisplayName?.firstOrNull()?.toString()?.uppercase() ?: "?",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(verticalSpacing))

            Text(
                text = profileDisplayName ?: "Not logged in",
                modifier = Modifier.padding(horizontal = horizontalPadding),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = profileUsername ?: "Sign in to continue",
                modifier = Modifier.padding(horizontal = horizontalPadding),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(verticalSpacing * 1.5f))

            HorizontalDivider()

            Spacer(modifier = Modifier.height(verticalSpacing * 0.5f))

            DrawerItem(
                icon = Icons.Outlined.AccountCircle,
                label = "Switch Account",
                onClick = {
                    onNavigate("account_switcher")
                    onDismiss()
                }
            )

            DrawerItem(
                icon = Icons.Outlined.List,
                label = "Lists",
                onClick = {
                    onNavigate("lists")
                    onDismiss()
                }
            )

            DrawerItem(
                icon = Icons.Outlined.Tag,
                label = "Followed hashtags",
                onClick = {
                    onNavigate("followed_hashtags")
                    onDismiss()
                }
            )

            DrawerItem(
                icon = Icons.Outlined.BookmarkBorder,
                label = "Bookmarks",
                onClick = {
                    onNavigate("bookmarks")
                    onDismiss()
                }
            )

            DrawerItem(
                icon = Icons.Outlined.Star,
                label = "Favorites",
                onClick = {
                    onNavigate("favorites")
                    onDismiss()
                }
            )

            Spacer(modifier = Modifier.height(verticalSpacing * 0.5f))

            HorizontalDivider()

            Spacer(modifier = Modifier.height(verticalSpacing * 0.5f))

            DrawerItem(
                icon = Icons.Outlined.Chat,
                label = "Kabinka Chat",
                onClick = {
                    onDismiss()
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = "Coming soon!",
                            duration = SnackbarDuration.Short
                        )
                    }
                },
                highlight = true
            )

            DrawerItem(
                icon = Icons.Outlined.Book,
                label = "Magazine",
                onClick = {
                    onNavigate("magazine")
                    onDismiss()
                }
            )

            DrawerItem(
                icon = Icons.Outlined.MonetizationOn,
                label = "Sponsored",
                onClick = {
                    onDismiss()
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = "Coming soon!",
                            duration = SnackbarDuration.Short
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(verticalSpacing * 0.5f))

            HorizontalDivider()

            Spacer(modifier = Modifier.height(verticalSpacing * 0.5f))

            DrawerItem(
                icon = Icons.Outlined.Settings,
                label = "Settings",
                onClick = {
                    onNavigate("settings")
                    onDismiss()
                }
            )

            DrawerItem(
                icon = Icons.Outlined.Info,
                label = "About",
                onClick = {
                    onNavigate("about")
                    onDismiss()
                }
            )

            Spacer(modifier = Modifier.height(verticalSpacing * 0.5f))

            HorizontalDivider()

            Spacer(modifier = Modifier.height(verticalSpacing * 0.5f))

            DrawerItem(
                icon = if (isLoggedIn) Icons.Outlined.ExitToApp else Icons.Outlined.Login,
                label = if (isLoggedIn) "Log Out" else "Login / Register",
                onClick = {
                    onDismiss()
                    onLogout()
                }
            )

            Spacer(modifier = Modifier.height(verticalSpacing))
            
            // Snackbar host at bottom
            SnackbarHost(hostState = snackbarHostState)
        }
    }
}

@Composable
private fun DrawerItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    highlight: Boolean = false
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = if (highlight) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        else MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (highlight) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = if (highlight) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
