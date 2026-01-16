package app.kabinka.frontend.onboarding

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
        _state.update { 
            it.copy(mastodonConnection = it.mastodonConnection.copy(instanceUrl = instanceUrl))
        }
    }
    
    fun startMastodonOAuth(instanceUrl: String) {
        viewModelScope.launch {
            try {
                android.util.Log.d("OnboardingViewModel", "Starting Mastodon OAuth for $instanceUrl")
                
                _state.update { 
                    it.copy(mastodonConnection = it.mastodonConnection.copy(status = ConnectionStatus.CONNECTING))
                }
                
                // Register app with the Mastodon instance
                val app = mastodonOAuth.registerApp(instanceUrl)
                android.util.Log.d("OnboardingViewModel", "App registered, client_id: ${app.clientId}")
                
                val connection = _state.value.mastodonConnection.copy(
                    instanceUrl = instanceUrl,
                    clientId = app.clientId,
                    clientSecret = app.clientSecret
                )
                _state.update { it.copy(mastodonConnection = connection) }
                repository.saveMastodonConnection(connection)
                
                // Launch OAuth browser flow
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    mastodonOAuth.launchOAuthFlow(instanceUrl, app.clientId)
                }
                android.util.Log.d("OnboardingViewModel", "OAuth browser launched successfully")
            } catch (e: Exception) {
                android.util.Log.e("OnboardingViewModel", "Failed to start Mastodon OAuth", e)
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
                android.util.Log.d("OnboardingViewModel", "Handling Mastodon OAuth callback")
                val connection = _state.value.mastodonConnection
                
                // Exchange code for token
                val token = mastodonOAuth.exchangeCodeForToken(
                    serverUrl = connection.instanceUrl,
                    clientId = connection.clientId ?: throw IllegalStateException("Missing client ID"),
                    clientSecret = connection.clientSecret ?: throw IllegalStateException("Missing client secret"),
                    code = code
                )
                android.util.Log.d("OnboardingViewModel", "Token obtained")
                
                // Get user account info
                val userAccount = mastodonOAuth.getUserAccount(
                    serverUrl = connection.instanceUrl,
                    accessToken = token.accessToken
                )
                android.util.Log.d("OnboardingViewModel", "User account: ${userAccount.username}")
                
                // Get instance info
                val instanceInfo = mastodonOAuth.getInstance(connection.instanceUrl)
                android.util.Log.d("OnboardingViewModel", "Instance: ${instanceInfo.title}")
                
                val updatedConnection = connection.copy(
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
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    try {
                        val domain = connection.instanceUrl.removePrefix("https://").removePrefix("http://")
                        
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
                            this.url = "${connection.instanceUrl}/@${userAccount.username}"
                        }
                        
                        // Create Application object
                        val application = app.kabinka.social.model.Application().apply {
                            this.name = "Kabinka"
                        }
                        
                        // Create Instance object
                        val instance = app.kabinka.social.model.InstanceV1().apply {
                            this.uri = domain
                            this.title = instanceInfo.title
                            this.description = instanceInfo.description ?: ""
                            this.version = instanceInfo.version ?: ""
                        }
                        
                        // Add account to AccountSessionManager
                        app.kabinka.social.api.session.AccountSessionManager.getInstance().addAccount(
                            instance,
                            sessionToken,
                            account,
                            application,
                            null
                        )
                        
                        android.util.Log.d("OnboardingViewModel", "Session created in AccountSessionManager")
                        
                        // Set mode to MASTODON now that we have a successful login
                        _state.update { it.copy(mode = OnboardingMode.MASTODON) }
                        repository.saveMode(OnboardingMode.MASTODON)
                    } catch (e: Exception) {
                        android.util.Log.e("OnboardingViewModel", "Failed to create session", e)
                    }
                }
                
                mastodonOAuth.clearPKCE()
            } catch (e: Exception) {
                android.util.Log.e("OnboardingViewModel", "Failed to complete Mastodon login", e)
                _errorMessage.value = "Failed to complete login: ${e.message}"
                _state.update { 
                    it.copy(mastodonConnection = it.mastodonConnection.copy(status = ConnectionStatus.DISCONNECTED))
                }
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
