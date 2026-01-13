package app.kabinka.frontend

import android.app.Application
import android.util.Log
import app.kabinka.social.MastodonApp
import app.kabinka.frontend.ui.theme.ThemePreferencesObserver

class KabinkaApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        Log.d(TAG, "KabinkaApplication.onCreate() - Initializing app")
        
        // Initialize MastodonApp which sets up V.setApplicationContext and other essentials
        MastodonApp.initializeWithContext(this)
        
        // Initialize theme preferences observer
        ThemePreferencesObserver.init(this)
        
        Log.d(TAG, "MastodonApp initialized successfully")
    }
    
    companion object {
        private const val TAG = "KabinkaApp"
    }
}
