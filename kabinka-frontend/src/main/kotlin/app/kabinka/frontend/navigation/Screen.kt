package app.kabinka.frontend.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Welcome : Screen("welcome")
    object ServerPicker : Screen("server_picker")
    object Login : Screen("login/{instance}") {
        fun createRoute(instance: String) = "login/$instance"
    }

    object Home : Screen("home")
    object LocalTimeline : Screen("local_timeline")
    object FederatedTimeline : Screen("federated_timeline")
    object Lists : Screen("lists")
    object ListTimeline : Screen("list_timeline/{listId}") {
        fun createRoute(listId: String) = "list_timeline/$listId"
    }
    object Bookmarks : Screen("bookmarks")
    object Favorites : Screen("favorites")
    object HashtagTimeline : Screen("hashtag/{tag}") {
        fun createRoute(tag: String) = "hashtag/$tag"
    }
    object Trending : Screen("trending")

    object StatusDetail : Screen("status/{statusId}") {
        fun createRoute(statusId: String) = "status/$statusId"
    }
    object Thread : Screen("thread/{statusId}") {
        fun createRoute(statusId: String) = "thread/$statusId"
    }

    object Compose : Screen("compose")
    object ComposeReply : Screen("compose_reply/{statusId}") {
        fun createRoute(statusId: String) = "compose_reply/$statusId"
    }

    object Search : Screen("search")
    object SearchResults : Screen("search_results/{query}") {
        fun createRoute(query: String) = "search_results/$query"
    }
    object Explore : Screen("explore")

    object Notifications : Screen("notifications")

    object Profile : Screen("profile")
    object ProfileDetail : Screen("profile/{userId}") {
        fun createRoute(userId: String) = "profile/$userId"
    }
    object Followers : Screen("followers/{userId}") {
        fun createRoute(userId: String) = "followers/$userId"
    }
    object Following : Screen("following/{userId}") {
        fun createRoute(userId: String) = "following/$userId"
    }

    object Conversations : Screen("conversations")
    object ConversationDetail : Screen("conversation/{conversationId}") {
        fun createRoute(conversationId: String) = "conversation/$conversationId"
    }
    object NewConversation : Screen("new_conversation")

    object FluffyChat : Screen("fluffychat")
    object FluffyChatServerSelection : Screen("fluffychat_server")
    object FluffyChatRoomList : Screen("fluffychat_rooms")
    object FluffyChatRoom : Screen("fluffychat_room/{roomId}") {
        fun createRoute(roomId: String) = "fluffychat_room/$roomId"
    }

    object Settings : Screen("settings")
    object AppearanceSettings : Screen("settings/appearance")
    object NotificationSettings : Screen("settings/notifications")
    object PrivacySettings : Screen("settings/privacy")
    object AccountManagement : Screen("settings/account")
    object About : Screen("about")

    object AccountSwitcher : Screen("account_switcher")
}
