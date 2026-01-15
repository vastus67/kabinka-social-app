package app.kabinka.frontend.timeline

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.kabinka.frontend.auth.SessionStateManager
import app.kabinka.social.E
import app.kabinka.social.api.requests.timelines.GetHomeTimeline
import app.kabinka.social.api.requests.timelines.GetPublicTimeline
import app.kabinka.social.api.requests.statuses.GetBookmarkedStatuses
import app.kabinka.social.api.requests.statuses.GetFavoritedStatuses
import app.kabinka.social.events.StatusCountersUpdatedEvent
import app.kabinka.social.model.Status
import com.squareup.otto.Subscribe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.grishka.appkit.api.Callback
import me.grishka.appkit.api.ErrorResponse

/**
 * ViewModel managing three distinct timeline types:
 * - HOME: Posts from followed accounts only
 * - LOCAL: Public posts from the current instance
 * - FEDERATED: Public posts from federated instances
 */
class TimelineViewModel(
    private val sessionManager: SessionStateManager,
    initialTimelineType: TimelineType = TimelineType.HOME
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<TimelineUiState>(TimelineUiState.Loading)
    val uiState: StateFlow<TimelineUiState> = _uiState.asStateFlow()
    
    private var currentTimelineType = initialTimelineType
    
    init {
        Log.d(TAG, "TimelineViewModel initialized with timeline type: $initialTimelineType")
        E.register(this)
        loadTimeline(initialTimelineType)
    }
    
    override fun onCleared() {
        super.onCleared()
        E.unregister(this)
    }
    
    /**
     * Handle status counter updates from the EventBus
     * EventBus posts on background thread, so we need to update on main thread
     */
    @Subscribe
    fun onStatusCountersUpdated(event: StatusCountersUpdatedEvent) {
        Log.d(TAG, "Status counters updated: ${event.id}, type: ${event.type}, " +
                "favorited=${event.favorited}, reblogged=${event.reblogged}, bookmarked=${event.bookmarked}")
        
        viewModelScope.launch {
            val currentState = _uiState.value as? TimelineUiState.Content ?: return@launch
            
            // Create a new list with updated status objects
            // This is necessary for Compose to detect the change
            val updatedStatuses = currentState.statuses.toMutableList()
            var foundAndUpdated = false
            
            for (i in updatedStatuses.indices) {
                val status = updatedStatuses[i]
                val statusToUpdate = status.reblog ?: status
                
                if (statusToUpdate.id == event.id) {
                    // Update the status with new counters
                    statusToUpdate.favouritesCount = event.favorites
                    statusToUpdate.favourited = event.favorited
                    statusToUpdate.reblogsCount = event.reblogs
                    statusToUpdate.reblogged = event.reblogged
                    statusToUpdate.repliesCount = event.replies
                    statusToUpdate.bookmarked = event.bookmarked
                    foundAndUpdated = true
                    Log.d(TAG, "Updated status in UI: favorited=${statusToUpdate.favourited}, " +
                            "reblogged=${statusToUpdate.reblogged}, bookmarked=${statusToUpdate.bookmarked}")
                    break
                }
            }
            
            if (foundAndUpdated) {
                // Create new TimelineUiState.Content with the updated list
                _uiState.value = TimelineUiState.Content(ArrayList(updatedStatuses))
            } else {
                Log.w(TAG, "Status ${event.id} not found in current timeline")
            }
        }
    }
    
    /**
     * Load timeline based on type
     * @param type The type of timeline to load (HOME, LOCAL, or FEDERATED)
     */
    fun loadTimeline(type: TimelineType) {
        currentTimelineType = type
        viewModelScope.launch {
            _uiState.value = TimelineUiState.Loading
            
            val session = sessionManager.getCurrentSession()
            
            when (type) {
                TimelineType.HOME -> loadHomeTimeline(session)
                TimelineType.LOCAL -> loadLocalTimeline(session)
                TimelineType.FEDERATED -> loadFederatedTimeline(session)
                TimelineType.BOOKMARKS -> loadBookmarksTimeline(session)
                TimelineType.FAVORITES -> loadFavoritesTimeline(session)
            }
        }
    }
    
    /**
     * Home Timeline: Posts from followed accounts only
     * Requires authentication
     */
    private fun loadHomeTimeline(session: app.kabinka.social.api.session.AccountSession?) {
        if (session == null) {
            Log.w(TAG, "Cannot load home timeline - no active session")
            _uiState.value = TimelineUiState.Empty(
                message = "Please log in to view your home timeline",
                isLoginRequired = true
            )
            return
        }
        
        Log.d(TAG, "Loading HOME timeline for account: ${session.getID()}")
        
        GetHomeTimeline(null, null, 40, null)
            .setCallback(object : Callback<List<Status>> {
                override fun onSuccess(result: List<Status>) {
                    Log.d(TAG, "Home timeline loaded: ${result.size} posts from followed accounts")
                    if (result.isEmpty()) {
                        _uiState.value = TimelineUiState.Empty(
                            message = "Your home timeline is quiet. Follow some accounts to see their posts here!",
                            isLoginRequired = false
                        )
                    } else {
                        _uiState.value = TimelineUiState.Content(result)
                    }
                }
                
                override fun onError(error: ErrorResponse?) {
                    Log.e(TAG, "Home timeline loading failed: ${error?.toString()}")
                    _uiState.value = TimelineUiState.Error(
                        error?.toString() ?: "Failed to load home timeline"
                    )
                }
            })
            .exec(session.getID())
    }
    
    /**
     * Local Timeline: Public posts from users on the current instance
     * Does not require following these accounts
     */
    private fun loadLocalTimeline(session: app.kabinka.social.api.session.AccountSession?) {
        val domain = session?.domain ?: "mastodon.social"
        Log.d(TAG, "Loading LOCAL timeline from instance: $domain")
        
        // local=true, remote=false: Only posts from this instance
        val request = GetPublicTimeline(
            true,  // local
            false, // remote
            null,  // maxID
            null,  // minID
            40,    // limit
            null   // sinceID
        )
        
        val callback = object : Callback<List<Status>> {
            override fun onSuccess(result: List<Status>) {
                Log.d(TAG, "Local timeline loaded: ${result.size} public posts from $domain")
                if (result.isEmpty()) {
                    _uiState.value = TimelineUiState.Empty(
                        message = "No public posts from $domain yet",
                        isLoginRequired = false
                    )
                } else {
                    _uiState.value = TimelineUiState.Content(result)
                }
            }
            
            override fun onError(error: ErrorResponse?) {
                Log.e(TAG, "Local timeline loading failed: ${error?.toString()}")
                _uiState.value = TimelineUiState.Error(
                    error?.toString() ?: "Failed to load local timeline"
                )
            }
        }
        
        if (session != null) {
            request.setCallback(callback).exec(session.getID())
        } else {
            request.setCallback(callback).execNoAuth(domain)
        }
    }
    
    /**
     * Federated Timeline: Public posts from remote federated instances
     * Shows posts discovered via follows, boosts, and federation
     */
    private fun loadFederatedTimeline(session: app.kabinka.social.api.session.AccountSession?) {
        val domain = session?.domain ?: "mastodon.social"
        Log.d(TAG, "Loading FEDERATED timeline from instance: $domain")
        
        // local=false, remote=true: Posts from federated instances
        val request = GetPublicTimeline(
            false, // local
            true,  // remote
            null,  // maxID
            null,  // minID
            40,    // limit
            null   // sinceID
        )
        
        val callback = object : Callback<List<Status>> {
            override fun onSuccess(result: List<Status>) {
                Log.d(TAG, "Federated timeline loaded: ${result.size} posts from federated instances")
                if (result.isEmpty()) {
                    _uiState.value = TimelineUiState.Empty(
                        message = "No federated posts available yet",
                        isLoginRequired = false
                    )
                } else {
                    _uiState.value = TimelineUiState.Content(result)
                }
            }
            
            override fun onError(error: ErrorResponse?) {
                Log.e(TAG, "Federated timeline loading failed: ${error?.toString()}")
                _uiState.value = TimelineUiState.Error(
                    error?.toString() ?: "Failed to load federated timeline"
                )
            }
        }
        
        if (session != null) {
            request.setCallback(callback).exec(session.getID())
        } else {
            request.setCallback(callback).execNoAuth(domain)
        }
    }
    
    /**
     * Bookmarks Timeline: User's bookmarked posts
     * Requires authentication
     */
    private fun loadBookmarksTimeline(session: app.kabinka.social.api.session.AccountSession?) {
        if (session == null) {
            Log.w(TAG, "Cannot load bookmarks - no active session")
            _uiState.value = TimelineUiState.Empty(
                message = "Please log in to view your bookmarks",
                isLoginRequired = true
            )
            return
        }
        
        Log.d(TAG, "Loading BOOKMARKS timeline for account: ${session.getID()}")
        
        GetBookmarkedStatuses(null, 40)
            .setCallback(object : Callback<app.kabinka.social.model.HeaderPaginationList<Status>> {
                override fun onSuccess(result: app.kabinka.social.model.HeaderPaginationList<Status>) {
                    Log.d(TAG, "Bookmarks timeline loaded: ${result.size} bookmarked posts")
                    if (result.isEmpty()) {
                        _uiState.value = TimelineUiState.Empty(
                            message = "No bookmarks yet. Bookmark posts to see them here!",
                            isLoginRequired = false
                        )
                    } else {
                        _uiState.value = TimelineUiState.Content(result)
                    }
                }
                
                override fun onError(error: ErrorResponse?) {
                    Log.e(TAG, "Bookmarks timeline loading failed: ${error?.toString()}")
                    _uiState.value = TimelineUiState.Error(
                        error?.toString() ?: "Failed to load bookmarks"
                    )
                }
            })
            .exec(session.getID())
    }
    
    /**
     * Favorites Timeline: User's favorited posts
     * Requires authentication
     */
    private fun loadFavoritesTimeline(session: app.kabinka.social.api.session.AccountSession?) {
        if (session == null) {
            Log.w(TAG, "Cannot load favorites - no active session")
            _uiState.value = TimelineUiState.Empty(
                message = "Please log in to view your favorites",
                isLoginRequired = true
            )
            return
        }
        
        Log.d(TAG, "Loading FAVORITES timeline for account: ${session.getID()}")
        
        GetFavoritedStatuses(null, 40)
            .setCallback(object : Callback<app.kabinka.social.model.HeaderPaginationList<Status>> {
                override fun onSuccess(result: app.kabinka.social.model.HeaderPaginationList<Status>) {
                    Log.d(TAG, "Favorites timeline loaded: ${result.size} favorited posts")
                    if (result.isEmpty()) {
                        _uiState.value = TimelineUiState.Empty(
                            message = "No favorites yet. Favorite posts to see them here!",
                            isLoginRequired = false
                        )
                    } else {
                        _uiState.value = TimelineUiState.Content(result)
                    }
                }
                
                override fun onError(error: ErrorResponse?) {
                    Log.e(TAG, "Favorites timeline loading failed: ${error?.toString()}")
                    _uiState.value = TimelineUiState.Error(
                        error?.toString() ?: "Failed to load favorites"
                    )
                }
            })
            .exec(session.getID())
    }
    
    /**
     * Refresh the current timeline
     */
    fun refresh() {
        loadTimeline(currentTimelineType)
    }
    
    /**
     * Toggle favorite (like) on a status
     */
    fun toggleFavorite(statusId: String) {
        val session = sessionManager.getCurrentSession() ?: return
        val currentState = _uiState.value as? TimelineUiState.Content ?: return
        
        // Find the status
        val status = currentState.statuses.find { 
            it.id == statusId || it.reblog?.id == statusId 
        } ?: return
        
        // Get the actual status (in case it's a reblog)
        val targetStatus = status.reblog ?: status
        
        Log.d(TAG, "Toggling favorite for status ${targetStatus.id}, current: ${targetStatus.favourited}")
        
        // Toggle favorite - the EventBus will update the UI
        session.getStatusInteractionController().setFavorited(targetStatus, !targetStatus.favourited)
    }
    
    /**
     * Toggle reblog (boost) on a status
     */
    fun toggleReblog(statusId: String) {
        val session = sessionManager.getCurrentSession() ?: return
        val currentState = _uiState.value as? TimelineUiState.Content ?: return
        
        // Find the status
        val status = currentState.statuses.find { 
            it.id == statusId || it.reblog?.id == statusId 
        } ?: return
        
        // Get the actual status (in case it's a reblog)
        val targetStatus = status.reblog ?: status
        
        Log.d(TAG, "Toggling reblog for status ${targetStatus.id}, current: ${targetStatus.reblogged}")
        
        // Toggle reblog - the EventBus will update the UI
        session.getStatusInteractionController().setReblogged(targetStatus, !targetStatus.reblogged)
    }
    
    /**
     * Toggle bookmark on a status
     */
    fun toggleBookmark(statusId: String) {
        val session = sessionManager.getCurrentSession() ?: return
        val currentState = _uiState.value as? TimelineUiState.Content ?: return
        
        // Find the status
        val status = currentState.statuses.find { 
            it.id == statusId || it.reblog?.id == statusId 
        } ?: return
        
        // Get the actual status (in case it's a reblog)
        val targetStatus = status.reblog ?: status
        
        Log.d(TAG, "Toggling bookmark for status ${targetStatus.id}, current: ${targetStatus.bookmarked}")
        
        // Toggle bookmark - the EventBus will update the UI
        session.getStatusInteractionController().setBookmarked(targetStatus, !targetStatus.bookmarked)
    }
}

/**
 * Timeline types with clear data source separation
 */
enum class TimelineType {
    /** Posts from accounts the user follows */
    HOME,
    /** Public posts from the current instance */
    LOCAL,
    /** Public posts from federated instances */
    FEDERATED,
    /** Bookmarked posts */
    BOOKMARKS,
    /** Favorited posts */
    FAVORITES
}

sealed class TimelineUiState {
    object Loading : TimelineUiState()
    data class Content(val statuses: List<Status>) : TimelineUiState()
    data class Error(val message: String) : TimelineUiState()
    data class Empty(val message: String, val isLoginRequired: Boolean) : TimelineUiState()
}

private const val TAG = "TimelineViewModel"
