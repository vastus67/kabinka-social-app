package app.kabinka.frontend.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.kabinka.social.api.session.AccountSessionManager
import compose.icons.LineAwesomeIcons
import compose.icons.lineawesomeicons.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToBehaviour: () -> Unit,
    onNavigateToDisplay: () -> Unit,
    onNavigateToPrivacy: () -> Unit,
    onNavigateToFilters: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToPostingDefaults: () -> Unit,
    onNavigateToAboutServer: () -> Unit,
    onNavigateToDonate: () -> Unit,
    onNavigateToDeleteAccount: () -> Unit
) {
    val session = AccountSessionManager.getInstance().lastActiveAccount
    val serverName = session?.domain ?: "Server"
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // App Settings Section
            SettingsSectionHeader("App settings")
            
            SettingsItem(
                icon = LineAwesomeIcons.SlidersHSolid,
                title = "Behaviour",
                subtitle = "Interface, language, media",
                onClick = onNavigateToBehaviour
            )
            
            SettingsItem(
                icon = LineAwesomeIcons.PaletteSolid,
                title = "Display",
                subtitle = "Theme, font size, animations",
                onClick = onNavigateToDisplay
            )
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            
            // Account Settings Section
            SettingsSectionHeader("Account settings")
            
            SettingsItem(
                icon = LineAwesomeIcons.UserShieldSolid,
                title = "Privacy and reach",
                subtitle = "Visibility, blocked users, muted users",
                onClick = onNavigateToPrivacy
            )
            
            SettingsItem(
                icon = LineAwesomeIcons.FilterSolid,
                title = "Filters",
                subtitle = "Filtered words and phrases",
                onClick = onNavigateToFilters
            )
            
            SettingsItem(
                icon = LineAwesomeIcons.BellSolid,
                title = "Notifications",
                subtitle = "Email and push notifications",
                onClick = onNavigateToNotifications
            )
            
            SettingsItem(
                icon = LineAwesomeIcons.EditSolid,
                title = "Posting defaults",
                subtitle = "Default privacy, language",
                onClick = onNavigateToPostingDefaults
            )
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            
            // Server Settings Section
            SettingsSectionHeader(serverName)
            
            SettingsItem(
                icon = LineAwesomeIcons.InfoCircleSolid,
                title = "About this server",
                subtitle = "Rules, moderation, server info",
                onClick = onNavigateToAboutServer
            )
            
            SettingsItem(
                icon = LineAwesomeIcons.HeartSolid,
                title = "Donate",
                subtitle = "Support this server",
                onClick = onNavigateToDonate
            )
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            
            // Manage Account Section
            SettingsSectionHeader("Manage account")
            
            SettingsItem(
                icon = LineAwesomeIcons.TrashAltSolid,
                title = "Delete account",
                subtitle = "Permanently delete your account",
                onClick = onNavigateToDeleteAccount,
                isDangerous = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
    )
}

@Composable
fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    isDangerous: Boolean = false
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = if (isDangerous) MaterialTheme.colorScheme.error 
                       else MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = if (isDangerous) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Icon(
                imageVector = LineAwesomeIcons.AngleRightSolid,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
