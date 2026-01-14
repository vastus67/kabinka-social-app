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
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.Eye
import compose.icons.fontawesomeicons.solid.QuoteRight
import compose.icons.fontawesomeicons.solid.Language
import compose.icons.fontawesomeicons.solid.InfoCircle
import app.kabinka.social.model.StatusPrivacy
import app.kabinka.social.model.StatusQuotePolicy
import app.kabinka.social.api.session.AccountSessionManager
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostingDefaultsSettingsScreen(
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
    
    var isLoading by remember { mutableStateOf(true) }
    
    // Posting defaults
    var selectedVisibility by remember { mutableStateOf(StatusPrivacy.PUBLIC) }
    var selectedQuotePolicy by remember { mutableStateOf(StatusQuotePolicy.PUBLIC) }
    var selectedLanguage by remember { mutableStateOf<Locale?>(null) }
    
    // Dialog states
    var showVisibilityDialog by remember { mutableStateOf(false) }
    var showQuotePolicyDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    
    // Track changes
    var initialVisibility by remember { mutableStateOf(StatusPrivacy.PUBLIC) }
    var initialQuotePolicy by remember { mutableStateOf(StatusQuotePolicy.PUBLIC) }
    var initialLanguage by remember { mutableStateOf<Locale?>(null) }
    
    // Load preferences
    LaunchedEffect(Unit) {
        accountSession?.let { session ->
            // Initialize preferences if null
            if (session.preferences == null) {
                session.preferences = app.kabinka.social.model.Preferences()
            }
            
            session.preferences?.let { prefs ->
                if (prefs.postingDefaultVisibility != null) {
                    selectedVisibility = prefs.postingDefaultVisibility
                    initialVisibility = prefs.postingDefaultVisibility
                }
                if (prefs.postingDefaultQuotePolicy != null) {
                    selectedQuotePolicy = prefs.postingDefaultQuotePolicy
                    initialQuotePolicy = prefs.postingDefaultQuotePolicy
                }
                if (prefs.postingDefaultLanguage != null) {
                    selectedLanguage = Locale.forLanguageTag(prefs.postingDefaultLanguage)
                    initialLanguage = Locale.forLanguageTag(prefs.postingDefaultLanguage)
                }
            }
            isLoading = false
        } ?: run {
            isLoading = false
        }
    }
    
    // Save on exit
    DisposableEffect(Unit) {
        onDispose {
            accountSession?.let { session ->
                // Ensure preferences object exists
                if (session.preferences == null) {
                    session.preferences = app.kabinka.social.model.Preferences()
                }
                
                var needsSave = false
                
                if (selectedVisibility != initialVisibility) {
                    session.preferences.postingDefaultVisibility = selectedVisibility
                    needsSave = true
                }
                
                if (selectedQuotePolicy != initialQuotePolicy) {
                    session.preferences.postingDefaultQuotePolicy = selectedQuotePolicy
                    needsSave = true
                }
                
                // Compare language tags instead of Locale objects
                val currentLanguageTag = selectedLanguage?.toLanguageTag()
                val initialLanguageTag = initialLanguage?.toLanguageTag()
                if (currentLanguageTag != initialLanguageTag) {
                    session.preferences.postingDefaultLanguage = currentLanguageTag
                    needsSave = true
                }
                
                if (needsSave) {
                    session.savePreferencesLater()
                }
                
                // Always call this after setting the flag to ensure it's sent to server
                session.savePreferencesIfPending()
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Posting defaults", 
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
                // Disclaimer
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = FontAwesomeIcons.Solid.InfoCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "These settings will be used as defaults when you create new posts, but you can edit them per post within the compose.",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Posting visibility
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = FontAwesomeIcons.Solid.Eye,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Posting visibility",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showVisibilityDialog = true }
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = when (selectedVisibility) {
                                StatusPrivacy.PUBLIC -> "Public"
                                StatusPrivacy.UNLISTED -> "Quiet public"
                                StatusPrivacy.PRIVATE -> "Followers"
                                StatusPrivacy.DIRECT -> "Direct"
                            },
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = when (selectedVisibility) {
                                StatusPrivacy.PUBLIC -> "Everyone"
                                StatusPrivacy.UNLISTED -> "Hidden from search results, trending and public timelines"
                                StatusPrivacy.PRIVATE -> "Only your followers"
                                StatusPrivacy.DIRECT -> "Direct messages"
                            },
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))
                
                // Who can quote
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = FontAwesomeIcons.Solid.QuoteRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Who can quote",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = selectedVisibility != StatusPrivacy.PRIVATE) { 
                            showQuotePolicyDialog = true 
                        }
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (selectedVisibility == StatusPrivacy.PRIVATE) {
                            "Quote posts are not available for followers-only posts"
                        } else {
                            when (selectedQuotePolicy) {
                                StatusQuotePolicy.PUBLIC -> "Anyone"
                                StatusQuotePolicy.FOLLOWERS -> "Followers only"
                                StatusQuotePolicy.NOBODY -> "Just me"
                            }
                        },
                        fontSize = 14.sp,
                        color = if (selectedVisibility == StatusPrivacy.PRIVATE) {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
                
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))
                
                // Posting language
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = FontAwesomeIcons.Solid.Language,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Posting language",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showLanguageDialog = true }
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedLanguage?.displayLanguage ?: "Not set",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
    
    // Visibility selection dialog
    if (showVisibilityDialog) {
        VisibilitySelectionDialog(
            selectedVisibility = selectedVisibility,
            onDismiss = { showVisibilityDialog = false },
            onSelect = { visibility ->
                selectedVisibility = visibility
                // Update quote policy if needed
                if (visibility == StatusPrivacy.PRIVATE) {
                    selectedQuotePolicy = StatusQuotePolicy.NOBODY
                }
                showVisibilityDialog = false
            }
        )
    }
    
    // Quote policy selection dialog
    if (showQuotePolicyDialog) {
        QuotePolicySelectionDialog(
            selectedPolicy = selectedQuotePolicy,
            currentVisibility = selectedVisibility,
            onDismiss = { showQuotePolicyDialog = false },
            onSelect = { policy ->
                selectedQuotePolicy = policy
                showQuotePolicyDialog = false
            }
        )
    }
    
    // Language selection dialog
    if (showLanguageDialog) {
        LanguageSelectionDialog(
            selectedLanguage = selectedLanguage,
            onDismiss = { showLanguageDialog = false },
            onSelect = { language ->
                selectedLanguage = language
                showLanguageDialog = false
            }
        )
    }
}

@Composable
fun VisibilitySelectionDialog(
    selectedVisibility: StatusPrivacy,
    onDismiss: () -> Unit,
    onSelect: (StatusPrivacy) -> Unit
) {
    val options = listOf(
        StatusPrivacy.PUBLIC to ("Public" to "Everyone"),
        StatusPrivacy.UNLISTED to ("Quiet public" to "Hidden from search results, trending and public timelines"),
        StatusPrivacy.PRIVATE to ("Followers" to "Only your followers")
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Posting visibility") },
        text = {
            Column {
                options.forEach { (privacy, labels) ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(privacy) }
                            .padding(vertical = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedVisibility == privacy,
                                onClick = { onSelect(privacy) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = labels.first,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = labels.second,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
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
fun QuotePolicySelectionDialog(
    selectedPolicy: StatusQuotePolicy,
    currentVisibility: StatusPrivacy,
    onDismiss: () -> Unit,
    onSelect: (StatusQuotePolicy) -> Unit
) {
    val options = listOf(
        StatusQuotePolicy.PUBLIC to "Anyone",
        StatusQuotePolicy.FOLLOWERS to "Followers only",
        StatusQuotePolicy.NOBODY to "Just me"
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Who can quote") },
        text = {
            Column {
                if (currentVisibility == StatusPrivacy.UNLISTED) {
                    Text(
                        text = "When you post with quiet public visibility, only people you follow and your followers can quote your posts.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }
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
fun LanguageSelectionDialog(
    selectedLanguage: Locale?,
    onDismiss: () -> Unit,
    onSelect: (Locale) -> Unit
) {
    // Get all available languages sorted alphabetically
    val languages = remember {
        Locale.getAvailableLocales()
            .map { it.language }
            .distinct()
            .map { Locale.forLanguageTag(it) }
            .sortedBy { it.displayLanguage }
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Posting language") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
            ) {
                // System default option
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { 
                            onSelect(Locale.forLanguageTag(Locale.getDefault().language))
                        }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedLanguage?.language == Locale.getDefault().language,
                        onClick = { onSelect(Locale.forLanguageTag(Locale.getDefault().language)) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "System default",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = Locale.getDefault().displayLanguage,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                HorizontalDivider()
                
                // Scrollable list of all languages
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    languages.forEach { locale ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(locale) }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedLanguage?.language == locale.language,
                                onClick = { onSelect(locale) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = locale.displayLanguage.replaceFirstChar { 
                                    if (it.isLowerCase()) it.titlecase() else it.toString() 
                                },
                                fontSize = 16.sp
                            )
                        }
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
