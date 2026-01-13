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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import compose.icons.LineAwesomeIcons
import compose.icons.lineawesomeicons.AngleLeftSolid
import app.kabinka.social.api.session.AccountSessionManager
import app.kabinka.social.api.requests.accounts.GetOwnAccount
import me.grishka.appkit.api.Callback
import me.grishka.appkit.api.ErrorResponse
import app.kabinka.social.model.Account
import app.kabinka.social.model.Preferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacySettingsScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    
    // Get current account session
    val accountSession = remember {
        try {
            AccountSessionManager.getInstance().lastActiveAccount
        } catch (e: Exception) {
            null
        }
    }
    
    var isLoading by remember { mutableStateOf(true) }
    var account by remember { mutableStateOf(accountSession?.self) }
    
    // Initialize with account values
    var discoverable by remember { 
        mutableStateOf(account?.discoverable ?: false) 
    }
    var indexable by remember { 
        mutableStateOf(account?.source?.indexable ?: true) 
    }
    
    // Fetch fresh account data from API
    LaunchedEffect(Unit) {
        accountSession?.let { session ->
            withContext(Dispatchers.IO) {
                suspendCoroutine { continuation ->
                    GetOwnAccount()
                        .setCallback(object : Callback<Account> {
                            override fun onSuccess(result: Account) {
                                account = result
                                discoverable = result.discoverable
                                indexable = result.source?.indexable ?: true
                                session.self = result
                                // Load preferences if not already loaded
                                if (session.preferences == null) {
                                    session.reloadPreferences(null)
                                }
                                AccountSessionManager.getInstance().updateAccountInfo(session.getID(), result)
                                isLoading = false
                                continuation.resume(Unit)
                            }
                            
                            override fun onError(error: ErrorResponse) {
                                // Use cached data on error
                                isLoading = false
                                continuation.resume(Unit)
                            }
                        })
                        .exec(session.getID())
                }
            }
        } ?: run {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Privacy and reach", 
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
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
            ) {
            // Feature profile and posts in discovery algorithms
            SettingsToggleItem(
                title = "Feature profile and posts in discovery algorithms",
                subtitle = "Your public posts and profile may appear in search results and trends",
                checked = discoverable,
                enabled = true,
                onCheckedChange = { checked: Boolean ->
                    discoverable = checked
                    accountSession?.let { session ->
                        session.self.discoverable = checked
                        session.savePreferencesLater()
                    }
                }
            )
            
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            
            // Include public posts in search results
            SettingsToggleItem(
                title = "Include public posts in search results",
                subtitle = "Allow search engines to index your public posts",
                checked = indexable,
                enabled = true,
                onCheckedChange = { checked: Boolean ->
                    indexable = checked
                    accountSession?.let { session ->
                        if (session.self.source == null) {
                            session.self.source = app.kabinka.social.model.Source()
                        }
                        session.self.source?.indexable = checked
                        session.savePreferencesLater()
                    }
                }
            )
            }
        }
    }
    
    // Save preferences when leaving the screen
    DisposableEffect(Unit) {
        onDispose {
            accountSession?.savePreferencesIfPending()
        }
    }
}

@Composable
fun SettingsToggleItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .alpha(if (enabled) 1f else 0.5f),
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
            enabled = enabled,
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
