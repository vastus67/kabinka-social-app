package app.kabinka.frontend

import android.app.Application
import android.util.Log
import app.kabinka.social.MastodonApp

class KabinkaApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        Log.d(TAG, "KabinkaApplication.onCreate() - Initializing app")
        
        // Initialize MastodonApp which sets up V.setApplicationContext and other essentials
        MastodonApp.initializeWithContext(this)
        
        Log.d(TAG, "MastodonApp initialized successfully")
    }
    
    companion object {
        private const val TAG = "KabinkaApp"
    }
}
