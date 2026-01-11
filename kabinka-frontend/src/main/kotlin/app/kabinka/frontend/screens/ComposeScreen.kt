package app.kabinka.frontend.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import app.kabinka.frontend.auth.SessionStateManager
import app.kabinka.social.api.ProgressListener
import app.kabinka.social.api.requests.statuses.CreateStatus
import app.kabinka.social.api.requests.statuses.EditStatus
import app.kabinka.social.api.requests.statuses.UploadAttachment
import app.kabinka.social.api.requests.statuses.UpdateAttachment
import app.kabinka.social.api.session.AccountSessionManager
import app.kabinka.social.model.Account
import app.kabinka.social.model.Attachment
import app.kabinka.social.model.Status
import app.kabinka.social.model.StatusPrivacy
import coil.compose.AsyncImage
import compose.icons.LineAwesomeIcons
import compose.icons.lineawesomeicons.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.grishka.appkit.api.Callback
import me.grishka.appkit.api.ErrorResponse
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComposeScreen(
    sessionManager: SessionStateManager,
    onNavigateBack: () -> Unit = {},
    replyToStatus: Status? = null,
    editingStatus: Status? = null
) {
    val viewModel: ComposeViewModel = viewModel { 
        ComposeViewModel(sessionManager, replyToStatus, editingStatus)
    }
    
    val uiState by viewModel.uiState.collectAsState()
    val statusText by viewModel.statusText.collectAsState()
    val visibility by viewModel.visibility.collectAsState()
    val hasSpoiler by viewModel.hasSpoiler.collectAsState()
    val spoilerText by viewModel.spoilerText.collectAsState()
    val attachments by viewModel.attachments.collectAsState()
    val poll by viewModel.poll.collectAsState()
    val currentAccount = viewModel.currentAccount
    val charCount = statusText.length
    val charLimit = viewModel.charLimit
    
    var showVisibilityMenu by remember { mutableStateOf(false) }
    var showPollOptions by remember { mutableStateOf(false) }
    var showEmojiPicker by remember { mutableStateOf(false) }
    
    // Navigate back on successful publish
    LaunchedEffect(uiState) {
        if (uiState is ComposeUiState.Success) {
            onNavigateBack()
        }
    }
    
    // Media picker launcher
    val mediaPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 4)
    ) { uris ->
        if (uris.isNotEmpty()) {
            viewModel.addMediaAttachments(uris)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        if (editingStatus != null) "Edit post"
                        else if (replyToStatus != null) "Reply"
                        else "New post"
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (viewModel.hasUnsavedChanges()) {
                            viewModel.saveDraft()
                        }
                        onNavigateBack()
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { 
                            println("ComposeScreen: Post button clicked")
                            viewModel.publish() 
                        },
                        enabled = statusText.isNotBlank() && uiState !is ComposeUiState.Publishing
                    ) {
                        if (uiState is ComposeUiState.Publishing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        } else {
                            Icon(
                                imageVector = LineAwesomeIcons.PlaySolid,
                                contentDescription = if (editingStatus != null) "Update" else "Post",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Main content area
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Reply-to preview
                if (replyToStatus != null) {
                    ReplyToPreview(replyToStatus)
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                // Visibility selector
                Surface(
                    modifier = Modifier.clickable { showVisibilityMenu = true },
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = when (visibility) {
                                StatusPrivacy.PUBLIC -> LineAwesomeIcons.GlobeSolid
                                StatusPrivacy.UNLISTED -> LineAwesomeIcons.LockSolid
                                StatusPrivacy.PRIVATE -> LineAwesomeIcons.UsersSolid
                                StatusPrivacy.DIRECT -> LineAwesomeIcons.EnvelopeSolid
                            },
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = when (visibility) {
                                StatusPrivacy.PUBLIC -> "Public, anyone can quote"
                                StatusPrivacy.UNLISTED -> "Unlisted"
                                StatusPrivacy.PRIVATE -> "Followers only"
                                StatusPrivacy.DIRECT -> "Direct message"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // User info
                if (currentAccount != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AsyncImage(
                            model = currentAccount.avatar,
                            contentDescription = null,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        
                        Column {
                            Text(
                                text = currentAccount.displayName.ifEmpty { currentAccount.username },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "@${currentAccount.username}@${viewModel.instanceDomain}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                // Content warning if enabled
                if (hasSpoiler) {
                    OutlinedTextField(
                        value = spoilerText,
                        onValueChange = { viewModel.updateSpoilerText(it) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Content warning") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                }
                
                // Status input
                TextField(
                    value = statusText,
                    onValueChange = { newText -> viewModel.updateStatusText(newText) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { 
                        Text(
                            "Type or paste what's on your mind",
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    },
                    minLines = 8,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    )
                )
                
                // Media attachments preview
                if (attachments.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    MediaAttachmentsGrid(
                        attachments = attachments,
                        onRemove = { viewModel.removeAttachment(it) }
                    )
                }
                
                // Poll options
                poll?.let { currentPoll ->
                    Spacer(modifier = Modifier.height(16.dp))
                    PollComposer(
                        poll = currentPoll,
                        onUpdateOption = { index, text -> viewModel.updatePollOption(index, text) },
                        onAddOption = { viewModel.addPollOption() },
                        onRemoveOption = { viewModel.removePollOption(it) },
                        onUpdateDuration = { viewModel.updatePollDuration(it) },
                        onToggleMultiple = { viewModel.togglePollMultiple() },
                        onRemovePoll = { viewModel.removePoll() }
                    )
                }
            }
            
            HorizontalDivider()
            
            // Bottom toolbar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left side - attachment options
                Row {
                    IconButton(
                        onClick = {
                            mediaPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                            )
                        },
                        enabled = attachments.size < 4 && poll == null
                    ) {
                        Icon(
                            imageVector = LineAwesomeIcons.ImageSolid,
                            contentDescription = "Add media",
                            tint = if (attachments.isNotEmpty()) MaterialTheme.colorScheme.primary
                                   else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    IconButton(
                        onClick = { 
                            if (poll == null) {
                                viewModel.createPoll()
                            } else {
                                viewModel.removePoll()
                            }
                        },
                        enabled = attachments.isEmpty()
                    ) {
                        Icon(
                            imageVector = LineAwesomeIcons.PollSolid,
                            contentDescription = "Add poll",
                            tint = if (poll != null) MaterialTheme.colorScheme.primary
                                   else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    IconButton(onClick = { showEmojiPicker = true }) {
                        Icon(
                            imageVector = LineAwesomeIcons.SmileSolid,
                            contentDescription = "Add emoji",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    IconButton(
                        onClick = { viewModel.toggleSpoiler() }
                    ) {
                        Icon(
                            imageVector = LineAwesomeIcons.ExclamationTriangleSolid,
                            contentDescription = "Add content warning",
                            tint = if (hasSpoiler) MaterialTheme.colorScheme.primary 
                                   else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    IconButton(onClick = { /* TODO: Language selector */ }) {
                        Icon(
                            imageVector = LineAwesomeIcons.GlobeSolid,
                            contentDescription = "Select language",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                
                // Right side - character counter
                Text(
                    text = "$charLimit",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (charCount > charLimit) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
        
        // Visibility menu
        DropdownMenu(
            expanded = showVisibilityMenu,
            onDismissRequest = { showVisibilityMenu = false }
        ) {
            DropdownMenuItem(
                text = { 
                    Column {
                        Text("Public")
                        Text(
                            "Anyone can see and quote",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                onClick = {
                    viewModel.updateVisibility(StatusPrivacy.PUBLIC)
                    showVisibilityMenu = false
                },
                leadingIcon = {
                    Icon(LineAwesomeIcons.GlobeSolid, contentDescription = null)
                }
            )
            DropdownMenuItem(
                text = { 
                    Column {
                        Text("Unlisted")
                        Text(
                            "Not shown in public timelines",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                onClick = {
                    viewModel.updateVisibility(StatusPrivacy.UNLISTED)
                    showVisibilityMenu = false
                },
                leadingIcon = {
                    Icon(LineAwesomeIcons.LockSolid, contentDescription = null)
                }
            )
            DropdownMenuItem(
                text = { 
                    Column {
                        Text("Followers only")
                        Text(
                            "Only followers can see",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                onClick = {
                    viewModel.updateVisibility(StatusPrivacy.PRIVATE)
                    showVisibilityMenu = false
                },
                leadingIcon = {
                    Icon(LineAwesomeIcons.UsersSolid, contentDescription = null)
                }
            )
            DropdownMenuItem(
                text = { 
                    Column {
                        Text("Direct")
                        Text(
                            "Only mentioned users",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                onClick = {
                    viewModel.updateVisibility(StatusPrivacy.DIRECT)
                    showVisibilityMenu = false
                },
                leadingIcon = {
                    Icon(LineAwesomeIcons.EnvelopeSolid, contentDescription = null)
                }
            )
        }
        
        // Emoji picker dialog
        if (showEmojiPicker) {
            BasicEmojiPickerDialog(
                onDismiss = { showEmojiPicker = false },
                onEmojiSelected = { emoji ->
                    viewModel.insertEmoji(emoji)
                    showEmojiPicker = false
                }
            )
        }
        
        // Error snackbar
        if (uiState is ComposeUiState.Error) {
            Snackbar(
                modifier = Modifier.padding(16.dp),
                action = {
                    TextButton(onClick = { viewModel.clearError() }) {
                        Text("Dismiss")
                    }
                }
            ) {
                Text((uiState as ComposeUiState.Error).message)
            }
        }
    }
}

@Composable
fun ReplyToPreview(status: Status) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AsyncImage(
                model = status.account.avatar,
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Replying to @${status.account.username}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = status.content,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 3,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun MediaAttachmentsGrid(
    attachments: List<DraftAttachment>,
    onRemove: (DraftAttachment) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(attachments) { attachment ->
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                AsyncImage(
                    model = attachment.uri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                
                // Upload progress
                if (attachment.uploadProgress < 1f && attachment.uploadProgress > 0f) {
                    CircularProgressIndicator(
                        progress = attachment.uploadProgress,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(40.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
                
                // Remove button
                IconButton(
                    onClick = { onRemove(attachment) },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(24.dp)
                        .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun PollComposer(
    poll: DraftPoll,
    onUpdateOption: (Int, String) -> Unit,
    onAddOption: () -> Unit,
    onRemoveOption: (Int) -> Unit,
    onUpdateDuration: (Int) -> Unit,
    onToggleMultiple: () -> Unit,
    onRemovePoll: () -> Unit
) {
    val currentPoll = poll
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Poll",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                IconButton(onClick = onRemovePoll) {
                    Icon(Icons.Default.Close, contentDescription = "Remove poll")
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Poll options
            currentPoll.options.forEachIndexed { index, option ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = option,
                        onValueChange = { onUpdateOption(index, it) },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Option ${index + 1}") },
                        singleLine = true
                    )
                    
                    if (currentPoll.options.size > 2) {
                        IconButton(onClick = { onRemoveOption(index) }) {
                            Icon(
                                imageVector = LineAwesomeIcons.MinusSolid,
                                contentDescription = "Remove option"
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            // Add option button
            if (currentPoll.options.size < 4) {
                TextButton(onClick = onAddOption) {
                    Icon(
                        imageVector = LineAwesomeIcons.PlusSolid,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add option")
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(12.dp))
            
            // Poll settings
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Duration", style = MaterialTheme.typography.bodySmall)
                    Text(
                        text = formatDuration(currentPoll.durationSeconds),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Column {
                    Text("Multiple choice", style = MaterialTheme.typography.bodySmall)
                    Switch(
                        checked = currentPoll.multipleChoice,
                        onCheckedChange = { onToggleMultiple() }
                    )
                }
            }
        }
    }
}

@Composable
fun BasicEmojiPickerDialog(
    onDismiss: () -> Unit,
    onEmojiSelected: (String) -> Unit
) {
    val commonEmojis = remember {
        listOf(
            "😀", "😃", "😄", "😁", "😆", "😅", "😂", "🤣", "😊", "😇",
            "🙂", "🙃", "😉", "😌", "😍", "🥰", "😘", "😗", "😙", "😚",
            "😋", "😛", "😝", "😜", "🤪", "🤨", "🧐", "🤓", "😎", "🤩",
            "🥳", "😏", "😒", "😞", "😔", "😟", "😕", "🙁", "☹️", "😣",
            "👍", "👎", "👏", "🙌", "👐", "🤲", "🤝", "🙏", "✨", "💫",
            "⭐", "🌟", "💥", "💢", "💯", "🔥", "❤️", "💙", "💚", "💛",
            "💜", "🖤", "🤍", "🤎", "💔", "❣️", "💕", "💞", "💓", "💗"
        )
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choose emoji") },
        text = {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(commonEmojis) { emoji ->
                    TextButton(
                        onClick = { onEmojiSelected(emoji) },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Text(
                            text = emoji,
                            style = MaterialTheme.typography.headlineMedium
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

fun formatDuration(seconds: Int): String {
    return when {
        seconds < 3600 -> "${seconds / 60} minutes"
        seconds < 86400 -> "${seconds / 3600} hours"
        else -> "${seconds / 86400} days"
    }
}

class ComposeViewModel(
    private val sessionManager: SessionStateManager,
    private val replyToStatus: Status? = null,
    private val editingStatus: Status? = null
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<ComposeUiState>(ComposeUiState.Idle)
    val uiState = _uiState.asStateFlow()
    
    private val _statusText = MutableStateFlow("")
    val statusText = _statusText.asStateFlow()
    
    private val _visibility = MutableStateFlow(StatusPrivacy.PUBLIC)
    val visibility = _visibility.asStateFlow()
    
    private val _hasSpoiler = MutableStateFlow(false)
    val hasSpoiler = _hasSpoiler.asStateFlow()
    
    private val _spoilerText = MutableStateFlow("")
    val spoilerText = _spoilerText.asStateFlow()
    
    private val _attachments = MutableStateFlow<List<DraftAttachment>>(emptyList())
    val attachments = _attachments.asStateFlow()
    
    private val _poll = MutableStateFlow<DraftPoll?>(null)
    val poll = _poll.asStateFlow()
    
    private val initialStatusText: String
    
    val currentAccount: Account?
    val instanceDomain: String
    
    init {
        val session = AccountSessionManager.getInstance().lastActiveAccount
        currentAccount = session?.self
        instanceDomain = session?.domain ?: "mastodon.social"
        
        // Initialize from reply or edit
        when {
            editingStatus != null -> {
                _statusText.value = editingStatus.content
                _visibility.value = editingStatus.visibility
                _hasSpoiler.value = !editingStatus.spoilerText.isNullOrEmpty()
                _spoilerText.value = editingStatus.spoilerText ?: ""
                
                // Load existing attachments
                _attachments.value = editingStatus.mediaAttachments.map { att ->
                    DraftAttachment(
                        uri = Uri.parse(att.url),
                        uploadedAttachment = att,
                        uploadProgress = 1f
                    )
                }
                
                // Load existing poll
                editingStatus.poll?.let { existingPoll ->
                    _poll.value = DraftPoll(
                        options = existingPoll.options.map { it.title }.toMutableList(),
                        durationSeconds = 86400, // Default
                        multipleChoice = existingPoll.multiple
                    )
                }
            }
            replyToStatus != null -> {
                _visibility.value = replyToStatus.visibility
                // Mention the person we're replying to
                val mention = "@${replyToStatus.account.username} "
                _statusText.value = mention
            }
        }
        
        initialStatusText = _statusText.value
    }
    
    val charLimit: Int
        get() {
            val session = AccountSessionManager.getInstance().lastActiveAccount
            val instance = session?.let { 
                AccountSessionManager.getInstance().getInstanceInfo(it.domain)
            }
            val limit = instance?.maxTootChars ?: 500
            println("ComposeViewModel: charLimit - instance=$instance, maxTootChars=${instance?.maxTootChars}, returning=$limit")
            return if (limit > 0) limit else 500
        }
    
    fun updateStatusText(text: String) {
        _statusText.value = text
    }
    
    fun updateVisibility(visibility: StatusPrivacy) {
        _visibility.value = visibility
    }
    
    fun toggleSpoiler() {
        _hasSpoiler.value = !_hasSpoiler.value
        if (!_hasSpoiler.value) {
            _spoilerText.value = ""
        }
    }
    
    fun updateSpoilerText(text: String) {
        _spoilerText.value = text
    }
    
    fun insertEmoji(emoji: String) {
        _statusText.value = _statusText.value + emoji
    }
    
    fun addMediaAttachments(uris: List<Uri>) {
        val session = AccountSessionManager.getInstance().lastActiveAccount ?: return
        
        viewModelScope.launch {
            uris.forEach { uri ->
                val draft = DraftAttachment(uri = uri, uploadProgress = 0f)
                _attachments.value = _attachments.value + draft
                
                // Upload asynchronously
                uploadAttachment(draft, session.getID())
            }
        }
    }
    
    private suspend fun uploadAttachment(draft: DraftAttachment, accountId: String) = withContext(Dispatchers.IO) {
        UploadAttachment(draft.uri)
            .setProgressListener(object : ProgressListener {
                override fun onProgress(current: Long, total: Long) {
                    val progress = current.toFloat() / total.toFloat()
                    _attachments.value = _attachments.value.map {
                        if (it.uri == draft.uri) it.copy(uploadProgress = progress) else it
                    }
                }
            })
            .setCallback(object : Callback<Attachment> {
                override fun onSuccess(result: Attachment) {
                    _attachments.value = _attachments.value.map {
                        if (it.uri == draft.uri) {
                            it.copy(uploadedAttachment = result, uploadProgress = 1f)
                        } else {
                            it
                        }
                    }
                }
                
                override fun onError(error: ErrorResponse) {
                    // Remove failed attachment
                    _attachments.value = _attachments.value.filterNot { it.uri == draft.uri }
                    _uiState.value = ComposeUiState.Error("Upload failed: $error")
                }
            })
            .exec(accountId)
    }
    
    fun removeAttachment(attachment: DraftAttachment) {
        _attachments.value = _attachments.value.filterNot { it == attachment }
    }
    
    fun createPoll() {
        _poll.value = DraftPoll(
            options = mutableListOf("", ""),
            durationSeconds = 86400, // 1 day default
            multipleChoice = false
        )
    }
    
    fun removePoll() {
        _poll.value = null
    }
    
    fun updatePollOption(index: Int, text: String) {
        _poll.value = _poll.value?.copy(
            options = _poll.value!!.options.toMutableList().apply {
                this[index] = text
            }
        )
    }
    
    fun addPollOption() {
        _poll.value = _poll.value?.copy(
            options = (_poll.value!!.options + "").toMutableList()
        )
    }
    
    fun removePollOption(index: Int) {
        _poll.value = _poll.value?.copy(
            options = _poll.value!!.options.toMutableList().apply {
                removeAt(index)
            }
        )
    }
    
    fun updatePollDuration(seconds: Int) {
        _poll.value = _poll.value?.copy(durationSeconds = seconds)
    }
    
    fun togglePollMultiple() {
        _poll.value = _poll.value?.copy(multipleChoice = !_poll.value!!.multipleChoice)
    }
    
    fun hasUnsavedChanges(): Boolean {
        return _statusText.value != initialStatusText ||
               _attachments.value.isNotEmpty() ||
               _poll.value != null ||
               _hasSpoiler.value
    }
    
    fun saveDraft() {
        // TODO: Implement draft saving to local storage
        // For now, just a placeholder
    }
    
    fun clearError() {
        _uiState.value = ComposeUiState.Idle
    }
    
    fun publish() {
        println("ComposeViewModel: publish() called")
        val session = AccountSessionManager.getInstance().lastActiveAccount
        if (session == null) {
            println("ComposeViewModel: No active session")
            _uiState.value = ComposeUiState.Error("No active session")
            return
        }
        
        val text = _statusText.value
        println("ComposeViewModel: Status text = '$text'")
        println("ComposeViewModel: text.isBlank() = ${text.isBlank()}")
        if (text.isBlank()) {
            _uiState.value = ComposeUiState.Error("Status cannot be empty")
            return
        }
        
        println("ComposeViewModel: charLimit = $charLimit, text.length = ${text.length}")
        if (text.length > charLimit) {
            _uiState.value = ComposeUiState.Error("Status exceeds character limit")
            return
        }
        
        println("ComposeViewModel: Setting state to Publishing")
        _uiState.value = ComposeUiState.Publishing
        
        viewModelScope.launch {
            println("ComposeViewModel: Starting publish coroutine")
            if (editingStatus != null) {
                // Edit existing status
                publishEdit(session.getID())
            } else {
                // Create new status
                publishNew(session.getID())
            }
        }
    }
    
    private fun publishNew(accountId: String) {
        println("ComposeViewModel: publishNew() called")
        val request = CreateStatus.Request().apply {
            status = _statusText.value
            visibility = _visibility.value
            
            // Add spoiler text if enabled
            if (_hasSpoiler.value) {
                if (_spoilerText.value.isNotEmpty()) {
                    spoilerText = _spoilerText.value
                } else {
                    sensitive = true
                }
            }
            
            // Add reply-to if present
            replyToStatus?.let {
                inReplyToId = it.id
            }
            
            // Add media attachments
            if (_attachments.value.isNotEmpty()) {
                mediaIds = _attachments.value.mapNotNull { it.uploadedAttachment?.id }
            }
            
            // Add poll
            _poll.value?.let { draftPoll ->
                poll = CreateStatus.Request.Poll().apply {
                    options = ArrayList(draftPoll.options.filter { it.isNotBlank() })
                    expiresIn = draftPoll.durationSeconds
                    multiple = draftPoll.multipleChoice
                }
            }
        }
        
        val uuid = UUID.randomUUID().toString()
        
        println("ComposeViewModel: Executing CreateStatus API call")
        CreateStatus(request, uuid)
            .setCallback(object : Callback<Status> {
                override fun onSuccess(result: Status) {
                    println("ComposeViewModel: CreateStatus onSuccess")
                    viewModelScope.launch {
                        _uiState.value = ComposeUiState.Success
                        println("ComposeViewModel: State set to Success")
                        // Post event for timeline update
                        app.kabinka.social.E.post(
                            app.kabinka.social.events.StatusCreatedEvent(result, accountId)
                        )
                    }
                }
                
                override fun onError(error: ErrorResponse) {
                    println("ComposeViewModel: CreateStatus onError: $error")
                    viewModelScope.launch {
                        _uiState.value = ComposeUiState.Error(error.toString())
                    }
                }
            })
            .exec(accountId)
    }
    
    private fun publishEdit(accountId: String) {
        val request = CreateStatus.Request().apply {
            status = _statusText.value
            
            // Add spoiler text if enabled
            if (_hasSpoiler.value && _spoilerText.value.isNotEmpty()) {
                spoilerText = _spoilerText.value
            }
            
            // Add media attachments
            if (_attachments.value.isNotEmpty()) {
                mediaIds = _attachments.value.mapNotNull { it.uploadedAttachment?.id }
            }
            
            // Add poll
            _poll.value?.let { draftPoll ->
                poll = CreateStatus.Request.Poll().apply {
                    options = ArrayList(draftPoll.options.filter { it.isNotBlank() })
                    expiresIn = draftPoll.durationSeconds
                    multiple = draftPoll.multipleChoice
                }
            }
        }
        
        EditStatus(request, editingStatus!!.id)
            .setCallback(object : Callback<Status> {
                override fun onSuccess(result: Status) {
                    viewModelScope.launch {
                        _uiState.value = ComposeUiState.Success
                        // Post event for timeline update
                        app.kabinka.social.E.post(
                            app.kabinka.social.events.StatusUpdatedEvent(result)
                        )
                    }
                }
                
                override fun onError(error: ErrorResponse) {
                    viewModelScope.launch {
                        _uiState.value = ComposeUiState.Error(error.toString())
                    }
                }
            })
            .exec(accountId)
    }
}

data class DraftAttachment(
    val uri: Uri,
    val uploadedAttachment: Attachment? = null,
    val uploadProgress: Float = 0f
)

data class DraftPoll(
    val options: MutableList<String>,
    val durationSeconds: Int,
    val multipleChoice: Boolean
)

sealed class ComposeUiState {
    object Idle : ComposeUiState()
    object Publishing : ComposeUiState()
    object Success : ComposeUiState()
    data class Error(val message: String) : ComposeUiState()
}

