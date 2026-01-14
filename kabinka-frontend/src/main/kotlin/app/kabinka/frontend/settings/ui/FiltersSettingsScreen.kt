package app.kabinka.frontend.settings.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import compose.icons.lineawesomeicons.PlusSolid
import app.kabinka.social.api.requests.filters.GetFilters
import app.kabinka.social.model.Filter
import me.grishka.appkit.api.Callback
import me.grishka.appkit.api.ErrorResponse
import app.kabinka.social.api.session.AccountSessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FiltersSettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAddFilter: () -> Unit,
    onNavigateToEditFilter: (String) -> Unit
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
    var filters by remember { mutableStateOf<List<Filter>>(emptyList()) }
    
    // Load filters from API
    LaunchedEffect(Unit) {
        accountSession?.let { session ->
            withContext(Dispatchers.IO) {
                suspendCoroutine { continuation ->
                    GetFilters()
                        .setCallback(object : Callback<List<Filter>> {
                            override fun onSuccess(result: List<Filter>) {
                                filters = result
                                isLoading = false
                                continuation.resume(Unit)
                            }
                            
                            override fun onError(error: ErrorResponse) {
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
                        "Filters", 
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
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddFilter
            ) {
                Icon(
                    imageVector = LineAwesomeIcons.PlusSolid,
                    contentDescription = "Add Filter"
                )
            }
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
        } else if (filters.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No filters yet. Tap + to create one.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(filters) { filter ->
                    FilterListItem(
                        filter = filter,
                        onClick = {
                            onNavigateToEditFilter(filter.id)
                        }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                }
            }
        }
    }
    
}

@Composable
fun FilterListItem(
    filter: Filter,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = filter.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (filter.isActive) "Active" else "Inactive",
                fontSize = 14.sp,
                color = if (filter.isActive) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Icon(
            imageVector = LineAwesomeIcons.AngleLeftSolid,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
