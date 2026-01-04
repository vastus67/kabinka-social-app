package app.kabinka.frontend.onboarding.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// NOTE: Phase 1 - Mastodon-only onboarding.
// Do not add future-proofing or abstractions for chat or media onboarding.

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "onboarding_prefs")

class OnboardingRepository(private val context: Context) {
    
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    
    private val encryptedPrefs = EncryptedSharedPreferences.create(
        context,
        "kabinka_secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    
    companion object {
        private val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        private val ONBOARDING_MODE = stringPreferencesKey("onboarding_mode")
        
        // Mastodon connection keys
        private val MASTODON_INSTANCE_URL = stringPreferencesKey("mastodon_instance_url")
        private val MASTODON_USERNAME = stringPreferencesKey("mastodon_username")
        private val MASTODON_DISPLAY_NAME = stringPreferencesKey("mastodon_display_name")
        private val MASTODON_AVATAR_URL = stringPreferencesKey("mastodon_avatar_url")
        private val MASTODON_ACCOUNT_HANDLE = stringPreferencesKey("mastodon_account_handle")
        
        // Encrypted Mastodon keys
        private const val MASTODON_CLIENT_ID = "mastodon_client_id"
        private const val MASTODON_CLIENT_SECRET = "mastodon_client_secret"
        private const val MASTODON_ACCESS_TOKEN = "mastodon_access_token"
    }
    
    val onboardingState: Flow<OnboardingState> = context.dataStore.data.map { prefs ->
        OnboardingState(
            mode = prefs[ONBOARDING_MODE]?.let { 
                // Migration: handle old SOCIAL_AND_CHAT value
                when (it) {
                    "SOCIAL_AND_CHAT" -> OnboardingMode.MASTODON
                    else -> try {
                        OnboardingMode.valueOf(it)
                    } catch (e: IllegalArgumentException) {
                        OnboardingMode.MASTODON
                    }
                }
            } ?: OnboardingMode.MASTODON,
            mastodonConnection = MastodonConnection(
                status = if (getMastodonAccessToken() != null) ConnectionStatus.CONNECTED else ConnectionStatus.DISCONNECTED,
                instanceUrl = prefs[MASTODON_INSTANCE_URL] ?: "https://mastodon.social",
                username = prefs[MASTODON_USERNAME],
                displayName = prefs[MASTODON_DISPLAY_NAME],
                avatarUrl = prefs[MASTODON_AVATAR_URL],
                accountHandle = prefs[MASTODON_ACCOUNT_HANDLE],
                clientId = getMastodonClientId(),
                clientSecret = getMastodonClientSecret(),
                accessToken = getMastodonAccessToken()
            ),
            onboardingCompleted = prefs[ONBOARDING_COMPLETED] ?: false
        )
    }
    
    suspend fun saveOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { it[ONBOARDING_COMPLETED] = completed }
    }
    
    suspend fun saveMode(mode: OnboardingMode) {
        context.dataStore.edit { it[ONBOARDING_MODE] = mode.name }
    }
    
    suspend fun saveMastodonConnection(connection: MastodonConnection) {
        context.dataStore.edit { prefs ->
            prefs[MASTODON_INSTANCE_URL] = connection.instanceUrl
            connection.username?.let { prefs[MASTODON_USERNAME] = it }
            connection.displayName?.let { prefs[MASTODON_DISPLAY_NAME] = it }
            connection.avatarUrl?.let { prefs[MASTODON_AVATAR_URL] = it }
            connection.accountHandle?.let { prefs[MASTODON_ACCOUNT_HANDLE] = it }
        }
        connection.clientId?.let { saveMastodonClientId(it) }
        connection.clientSecret?.let { saveMastodonClientSecret(it) }
        connection.accessToken?.let { saveMastodonAccessToken(it) }
    }
    
    suspend fun clearMastodonConnection() {
        context.dataStore.edit { prefs ->
            prefs.remove(MASTODON_USERNAME)
            prefs.remove(MASTODON_DISPLAY_NAME)
            prefs.remove(MASTODON_AVATAR_URL)
            prefs.remove(MASTODON_ACCOUNT_HANDLE)
        }
        encryptedPrefs.edit()
            .remove(MASTODON_CLIENT_ID)
            .remove(MASTODON_CLIENT_SECRET)
            .remove(MASTODON_ACCESS_TOKEN)
            .apply()
    }
    
    suspend fun resetOnboarding() {
        context.dataStore.edit { prefs ->
            prefs.clear()
        }
        encryptedPrefs.edit().clear().apply()
    }
    
    // Encrypted storage helpers
    private fun getMastodonClientId(): String? = encryptedPrefs.getString(MASTODON_CLIENT_ID, null)
    private fun getMastodonClientSecret(): String? = encryptedPrefs.getString(MASTODON_CLIENT_SECRET, null)
    private fun getMastodonAccessToken(): String? = encryptedPrefs.getString(MASTODON_ACCESS_TOKEN, null)
    
    private fun saveMastodonClientId(value: String) = encryptedPrefs.edit().putString(MASTODON_CLIENT_ID, value).apply()
    private fun saveMastodonClientSecret(value: String) = encryptedPrefs.edit().putString(MASTODON_CLIENT_SECRET, value).apply()
    private fun saveMastodonAccessToken(value: String) = encryptedPrefs.edit().putString(MASTODON_ACCESS_TOKEN, value).apply()
}
