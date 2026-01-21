package app.kabinka.frontend.onboarding

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.kabinka.frontend.auth.PendingLoginStorage
import app.kabinka.frontend.onboarding.auth.MastodonOAuthHelper
import app.kabinka.frontend.onboarding.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// NOTE: Phase 1 - Mastodon-only onboarding.
// Do not add future-proofing or abstractions for chat or media onboarding.

class OnboardingViewModel(
    private val repository: OnboardingRepository,
    private val context: Context
) : ViewModel() {
    
    private val _state = MutableStateFlow(OnboardingState())
    val state: StateFlow<OnboardingState> = _state.asStateFlow()
    
    private val mastodonOAuth = MastodonOAuthHelper(context)
    private val pendingLoginStorage = PendingLoginStorage(context)
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    init {
        viewModelScope.launch {
            repository.onboardingState.collect { savedState ->
                _state.value = savedState
            }
        }
    }
    
    fun setMode(mode: OnboardingMode) {
        _state.update { it.copy(mode = mode) }
        viewModelScope.launch {
            repository.saveMode(mode)
        }
    }
    
    fun completeOnboarding() {
        viewModelScope.launch {
            repository.saveOnboardingCompleted(true)
            _state.update { it.copy(onboardingCompleted = true) }
        }
    }
    
    fun browsWithoutAccount() {
        setMode(OnboardingMode.BROWSE_ONLY)
        completeOnboarding()
    }
    
    fun resetOnboarding() {
        viewModelScope.launch {
            repository.resetOnboarding()
            _state.update { OnboardingState() }
        }
    }
    
    // Mastodon connection methods
    fun setMastodonInstance(instanceUrl: String) {
        val normalized = try {
            pendingLoginStorage.normalizeInstanceUrl(instanceUrl)
        } catch (e: Exception) {
            android.util.Log.e("OnboardingViewModel", "Invalid instance URL", e)
            instanceUrl
        }
        _state.update { 
            it.copy(mastodonConnection = it.mastodonConnection.copy(instanceUrl = normalized))
        }
    }
    
    fun startMastodonOAuth(instanceUrl: String) {
        viewModelScope.launch {
            try {
                android.util.Log.d("OAuth:Start", "Starting Mastodon OAuth for $instanceUrl")
                
                _state.update { 
                    it.copy(mastodonConnection = it.mastodonConnection.copy(status = ConnectionStatus.CONNECTING))
                }
                
                // Register app with the Mastodon instance
                val app = mastodonOAuth.registerApp(instanceUrl)
                android.util.Log.d("OAuth:Start", "App registered, client_id exists: ${app.clientId.isNotEmpty()}")
                
                val connection = _state.value.mastodonConnection.copy(
                    instanceUrl = instanceUrl,
                    clientId = app.clientId,
                    clientSecret = app.clientSecret
                )
                _state.update { it.copy(mastodonConnection = connection) }
                repository.saveMastodonConnection(connection)
                
                // Persist pending login state before opening browser
                val oauthState = pendingLoginStorage.savePendingLogin(
                    instanceUrl = instanceUrl,
                    clientId = app.clientId,
                    clientSecret = app.clientSecret
                )
                android.util.Log.d("OAuth:Start", "Pending login saved, state=${oauthState.take(8)}")
                
                // Launch OAuth browser flow
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    mastodonOAuth.launchOAuthFlow(instanceUrl, app.clientId)
                }
                android.util.Log.d("OAuth:Start", "OAuth browser launched successfully")
            } catch (e: Exception) {
                android.util.Log.e("OAuth:Start", "Failed to start Mastodon OAuth", e)
                _errorMessage.value = "Failed to connect to Mastodon: ${e.message}"
                _state.update { 
                    it.copy(mastodonConnection = it.mastodonConnection.copy(status = ConnectionStatus.DISCONNECTED))
                }
            }
        }
    }
    
    fun handleMastodonOAuthCallback(code: String) {
        viewModelScope.launch {
            try {
                android.util.Log.d("OAuth:Callback", "Handling Mastodon OAuth callback")
                
                // Load pending login from storage
                val pendingLogin = pendingLoginStorage.loadPendingLogin()
                if (pendingLogin == null) {
                    android.util.Log.e("OAuth:Callback", "No pending login found")
                    _errorMessage.value = "OAuth session expired. Please try again."
                    _state.update { 
                        it.copy(mastodonConnection = it.mastodonConnection.copy(status = ConnectionStatus.DISCONNECTED))
                    }
                    pendingLoginStorage.clearPendingLogin()
                    return@launch
                }
                
                if (!pendingLogin.isValid()) {
                    android.util.Log.e("OAuth:Callback", "Pending login expired")
                    _errorMessage.value = "OAuth session expired. Please try again."
                    pendingLoginStorage.clearPendingLogin()
                    return@launch
                }
                
                android.util.Log.d("OAuth:Callback", "Pending login valid: instance=${pendingLogin.instanceBaseUrl}")
                
                val connection = _state.value.mastodonConnection
                
                // Exchange code for token
                android.util.Log.d("OAuth:Callback", "Exchanging code for token")
                val token = mastodonOAuth.exchangeCodeForToken(
                    serverUrl = pendingLogin.instanceBaseUrl,
                    clientId = pendingLogin.clientId,
                    clientSecret = pendingLogin.clientSecret,
                    code = code
                )
                android.util.Log.d("OAuth:Callback", "Token obtained successfully")
                
                // Get user account info (verifyCredentials equivalent)
                android.util.Log.d("OAuth:Callback", "Fetching user account")
                val userAccount = mastodonOAuth.getUserAccount(
                    serverUrl = pendingLogin.instanceBaseUrl,
                    accessToken = token.accessToken
                )
                android.util.Log.d("OAuth:Callback", "User account fetched: accountId=${userAccount.id}, username=${userAccount.username}")
                
                // Get instance info
                val instanceInfo = mastodonOAuth.getInstance(pendingLogin.instanceBaseUrl)
                android.util.Log.d("OAuth:Callback", "Instance info fetched: ${instanceInfo.title}")
                
                val updatedConnection = connection.copy(
                    instanceUrl = pendingLogin.instanceBaseUrl,
                    status = ConnectionStatus.CONNECTED,
                    username = userAccount.username,
                    displayName = userAccount.displayName,
                    avatarUrl = userAccount.avatar,
                    accountHandle = userAccount.acct,
                    accessToken = token.accessToken
                )
                _state.update { it.copy(mastodonConnection = updatedConnection) }
                repository.saveMastodonConnection(updatedConnection)
                
                // Create session in kabinka-social AccountSessionManager
                android.util.Log.d("OAuth:Callback", "Persisting session to database")
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    try {
                        val domain = pendingLogin.instanceBaseUrl.removePrefix("https://").removePrefix("http://")
                        
                        // Create Token object
                        val sessionToken = app.kabinka.social.model.Token().apply {
                            this.accessToken = token.accessToken
                            this.tokenType = token.tokenType
                            this.scope = token.scope
                            this.createdAt = token.createdAt
                        }
                        
                        // Create Account object
                        val account = app.kabinka.social.model.Account().apply {
                            this.id = userAccount.id
                            this.username = userAccount.username
                            this.acct = userAccount.acct
                            this.displayName = userAccount.displayName
                            this.avatar = userAccount.avatar
                            this.header = userAccount.header
                            this.locked = userAccount.locked ?: false
                            this.bot = userAccount.bot ?: false
                            this.url = "${pendingLogin.instanceBaseUrl}/@${userAccount.username}"
                        }
                        
                        // Create Application object
                        val application = app.kabinka.social.model.Application().apply {
                            this.name = "Kabinka"
                            this.clientId = pendingLogin.clientId
                            this.clientSecret = pendingLogin.clientSecret
                        }
                        
                        // Create Instance object
                        val instance = app.kabinka.social.model.InstanceV1().apply {
                            this.uri = domain
                            this.title = instanceInfo.title
                            this.description = instanceInfo.description ?: ""
                            this.version = instanceInfo.version ?: ""
                        }
                        
                        // Add account to AccountSessionManager (this persists to database)
                        app.kabinka.social.api.session.AccountSessionManager.getInstance().addAccount(
                            instance,
                            sessionToken,
                            account,
                            application,
                            null
                        )
                        
                        android.util.Log.d("OAuth:Callback", "Session persisted to database successfully")
                        
                        // Explicitly set active account
                        val accountId = userAccount.id + "@" + domain
                        app.kabinka.social.api.session.AccountSessionManager.getInstance().setLastActiveAccountID(accountId)
                        android.util.Log.d("OAuth:Callback", "Active account set: $accountId")
                        
                        // Set mode to MASTODON now that we have a successful login
                        _state.update { it.copy(mode = OnboardingMode.MASTODON) }
                        repository.saveMode(OnboardingMode.MASTODON)
                        
                        // Clear pending login
                        pendingLoginStorage.clearPendingLogin()
                        android.util.Log.d("OAuth:Callback", "Pending login cleared")
                        
                    } catch (e: Exception) {
                        android.util.Log.e("OAuth:Callback", "Failed to persist session", e)
                        throw e
                    }
                }
                
                mastodonOAuth.clearPKCE()
                android.util.Log.d("OAuth:Callback", "OAuth callback completed successfully")
                
            } catch (e: Exception) {
                android.util.Log.e("OAuth:Callback", "Failed to complete Mastodon login", e)
                _errorMessage.value = "Failed to complete login: ${e.message}"
                _state.update { 
                    it.copy(mastodonConnection = it.mastodonConnection.copy(status = ConnectionStatus.DISCONNECTED))
                }
                // Clear pending login on error
                pendingLoginStorage.clearPendingLogin()
            }
        }
    }
    
    fun disconnectMastodon() {
        viewModelScope.launch {
            repository.clearMastodonConnection()
            _state.update { 
                it.copy(mastodonConnection = MastodonConnection(status = ConnectionStatus.DISCONNECTED))
            }
        }
    }
    
    fun clearError() {
        _errorMessage.value = null
    }
}
