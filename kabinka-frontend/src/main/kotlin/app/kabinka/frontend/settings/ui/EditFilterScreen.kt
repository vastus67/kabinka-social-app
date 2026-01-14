package app.kabinka.frontend.settings.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import compose.icons.lineawesomeicons.AngleRightSolid
import compose.icons.lineawesomeicons.PlusSolid
import compose.icons.lineawesomeicons.TimesSolid
import app.kabinka.social.model.Filter
import app.kabinka.social.model.FilterContext
import app.kabinka.social.model.FilterKeyword
import app.kabinka.social.model.FilterAction
import app.kabinka.social.api.requests.filters.CreateFilter
import app.kabinka.social.api.requests.filters.UpdateFilter
import app.kabinka.social.api.requests.filters.DeleteFilter
import app.kabinka.social.api.requests.filters.GetFilters
import app.kabinka.social.api.session.AccountSessionManager
import me.grishka.appkit.api.Callback
import me.grishka.appkit.api.ErrorResponse
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.time.format.DateTimeFormatter
import java.time.ZoneId
import java.util.EnumSet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditFilterScreen(
    filterId: String?,
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
    
    var filter by remember { mutableStateOf<Filter?>(null) }
    var isLoading by remember { mutableStateOf(filterId != null) }
    var title by remember { mutableStateOf("") }
    var showDurationDialog by remember { mutableStateOf(false) }
    var selectedDuration by remember { mutableStateOf<String>("Forever") }
    var expiresAt by remember { mutableStateOf<Instant?>(null) }
    var showWordsDialog by remember { mutableStateOf(false) }
    var keywords by remember { mutableStateOf(mutableListOf<FilterKeyword>()) }
    var showContextDialog by remember { mutableStateOf(false) }
    var filterContext by remember { 
        mutableStateOf<EnumSet<FilterContext>>(EnumSet.allOf(FilterContext::class.java)) 
    }
    var showWithCW by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    
    // Load filter if editing
    LaunchedEffect(filterId) {
        if (filterId != null && accountSession != null) {
            withContext(Dispatchers.IO) {
                suspendCoroutine { continuation ->
                    GetFilters()
                        .setCallback(object : Callback<List<Filter>> {
                            override fun onSuccess(result: List<Filter>) {
                                val foundFilter = result.find { it.id == filterId }
                                if (foundFilter != null) {
                                    filter = foundFilter
                                    title = foundFilter.title
                                    expiresAt = foundFilter.expiresAt
                                    keywords = foundFilter.keywords?.toMutableList() ?: mutableListOf()
                                    filterContext = foundFilter.context?.clone() as? EnumSet<FilterContext> ?: EnumSet.allOf(FilterContext::class.java)
                                    showWithCW = foundFilter.filterAction == FilterAction.WARN
                                }
                                isLoading = false
                                continuation.resume(Unit)
                            }
                            
                            override fun onError(error: ErrorResponse) {
                                isLoading = false
                                continuation.resume(Unit)
                            }
                        })
                        .exec(accountSession.getID())
                }
            }
        }
    }
    
    // Update duration text based on expiresAt
    LaunchedEffect(expiresAt) {
        selectedDuration = when {
            expiresAt == null -> "Forever"
            else -> {
                val now = Instant.now()
                val seconds = now.until(expiresAt, ChronoUnit.SECONDS)
                when {
                    seconds <= 1800 -> "30 minutes"
                    seconds <= 3600 -> "1 hour"
                    seconds <= 12 * 3600 -> "12 hours"
                    seconds <= 24 * 3600 -> "1 day"
                    seconds <= 3 * 24 * 3600 -> "3 days"
                    seconds <= 7 * 24 * 3600 -> "1 week"
                    else -> "Custom"
                }
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        if (filterId == null) "Add Filter" else "Edit Filter",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = LineAwesomeIcons.TimesSolid,
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
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    // Title field
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title") },
                        placeholder = { Text("e.g., Spoilers, Politics") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Duration
                    Text(
                        text = "Duration",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDurationDialog = true }
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (expiresAt == null) {
                                "Never expires"
                            } else {
                                val formatter = DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a")
                                    .withZone(ZoneId.systemDefault())
                                "Expires ${formatter.format(expiresAt)}"
                            },
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Icon(
                            imageVector = LineAwesomeIcons.AngleRightSolid,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Muted words
                    Text(
                        text = "Muted words",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showWordsDialog = true }
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (keywords.size == 0) {
                                "No words added"
                            } else {
                                keywords.joinToString(", ") { it.keyword }
                            },
                            fontSize = 14.sp,
                            color = if (keywords.size == 0) 
                                MaterialTheme.colorScheme.onSurfaceVariant 
                            else 
                                MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2
                        )
                        Icon(
                            imageVector = LineAwesomeIcons.AngleRightSolid,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Mute from
                    Text(
                        text = "Mute from",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showContextDialog = true }
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = filterContext.joinToString(", ") { ctx ->
                                when (ctx) {
                                    FilterContext.HOME -> "Home & Lists"
                                    FilterContext.NOTIFICATIONS -> "Notifications"
                                    FilterContext.PUBLIC -> "Public timelines"
                                    FilterContext.THREAD -> "Threads & replies"
                                    FilterContext.ACCOUNT -> "Profiles"
                                    else -> ctx.name
                                }
                            },
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2
                        )
                        Icon(
                            imageVector = LineAwesomeIcons.AngleRightSolid,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Show with content warning
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Show with content warning",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Hide filtered content behind a warning",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = showWithCW,
                            onCheckedChange = { showWithCW = it }
                        )
                    }
                    
                    if (filter != null) {
                        val currentFilter = filter!!
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = {
                                // Delete filter
                                accountSession?.let { session ->
                                    isSaving = true
                                    DeleteFilter(currentFilter.id)
                                        .setCallback(object : Callback<Void> {
                                            override fun onSuccess(result: Void?) {
                                                isSaving = false
                                                onNavigateBack()
                                            }
                                            
                                            override fun onError(error: ErrorResponse) {
                                                isSaving = false
                                            }
                                        })
                                        .exec(session.getID())
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isSaving
                        ) {
                            Text("Delete Filter")
                        }
                    }
                }
                
                HorizontalDivider()
                
                // Footer
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onNavigateBack, enabled = !isSaving) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (title.isBlank()) return@Button
                            
                            accountSession?.let { session ->
                                isSaving = true
                                
                                val filterAction = if (showWithCW) FilterAction.WARN else FilterAction.HIDE
                                
                                if (filter == null) {
                                    // Create new filter
                                    CreateFilter(
                                        title,
                                        filterContext,
                                        filterAction,
                                        expiresAt?.epochSecond?.toInt() ?: 0,
                                        keywords
                                    )
                                        .setCallback(object : Callback<Filter> {
                                            override fun onSuccess(result: Filter) {
                                                isSaving = false
                                                onNavigateBack()
                                            }
                                            
                                            override fun onError(error: ErrorResponse) {
                                                isSaving = false
                                            }
                                        })
                                        .exec(session.getID())
                                } else {
                                    val currentFilter = filter!!
                                    // Update existing filter
                                    UpdateFilter(
                                        currentFilter.id,
                                        title,
                                        filterContext,
                                        filterAction,
                                        expiresAt?.epochSecond?.toInt() ?: 0,
                                        keywords,
                                        emptyList()
                                    )
                                        .setCallback(object : Callback<Filter> {
                                            override fun onSuccess(result: Filter) {
                                                isSaving = false
                                                onNavigateBack()
                                            }
                                            
                                            override fun onError(error: ErrorResponse) {
                                                isSaving = false
                                            }
                                        })
                                        .exec(session.getID())
                                }
                            }
                        },
                        enabled = title.isNotBlank() && !isSaving
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Save")
                        }
                    }
                }
            }
        }
    }
    
    // Duration dialog
    if (showDurationDialog) {
        DurationPickerDialog(
            selectedDuration = selectedDuration,
            onDismiss = { showDurationDialog = false },
            onSelect = { duration, instant ->
                selectedDuration = duration
                expiresAt = instant
                showDurationDialog = false
            }
        )
    }
    
    // Words dialog
    if (showWordsDialog) {
        WordsPickerDialog(
            keywords = keywords,
            onDismiss = { showWordsDialog = false },
            onSave = { updatedKeywords ->
                keywords = updatedKeywords.toMutableList()
                showWordsDialog = false
            }
        )
    }
    
    // Context dialog
    if (showContextDialog) {
        ContextPickerDialog(
            context = filterContext,
            onDismiss = { showContextDialog = false },
            onSave = { updatedContext ->
                filterContext = updatedContext
                showContextDialog = false
            }
        )
    }
}

@Composable
fun DurationPickerDialog(
    selectedDuration: String,
    onDismiss: () -> Unit,
    onSelect: (String, Instant?) -> Unit
) {
    val durations = listOf(
        "Forever" to null,
        "30 minutes" to 1800L,
        "1 hour" to 3600L,
        "12 hours" to (12 * 3600L),
        "1 day" to (24 * 3600L),
        "3 days" to (3 * 24 * 3600L),
        "1 week" to (7 * 24 * 3600L)
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filter duration") },
        text = {
            Column {
                durations.forEach { (label, seconds) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val instant = seconds?.let { Instant.now().plusSeconds(it) }
                                onSelect(label, instant)
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedDuration == label,
                            onClick = {
                                val instant = seconds?.let { Instant.now().plusSeconds(it) }
                                onSelect(label, instant)
                            }
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = label)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun WordsPickerDialog(
    keywords: List<FilterKeyword>,
    onDismiss: () -> Unit,
    onSave: (List<FilterKeyword>) -> Unit
) {
    var workingKeywords by remember { mutableStateOf(keywords.toMutableList()) }
    var showAddWordDialog by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Muted words") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                if (workingKeywords.size == 0) {
                    Text(
                        text = "No words added yet",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                    ) {
                        items(workingKeywords) { keyword ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = keyword.keyword,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = {
                                        workingKeywords = workingKeywords.toMutableList().apply {
                                            remove(keyword)
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = LineAwesomeIcons.TimesSolid,
                                        contentDescription = "Remove",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Button(
                    onClick = { showAddWordDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = LineAwesomeIcons.PlusSolid,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add word or phrase")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(workingKeywords) }) {
                Text("Done")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
    
    if (showAddWordDialog) {
        AddWordDialog(
            onDismiss = { showAddWordDialog = false },
            onAdd = { word, wholeWord ->
                val newKeyword = FilterKeyword()
                newKeyword.keyword = word
                newKeyword.wholeWord = wholeWord
                workingKeywords = workingKeywords.toMutableList().apply { add(newKeyword) }
                showAddWordDialog = false
            }
        )
    }
}

@Composable
fun AddWordDialog(
    onDismiss: () -> Unit,
    onAdd: (String, Boolean) -> Unit
) {
    var word by remember { mutableStateOf("") }
    var wholeWord by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add word or phrase") },
        text = {
            Column {
                OutlinedTextField(
                    value = word,
                    onValueChange = { word = it },
                    label = { Text("Word or phrase") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = wholeWord,
                        onCheckedChange = { wholeWord = it }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Whole word")
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onAdd(word, wholeWord) },
                enabled = word.isNotBlank()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ContextPickerDialog(
    context: EnumSet<FilterContext>,
    onDismiss: () -> Unit,
    onSave: (EnumSet<FilterContext>) -> Unit
) {
    var workingContext by remember { mutableStateOf<EnumSet<FilterContext>>(EnumSet.copyOf(context)) }
    
    val contextItems = listOf(
        FilterContext.HOME to "Home & Lists",
        FilterContext.NOTIFICATIONS to "Notifications",
        FilterContext.PUBLIC to "Public timelines",
        FilterContext.THREAD to "Threads & replies",
        FilterContext.ACCOUNT to "Profiles"
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Mute from") },
        text = {
            Column {
                contextItems.forEach { (ctx, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                workingContext = if (workingContext.contains(ctx)) {
                                    EnumSet.copyOf(workingContext).apply { remove(ctx) }
                                } else {
                                    EnumSet.copyOf(workingContext).apply { add(ctx) }
                                }
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = workingContext.contains(ctx),
                            onCheckedChange = {
                                workingContext = if (it) {
                                    EnumSet.copyOf(workingContext).apply { add(ctx) }
                                } else {
                                    EnumSet.copyOf(workingContext).apply { remove(ctx) }
                                }
                            }
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = label)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(workingContext) }) {
                Text("Done")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
