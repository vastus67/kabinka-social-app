package app.kabinka.frontend

import android.app.Application
import android.util.Log
import app.kabinka.social.MastodonApp

class KabinkaApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        Log.d(TAG, "KabinkaApplication.onCreate() - Initializing app")
        
        // ONLY set the context - nothing else
        // All other initialization will happen lazily when needed
        MastodonApp.context = this
        
        Log.d(TAG, "MastodonApp.context set successfully")
    }
    
    companion object {
        private const val TAG = "KabinkaApp"
    }
}
