package app.kabinka.frontend.onboarding.navigation

// NOTE: Phase 1 - Mastodon-only onboarding.
// Do not add future-proofing or abstractions for chat or media onboarding.

sealed class OnboardingRoute(val route: String) {
    // Splash
    object Splash : OnboardingRoute("splash")
    
    // Mastodon connection flow
    object MastodonInstanceInput : OnboardingRoute("mastodon_instance_input")
    object MastodonOAuthLogin : OnboardingRoute("mastodon_oauth_login")
    object MastodonOAuthCallback : OnboardingRoute("mastodon_oauth_callback")
    
    // App Shell
    object AppShell : OnboardingRoute("app_shell")
}
