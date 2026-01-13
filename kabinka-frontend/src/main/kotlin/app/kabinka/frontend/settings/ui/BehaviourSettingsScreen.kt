package app.kabinka.frontend.settings.ui

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import compose.icons.LineAwesomeIcons
import compose.icons.lineawesomeicons.AngleLeftSolid
import compose.icons.lineawesomeicons.AngleRightSolid
import app.kabinka.social.GlobalUserPreferences

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BehaviourSettingsScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { 
        context.getSharedPreferences("global", Context.MODE_PRIVATE) 
    }
    
    var showBrowserDialog by remember { mutableStateOf(false) }
    var useCustomTabs by remember { 
        mutableStateOf(prefs.getBoolean("useCustomTabs", true)) 
    }
    var altTextReminder by remember { 
        mutableStateOf(prefs.getBoolean("altTextReminders", false)) 
    }
    var playAnimatedContent by remember { 
        mutableStateOf(prefs.getBoolean("playGifs", true)) 
    }
    var askBeforeUnfollowing by remember { 
        mutableStateOf(prefs.getBoolean("confirmUnfollow", false)) 
    }
    var askBeforeBoosting by remember { 
        mutableStateOf(prefs.getBoolean("confirmBoost", false)) 
    }
    var askBeforeDeletingPost by remember { 
        mutableStateOf(prefs.getBoolean("confirmDeletePost", true)) 
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Behaviour", 
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = LineAwesomeIcons.AngleLeftSolid,
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
                .verticalScroll(rememberScrollState())
        ) {
            // Open links in browser - selection option
            SettingsSelectionItem(
                title = "Open links in browser",
                subtitle = if (useCustomTabs) "In-app browser" else "System browser",
                onClick = { showBrowserDialog = true }
            )
            
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            
            // Alt text reminder
            SettingsToggleItem(
                title = "Add alt text reminder",
                subtitle = "Remind to add descriptions to images",
                checked = altTextReminder,
                onCheckedChange = { checked ->
                    altTextReminder = checked
                    prefs.edit()
                        .putBoolean("altTextReminders", checked)
                        .apply()
                    GlobalUserPreferences.altTextReminders = checked
                    GlobalUserPreferences.save()
                }
            )
            
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            
            // Play animated avatars and emoji
            SettingsToggleItem(
                title = "Play animated avatars and emoji",
                subtitle = "Auto-play GIFs and animated emoji",
                checked = playAnimatedContent,
                onCheckedChange = { checked ->
                    playAnimatedContent = checked
                    prefs.edit()
                        .putBoolean("playGifs", checked)
                        .apply()
                    GlobalUserPreferences.playGifs = checked
                    GlobalUserPreferences.save()
                }
            )
            
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            
            // Ask before unfollowing
            SettingsToggleItem(
                title = "Ask before unfollowing someone",
                subtitle = "Show confirmation dialog",
                checked = askBeforeUnfollowing,
                onCheckedChange = { checked ->
                    askBeforeUnfollowing = checked
                    prefs.edit()
                        .putBoolean("confirmUnfollow", checked)
                        .apply()
                    GlobalUserPreferences.confirmUnfollow = checked
                    GlobalUserPreferences.save()
                }
            )
            
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            
            // Ask before boosting
            SettingsToggleItem(
                title = "Ask before boosting",
                subtitle = "Show confirmation before reblog",
                checked = askBeforeBoosting,
                onCheckedChange = { checked ->
                    askBeforeBoosting = checked
                    prefs.edit()
                        .putBoolean("confirmBoost", checked)
                        .apply()
                    GlobalUserPreferences.confirmBoost = checked
                    GlobalUserPreferences.save()
                }
            )
            
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            
            // Ask before deleting posts
            SettingsToggleItem(
                title = "Ask before deleting posts",
                subtitle = "Show confirmation before deletion",
                checked = askBeforeDeletingPost,
                onCheckedChange = { checked ->
                    askBeforeDeletingPost = checked
                    prefs.edit()
                        .putBoolean("confirmDeletePost", checked)
                        .apply()
                    GlobalUserPreferences.confirmDeletePost = checked
                    GlobalUserPreferences.save()
                }
            )
        }
        
        // Browser selection dialog
        if (showBrowserDialog) {
            AlertDialog(
                onDismissRequest = { showBrowserDialog = false },
                title = { Text("Open links in browser") },
                text = {
                    Column {
                        BrowserOption(
                            title = "In-app browser",
                            isSelected = useCustomTabs,
                            onClick = {
                                useCustomTabs = true
                                prefs.edit()
                                    .putBoolean("useCustomTabs", true)
                                    .apply()
                                GlobalUserPreferences.useCustomTabs = true
                                GlobalUserPreferences.save()
                                showBrowserDialog = false
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        BrowserOption(
                            title = "System browser",
                            isSelected = !useCustomTabs,
                            onClick = {
                                useCustomTabs = false
                                prefs.edit()
                                    .putBoolean("useCustomTabs", false)
                                    .apply()
                                GlobalUserPreferences.useCustomTabs = false
                                GlobalUserPreferences.save()
                                showBrowserDialog = false
                            }
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showBrowserDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
private fun SettingsSelectionItem(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Icon(
            imageVector = LineAwesomeIcons.AngleRightSolid,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun BrowserOption(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onClick
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            fontSize = 16.sp
        )
    }
}

@Composable
private fun SettingsToggleItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // Pill-style switch
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
}
