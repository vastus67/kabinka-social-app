package app.kabinka.frontend.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.kabinka.social.api.requests.instance.GetInstanceV2
import app.kabinka.social.model.Instance
import app.kabinka.social.model.InstanceV2
import app.kabinka.social.api.session.AccountSessionManager
import coil.compose.AsyncImage
import compose.icons.LineAwesomeIcons
import compose.icons.lineawesomeicons.InfoCircleSolid
import compose.icons.lineawesomeicons.ListSolid
import compose.icons.lineawesomeicons.EnvelopeSolid
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.grishka.appkit.api.Callback
import me.grishka.appkit.api.ErrorResponse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutServerScreen(
    onBack: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("ABOUT", "RULES")
    
    var isLoading by remember { mutableStateOf(true) }
    var instance by remember { mutableStateOf<Instance?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    
    // Get account ID and server domain from active session
    val accountSession = AccountSessionManager.getInstance().lastActiveAccount
    val accountId = AccountSessionManager.getInstance().lastActiveAccountID
    val serverDomain = accountSession?.domain ?: "mastodon.social"

    // Load instance info using authenticated API
    LaunchedEffect(accountId) {
        if (accountId == null) {
            errorMessage = "No active account"
            isLoading = false
            return@LaunchedEffect
        }
        
        scope.launch(Dispatchers.IO) {
            try {
                GetInstanceV2()
                    .exec(accountId)
                    .setCallback(object : Callback<InstanceV2> {
                        override fun onSuccess(result: InstanceV2) {
                            instance = result
                            isLoading = false
                        }

                        override fun onError(error: ErrorResponse) {
                            errorMessage = error.toString()
                            isLoading = false
                        }
                    })
            } catch (e: Exception) {
                errorMessage = e.message ?: "Failed to load server information"
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("About this server") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = {
                            Icon(
                                imageVector = when (index) {
                                    0 -> LineAwesomeIcons.InfoCircleSolid
                                    1 -> LineAwesomeIcons.ListSolid
                                    else -> LineAwesomeIcons.InfoCircleSolid
                                },
                                contentDescription = title
                            )
                        },
                        text = { 
                            Text(
                                title,
                                style = MaterialTheme.typography.labelMedium,
                                maxLines = 1,
                                softWrap = false
                            ) 
                        }
                    )
                }
            }

            // Tab content
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                when {
                    isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    errorMessage != null -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = errorMessage!!,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                    instance != null -> {
                        when (selectedTab) {
                            0 -> AboutTab(instance = instance!!, serverDomain = serverDomain)
                            1 -> RulesTab(instance = instance!!, serverDomain = serverDomain)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AboutTab(
    instance: Instance,
    serverDomain: String
) {
    val context = LocalContext.current
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Server Name
        item {
            Text(
                text = instance.title ?: serverDomain,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        // Administrator Section
        if (instance is InstanceV2) {
            (instance as InstanceV2).contact?.let { contactInfo ->
                contactInfo.account?.let { admin ->
                    item {
                        Text(
                            text = "Server Administrator",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                    
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Avatar
                                AsyncImage(
                                    model = admin.avatar,
                                    contentDescription = "Administrator avatar",
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                                
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = admin.displayName ?: admin.username,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "@${admin.acct}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
                
                // Contact message and Email option
                if (!contactInfo.email.isNullOrBlank()) {
                    item {
                        Text(
                            text = "Please mind that ${contactInfo.email} is an email address for reporting violations and administrative requests only. If you have questions about using Mastodon, please visit the #AskMastodon hashtag.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                                            data = Uri.parse("mailto:${contactInfo.email}")
                                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                        }
                                        context.startActivity(intent)
                                    }
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = LineAwesomeIcons.EnvelopeSolid,
                                    contentDescription = "Email",
                                    modifier = Modifier.size(24.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Message admin",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }
                }
            }
        }

        // Server Description/About
        if (!instance.description.isNullOrBlank()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Overview",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = instance.description!!,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
        
        // Moderation Information
        if (instance is InstanceV2) {
            (instance as InstanceV2).contact?.let { contactInfo ->
                if (!contactInfo.email.isNullOrBlank()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Moderation",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "This server is moderated by the administrator. For reporting violations or administrative requests, please use the contact information above.",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
        }

        // Server Information
        item {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Server Information",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Domain: $serverDomain",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Text(
                        text = "Version: ${instance.version ?: "Unknown"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun RulesTab(
    instance: Instance,
    serverDomain: String
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        item {
            Text(
                text = "Rules for $serverDomain",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "These are the rules for this server.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        if (instance.rules.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "No specific rules",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "This server has not published specific rules, but you are still expected to follow community guidelines and respect other users.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        } else {
            itemsIndexed(instance.rules) { index, rule ->
                RuleItem(
                    number = index + 1,
                    rule = rule
                )
                if (index < instance.rules.size - 1) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun RuleItem(
    number: Int,
    rule: Instance.Rule
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    text = number.toString(),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            
            Text(
                text = rule.text,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
