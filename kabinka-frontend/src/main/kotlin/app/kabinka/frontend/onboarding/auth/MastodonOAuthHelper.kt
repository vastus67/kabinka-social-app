package app.kabinka.frontend.onboarding.auth

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

data class MastodonApp(
    @SerializedName("client_id") val clientId: String,
    @SerializedName("client_secret") val clientSecret: String,
    @SerializedName("id") val id: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("redirect_uri") val redirectUri: String?,
    @SerializedName("vapid_key") val vapidKey: String?
)

data class MastodonAppRequest(
    @SerializedName("client_name") val clientName: String,
    @SerializedName("redirect_uris") val redirectUris: String,
    @SerializedName("scopes") val scopes: String,
    @SerializedName("website") val website: String?
)

data class MastodonToken(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("token_type") val tokenType: String,
    @SerializedName("scope") val scope: String,
    @SerializedName("created_at") val createdAt: Long
)

data class MastodonAccount(
    @SerializedName("id") val id: String,
    @SerializedName("username") val username: String,
    @SerializedName("acct") val acct: String,
    @SerializedName("display_name") val displayName: String,
    @SerializedName("avatar") val avatar: String?,
    @SerializedName("header") val header: String?,
    @SerializedName("locked") val locked: Boolean?,
    @SerializedName("bot") val bot: Boolean?
)

data class MastodonInstance(
    @SerializedName("uri") val uri: String,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String?,
    @SerializedName("version") val version: String?
)

interface MastodonApi {
    @POST("api/v1/apps")
    suspend fun registerApp(@Body request: MastodonAppRequest): MastodonApp
    
    @FormUrlEncoded
    @POST("oauth/token")
    suspend fun getToken(
        @Field("client_id") clientId: String,
        @Field("client_secret") clientSecret: String,
        @Field("redirect_uri") redirectUri: String,
        @Field("grant_type") grantType: String,
        @Field("code") code: String,
        @Field("code_verifier") codeVerifier: String
    ): MastodonToken
    
    @retrofit2.http.GET("api/v1/accounts/verify_credentials")
    suspend fun verifyCredentials(
        @retrofit2.http.Header("Authorization") authorization: String
    ): MastodonAccount
    
    @retrofit2.http.GET("api/v1/instance")
    suspend fun getInstance(): MastodonInstance
}

class MastodonOAuthHelper(private val context: Context) {
    
    companion object {
        const val REDIRECT_URI = "kabinka://oauth/mastodon"
        const val SCOPES = "read write follow push"
        private const val CLIENT_NAME = "Kabinka"
        private const val WEBSITE = "https://kabinka.app"
        private const val PREFS_NAME = "mastodon_oauth"
        private const val KEY_CODE_VERIFIER = "code_verifier"
    }
    
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    private var codeVerifier: String?
        get() {
            val value = prefs.getString(KEY_CODE_VERIFIER, null)
            android.util.Log.d("MastodonOAuth", "Getting code verifier: ${value?.take(10)}...")
            return value
        }
        set(value) {
            android.util.Log.d("MastodonOAuth", "Saving code verifier: ${value?.take(10)}...")
            prefs.edit().putString(KEY_CODE_VERIFIER, value).commit() // Use commit() for synchronous save
        }
    
    private var codeChallenge: String? = null
    
    private fun createRetrofit(baseUrl: String): Retrofit {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
        
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    private fun generateCodeVerifier(): String {
        val bytes = ByteArray(32)
        SecureRandom().nextBytes(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }
    
    private fun generateCodeChallenge(verifier: String): String {
        val bytes = verifier.toByteArray(Charsets.US_ASCII)
        val digest = MessageDigest.getInstance("SHA-256").digest(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(digest)
    }
    
    suspend fun registerApp(serverUrl: String): MastodonApp = withContext(Dispatchers.IO) {
        val api = createRetrofit(serverUrl).create(MastodonApi::class.java)
        api.registerApp(
            MastodonAppRequest(
                clientName = CLIENT_NAME,
                redirectUris = REDIRECT_URI,
                scopes = SCOPES,
                website = WEBSITE
            )
        )
    }
    
    fun launchOAuthFlow(
        serverUrl: String,
        clientId: String
    ) {
        try {
            // Generate PKCE parameters
            codeVerifier = generateCodeVerifier()
            codeChallenge = generateCodeChallenge(codeVerifier!!)
            
            // Build the authorization URL - works with any Mastodon instance
            val authorizeUrl = Uri.parse("$serverUrl/oauth/authorize").buildUpon()
                .appendQueryParameter("client_id", clientId)
                .appendQueryParameter("redirect_uri", REDIRECT_URI)
                .appendQueryParameter("response_type", "code")
                .appendQueryParameter("scope", SCOPES)
                .appendQueryParameter("code_challenge", codeChallenge)
                .appendQueryParameter("code_challenge_method", "S256")
                .build()
            
            android.util.Log.d("MastodonOAuth", "Launching OAuth flow to: $authorizeUrl")
            
            // Try Chrome Custom Tabs first
            try {
                val customTabsIntent = CustomTabsIntent.Builder()
                    .setShowTitle(true)
                    .build()
                customTabsIntent.launchUrl(context, authorizeUrl)
            } catch (e: Exception) {
                android.util.Log.w("MastodonOAuth", "Custom Tabs failed, falling back to browser", e)
                // Fallback to regular browser if Custom Tabs fail
                val intent = Intent(Intent.ACTION_VIEW, authorizeUrl).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            }
        } catch (e: Exception) {
            android.util.Log.e("MastodonOAuth", "Failed to launch OAuth flow", e)
            throw e
        }
    }
    
    suspend fun exchangeCodeForToken(
        serverUrl: String,
        clientId: String,
        clientSecret: String,
        code: String
    ): MastodonToken = withContext(Dispatchers.IO) {
        val verifier = codeVerifier
        android.util.Log.d("MastodonOAuth", "Exchanging code for token, verifier exists: ${verifier != null}")
        
        if (verifier == null) {
            throw IllegalStateException("Code verifier not set")
        }
        
        val api = createRetrofit(serverUrl).create(MastodonApi::class.java)
        try {
            val token = api.getToken(
                clientId = clientId,
                clientSecret = clientSecret,
                redirectUri = REDIRECT_URI,
                grantType = "authorization_code",
                code = code,
                codeVerifier = verifier
            )
            android.util.Log.d("MastodonOAuth", "Token exchange successful")
            token
        } catch (e: Exception) {
            android.util.Log.e("MastodonOAuth", "Token exchange failed", e)
            throw e
        }
    }
    
    suspend fun getUserAccount(
        serverUrl: String,
        accessToken: String
    ): MastodonAccount = withContext(Dispatchers.IO) {
        val api = createRetrofit(serverUrl).create(MastodonApi::class.java)
        api.verifyCredentials("Bearer $accessToken")
    }
    
    suspend fun getInstance(
        serverUrl: String
    ): MastodonInstance = withContext(Dispatchers.IO) {
        val api = createRetrofit(serverUrl).create(MastodonApi::class.java)
        api.getInstance()
    }
    
    fun clearPKCE() {
        android.util.Log.d("MastodonOAuth", "Clearing PKCE")
        codeVerifier = null
        codeChallenge = null
    }
}
