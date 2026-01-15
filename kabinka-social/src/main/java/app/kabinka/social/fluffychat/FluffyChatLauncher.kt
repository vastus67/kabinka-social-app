package app.kabinka.social.fluffychat

import android.content.Context
// import io.flutter.embedding.android.FlutterActivity

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
        // TODO: Re-enable when Flutter is properly integrated
        // context.startActivity(
        //     FlutterActivity
        //         .withNewEngine()
        //         .initialRoute("/")
        //         .build(context)
        // )
    }
    
    /**
     * Launch FluffyChat directly (alias for launch).
     * Use this to open the FluffyChat UI.
     */
    fun launchDirectly(context: Context) {
        launch(context)
    }
    
    /**
     * Check if FluffyChat is available.
     * Returns true if the Flutter module is properly initialized.
     */
    fun isAvailable(): Boolean {
        return false // TODO: Implement proper check
    }
}
