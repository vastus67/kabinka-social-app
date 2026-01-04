package app.kabinka.social.fluffychat

import android.content.Context
import io.flutter.embedding.android.FlutterActivity

/**
 * Launcher for the FluffyChat Flutter module.
 * Provides methods to start FluffyChat from Kabinka.
 */
object FluffyChatLauncher {
    
    /**
     * Launch FluffyChat UI.
     * Opens FluffyChat in a new FlutterActivity with proper configuration.
     */
    fun launch(context: Context) {
        context.startActivity(
            FlutterActivity
                .withNewEngine()
                .initialRoute("/")
                .build(context)
        )
    }
    
    /**
     * Launch FluffyChat directly (alias for launch).
     * Use this to open the FluffyChat UI.
     */
    fun launchDirectly(context: Context) {
        launch(context)
    }
    
    /**
     * Check if the Flutter engine is ready.
     * Always returns true since we use on-demand engine creation.
     */
    fun isEngineReady(): Boolean {
        return true
    }
}
