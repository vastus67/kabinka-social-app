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
 * 
 * This class handles:
 * - Session state flow (LOADING -> AUTHENTICATED or ANONYMOUS)
 * - Auto-selection of active account when missing
 * - Prevention of anonymous fallback when accounts exist
 */
class SessionStateManager(private val context: Context) {
    private val _sessionState = MutableStateFlow<SessionState>(SessionState.Loading)
    val sessionState: StateFlow<SessionState> = _sessionState.asStateFlow()
    
    // Flag to force anonymous mode (browse without account)
    private var forceAnonymous = false
    
    init {
        Log.d(TAG, "SessionStateManager initialized with Loading state")
    }
    
    fun setAnonymousMode(anonymous: Boolean) {
        forceAnonymous = anonymous
        if (anonymous) {
            Log.d(TAG, "Anonymous mode enabled")
            _sessionState.value = SessionState.Anonymous
        } else {
            Log.d(TAG, "Anonymous mode disabled, checking session")
            checkSessionState()
        }
    }
    
    /**
     * Checks session state and auto-selects active account if needed.
     * 
     * Flow:
     * 1. If forceAnonymous, set Anonymous
     * 2. Load AccountSessionManager
     * 3. Get lastActiveAccount
     * 4. If null but accounts exist, auto-select first/most recent account
     * 5. Set state to Authenticated if account found, else Anonymous
     */
    fun checkSessionState() {
        if (forceAnonymous) {
            _sessionState.value = SessionState.Anonymous
            return
        }
        
        try {
            Log.d(TAG, "Checking session state...")
            val manager = AccountSessionManager.getInstance()
            var lastActiveAccount = manager.lastActiveAccount
            
            // Auto-select account if missing but accounts exist
            if (lastActiveAccount == null) {
                val allAccounts = manager.loggedInAccounts
                if (allAccounts.isNotEmpty()) {
                    Log.w(TAG, "Active account missing but ${allAccounts.size} accounts exist - auto-selecting")
                    
                    // Select the most recently used account (or first if no timestamp)
                    val selectedAccount = allAccounts.maxByOrNull { it.infoLastUpdated }
                        ?: allAccounts.first()
                    
                    Log.d(TAG, "Auto-selected account: ${selectedAccount.self.username}@${selectedAccount.domain}")
                    manager.setLastActiveAccountID(selectedAccount.id)
                    lastActiveAccount = selectedAccount
                }
            }
            
            _sessionState.value = if (lastActiveAccount != null) {
                Log.d(TAG, "Session state: Authenticated (account: ${lastActiveAccount.self.username}@${lastActiveAccount.domain})")
                SessionState.Authenticated(lastActiveAccount)
            } else {
                Log.d(TAG, "Session state: Anonymous (no accounts)")
                SessionState.Anonymous
            }
        } catch (e: NoClassDefFoundError) {
            Log.e(TAG, "AccountSessionManager failed to initialize (shortcuts error)", e)
            _sessionState.value = SessionState.Anonymous
        } catch (e: ExceptionInInitializerError) {
            Log.e(TAG, "AccountSessionManager initialization error", e)
            _sessionState.value = SessionState.Anonymous
        } catch (e: Exception) {
            Log.e(TAG, "Error checking session state", e)
            _sessionState.value = SessionState.Anonymous
        }
    }
    
    companion object {
        private const val TAG = "OAuth:SessionState"
    }
    
    fun getCurrentSession(): AccountSession? {
        // Return null if in anonymous mode, even if a session exists
        if (forceAnonymous) {
            return null
        }
        return AccountSessionManager.getInstance().lastActiveAccount
    }
}

/**
 * Represents the current session state.
 */
sealed class SessionState {
    /** Initial state - session is being loaded */
    object Loading : SessionState()
    
    /** No active session - user is anonymous */
    object Anonymous : SessionState()
    
    /** Active authenticated session */
    data class Authenticated(val session: AccountSession) : SessionState()
}
