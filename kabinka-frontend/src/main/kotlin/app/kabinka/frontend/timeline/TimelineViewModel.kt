package app.kabinka.frontend.timeline

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.kabinka.frontend.auth.SessionStateManager
import app.kabinka.social.api.requests.timelines.GetHomeTimeline
import app.kabinka.social.api.requests.timelines.GetPublicTimeline
import app.kabinka.social.model.Status
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
    private val sessionManager: SessionStateManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<TimelineUiState>(TimelineUiState.Loading)
    val uiState: StateFlow<TimelineUiState> = _uiState.asStateFlow()
    
    private var currentTimelineType = TimelineType.HOME
    
    init {
        Log.d(TAG, "TimelineViewModel initialized")
        loadTimeline(TimelineType.HOME)
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
     * Refresh the current timeline
     */
    fun refresh() {
        loadTimeline(currentTimelineType)
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
    FEDERATED
}

sealed class TimelineUiState {
    object Loading : TimelineUiState()
    data class Content(val statuses: List<Status>) : TimelineUiState()
    data class Error(val message: String) : TimelineUiState()
    data class Empty(val message: String, val isLoginRequired: Boolean) : TimelineUiState()
}

private const val TAG = "TimelineViewModel"
