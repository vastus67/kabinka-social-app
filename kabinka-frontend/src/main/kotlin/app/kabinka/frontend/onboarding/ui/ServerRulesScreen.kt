package app.kabinka.frontend.onboarding.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import app.kabinka.social.api.MastodonAPIController
import app.kabinka.social.api.requests.instance.GetInstanceV2
import app.kabinka.social.model.Instance
import app.kabinka.social.model.InstanceV2
import compose.icons.LineAwesomeIcons
import compose.icons.lineawesomeicons.CheckCircleSolid
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.grishka.appkit.api.Callback
import me.grishka.appkit.api.ErrorResponse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerRulesScreen(
    serverDomain: String,
    onAgree: (Instance) -> Unit,
    onDisagree: () -> Unit
) {
    var isLoading by remember { mutableStateOf(true) }
    var instance by remember { mutableStateOf<Instance?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    // Load instance rules
    LaunchedEffect(serverDomain) {
        scope.launch(Dispatchers.IO) {
            try {
                GetInstanceV2()
                    .execNoAuth(serverDomain)
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
                errorMessage = e.message ?: "Failed to load server rules"
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Server rules") },
                navigationIcon = {
                    IconButton(onClick = onDisagree) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                shadowElevation = 8.dp,
                tonalElevation = 3.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Button(
                        onClick = { instance?.let { onAgree(it) } },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading && instance != null
                    ) {
                        Text("I agree to these rules")
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedButton(
                        onClick = onDisagree,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("I disagree, choose another server")
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
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
                                text = "Please read and agree to these rules before creating your account.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                        }

                        if (instance!!.rules.isEmpty()) {
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
                            itemsIndexed(instance!!.rules) { index, rule ->
                                RuleItem(
                                    number = index + 1,
                                    rule = rule
                                )
                                if (index < instance!!.rules.size - 1) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(100.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RuleItem(
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
