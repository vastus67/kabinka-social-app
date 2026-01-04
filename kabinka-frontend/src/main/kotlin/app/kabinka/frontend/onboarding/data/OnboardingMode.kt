package app.kabinka.frontend.onboarding.data

// NOTE: Phase 1 - Mastodon-only onboarding.
// Do not add future-proofing or abstractions for chat or media onboarding.
enum class OnboardingMode {
    MASTODON,
    BROWSE_ONLY
}
