# Kabinka Social

A modern, alternative Mastodon client for Android built with Kotlin, Jetpack Compose, and Material 3.

## Features

### Mastodon Client
- **Timelines**: Home, Local, Federated, Lists, Bookmarks, Favorites
- **Status Interactions**: Post, Reply, Boost, Favorite, Bookmark
- **Content**: Polls, Media, Content Warnings, Link Previews
- **Search & Discovery**: Search posts, accounts, hashtags
- **Notifications**: Follow, Mention, Boost, Favorite
- **Profile Management**: View profiles, followers, following
- **Direct Messages**: Mastodon DM conversations

### FluffyChat Integration
- Decentralized Matrix-based chat
- End-to-end encrypted messaging
- Separate visual identity from Mastodon DMs
- Server selection and room management

### Design
- **Material 3** design system
- **Orange brand** personality (#F97316 primary)
- **Light + Dark mode** with charcoal/slate dark theme
- Clean, modern, outdoor-inspired aesthetic
- Rounded corners, generous spacing, subtle elevation
- Smooth transitions and animations

## Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose
- **Design**: Material 3
- **Navigation**: Compose Navigation
- **Architecture**: Single-activity
- **SDK**: Android SDK 34+

## Project Structure

```
app/src/main/kotlin/com/kabinka/social/
├── MainActivity.kt
├── KabinkaApp.kt
├── data/
│   └── models/           # Data models (User, Status, Notification, etc.)
├── navigation/
│   ├── Screen.kt         # Screen definitions
│   └── KabinkaNavigation.kt
├── ui/
│   ├── theme/            # Material 3 theme, colors, typography
│   ├── components/       # Reusable components (StatusCard, TopBar, etc.)
│   └── screens/
│       ├── onboarding/   # Splash, Welcome, Server Picker, Login
│       ├── home/         # Timelines (Home, Local, Federated, Lists, etc.)
│       ├── status/       # Status detail, thread view
│       ├── compose/      # Compose post, reply
│       ├── search/       # Search, results, explore
│       ├── notifications/
│       ├── profile/      # Profile, followers, following
│       ├── conversations/# Mastodon DMs
│       ├── fluffychat/   # FluffyChat integration
│       └── settings/     # App settings
```

## Modular Design

The app is designed for future extraction:
- **core-ui module**: Theme + reusable components
- **kabinka-frontend**: Screen implementations

## Building

1. Open the project in Android Studio
2. Sync Gradle
3. Run on an emulator or device (API 26+)

## Navigation

The app uses Compose Navigation with a bottom navigation bar:
- **Home**: Main timeline
- **Search**: Discover content
- **Compose**: Create posts (center FAB)
- **Notifications**: Activity feed
- **Profile**: User profile

## No Backend

This is a **frontend-only UI mockup**. All data uses simple mock generators. No real networking or backend integration.

## License

This is a UI mockup for demonstration purposes.
