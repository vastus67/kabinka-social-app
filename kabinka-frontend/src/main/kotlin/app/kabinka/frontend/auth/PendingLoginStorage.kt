package app.kabinka.frontend.auth

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.net.URI

/**
 * Durable storage for pending OAuth login state.
 * 
 * This prevents race conditions and state loss during the OAuth callback flow.
 * The pending login is persisted to DataStore before opening the browser,
 * and retrieved when the OAuth callback is received.
 */
class PendingLoginStorage(private val context: Context) {
    
    companion object {
        private const val TAG = "PendingLogin"
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "pending_login")
        
        private val KEY_INSTANCE_BASE_URL = stringPreferencesKey("instance_base_url")
        private val KEY_OAUTH_STATE = stringPreferencesKey("oauth_state")
        private val KEY_CLIENT_ID = stringPreferencesKey("client_id")
        private val KEY_CLIENT_SECRET = stringPreferencesKey("client_secret")
        private val KEY_CREATED_AT = longPreferencesKey("created_at")
    }
    
    /**
     * Represents a pending OAuth login session.
     * 
     * @param instanceBaseUrl Canonical instance URL (https://domain.tld, no trailing slash)
     * @param oauthState OAuth state parameter for CSRF protection
     * @param clientId OAuth client ID
     * @param clientSecret OAuth client secret
     * @param createdAt Timestamp when this pending login was created
     */
    data class PendingLogin(
        val instanceBaseUrl: String,
        val oauthState: String,
        val clientId: String,
        val clientSecret: String,
        val createdAt: Long
    ) {
        /**
         * Validates if this pending login is still fresh (not expired).
         * Pending logins expire after 10 minutes to prevent stale state.
         */
        fun isValid(): Boolean {
            val age = System.currentTimeMillis() - createdAt
            val maxAge = 10 * 60 * 1000 // 10 minutes
            return age < maxAge
        }
    }
    
    /**
     * Normalizes instance URL to canonical form:
     * - Lowercase host
     * - HTTPS scheme
     * - No trailing slash
     * - No path, query, or fragment
     * 
     * Examples:
     * - "mastodon.social" -> "https://mastodon.social"
     * - "MASTODON.SOCIAL/" -> "https://mastodon.social"
     * - "http://example.com/path" -> "https://example.com"
     */
    fun normalizeInstanceUrl(url: String): String {
        var normalized = url.trim().lowercase()
        
        // Add scheme if missing
        if (!normalized.startsWith("http://") && !normalized.startsWith("https://")) {
            normalized = "https://$normalized"
        }
        
        // Force HTTPS
        if (normalized.startsWith("http://")) {
            normalized = "https://" + normalized.substring(7)
        }
        
        // Parse and extract just scheme + host
        return try {
            val uri = URI(normalized)
            val host = uri.host ?: throw IllegalArgumentException("Invalid URL: no host")
            "https://${host.lowercase()}"
        } catch (e: Exception) {
            Log.e(TAG, "Failed to normalize URL: $url", e)
            throw IllegalArgumentException("Invalid instance URL: $url", e)
        }
    }
    
    /**
     * Generates a random OAuth state parameter for CSRF protection.
     */
    fun generateOAuthState(): String {
        val random = java.security.SecureRandom()
        val bytes = ByteArray(32)
        random.nextBytes(bytes)
        return java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }
    
    /**
     * Saves a pending login to DataStore.
     * This should be called before opening the OAuth browser flow.
     * 
     * @param instanceUrl Raw instance URL (will be normalized)
     * @param clientId OAuth client ID
     * @param clientSecret OAuth client secret
     * @return The generated OAuth state parameter
     */
    suspend fun savePendingLogin(
        instanceUrl: String,
        clientId: String,
        clientSecret: String
    ): String {
        val normalizedUrl = normalizeInstanceUrl(instanceUrl)
        val oauthState = generateOAuthState()
        val createdAt = System.currentTimeMillis()
        
        Log.d(TAG, "Saving pending login: instance=$normalizedUrl, stateHash=${oauthState.take(8)}")
        
        context.dataStore.edit { prefs ->
            prefs[KEY_INSTANCE_BASE_URL] = normalizedUrl
            prefs[KEY_OAUTH_STATE] = oauthState
            prefs[KEY_CLIENT_ID] = clientId
            prefs[KEY_CLIENT_SECRET] = clientSecret
            prefs[KEY_CREATED_AT] = createdAt
        }
        
        Log.d(TAG, "Pending login saved successfully")
        return oauthState
    }
    
    /**
     * Loads the pending login from DataStore.
     * Returns null if no pending login exists.
     */
    suspend fun loadPendingLogin(): PendingLogin? {
        Log.d(TAG, "Loading pending login")
        
        val prefs = context.dataStore.data.first()
        val instanceUrl = prefs[KEY_INSTANCE_BASE_URL]
        val oauthState = prefs[KEY_OAUTH_STATE]
        val clientId = prefs[KEY_CLIENT_ID]
        val clientSecret = prefs[KEY_CLIENT_SECRET]
        val createdAt = prefs[KEY_CREATED_AT]
        
        if (instanceUrl == null || oauthState == null || clientId == null || 
            clientSecret == null || createdAt == null) {
            Log.d(TAG, "No pending login found")
            return null
        }
        
        val pendingLogin = PendingLogin(
            instanceBaseUrl = instanceUrl,
            oauthState = oauthState,
            clientId = clientId,
            clientSecret = clientSecret,
            createdAt = createdAt
        )
        
        Log.d(TAG, "Pending login loaded: instance=${instanceUrl}, valid=${pendingLogin.isValid()}")
        return pendingLogin
    }
    
    /**
     * Clears the pending login from DataStore.
     * This should be called after successful OAuth completion or on error.
     */
    suspend fun clearPendingLogin() {
        Log.d(TAG, "Clearing pending login")
        context.dataStore.edit { prefs ->
            prefs.remove(KEY_INSTANCE_BASE_URL)
            prefs.remove(KEY_OAUTH_STATE)
            prefs.remove(KEY_CLIENT_ID)
            prefs.remove(KEY_CLIENT_SECRET)
            prefs.remove(KEY_CREATED_AT)
        }
        Log.d(TAG, "Pending login cleared")
    }
}
