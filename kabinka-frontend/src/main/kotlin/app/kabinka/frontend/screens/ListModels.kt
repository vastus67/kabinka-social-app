package app.kabinka.frontend.screens

data class UserList(
    val id: String,
    val name: String,
    val memberCount: Int
)

data class ListMember(
    val id: String,
    val displayName: String,
    val username: String,
    val avatarUrl: String?
)

enum class ShowRepliesToOption(val displayName: String) {
    MEMBERS("Members of the list"),
    FOLLOWING("Anyone I follow"),
    NO_ONE("No one")
}
