package app.kabinka.frontend.screens

import android.net.Uri
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import android.widget.Toast
import android.content.Intent
import androidx.lifecycle.viewmodel.compose.viewModel
import app.kabinka.frontend.auth.SessionStateManager
import app.kabinka.frontend.timeline.TimelineViewModel
import app.kabinka.social.api.requests.accounts.GetAccountStatuses
import app.kabinka.social.api.requests.accounts.GetOwnAccount
import app.kabinka.social.api.requests.accounts.UpdateAccountCredentials
import app.kabinka.social.api.session.AccountSessionManager
import app.kabinka.social.model.Account
import app.kabinka.social.model.AccountField
import app.kabinka.social.model.Status
import app.kabinka.social.model.Attachment
import coil.compose.AsyncImage
import compose.icons.LineAwesomeIcons
import compose.icons.lineawesomeicons.HomeSolid
import app.kabinka.frontend.components.ImageViewerDialog
import compose.icons.lineawesomeicons.SearchSolid
import compose.icons.lineawesomeicons.EditSolid
import compose.icons.lineawesomeicons.BellSolid
import compose.icons.lineawesomeicons.UserSolid
import compose.icons.lineawesomeicons.ShareSolid
import compose.icons.lineawesomeicons.CogSolid
import compose.icons.lineawesomeicons.CommentSolid
import compose.icons.lineawesomeicons.EllipsisVSolid
import compose.icons.lineawesomeicons.GlobeSolid
import compose.icons.lineawesomeicons.HeartSolid
import compose.icons.lineawesomeicons.RetweetSolid
import compose.icons.lineawesomeicons.StarSolid
import compose.icons.lineawesomeicons.FileAltSolid
import compose.icons.lineawesomeicons.HashtagSolid
import compose.icons.lineawesomeicons.FileSolid
import compose.icons.lineawesomeicons.UsersSolid
import compose.icons.lineawesomeicons.RssSolid
import compose.icons.lineawesomeicons.AtSolid
import compose.icons.lineawesomeicons.UserPlusSolid
import compose.icons.lineawesomeicons.ChartBarSolid
import compose.icons.lineawesomeicons.BookmarkSolid
import compose.icons.lineawesomeicons.MapMarkerSolid
import compose.icons.lineawesomeicons.InfoCircleSolid
import compose.icons.lineawesomeicons.ExclamationTriangleSolid
import compose.icons.lineawesomeicons.SyncSolid
import compose.icons.lineawesomeicons.PhoneSolid
import compose.icons.lineawesomeicons.ReplySolid
import compose.icons.lineawesomeicons.PlaySolid
import compose.icons.lineawesomeicons.QrcodeSolid
import compose.icons.lineawesomeicons.TimesSolid
import compose.icons.lineawesomeicons.CameraSolid

import me.grishka.appkit.api.Callback
import me.grishka.appkit.api.ErrorResponse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateToUser: (String) -> Unit = {},
    onNavigateToReply: (String) -> Unit = {},
    onNavigateToThread: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionStateManager(context) }
    val timelineViewModel: TimelineViewModel = viewModel { TimelineViewModel(sessionManager) }
    
    var account by remember { mutableStateOf<Account?>(null) }
    var statuses by remember { mutableStateOf<List<Status>>(emptyList()) }
    var pinnedStatuses by remember { mutableStateOf<List<Status>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var selectedTab by remember { mutableStateOf(0) }
    var selectedFilter by remember { mutableStateOf(0) }
    var showUpdateDialog by remember { mutableStateOf(false) }
    var showQrCodeDialog by remember { mutableStateOf(false) }
    var refreshTrigger by remember { mutableStateOf(0) }
    
    // Image viewer state
    var showImageViewer by remember { mutableStateOf(false) }
    var imageViewerAttachments by remember { mutableStateOf<List<Attachment>>(emptyList()) }
    var imageViewerInitialIndex by remember { mutableStateOf(0) }
    
    val tabs = listOf("Featured", "Timeline", "About")
    val filters = listOf("Posts", "Replies", "Media")
    
    // Check if user is logged in
    val isLoggedIn = remember {
        try {
            AccountSessionManager.getInstance().lastActiveAccount != null
        } catch (e: Exception) {
            false
        }
    }
    
    // Load own account
    LaunchedEffect(selectedFilter, refreshTrigger) {
        if (isLoggedIn) {
            isLoading = true
            val accountId = AccountSessionManager.getInstance().lastActiveAccountID
            if (accountId != null) {
                val accountRequest = GetOwnAccount()
                accountRequest.setCallback(object : Callback<Account> {
                    override fun onSuccess(result: Account) {
                        account = result
                        
                        // Load pinned posts for Featured tab
                        val pinnedRequest = GetAccountStatuses(result.id, null, null, 20, GetAccountStatuses.Filter.PINNED, null)
                        pinnedRequest.setCallback(object : Callback<List<Status>> {
                            override fun onSuccess(pinnedResult: List<Status>) {
                                pinnedStatuses = pinnedResult
                            }
                            override fun onError(errorResponse: ErrorResponse) {
                                // Pinned posts are optional, don't show error
                            }
                        }).exec(accountId)
                        
                        // Load account statuses based on filter
                        val statusFilter = when (selectedFilter) {
                            0 -> GetAccountStatuses.Filter.DEFAULT
                            1 -> GetAccountStatuses.Filter.INCLUDE_REPLIES
                            2 -> GetAccountStatuses.Filter.MEDIA
                            else -> GetAccountStatuses.Filter.DEFAULT
                        }
                        
                        val statusRequest = GetAccountStatuses(result.id, null, null, 20, statusFilter, null)
                        statusRequest.setCallback(object : Callback<List<Status>> {
                            override fun onSuccess(statusResult: List<Status>) {
                                statuses = statusResult
                                isLoading = false
                            }
                            override fun onError(errorResponse: ErrorResponse) {
                                error = "Failed to load posts"
                                isLoading = false
                            }
                        }).exec(accountId)
                    }
                    override fun onError(errorResponse: ErrorResponse) {
                        error = "Failed to load profile"
                        isLoading = false
                    }
                }).exec(accountId)
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = account?.displayName ?: "Profile",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                actions = {
                    IconButton(onClick = { /* TODO: Share */ }) {
                        Icon(
                            imageVector = LineAwesomeIcons.ShareSolid,
                            contentDescription = "Share",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = { /* TODO: Settings */ }) {
                        Icon(
                            imageVector = LineAwesomeIcons.CogSolid,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        }
    ) { padding ->


        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = error!!,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            account != null -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    // Profile Header
                    item {
                        ProfileHeader(
                            account = account!!, 
                            postsCount = statuses.size,
                            onEditProfile = { showUpdateDialog = true },
                            onRefresh = { refreshTrigger++ },
                            onQrCodeClick = { showQrCodeDialog = true }
                        )
                    }
                    
                    // Tabs
                    item {
                        TabRow(
                            selectedTabIndex = selectedTab,
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.primary
                        ) {
                            tabs.forEachIndexed { index, title ->
                                Tab(
                                    selected = selectedTab == index,
                                    onClick = { selectedTab = index },
                                    text = {
                                        Text(
                                            text = title,
                                            style = MaterialTheme.typography.labelLarge
                                        )
                                    }
                                )
                            }
                        }
                    }
                    
                    // Filter chips (only for Timeline tab)
                    if (selectedTab == 1) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                filters.forEachIndexed { index, title ->
                                    FilterChip(
                                        selected = selectedFilter == index,
                                        onClick = { selectedFilter = index },
                                        label = { Text(title) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                        )
                                    )
                                }
                            }
                        }
                    }
                    
                    // Content based on selected tab
                    when (selectedTab) {
                        0 -> {
                            // Featured - show pinned posts
                            if (pinnedStatuses.isEmpty() && !isLoading) {
                                item {
                                    EmptyContentCard(
                                        icon = LineAwesomeIcons.StarSolid,
                                        title = "No featured posts",
                                        message = "Pin your best posts to feature them here"
                                    )
                                }
                            } else {
                                items(pinnedStatuses) { status ->
                                    app.kabinka.frontend.components.timeline.StatusCardComplete(
                                        status = status,
                                        onStatusClick = { onNavigateToThread(status.id) },
                                        onProfileClick = onNavigateToUser,
                                        onReply = { statusId -> onNavigateToReply(statusId) },
                                        onBoost = { statusId -> timelineViewModel.toggleReblog(statusId) },
                                        onFavorite = { statusId -> timelineViewModel.toggleFavorite(statusId) },
                                        onBookmark = { statusId -> timelineViewModel.toggleBookmark(statusId) },
                                        onMore = { /* Handled internally in StatusCardComplete */ },
                                        onLinkClick = { url ->
                                            try {
                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                                context.startActivity(intent)
                                            } catch (e: Exception) {
                                                android.util.Log.e("Profile", "Failed to open URL: $url", e)
                                            }
                                        },
                                        onHashtagClick = { tag ->
                                            android.util.Log.d("Profile", "Hashtag clicked: $tag")
                                        },
                                        onMentionClick = { userId ->
                                            onNavigateToUser(userId)
                                        },
                                        isProfileView = true
                                    )
                                }
                            }
                        }
                        1 -> {
                            // Timeline
                            if (statuses.isEmpty() && !isLoading) {
                                item {
                                    EmptyContentCard(
                                        icon = LineAwesomeIcons.FileAltSolid,
                                        title = "No posts yet",
                                        message = "Start sharing your thoughts"
                                    )
                                }
                            } else {
                                items(statuses) { status ->
                                    app.kabinka.frontend.components.timeline.StatusCardComplete(
                                        status = status,
                                        onStatusClick = { onNavigateToThread(status.id) },
                                        onProfileClick = onNavigateToUser,
                                        onReply = { statusId -> onNavigateToReply(statusId) },
                                        onBoost = { statusId -> timelineViewModel.toggleReblog(statusId) },
                                        onFavorite = { statusId -> timelineViewModel.toggleFavorite(statusId) },
                                        onBookmark = { statusId -> timelineViewModel.toggleBookmark(statusId) },
                                        onMore = { /* Handled internally in StatusCardComplete */ },
                                        onLinkClick = { url ->
                                            try {
                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                                context.startActivity(intent)
                                            } catch (e: Exception) {
                                                android.util.Log.e("Profile", "Failed to open URL: $url", e)
                                            }
                                        },
                                        onHashtagClick = { tag ->
                                            android.util.Log.d("Profile", "Hashtag clicked: $tag")
                                        },
                                        onMentionClick = { userId ->
                                            onNavigateToUser(userId)
                                        },
                                        onMediaClick = { index ->
                                            val displayedStatus = status.reblog ?: status
                                            if (!displayedStatus.mediaAttachments.isNullOrEmpty()) {
                                                imageViewerAttachments = displayedStatus.mediaAttachments
                                                imageViewerInitialIndex = index
                                                showImageViewer = true
                                            }
                                        },
                                        isProfileView = true
                                    )
                                }
                            }
                        }
                        2 -> {
                            // About
                            item {
                                AboutSection(account!!)
                            }
                            
                            // Profile fields
                            if (!account?.fields.isNullOrEmpty()) {
                                items(account!!.fields!!) { field ->
                                    ProfileFieldCard(field)
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // Update Profile Dialog
        if (showUpdateDialog && account != null) {
            UpdateProfileDialog(
                account = account,
                onDismiss = { showUpdateDialog = false },
                onUpdate = { 
                    showUpdateDialog = false
                    refreshTrigger++
                }
            )
        }
        
        // QR Code Dialog
        val currentAccount = account
        if (showQrCodeDialog && currentAccount != null) {
            QrCodeDialog(
                account = currentAccount,
                onDismiss = { showQrCodeDialog = false }
            )
        }
        
        // Image viewer dialog
        if (showImageViewer && imageViewerAttachments.isNotEmpty()) {
            ImageViewerDialog(
                attachments = imageViewerAttachments,
                initialIndex = imageViewerInitialIndex,
                onDismiss = { showImageViewer = false }
            )
        }
    }
}

@Composable
private fun ProfileHeader(
    account: Account, 
    postsCount: Int,
    onEditProfile: () -> Unit = {},
    onRefresh: () -> Unit = {},
    onQrCodeClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Banner area with image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            ) {
                if (!account.header.isNullOrEmpty() && account.header != "missing.png") {
                    AsyncImage(
                        model = account.header,
                        contentDescription = "Profile banner",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                            )
                    )
                }
                
                // Camera icon for updating banner
                IconButton(
                    onClick = onEditProfile,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .size(32.dp)
                        .background(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = LineAwesomeIcons.CameraSolid,
                        contentDescription = "Update banner",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    // Avatar (overlapping banner)
                    Box(
                        modifier = Modifier.offset(y = (-40).dp)
                    ) {
                        if (account.avatar != null) {
                            AsyncImage(
                                model = account.avatar,
                                contentDescription = "Profile picture",
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surface),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = account.displayName.firstOrNull()?.toString() ?: "?",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    // Action buttons
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onQrCodeClick,
                            modifier = Modifier.size(40.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(
                                imageVector = LineAwesomeIcons.QrcodeSolid,
                                contentDescription = "QR Code",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        
                        Button(
                            onClick = onEditProfile,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = LineAwesomeIcons.EditSolid,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Edit", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
                
                // Name and username (moved up, no duplication)
                Column(
                    modifier = Modifier.offset(y = (-24).dp)
                ) {
                    Text(
                        text = account.displayName,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Text(
                        text = "@${account.acct}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Bio (if present)
                if (!account.note.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = account.note.replace(Regex("<[^>]*>"), ""),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Compact stats row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CompactStatItem(
                        count = postsCount.toLong(),
                        label = "Posts"
                    )
                    CompactStatItem(
                        count = account.followersCount,
                        label = "Followers"
                    )
                    CompactStatItem(
                        count = account.followingCount,
                        label = "Following"
                    )
                }
            }
        }
    }
}

// Components moved to separate files:
// - CompactStatItem -> ProfileComponents.kt
// - EmptyContentCard -> ProfileComponents.kt  
// - AboutSection -> ProfileAboutTab.kt
// - ProfileFieldCard -> ProfileAboutTab.kt
// - StatusCard -> ProfileTimelineTab.kt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UpdateProfileDialog(
    account: Account?,
    onDismiss: () -> Unit,
    onUpdate: () -> Unit
) {
    val context = LocalContext.current
    var selectedAvatarUri by remember { mutableStateOf<Uri?>(null) }
    var selectedBannerUri by remember { mutableStateOf<Uri?>(null) }
    var isUpdating by remember { mutableStateOf(false) }
    var displayName by remember { mutableStateOf(account?.displayName ?: "") }
    var bio by remember { mutableStateOf(account?.source?.note ?: account?.note?.replace(Regex("<[^>]*>"), "") ?: "") }
    var showDiscardDialog by remember { mutableStateOf(false) }
    
    // Profile fields - start with existing fields or empty list
    var profileFields by remember {
        mutableStateOf<List<AccountField>>(
            account?.source?.fields?.map { 
                AccountField().apply { 
                    name = it.name
                    value = it.value
                }
            } ?: emptyList()
        )
    }
    
    // Check if there are unsaved changes
    val hasChanges = remember(displayName, bio, selectedAvatarUri, selectedBannerUri, profileFields) {
        val sourceNote = account?.source?.note ?: account?.note?.replace(Regex("<[^>]*>"), "") ?: ""
        displayName != (account?.displayName ?: "") ||
        bio != sourceNote ||
        selectedAvatarUri != null ||
        selectedBannerUri != null ||
        !areFieldsEqual(profileFields, account?.source?.fields)
    }
    
    val avatarLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedAvatarUri = uri
    }
    
    val bannerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedBannerUri = uri
    }
    
    // Discard changes confirmation dialog
    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text("Discard changes?") },
            text = { Text("You have unsaved changes. Are you sure you want to discard them?") },
            confirmButton = {
                TextButton(onClick = {
                    showDiscardDialog = false
                    onDismiss()
                }) {
                    Text("Discard")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    AlertDialog(
        onDismissRequest = {
            if (hasChanges) {
                showDiscardDialog = true
            } else {
                onDismiss()
            }
        },
        title = {
            Text(
                text = "Edit Profile",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Banner preview and selector
                Text(
                    text = "Profile Banner",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { bannerLauncher.launch("image/*") }
                ) {
                    if (selectedBannerUri != null) {
                        AsyncImage(
                            model = selectedBannerUri,
                            contentDescription = "New banner",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else if (!account?.header.isNullOrEmpty() && account?.header != "missing.png") {
                        AsyncImage(
                            model = account?.header,
                            contentDescription = "Current banner",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = LineAwesomeIcons.CameraSolid,
                                    contentDescription = "Add banner",
                                    modifier = Modifier.size(32.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Tap to select banner",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    
                    // Camera icon overlay
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp)
                            .size(32.dp)
                            .background(
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = LineAwesomeIcons.CameraSolid,
                            contentDescription = "Change banner",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                
                // Avatar preview and selector
                Text(
                    text = "Profile Picture",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .clickable { avatarLauncher.launch("image/*") }
                ) {
                    if (selectedAvatarUri != null) {
                        AsyncImage(
                            model = selectedAvatarUri,
                            contentDescription = "New avatar",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else if (account?.avatar != null) {
                        AsyncImage(
                            model = account.avatar,
                            contentDescription = "Current avatar",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = account?.displayName?.firstOrNull()?.toString() ?: "?",
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                    
                    // Camera icon overlay
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(28.dp)
                            .background(
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = LineAwesomeIcons.CameraSolid,
                            contentDescription = "Change avatar",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                
                HorizontalDivider()
                
                // Display name field
                OutlinedTextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    label = { Text("Display Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                // Bio field
                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text("Bio") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    maxLines = 5
                )
                
                HorizontalDivider()
                
                // Profile fields section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Profile Fields",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (profileFields.size < 4) {
                        TextButton(
                            onClick = {
                                profileFields = profileFields + AccountField().apply {
                                    name = ""
                                    value = ""
                                }
                            }
                        ) {
                            Icon(
                                imageVector = LineAwesomeIcons.UserPlusSolid,
                                contentDescription = "Add field",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Field")
                        }
                    }
                }
                
                Text(
                    text = "Add up to 4 custom fields to your profile (e.g., website, location, pronouns)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                // Profile fields list
                for (index in profileFields.indices) {
                    val field = profileFields[index]
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Field ${index + 1}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                IconButton(
                                    onClick = {
                                        profileFields = profileFields.filterIndexed { i, _ -> i != index }
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = LineAwesomeIcons.TimesSolid,
                                        contentDescription = "Remove field",
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                            
                            OutlinedTextField(
                                value = field.name ?: "",
                                onValueChange = { newValue ->
                                    profileFields = profileFields.mapIndexed { i, f ->
                                        if (i == index) {
                                            AccountField().apply {
                                                name = newValue
                                                value = f.value
                                            }
                                        } else f
                                    }
                                },
                                label = { Text("Label (e.g., Website)") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            
                            OutlinedTextField(
                                value = field.value ?: "",
                                onValueChange = { newValue ->
                                    profileFields = profileFields.mapIndexed { i, f ->
                                        if (i == index) {
                                            AccountField().apply {
                                                name = f.name
                                                value = newValue
                                            }
                                        } else f
                                    }
                                },
                                label = { Text("Content (e.g., https://example.com)") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (account != null) {
                        isUpdating = true
                        val accountId = AccountSessionManager.getInstance().lastActiveAccountID
                        if (accountId != null) {
                            // Filter out empty fields
                            val validFields = profileFields.filter { 
                                !it.name.isNullOrBlank() || !it.value.isNullOrBlank() 
                            }
                            
                            val updateRequest = UpdateAccountCredentials(
                                displayName,
                                bio,
                                selectedAvatarUri,
                                selectedBannerUri,
                                validFields
                            )
                            updateRequest.setCallback(object : Callback<Account> {
                                override fun onSuccess(result: Account) {
                                    isUpdating = false
                                    AccountSessionManager.getInstance().updateAccountInfo(accountId, result)
                                    Toast.makeText(
                                        context,
                                        "Profile updated successfully",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    onUpdate()
                                }
                                override fun onError(errorResponse: ErrorResponse) {
                                    isUpdating = false
                                    errorResponse.showToast(context)
                                }
                            }).exec(accountId)
                        }
                    }
                },
                enabled = !isUpdating
            ) {
                if (isUpdating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Save")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    if (hasChanges) {
                        showDiscardDialog = true
                    } else {
                        onDismiss()
                    }
                },
                enabled = !isUpdating
            ) {
                Text("Cancel")
            }
        }
    )
}

// Helper function to compare profile fields
private fun areFieldsEqual(fields1: List<AccountField>?, fields2: List<app.kabinka.social.model.AccountField>?): Boolean {
    if (fields1 == null && fields2 == null) return true
    if (fields1 == null || fields2 == null) return false
    if (fields1.size != fields2.size) return false
    
    return fields1.zip(fields2).all { (f1, f2) ->
        f1.name == f2.name && f1.value == f2.value
    }
}

