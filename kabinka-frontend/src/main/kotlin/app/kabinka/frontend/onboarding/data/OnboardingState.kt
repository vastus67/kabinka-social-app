package app.kabinka.frontend.onboarding.data

// NOTE: Phase 1 - Mastodon-only onboarding.
// Do not add future-proofing or abstractions for chat or media onboarding.
data class OnboardingState(
    val mode: OnboardingMode = OnboardingMode.MASTODON,
    val mastodonConnection: MastodonConnection = MastodonConnection(),
    val onboardingCompleted: Boolean = false
)
