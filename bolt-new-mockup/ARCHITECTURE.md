# Kabinka Social - Architecture Overview

## Project Structure

This is a native Android application built with Kotlin, Jetpack Compose, and Material 3. The app is a comprehensive Mastodon client UI mockup with FluffyChat integration.

## Technology Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Design System**: Material 3
- **Navigation**: Compose Navigation
- **Architecture**: Single-activity, MVVM-ready
- **Minimum SDK**: 26 (Android 8.0)
- **Target SDK**: 34 (Android 14)

## Color Palette

### Light Mode
- Primary Orange: #F97316
- Secondary Amber: #FB923C
- Background: #FAFAFA
- Surface: #FFFFFF
- Text: #111827

### Dark Mode
- Background: #0B0F14 (charcoal, NOT brown/sepia)
- Surface: #111827
- Elevated Surface: #161F2E
- Text: #E5E7EB
- Outline: #263244

## Directory Structure

```
kabinka-social/
├── app/
│   ├── build.gradle.kts
│   ├── proguard-rules.pro
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── kotlin/com/kabinka/social/
│       │   ├── MainActivity.kt
│       │   ├── KabinkaApp.kt
│       │   ├── data/models/
│       │   │   ├── User.kt
│       │   │   ├── Status.kt
│       │   │   ├── Notification.kt
│       │   │   └── Conversation.kt
│       │   ├── navigation/
│       │   │   ├── Screen.kt
│       │   │   └── KabinkaNavigation.kt
│       │   └── ui/
│       │       ├── theme/
│       │       │   ├── Color.kt
│       │       │   ├── Type.kt
│       │       │   └── Theme.kt
│       │       ├── components/
│       │       │   ├── EmptyState.kt
│       │       │   ├── LoadingState.kt
│       │       │   ├── ErrorState.kt
│       │       │   ├── StatusCard.kt
│       │       │   ├── KabinkaTopBar.kt
│       │       │   ├── KabinkaBottomNav.kt
│       │       │   └── KabinkaDrawer.kt
│       │       └── screens/
│       │           ├── onboarding/
│       │           ├── home/
│       │           ├── status/
│       │           ├── compose/
│       │           ├── search/
│       │           ├── notifications/
│       │           ├── profile/
│       │           ├── conversations/
│       │           ├── fluffychat/
│       │           └── settings/
│       └── res/
│           └── values/
│               ├── strings.xml
│               └── themes.xml
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── README.md
├── ARCHITECTURE.md
└── .gitignore
```

## Navigation Flow

### Onboarding
1. Splash Screen
2. Welcome Screen
3. Server Picker
4. Login (OAuth-style)
5. → Home

### Main Navigation (Bottom Bar)
- **Home**: Timeline feeds
- **Search**: Discovery and search
- **Compose**: Create posts (center FAB)
- **Notifications**: Activity feed
- **Profile**: User profile and settings

### Side Drawer
- Account Switcher
- Lists
- Bookmarks
- Favorites
- FluffyChat
- Settings
- About

## Screen Inventory

### Onboarding & Auth (4 screens)
- SplashScreen
- WelcomeScreen
- ServerPickerScreen
- LoginScreen

### Timelines (9 screens)
- HomeScreen (Home/Local/Federated tabs)
- LocalTimelineScreen
- FederatedTimelineScreen
- ListsScreen
- ListTimelineScreen
- BookmarksScreen
- FavoritesScreen
- HashtagTimelineScreen
- TrendingScreen

### Status & Threads (2 screens)
- StatusDetailScreen
- ThreadScreen

### Compose (2 screens)
- ComposeScreen
- ComposeReplyScreen

### Search & Discovery (3 screens)
- SearchScreen
- SearchResultsScreen (Posts/People/Hashtags tabs)
- ExploreScreen

### Notifications (1 screen)
- NotificationsScreen

### Profile (4 screens)
- ProfileScreen
- FollowersScreen
- FollowingScreen
- AccountSwitcherScreen

### Conversations/DMs (3 screens)
- ConversationsScreen
- ConversationDetailScreen
- NewConversationScreen

### FluffyChat (4 screens)
- FluffyChatLandingScreen
- FluffyChatServerSelectionScreen
- FluffyChatRoomListScreen
- FluffyChatRoomScreen

### Settings (6 screens)
- SettingsScreen
- AppearanceSettingsScreen
- NotificationSettingsScreen
- PrivacySettingsScreen
- AccountManagementScreen
- AboutScreen

**Total: 42 screens**

## Key Components

### StatusCard
- User avatar, name, username
- Timestamp
- Content with CW support
- Poll rendering
- Action buttons (reply, boost, favorite, share)
- Media placeholders

### KabinkaTopBar
- Avatar with drawer toggle
- Title/instance indicator
- FluffyChat icon (global)
- Back navigation

### KabinkaBottomNav
- 5 items: Home, Search, Compose (FAB), Notifications, Profile
- Active state indicators
- Material 3 styling

### KabinkaDrawer
- Current account info
- Navigation items
- FluffyChat highlight
- Settings access

### State Components
- EmptyState: Icon, title, message, optional action
- LoadingState: Spinner with message
- ErrorState: Error icon, message, retry button

## Design Principles

1. **Orange as Accent**: Primary color for CTAs, not backgrounds
2. **Dark Mode**: Cool charcoal/slate, not warm brown/sepia
3. **Generous Spacing**: 8px base unit, 16-24dp padding
4. **Rounded Corners**: 12-16dp for cards
5. **Modern Typography**: Clear hierarchy, 150% line height for body
6. **Smooth Animations**: Transitions and micro-interactions
7. **Outdoor/Alternative Feel**: Clean, modern, slightly hipster aesthetic

## Data Models

All models are simple Kotlin data classes with mock generators:

- **User**: Profile information
- **Status**: Posts with content, media, polls
- **Notification**: Activity feed items
- **Conversation**: DM threads
- **DirectMessage**: Individual messages

## Future Modularity

The codebase is structured to enable easy extraction:

### core-ui Module
- `ui/theme/`
- `ui/components/`

### kabinka-frontend Module
- `ui/screens/`
- `navigation/`
- `data/models/`

## No Backend

This is a **frontend-only mockup**. All data uses:
- Mock functions (e.g., `mockUsers()`, `mockStatuses()`)
- No real API calls
- No database
- No authentication logic

## Build & Run

1. Open in Android Studio
2. Sync Gradle
3. Run on emulator/device (API 26+)
4. No configuration required

## Compilation Status

The project is a complete, navigable Android Studio Gradle project that should compile successfully. All screens are wired with navigation, and all components are properly structured.
