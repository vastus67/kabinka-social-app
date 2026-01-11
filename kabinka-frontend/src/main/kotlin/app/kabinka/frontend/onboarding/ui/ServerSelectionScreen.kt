package app.kabinka.frontend.onboarding.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import app.kabinka.social.api.requests.catalog.GetCatalogInstances
import app.kabinka.social.model.catalog.CatalogInstance
import compose.icons.LineAwesomeIcons
import compose.icons.lineawesomeicons.ServerSolid
import kotlinx.coroutines.launch
import me.grishka.appkit.api.Callback
import me.grishka.appkit.api.ErrorResponse
import android.util.Log

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerSelectionScreen(
    onServerSelected: (String) -> Unit,
    onBack: () -> Unit
) {
    var customServerInput by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var servers by remember { mutableStateOf<List<CatalogInstance>>(emptyList()) }
    var showCustomInput by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current

    // Load server catalog on launch
    LaunchedEffect(Unit) {
        Log.d("ServerSelection", "Starting catalog fetch...")
        GetCatalogInstances(null, null, false)
            .setCallback(object : Callback<List<CatalogInstance>> {
                override fun onSuccess(result: List<CatalogInstance>) {
                    Log.d("ServerSelection", "Success! Got ${result.size} servers")
                    servers = result.filter { it.approvalRequired == false }
                        .take(20)
                    isLoading = false
                }

                override fun onError(error: ErrorResponse) {
                    Log.e("ServerSelection", "Error loading servers", error as? Throwable)
                    val msg = if (error is app.kabinka.social.api.MastodonErrorResponse) {
                        Log.e("ServerSelection", "HTTP ${error.httpStatus}: ${error.error}")
                        error.underlyingException?.printStackTrace()
                        error.error ?: "Network error"
                    } else {
                        "Failed to load servers"
                    }
                    errorMessage = msg
                    isLoading = false
                }
            })
            .execNoAuth("")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Choose a server") },
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
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            text = "Popular servers",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    items(servers) { server ->
                        ServerCard(
                            server = server,
                            onClick = { onServerSelected(server.domain) }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedButton(
                            onClick = { showCustomInput = !showCustomInput },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (showCustomInput) "Hide custom server" else "Enter custom server")
                        }
                    }

                    if (showCustomInput) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = customServerInput,
                                onValueChange = { customServerInput = it },
                                label = { Text("Server domain") },
                                placeholder = { Text("example.social") },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Uri,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        keyboardController?.hide()
                                        if (customServerInput.isNotBlank()) {
                                            onServerSelected(customServerInput.trim())
                                        }
                                    }
                                ),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Button(
                                onClick = {
                                    if (customServerInput.isNotBlank()) {
                                        onServerSelected(customServerInput.trim())
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = customServerInput.isNotBlank()
                            ) {
                                Text("Continue with this server")
                            }
                        }
                    }

                    if (errorMessage != null) {
                        item {
                            Text(
                                text = errorMessage!!,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ServerCard(
    server: CatalogInstance,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = LineAwesomeIcons.ServerSolid,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = server.domain,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (server.totalUsers > 0) {
                        Text(
                            text = "${server.totalUsers} users",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            if (!server.description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = server.description!!,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 3,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
