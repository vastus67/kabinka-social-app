package app.kabinka.frontend.onboarding.data

// NOTE: Phase 1 - Mastodon-only onboarding.
// Do not add future-proofing or abstractions for chat or media onboarding.
data class MastodonConnection(
    val status: ConnectionStatus = ConnectionStatus.DISCONNECTED,
    val instanceUrl: String = "https://mastodon.social",
    val username: String? = null,
    val displayName: String? = null,
    val avatarUrl: String? = null,
    val accountHandle: String? = null,
    val clientId: String? = null,
    val clientSecret: String? = null,
    val accessToken: String? = null
)
