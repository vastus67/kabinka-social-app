package app.kabinka.frontend.auth

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Tests for PendingLoginStorage to ensure OAuth state persistence is reliable.
 */
@RunWith(AndroidJUnit4::class)
class PendingLoginStorageTest {
    
    private lateinit var context: Context
    private lateinit var storage: PendingLoginStorage
    
    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        storage = PendingLoginStorage(context)
    }
    
    @Test
    fun testNormalizeInstanceUrl_addHttps() {
        val normalized = storage.normalizeInstanceUrl("mastodon.social")
        assertEquals("https://mastodon.social", normalized)
    }
    
    @Test
    fun testNormalizeInstanceUrl_lowercase() {
        val normalized = storage.normalizeInstanceUrl("MASTODON.SOCIAL")
        assertEquals("https://mastodon.social", normalized)
    }
    
    @Test
    fun testNormalizeInstanceUrl_removeTrailingSlash() {
        val normalized = storage.normalizeInstanceUrl("https://mastodon.social/")
        assertEquals("https://mastodon.social", normalized)
    }
    
    @Test
    fun testNormalizeInstanceUrl_removePath() {
        val normalized = storage.normalizeInstanceUrl("https://mastodon.social/about")
        assertEquals("https://mastodon.social", normalized)
    }
    
    @Test
    fun testNormalizeInstanceUrl_forceHttps() {
        val normalized = storage.normalizeInstanceUrl("http://mastodon.social")
        assertEquals("https://mastodon.social", normalized)
    }
    
    @Test
    fun testSaveAndLoadPendingLogin() = runTest {
        // Clear any existing pending login
        storage.clearPendingLogin()
        
        // Save pending login
        val oauthState = storage.savePendingLogin(
            instanceUrl = "mastodon.social",
            clientId = "test_client_id",
            clientSecret = "test_client_secret"
        )
        
        assertNotNull(oauthState)
        assertTrue(oauthState.length > 16) // Should be a reasonable length
        
        // Load pending login
        val loaded = storage.loadPendingLogin()
        assertNotNull(loaded)
        assertEquals("https://mastodon.social", loaded!!.instanceBaseUrl)
        assertEquals("test_client_id", loaded.clientId)
        assertEquals("test_client_secret", loaded.clientSecret)
        assertEquals(oauthState, loaded.oauthState)
        assertTrue(loaded.isValid())
    }
    
    @Test
    fun testClearPendingLogin() = runTest {
        // Save pending login
        storage.savePendingLogin(
            instanceUrl = "mastodon.social",
            clientId = "test_client_id",
            clientSecret = "test_client_secret"
        )
        
        // Verify it exists
        var loaded = storage.loadPendingLogin()
        assertNotNull(loaded)
        
        // Clear it
        storage.clearPendingLogin()
        
        // Verify it's gone
        loaded = storage.loadPendingLogin()
        assertNull(loaded)
    }
    
    @Test
    fun testPendingLoginExpiry() = runTest {
        // Create a pending login with a createdAt in the past (11 minutes ago)
        val elevenMinutesAgo = System.currentTimeMillis() - (11 * 60 * 1000)
        val pendingLogin = PendingLoginStorage.PendingLogin(
            instanceBaseUrl = "https://mastodon.social",
            oauthState = "test_state",
            clientId = "test_client",
            clientSecret = "test_secret",
            createdAt = elevenMinutesAgo
        )
        
        assertFalse("Pending login should be expired after 11 minutes", pendingLogin.isValid())
        
        // Create a fresh pending login
        val freshPendingLogin = PendingLoginStorage.PendingLogin(
            instanceBaseUrl = "https://mastodon.social",
            oauthState = "test_state",
            clientId = "test_client",
            clientSecret = "test_secret",
            createdAt = System.currentTimeMillis()
        )
        
        assertTrue("Fresh pending login should be valid", freshPendingLogin.isValid())
    }
    
    @Test
    fun testGenerateOAuthState_unique() {
        val state1 = storage.generateOAuthState()
        val state2 = storage.generateOAuthState()
        
        assertNotNull(state1)
        assertNotNull(state2)
        assertNotEquals("OAuth states should be unique", state1, state2)
        assertTrue("OAuth state should be at least 32 chars", state1.length >= 32)
    }
    
    @Test(expected = IllegalArgumentException::class)
    fun testNormalizeInstanceUrl_invalid() {
        storage.normalizeInstanceUrl("not a valid url !!!!")
    }
}
