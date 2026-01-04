package app.kabinka.social

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KabinkaSocialApi @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    fun isUserLoggedIn(): Boolean {
        // TODO: Check if user has logged-in Mastodon accounts
        // For now, return false to force showing the sign-in flow
        return false
    }
    
    fun getOnboardingIntent(): Intent {
        // Create intent to launch Mastodon onboarding/login
        val intent = Intent(context, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        return intent
    }
    
    fun getSignInIntent(): Intent {
        // Create intent to launch Mastodon sign-in
        val intent = Intent(context, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        return intent
    }
    
    fun getMainIntent(): Intent {
        // Create intent to launch main Mastodon activity
        val intent = Intent(context, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        return intent
    }
    
    @Composable
    fun MainSocialScreen(
        onLogout: () -> Unit = {},
        onComposeClick: () -> Unit = {},
        onElementClick: (String) -> Unit = {}
    ) {
        // Launch the actual Mastodon MainActivity instead of showing placeholder
        LaunchedEffect(Unit) {
            val intent = Intent(context, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
        
        // Show a brief loading message while launching
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Launching Mastodon...")
        }
    }
    
    fun signOut() {
        // TODO: Sign out from all Mastodon accounts
        // For now, do nothing
    }
}