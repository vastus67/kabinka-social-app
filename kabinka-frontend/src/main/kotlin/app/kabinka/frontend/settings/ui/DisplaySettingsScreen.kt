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
fun DisplaySettingsScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { 
        context.getSharedPreferences("global", Context.MODE_PRIVATE) 
    }
    
    var showAppearanceDialog by remember { mutableStateOf(false) }
    var themeOrdinal by remember { 
        mutableStateOf(prefs.getInt("theme", 0)) 
    }
    var useDynamicColors by remember { 
        mutableStateOf(prefs.getBoolean("useDynamicColors", false)) 
    }
    var showContentWarnings by remember { 
        mutableStateOf(prefs.getBoolean("showCWs", true)) 
    }
    var coverSensitiveMedia by remember { 
        mutableStateOf(prefs.getBoolean("hideSensitive", true)) 
    }
    var showInteractionCounts by remember { 
        mutableStateOf(prefs.getBoolean("interactionCounts", true)) 
    }
    var customEmojiInNames by remember { 
        mutableStateOf(prefs.getBoolean("emojiInNames", true)) 
    }

    val themeNames = listOf("Device appearance", "Light", "Dark")
    val currentThemeName = themeNames.getOrNull(themeOrdinal) ?: "Device appearance"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Display", 
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
            // Appearance selection
            SettingsSelectionItem(
                title = "Appearance",
                subtitle = currentThemeName,
                onClick = { showAppearanceDialog = true }
            )
            
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            
            // Use system dynamic color
            SettingsToggleItem(
                title = "Use system dynamic color",
                subtitle = "Apply Material You color scheme",
                checked = useDynamicColors,
                onCheckedChange = { checked ->
                    useDynamicColors = checked
                    prefs.edit()
                        .putBoolean("useDynamicColors", checked)
                        .apply()
                    GlobalUserPreferences.useDynamicColors = checked
                    GlobalUserPreferences.save()
                }
            )
            
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            
            // Show content warnings
            SettingsToggleItem(
                title = "Show content warnings",
                subtitle = "Display posts with content warnings",
                checked = showContentWarnings,
                onCheckedChange = { checked ->
                    showContentWarnings = checked
                    prefs.edit()
                        .putBoolean("showCWs", checked)
                        .apply()
                    GlobalUserPreferences.showCWs = checked
                    GlobalUserPreferences.save()
                }
            )
            
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            
            // Cover media marked as sensitive
            SettingsToggleItem(
                title = "Cover media marked as sensitive",
                subtitle = "Hide sensitive images behind warning",
                checked = coverSensitiveMedia,
                onCheckedChange = { checked ->
                    coverSensitiveMedia = checked
                    prefs.edit()
                        .putBoolean("hideSensitive", checked)
                        .apply()
                    GlobalUserPreferences.hideSensitiveMedia = checked
                    GlobalUserPreferences.save()
                }
            )
            
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            
            // Post interaction counts
            SettingsToggleItem(
                title = "Post interaction counts",
                subtitle = "Show likes, boosts, and reply counts",
                checked = showInteractionCounts,
                onCheckedChange = { checked ->
                    showInteractionCounts = checked
                    prefs.edit()
                        .putBoolean("interactionCounts", checked)
                        .apply()
                    GlobalUserPreferences.showInteractionCounts = checked
                    GlobalUserPreferences.save()
                }
            )
            
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            
            // Custom emoji in display names
            SettingsToggleItem(
                title = "Custom emoji in display names",
                subtitle = "Show custom emoji in user names",
                checked = customEmojiInNames,
                onCheckedChange = { checked ->
                    customEmojiInNames = checked
                    prefs.edit()
                        .putBoolean("emojiInNames", checked)
                        .apply()
                    GlobalUserPreferences.customEmojiInNames = checked
                    GlobalUserPreferences.save()
                }
            )
        }
        
        // Appearance selection dialog
        if (showAppearanceDialog) {
            AlertDialog(
                onDismissRequest = { showAppearanceDialog = false },
                title = { Text("Appearance") },
                text = {
                    Column {
                        AppearanceOption(
                            title = "Device appearance",
                            subtitle = "Follow system theme",
                            isSelected = themeOrdinal == 0,
                            onClick = {
                                themeOrdinal = 0
                                prefs.edit()
                                    .putInt("theme", 0)
                                    .apply()
                                GlobalUserPreferences.theme = GlobalUserPreferences.ThemePreference.AUTO
                                GlobalUserPreferences.save()
                                showAppearanceDialog = false
                            }
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        AppearanceOption(
                            title = "Light",
                            subtitle = "Always use light theme",
                            isSelected = themeOrdinal == 1,
                            onClick = {
                                themeOrdinal = 1
                                prefs.edit()
                                    .putInt("theme", 1)
                                    .apply()
                                GlobalUserPreferences.theme = GlobalUserPreferences.ThemePreference.LIGHT
                                GlobalUserPreferences.save()
                                showAppearanceDialog = false
                            }
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        AppearanceOption(
                            title = "Dark",
                            subtitle = "Always use dark theme",
                            isSelected = themeOrdinal == 2,
                            onClick = {
                                themeOrdinal = 2
                                prefs.edit()
                                    .putInt("theme", 2)
                                    .apply()
                                GlobalUserPreferences.theme = GlobalUserPreferences.ThemePreference.DARK
                                GlobalUserPreferences.save()
                                showAppearanceDialog = false
                            }
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showAppearanceDialog = false }) {
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
private fun AppearanceOption(
    title: String,
    subtitle: String,
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
        Column {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
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
