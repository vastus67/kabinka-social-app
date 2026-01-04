package app.kabinka.frontend.screens

import android.app.Activity
import android.app.ProgressDialog
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.kabinka.social.R
import app.kabinka.social.api.requests.catalog.GetCatalogInstances
import app.kabinka.social.api.session.AccountSessionManager
import app.kabinka.social.model.Instance
import app.kabinka.social.model.catalog.CatalogInstance
import me.grishka.appkit.api.Callback
import me.grishka.appkit.api.ErrorResponse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginChooserScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as Activity
    
    var searchQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var servers by remember { mutableStateOf<List<CatalogInstance>>(emptyList()) }
    var selectedServer by remember { mutableStateOf<CatalogInstance?>(null) }
    
    // Create default servers as fallback
    val defaultServers = remember {
        listOf(
            CatalogInstance().apply {
                domain = "mastodon.social"
                normalizedDomain = "mastodon.social"
                description = "The original server operated by the Mastodon gGmbH non-profit"
                lastWeekUsers = 1000000
            },
            CatalogInstance().apply {
                domain = "mastodon.online"
                normalizedDomain = "mastodon.online"
                description = "A general-purpose Mastodon server with a 500 character limit"
                lastWeekUsers = 500000
            }
        )
    }
    
    // Load autocomplete servers on first composition
    LaunchedEffect(Unit) {
        GetCatalogInstances(null, null, true)
            .setCallback(object : Callback<List<CatalogInstance>> {
                override fun onSuccess(result: List<CatalogInstance>) {
                    servers = result.sortedByDescending { it.lastWeekUsers }
                    Log.d("LoginChooser", "Loaded ${result.size} servers from catalog")
                }
                
                override fun onError(error: ErrorResponse) {
                    Log.w("LoginChooser", "Failed to load server catalog, using defaults: $error")
                    // Use default servers if catalog fails
                    servers = defaultServers
                }
            })
            .execNoAuth("")
    }
    
    // Filter servers based on search query
    val filteredServers = remember(searchQuery, servers) {
        if (searchQuery.isEmpty()) {
            // Show default servers when no search
            servers.filter { 
                it.normalizedDomain == "mastodon.social" || 
                it.normalizedDomain == "mastodon.online" 
            }
        } else {
            val query = searchQuery.lowercase()
            val matches = servers.filter { it.normalizedDomain.contains(query) }
            
            // If no matches found, show default servers as suggestions
            if (matches.isEmpty()) {
                servers.filter { 
                    it.normalizedDomain == "mastodon.social" || 
                    it.normalizedDomain == "mastodon.online" 
                }
            } else {
                matches
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Log in",
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFF97316),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFFF5F5F5)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search box
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(2.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { 
                        Text(
                            "Search for a server",
                            color = Color(0xFF9CA3AF)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    Icons.Default.Clear,
                                    contentDescription = "Clear",
                                    tint = Color(0xFF9CA3AF)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFF97316),
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = Color(0xFF1F2937),
                        unfocusedTextColor = Color(0xFF1F2937)
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
            
            // Server list
            if (searchQuery.isNotEmpty() && searchQuery.contains(".") && 
                !filteredServers.any { it.normalizedDomain.equals(searchQuery, ignoreCase = true) }) {
                // Show custom server option when user types a domain
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .clickable {
                            isLoading = true
                            val progressDialog = ProgressDialog(activity)
                            progressDialog.setCancelable(false)
                            progressDialog.setMessage(activity.getString(R.string.loading_instance))
                            progressDialog.show()
                            
                            AccountSessionManager.loadInstanceInfo(searchQuery, object : Callback<Instance> {
                                override fun onSuccess(result: Instance) {
                                    progressDialog.dismiss()
                                    isLoading = false
                                    AccountSessionManager.getInstance().authenticate(activity, result)
                                }
                                
                                override fun onError(error: ErrorResponse) {
                                    progressDialog.dismiss()
                                    isLoading = false
                                    error.showToast(activity)
                                }
                            })
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    elevation = CardDefaults.cardElevation(1.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = false,
                            onClick = null,
                            colors = RadioButtonDefaults.colors(
                                selectedColor = Color(0xFFF97316),
                                unselectedColor = Color(0xFF9CA3AF)
                            )
                        )
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = searchQuery,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF1F2937)
                            )
                            Text(
                                text = "Tap to connect to this server",
                                fontSize = 14.sp,
                                color = Color(0xFF6B7280)
                            )
                        }
                    }
                }
            }
            
            // Server list from catalog
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredServers) { server ->
                    ServerListItem(
                        server = server,
                        isSelected = selectedServer == server,
                        onClick = {
                            selectedServer = server
                            isLoading = true
                            
                            val progressDialog = ProgressDialog(activity)
                            progressDialog.setCancelable(false)
                            progressDialog.setMessage(activity.getString(R.string.loading_instance))
                            progressDialog.show()
                            
                            AccountSessionManager.loadInstanceInfo(server.domain, object : Callback<Instance> {
                                override fun onSuccess(result: Instance) {
                                    progressDialog.dismiss()
                                    isLoading = false
                                    AccountSessionManager.getInstance().authenticate(activity, result)
                                }
                                
                                override fun onError(error: ErrorResponse) {
                                    progressDialog.dismiss()
                                    isLoading = false
                                    error.showToast(activity)
                                }
                            })
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ServerListItem(
    server: CatalogInstance,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(
                    selectedColor = Color(0xFFF97316),
                    unselectedColor = Color(0xFF9CA3AF)
                )
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = server.domain,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF1F2937)
                )
                if (server.description.isNotEmpty()) {
                    Text(
                        text = server.description,
                        fontSize = 14.sp,
                        color = Color(0xFF6B7280),
                        maxLines = 2
                    )
                }
            }
        }
    }
}
