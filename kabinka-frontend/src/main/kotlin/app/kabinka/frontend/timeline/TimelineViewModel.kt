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

class TimelineViewModel(
    private val sessionManager: SessionStateManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<TimelineUiState>(TimelineUiState.Loading)
    val uiState: StateFlow<TimelineUiState> = _uiState.asStateFlow()
    
    init {
        Log.d(TAG, "TimelineViewModel initialized")
        loadTimeline()
    }
    
    fun loadTimeline() {
        viewModelScope.launch {
            _uiState.value = TimelineUiState.Loading
            
            val session = sessionManager.getCurrentSession()
            
            if (session == null) {
                // No session - load public federated timeline (browse without account)
                // This shows the public explore feed like mastodon.social/explore
                Log.d(TAG, "No active session - loading public federated timeline from mastodon.social")
                
                // Use execNoAuth for anonymous public timeline requests
                GetPublicTimeline(false, true, null, null, 40, null)
                    .setCallback(object : Callback<List<Status>> {
                        override fun onSuccess(result: List<Status>) {
                            Log.d(TAG, "Public timeline loaded successfully: ${result.size} items")
                            _uiState.value = TimelineUiState.Content(result)
                        }
                        
                        override fun onError(error: ErrorResponse?) {
                            Log.e(TAG, "Public timeline loading failed: ${error?.toString()}")
                            _uiState.value = TimelineUiState.Error(
                                error?.toString() ?: "Failed to load public timeline"
                            )
                        }
                    })
                    .execNoAuth("mastodon.social")  // Execute without authentication for public browsing
            } else {
                // Logged in - load home timeline
                Log.d(TAG, "Loading home timeline for account: ${session.getID()}")
                
                GetHomeTimeline(null, null, 20, null)
                    .setCallback(object : Callback<List<Status>> {
                        override fun onSuccess(result: List<Status>) {
                            Log.d(TAG, "Home timeline loaded successfully: ${result.size} items")
                            _uiState.value = TimelineUiState.Content(result)
                        }
                        
                        override fun onError(error: ErrorResponse?) {
                            Log.e(TAG, "Home timeline loading failed: ${error?.toString()}")
                            _uiState.value = TimelineUiState.Error(
                                error?.toString() ?: "Failed to load timeline"
                            )
                        }
                    })
                    .exec(session.getID())
            }
        }
    }
    
    fun refresh() {
        loadTimeline()
    }
}

sealed class TimelineUiState {
    object Loading : TimelineUiState()
    data class Content(val statuses: List<Status>) : TimelineUiState()
    data class Error(val message: String) : TimelineUiState()
}

private const val TAG = "TimelineViewModel"
