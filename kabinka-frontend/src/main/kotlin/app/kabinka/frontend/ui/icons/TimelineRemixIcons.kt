package app.kabinka.frontend.ui.icons

import androidx.compose.ui.graphics.vector.ImageVector
import compose.icons.FeatherIcons
import compose.icons.feathericons.*

/**
 * Timeline navigation icons using Feather Icons
 * 
 * Feather is a beautiful open source icon set with clean, minimal designs.
 */
object TimelineRemixIcons {
    
    /**
     * Home timeline icon - Shows posts from followed accounts
     */
    fun home(selected: Boolean): ImageVector = FeatherIcons.Rss
    
    /**
     * Local timeline icon - Shows posts from current instance  
     */
    fun local(selected: Boolean): ImageVector = FeatherIcons.Users
    
    /**
     * Federated timeline icon - Shows posts from federated instances
     */
    fun federated(selected: Boolean): ImageVector = FeatherIcons.Globe
}
