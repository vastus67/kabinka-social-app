package app.kabinka.frontend.settings.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import compose.icons.LineAwesomeIcons
import compose.icons.lineawesomeicons.AngleLeftSolid
import app.kabinka.social.model.PushSubscription
import app.kabinka.social.api.session.AccountSessionManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsSettingsScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val accountSession = remember {
        try {
            AccountSessionManager.getInstance().lastActiveAccount
        } catch (e: Exception) {
            null
        }
    }
    
    var pushSubscription by remember { mutableStateOf<PushSubscription?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    
    // Pause all notifications
    var pauseAll by remember { mutableStateOf(false) }
    var pauseEndTime by remember { mutableStateOf(0L) }
    var showPauseDurationDialog by remember { mutableStateOf(false) }
    
    // Policy (Get notifications from)
    var selectedPolicy by remember { mutableStateOf(PushSubscription.Policy.ALL) }
    var showPolicyDialog by remember { mutableStateOf(false) }
    
    // Notification types
    var mentions by remember { mutableStateOf(true) }
    var boosts by remember { mutableStateOf(true) }
    var favorites by remember { mutableStateOf(true) }
    var newFollowers by remember { mutableStateOf(true) }
    var polls by remember { mutableStateOf(true) }
    var newPosts by remember { mutableStateOf(true) }
    var quotes by remember { mutableStateOf(true) }
    
    // Load push subscription
    LaunchedEffect(Unit) {
        accountSession?.let { session ->
            val ps = if (session.pushSubscription == null) {
                PushSubscription().apply {
                    alerts = PushSubscription.Alerts.ofAll()
                    policy = PushSubscription.Policy.ALL
                }
            } else {
                session.pushSubscription.clone()
            }
            pushSubscription = ps
            
            // Update state
            selectedPolicy = ps.policy
            mentions = ps.alerts.mention
            boosts = ps.alerts.reblog
            favorites = ps.alerts.favourite
            newFollowers = ps.alerts.follow
            polls = ps.alerts.poll
            newPosts = ps.alerts.status
            quotes = ps.alerts.quote
            
            // Check if notifications are paused
            val pauseTime = session.getLocalPreferences().getNotificationsPauseEndTime()
            pauseEndTime = pauseTime
            pauseAll = pauseTime > System.currentTimeMillis()
            
            isLoading = false
        } ?: run {
            isLoading = false
        }
    }
    
    // Save on exit
    DisposableEffect(Unit) {
        onDispose {
            accountSession?.let { session ->
                pushSubscription?.let { ps ->
                    val needsUpdate = ps.alerts.mention != mentions ||
                        ps.alerts.reblog != boosts ||
                        ps.alerts.favourite != favorites ||
                        ps.alerts.follow != newFollowers ||
                        ps.alerts.poll != polls ||
                        ps.alerts.status != newPosts ||
                        ps.alerts.quote != quotes ||
                        ps.policy != selectedPolicy
                    
                    if (needsUpdate) {
                        ps.alerts.mention = mentions
                        ps.alerts.reblog = boosts
                        ps.alerts.favourite = favorites
                        ps.alerts.follow = newFollowers
                        ps.alerts.poll = polls
                        ps.alerts.status = newPosts
                        ps.alerts.quote = quotes
                        ps.policy = selectedPolicy
                        
                        // Update session and persist
                        session.pushSubscription = ps
                        
                        // Use PushSubscriptionManager to update settings properly
                        session.getPushSubscriptionManager().updatePushSettings(ps)
                    }
                }
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Notifications", 
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
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
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
                    .padding(16.dp)
            ) {
                // Pause all notifications
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Pause all",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (pauseAll) {
                                val hours = (pauseEndTime - System.currentTimeMillis()) / (1000 * 3600)
                                val minutes = ((pauseEndTime - System.currentTimeMillis()) / (1000 * 60)) % 60
                                when {
                                    hours >= 24 -> "Paused for ${hours / 24} day(s)"
                                    hours > 0 -> "Paused for ${hours}h ${minutes}m"
                                    else -> "Paused for ${minutes}m"
                                }
                            } else "Not paused",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = pauseAll,
                        onCheckedChange = { checked ->
                            if (checked) {
                                showPauseDurationDialog = true
                            } else {
                                pauseAll = false
                                pauseEndTime = 0L
                                accountSession?.getLocalPreferences()?.setNotificationsPauseEndTime(0)
                            }
                        }
                    )
                }
                
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))
                
                // Get notifications from
                Text(
                    text = "Get notifications from",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showPolicyDialog = true }
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when (selectedPolicy) {
                            PushSubscription.Policy.ALL -> "Anyone"
                            PushSubscription.Policy.FOLLOWED -> "People you follow"
                            PushSubscription.Policy.FOLLOWER -> "People who follow you"
                            PushSubscription.Policy.NONE -> "No one"
                        },
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))
                
                
                NotificationTypeCheckbox(
                    label = "Mentions and replies",
                    checked = mentions,
                    onCheckedChange = { mentions = it },
                    enabled = selectedPolicy != PushSubscription.Policy.NONE
                )
                
                NotificationTypeCheckbox(
                    label = "Boosts",
                    checked = boosts,
                    onCheckedChange = { boosts = it },
                    enabled = selectedPolicy != PushSubscription.Policy.NONE
                )
                
                NotificationTypeCheckbox(
                    label = "Favorites",
                    checked = favorites,
                    onCheckedChange = { favorites = it },
                    enabled = selectedPolicy != PushSubscription.Policy.NONE
                )
                
                NotificationTypeCheckbox(
                    label = "New followers",
                    checked = newFollowers,
                    onCheckedChange = { newFollowers = it },
                    enabled = selectedPolicy != PushSubscription.Policy.NONE
                )
                
                NotificationTypeCheckbox(
                    label = "Polls",
                    checked = polls,
                    onCheckedChange = { polls = it },
                    enabled = selectedPolicy != PushSubscription.Policy.NONE
                )
                
                NotificationTypeCheckbox(
                    label = "New posts",
                    checked = newPosts,
                    onCheckedChange = { newPosts = it },
                    enabled = selectedPolicy != PushSubscription.Policy.NONE
                )
                
                NotificationTypeCheckbox(
                    label = "Quotes",
                    checked = quotes,
                    onCheckedChange = { quotes = it },
                    enabled = selectedPolicy != PushSubscription.Policy.NONE
                )
            }
        }
    }
    
    // Pause duration dialog
    if (showPauseDurationDialog) {
        PauseDurationDialog(
            onDismiss = { showPauseDurationDialog = false },
            onSelect = { durationSeconds ->
                val endTime = System.currentTimeMillis() + durationSeconds * 1000L
                pauseEndTime = endTime
                pauseAll = true
                accountSession?.getLocalPreferences()?.setNotificationsPauseEndTime(endTime)
                showPauseDurationDialog = false
            }
        )
    }
    
    // Policy selection dialog
    if (showPolicyDialog) {
        PolicySelectionDialog(
            selectedPolicy = selectedPolicy,
            onDismiss = { showPolicyDialog = false },
            onSelect = { policy ->
                selectedPolicy = policy
                showPolicyDialog = false
            }
        )
    }
}

@Composable
fun NotificationTypeCheckbox(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onCheckedChange(!checked) }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            fontSize = 16.sp,
            color = if (enabled) 
                MaterialTheme.colorScheme.onSurface 
            else 
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
    }
}

@Composable
fun PolicySelectionDialog(
    selectedPolicy: PushSubscription.Policy,
    onDismiss: () -> Unit,
    onSelect: (PushSubscription.Policy) -> Unit
) {
    val options = listOf(
        PushSubscription.Policy.ALL to "Anyone",
        PushSubscription.Policy.FOLLOWER to "People who follow you",
        PushSubscription.Policy.FOLLOWED to "People you follow",
        PushSubscription.Policy.NONE to "No one"
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Get notifications from") },
        text = {
            Column {
                options.forEach { (policy, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(policy) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedPolicy == policy,
                            onClick = { onSelect(policy) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = label,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun PauseDurationDialog(
    onDismiss: () -> Unit,
    onSelect: (Int) -> Unit
) {
    val durations = listOf(
        1800 to "30 minutes",
        3600 to "1 hour",
        12 * 3600 to "12 hours",
        24 * 3600 to "1 day",
        3 * 24 * 3600 to "3 days",
        7 * 24 * 3600 to "1 week"
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pause notifications") },
        text = {
            Column {
                durations.forEach { (seconds, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(seconds) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = label,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
