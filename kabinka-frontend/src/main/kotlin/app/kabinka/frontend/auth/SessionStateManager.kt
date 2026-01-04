package app.kabinka.frontend.auth

import android.content.Context
import android.util.Log
import app.kabinka.social.api.session.AccountSession
import app.kabinka.social.api.session.AccountSessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Session state provider that uses kabinka-social AccountSessionManager
 * to determine if user is logged in.
 */
class SessionStateManager(private val context: Context) {
    private val _sessionState = MutableStateFlow<SessionState>(SessionState.LoggedOut)
    val sessionState: StateFlow<SessionState> = _sessionState.asStateFlow()
    
    // Flag to force anonymous mode (browse without account)
    private var forceAnonymous = false
    
    init {
        Log.d(TAG, "SessionStateManager initialized with LoggedOut state")
    }
    
    fun setAnonymousMode(anonymous: Boolean) {
        forceAnonymous = anonymous
        if (anonymous) {
            _sessionState.value = SessionState.LoggedOut
        } else {
            checkSessionState()
        }
    }
    
    fun checkSessionState() {
        if (forceAnonymous) {
            _sessionState.value = SessionState.LoggedOut
            return
        }
        
        try {
            Log.d(TAG, "Checking session state...")
            val manager = AccountSessionManager.getInstance()
            val lastActiveAccount = manager.lastActiveAccount
            _sessionState.value = if (lastActiveAccount != null) {
                Log.d(TAG, "Session state: LoggedIn (account: ${lastActiveAccount.self.username}@${lastActiveAccount.domain})")
                SessionState.LoggedIn(lastActiveAccount)
            } else {
                Log.d(TAG, "Session state: LoggedOut (no active account)")
                SessionState.LoggedOut
            }
        } catch (e: NoClassDefFoundError) {
            Log.e(TAG, "AccountSessionManager failed to initialize (shortcuts error)", e)
            _sessionState.value = SessionState.LoggedOut
        } catch (e: ExceptionInInitializerError) {
            Log.e(TAG, "AccountSessionManager initialization error", e)
            _sessionState.value = SessionState.LoggedOut
        } catch (e: Exception) {
            Log.e(TAG, "Error checking session state", e)
            _sessionState.value = SessionState.LoggedOut
        }
    }
    
    companion object {
        private const val TAG = "SessionState"
    }
    
    fun getCurrentSession(): AccountSession? {
        // Return null if in anonymous mode, even if a session exists
        if (forceAnonymous) {
            return null
        }
        return AccountSessionManager.getInstance().lastActiveAccount
    }
}

sealed class SessionState {
    object LoggedOut : SessionState()
    data class LoggedIn(val session: AccountSession) : SessionState()
}
