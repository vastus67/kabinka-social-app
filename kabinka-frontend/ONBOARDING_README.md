# Kabinka Unified Onboarding Flow

## Overview
Complete unified onboarding implementation for Kabinka with orange theming, native OAuth for Mastodon, and native Matrix login.

## Features Implemented

### ✅ Core Requirements
- **Local Kabinka Identity**: displayName, optional avatar URI, optional localHandle
- **Persistent Storage**: DataStore for preferences + EncryptedSharedPreferences for tokens
- **Optional Service Connections**: Can skip during onboarding, connect later via Settings
- **User-Friendly Language**: "Social" and "Chat" instead of protocol jargon
- **Server Customization**: Default servers with ability to change
- **No Dead Ends**: Every screen has proper back navigation

### ✅ Technology Stack
- **UI**: Jetpack Compose + Material 3
- **Navigation**: Navigation Compose with deep links
- **OAuth**: AppAuth (NO WebViews, Custom Tabs only)
- **Networking**: OkHttp + Retrofit for Mastodon API
- **Storage**: DataStore + EncryptedSharedPreferences
- **Matrix**: Direct API calls using Retrofit
- **Images**: Coil for avatar loading

### ✅ Orange Kabinka Theme
- Primary: `#FF6B35` (Kabinka Orange)
- Light and Dark color schemes
- Material 3 compliant
- Located in `ui/theme/`

## Architecture

### Data Layer
```
data/
├── OnboardingMode.kt - SOCIAL_AND_CHAT, BROWSE_ONLY
├── ConnectionStatus.kt - DISCONNECTED, CONNECTING, CONNECTED
├── KabinkaProfile.kt - Local user identity
├── SocialConnection.kt - Mastodon connection state
├── ChatConnection.kt - Matrix connection state
├── OnboardingState.kt - Complete state model
└── OnboardingRepository.kt - DataStore + encrypted storage
```

### Authentication
```
auth/
├── MastodonOAuthHelper.kt - AppAuth PKCE flow, app registration
└── MatrixLoginHelper.kt - Native password login
```

### Navigation
```
navigation/
├── OnboardingRoute.kt - Sealed class with all routes
└── OnboardingNavGraph.kt - Complete navigation graph
```

### UI Screens (13 total)
```
ui/
├── SplashLoginScreen.kt
├── ModeSelectionScreen.kt
├── KabinkaIdentityScreen.kt
├── ConnectServicesScreen.kt
├── SocialServerPickerScreen.kt
├── SocialLoginStartScreen.kt
├── ChatServerPickerScreen.kt
├── ChatPasswordLoginScreen.kt
├── OnboardingCompleteScreen.kt
├── AppHomeScreen.kt
└── ConnectionsSettingsScreen.kt
```

## Navigation Flow

### 1. Initial Flow
```
SplashLoginScreen
├─> "Get started" → ModeSelectionScreen
└─> "Browse without account" → AppHomeScreen (Anonymous)

ModeSelectionScreen
├─> Select mode → KabinkaIdentityScreen
└─> Back → SplashLoginScreen

KabinkaIdentityScreen
├─> "Continue" → ConnectServicesScreen (with profile)
├─> "Skip profile" → ConnectServicesScreen (Anonymous)
└─> Back → ModeSelectionScreen
```

### 2. Service Connection Flow
```
ConnectServicesScreen
├─> Social: "Connect" → SocialServerPicker(LOGIN)
├─> Social: "Create account" → External browser (Custom Tab)
├─> Chat: "Connect" → ChatServerPicker(LOGIN)
├─> Chat: "Create account" → External browser (Custom Tab)
├─> "Finish" → OnboardingCompleteScreen
└─> Back → KabinkaIdentityScreen
```

### 3. Social (Mastodon) OAuth
```
SocialServerPicker → SocialLoginStart
  └─> OAuth via Custom Tab → Deep link callback
      └─> ConnectServicesScreen (connected)
```

### 4. Chat (Matrix) Login
```
ChatServerPicker → ChatPasswordLogin
  └─> Native login → ConnectServicesScreen (connected)
```

### 5. Completion
```
OnboardingCompleteScreen
├─> "Go to feed" → AppHomeScreen
├─> "Start chat" → AppHomeScreen
└─> "Manage connections" → ConnectionsSettingsScreen

AppHomeScreen
└─> "Connections banner" → ConnectionsSettingsScreen

ConnectionsSettingsScreen
├─> Connect/Disconnect services
└─> Back → AppHomeScreen
```

## Deep Links

### Mastodon OAuth Callback
```
kabinka://oauth/mastodon?code={authCode}
```

### Matrix SSO Callback (future)
```
kabinka://oauth/matrix?code={authCode}
```

Configured in `AndroidManifest.xml` with `android:launchMode="singleTask"` to ensure proper handling.

## Key Implementation Details

### 1. Mastodon OAuth (PKCE Flow)
- Dynamically registers app with Mastodon instance via `/api/v1/apps`
- Generates code_verifier and code_challenge
- Launches Custom Tab for authorization
- Receives callback via deep link
- Exchanges code for access token
- Stores client_id, client_secret, access_token securely

### 2. Matrix Login
- Direct API call to `/_matrix/client/r0/login`
- Password-based authentication
- Stores access_token and device_id securely
- SSO support can be added later

### 3. Secure Storage
- Preferences in DataStore (non-sensitive)
- Tokens in EncryptedSharedPreferences with AES256_GCM
- MasterKey with AES256_GCM scheme

### 4. State Management
- Single ViewModel for entire onboarding
- State persisted across process death
- Reactive state collection with Flow

## Testing the Flow

### Happy Path
1. Launch app → Splash screen
2. "Get started" → Mode selection
3. Choose "Social + Chat" → Identity setup
4. Enter name, optional avatar → Connect services
5. Connect Social → Pick server → OAuth in browser → Return connected
6. Connect Chat → Pick server → Enter credentials → Connected
7. Finish → Complete screen → App home

### Alternative Paths
- Browse without account: Skip all auth
- Skip profile: Use "Anonymous"
- Skip services: Complete onboarding without connections
- Connect later: Use ConnectionsSettingsScreen

## Files Created

### Dependencies
- `build.gradle.kts` - Added DataStore, AppAuth, Retrofit, Security

### Data Models (6 files)
- OnboardingMode, ConnectionStatus, KabinkaProfile
- SocialConnection, ChatConnection, OnboardingState
- OnboardingRepository

### Navigation (2 files)
- OnboardingRoute (sealed class)
- OnboardingNavGraph (NavHost)

### Authentication (2 files)
- MastodonOAuthHelper (AppAuth)
- MatrixLoginHelper (direct API)

### UI Theme (2 files)
- Color.kt (orange palette)
- Theme.kt (Material 3)

### Screens (11 files)
- All composable screens

### Integration (2 files)
- MainActivity.kt (updated)
- AndroidManifest.xml (deep links)

### ViewModel (1 file)
- OnboardingViewModel

## Next Steps

1. **Build the app**:
   ```powershell
   .\gradlew :kabinka-frontend:assembleFrontendDebug
   ```

2. **Install and test**:
   ```powershell
   adb install -r kabinka-frontend/build/outputs/apk/frontend/debug/kabinka-frontend-frontend-debug.apk
   ```

3. **Test OAuth flow**:
   - Ensure Custom Tabs work
   - Verify deep link handling
   - Check token storage

4. **Future enhancements**:
   - Add Matrix SSO support
   - Implement actual feed/chat screens
   - Add avatar cropping
   - Improve error handling
   - Add loading states

## Known Limitations

- Mastodon account creation opens external browser (by design)
- Matrix account creation opens external browser (by design)
- AppHomeScreen is placeholder (add actual functionality)
- No avatar cropping (uses raw URI)
- Error messages printed to console (add Snackbar)

## Architecture Benefits

- ✅ Expandable to new modules (Video, Music, Mesh)
- ✅ No protocol-specific jargon in UI
- ✅ Clean separation of concerns
- ✅ Secure token storage
- ✅ Deep link support for OAuth
- ✅ No WebViews (Custom Tabs only)
- ✅ Material 3 with orange branding
- ✅ Complete navigation with no dead ends
