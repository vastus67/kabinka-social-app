package app.kabinka.frontend.onboarding.navigation

// NOTE: Phase 1 - Mastodon-only onboarding.
// Do not add future-proofing or abstractions for chat or media onboarding.

sealed class OnboardingRoute(val route: String) {
    // Splash
    object Splash : OnboardingRoute("splash")
    
    // Mastodon connection flow - Complete registration
    object ServerSelection : OnboardingRoute("server_selection")
    object ServerRules : OnboardingRoute("server_rules")
    object PrivacyPolicy : OnboardingRoute("privacy_policy")
    object MastodonRegister : OnboardingRoute("mastodon_register")
    object EmailConfirmation : OnboardingRoute("email_confirmation")
    object FeedPersonalization : OnboardingRoute("feed_personalization")
    object ProfileSetup : OnboardingRoute("profile_setup")
    
    // Mastodon login flow (existing accounts)
    object MastodonInstanceInput : OnboardingRoute("mastodon_instance_input")
    object MastodonOAuthLogin : OnboardingRoute("mastodon_oauth_login")
    object MastodonOAuthCallback : OnboardingRoute("mastodon_oauth_callback")
    
    // Post-login bootstrap (session loading)
    object PostLoginBootstrap : OnboardingRoute("post_login_bootstrap")
    
    // App Shell
    object AppShell : OnboardingRoute("app_shell")
}
