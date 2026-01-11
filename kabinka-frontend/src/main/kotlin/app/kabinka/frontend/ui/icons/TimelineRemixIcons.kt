package app.kabinka.frontend.ui.icons

import androidx.compose.ui.graphics.vector.ImageVector
import compose.icons.LineAwesomeIcons
import compose.icons.lineawesomeicons.HomeSolid
import compose.icons.lineawesomeicons.SearchSolid
import compose.icons.lineawesomeicons.EditSolid
import compose.icons.lineawesomeicons.BellSolid
import compose.icons.lineawesomeicons.UserSolid
import compose.icons.lineawesomeicons.ShareSolid
import compose.icons.lineawesomeicons.CogSolid
import compose.icons.lineawesomeicons.CommentSolid
import compose.icons.lineawesomeicons.EllipsisVSolid
import compose.icons.lineawesomeicons.GlobeSolid
import compose.icons.lineawesomeicons.HeartSolid
import compose.icons.lineawesomeicons.RetweetSolid
import compose.icons.lineawesomeicons.StarSolid
import compose.icons.lineawesomeicons.FileAltSolid
import compose.icons.lineawesomeicons.HashtagSolid
import compose.icons.lineawesomeicons.FileSolid
import compose.icons.lineawesomeicons.UsersSolid
import compose.icons.lineawesomeicons.RssSolid
import compose.icons.lineawesomeicons.AtSolid
import compose.icons.lineawesomeicons.UserPlusSolid
import compose.icons.lineawesomeicons.ChartBarSolid
import compose.icons.lineawesomeicons.BookmarkSolid
import compose.icons.lineawesomeicons.MapMarkerSolid
import compose.icons.lineawesomeicons.InfoCircleSolid
import compose.icons.lineawesomeicons.ExclamationTriangleSolid
import compose.icons.lineawesomeicons.SyncSolid
import compose.icons.lineawesomeicons.PhoneSolid
import compose.icons.lineawesomeicons.ReplySolid
import compose.icons.lineawesomeicons.PlaySolid
import compose.icons.lineawesomeicons.QrcodeSolid
import compose.icons.lineawesomeicons.TimesSolid


/**
 * Timeline navigation icons using Feather Icons
 * 
 * Feather is a beautiful open source icon set with clean, minimal designs.
 */
object TimelineRemixIcons {
    
    /**
     * Home timeline icon - Shows posts from followed accounts
     */
    fun home(selected: Boolean): ImageVector = LineAwesomeIcons.RssSolid
    
    /**
     * Local timeline icon - Shows posts from current instance  
     */
    fun local(selected: Boolean): ImageVector = LineAwesomeIcons.UsersSolid
    
    /**
     * Federated timeline icon - Shows posts from federated instances
     */
    fun federated(selected: Boolean): ImageVector = LineAwesomeIcons.GlobeSolid
}
