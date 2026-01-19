# Magazine Module

A standalone, opt-in advertising module for Kabinka Social - designed as a curated digital magazine.

## Quick Links

- 📚 [Implementation Details](../MAGAZINE_IMPLEMENTATION.md)
- 📖 [User Guide](../MAGAZINE_USER_GUIDE.md)

## Module Structure

```
magazine/
├── MagazineModels.kt           - Data classes (Issue, Ad, Submission, etc.)
├── MagazineRepository.kt       - Data repository with mock data
├── MagazineViewModel.kt        - State management
├── MagazineGestures.kt         - Gesture detection & animations
├── MagazineShelfScreen.kt      - Main shelf view (browse issues)
├── MagazineIssueScreen.kt      - Issue view (ad cards)
├── AdvertiserDetailScreen.kt   - Full ad details
└── SubmissionFormScreen.kt     - Ad submission form
```

## Key Features

### 🎯 Core Principles
- **No timeline injection** - Ads only in Magazine
- **Finite content** - No infinite scroll
- **Editorial curation** - 10-30 ads per issue
- **Clear labeling** - All ads marked "Sponsored"
- **Gesture-driven** - Tactile, paper-like interactions

### 📱 Screens

#### 1. Magazine Shelf
Browse current and past issues. Entry point to Magazine feature.

#### 2. Issue View
Swipeable ad cards with three gestures:
- **Double-tap** → Circle (mark interesting)
- **Swipe horizontal** → Crumple (dismiss)
- **Swipe down** → Tear off (view details)

#### 3. Advertiser Detail
Full ad information with CTA to external link.

#### 4. Submission Form
Users can submit their own ads for editorial review.

## Data Models

### MagazineIssue
```kotlin
data class MagazineIssue(
    val issueNumber: Int,
    val title: String,
    val month: String,
    val tagline: String,
    val ads: List<MagazineAd>,
    val isCurrent: Boolean
)
```

### MagazineAd
```kotlin
data class MagazineAd(
    val headline: String,
    val bodyCopy: String,
    val sponsorName: String,
    val category: AdCategory,
    val destinationUrl: String?,
    val fullDescription: String?
)
```

### AdCategory
```kotlin
enum class AdCategory {
    APP, ART, LOCAL, EVENT, 
    MUSIC, FASHION, FOOD, CULTURE, OTHER
}
```

## Gesture System

### Implementation
Uses Compose `pointerInput` modifiers with custom gesture detection:

```kotlin
Modifier.magazineGestures(
    onCircle = { /* mark as interesting */ },
    onCrumple = { /* dismiss card */ },
    onTearOff = { /* navigate to details */ }
)
```

### Animations
- **Circle**: Hand-drawn circle overlay (800ms)
- **Crumple**: Scale + rotate + fade (600ms)
- **Tear Off**: Slide down + fade (500ms)

All animations include haptic feedback.

## Design System

### Colors

#### Dark Mode
- Background: `#1E1E22` (charcoal)
- Cards: `#2D2D32` (slate)
- Text: `#E8E8E8`

#### Light Mode
- Background: `#FFFBF5` (paper white)
- Cards: `#FAFAFA` (light gray)
- Text: `#1A1A1A`

#### Accent
- Orange: `#FF6B35` (sparingly used)

### Typography
- Material 3 defaults (Roboto)
- Headlines: Bold, prominent
- Body: 1.5x line height for readability

### Spacing
- Card padding: 24dp
- Element spacing: 16-20dp
- Border radius: 12-16dp

## State Management

### Repository Pattern
```kotlin
class MagazineRepository {
    val interactions: StateFlow<Map<String, AdInteractionState>>
    val submissions: StateFlow<List<AdSubmission>>
    
    fun markAsCircled(adId: String)
    fun markAsCrumpled(adId: String)
    fun markAsTornOff(adId: String)
    fun submitAd(submission: AdSubmission)
}
```

### ViewModel
```kotlin
class MagazineViewModel : ViewModel() {
    val currentIssue: StateFlow<MagazineIssue?>
    val pastIssues: StateFlow<List<MagazineIssue>>
    val circledAds: StateFlow<List<MagazineAd>>
    val interactions: StateFlow<Map<String, AdInteractionState>>
}
```

## Navigation

Routes defined in `Screen.kt`:
- `/magazine` - Shelf
- `/magazine/issue/{issueId}` - Issue view
- `/magazine/ad/{adId}` - Ad details
- `/magazine/submit` - Submission form

Navigation added to `KabinkaApp.kt` NavHost.

## Mock Data

### Current Issues
1. **Issue #3** - "Creative Horizons" (5 ads) - Current
2. **Issue #2** - "Winter Collection" (3 ads)
3. **Issue #1** - "Launch Edition" (2 ads)

### Sample Ads
- Coffee shop (Food & Drink)
- Art gallery (Art)
- Yoga studio (Local)
- Task management app (App)
- Live music venue (Music)

## Testing

### Manual Test Cases
1. ✅ Navigate to Magazine via drawer
2. ✅ Browse current and past issues
3. ✅ Open issue and view ads
4. ✅ Double-tap to circle ad
5. ✅ Swipe left/right to crumple
6. ✅ Swipe down to tear off
7. ✅ View ad details
8. ✅ Tap CTA to open link
9. ✅ Submit ad via form
10. ✅ Toggle dark mode

### Edge Cases
- All ads crumpled
- Empty issue
- Long text content
- Missing images
- Invalid URLs

## Performance

### Optimizations
- LazyColumn for lists
- remember() for calculations
- StateFlow for reactive updates
- Minimal recompositions
- Efficient gesture detection

### Metrics
- Animations: 60 FPS target
- Gesture response: < 16ms
- List rendering: Virtualized

## Accessibility

- ✅ High contrast (both modes)
- ✅ Screen reader support
- ✅ Large touch targets (min 48dp)
- ✅ Haptic feedback
- ✅ Clear labeling

## Backend Migration

### Current: Mock Repository
In-memory data, no network calls.

### Future: Real Backend
Architecture supports easy migration:

1. **Keep interfaces unchanged**
2. **Replace repository implementation**:
   ```kotlin
   class MagazineApiRepository : MagazineRepository {
       private val api: MagazineApiService
       override suspend fun getAllIssues() = api.fetchIssues()
   }
   ```
3. **Add persistence** (Room database)
4. **Implement caching** strategy

No UI changes required.

## Dependencies

### Current
- Jetpack Compose
- Material 3
- Kotlin Coroutines
- StateFlow
- ViewModel
- Navigation Compose

### Future (for backend)
- Retrofit
- OkHttp
- Room
- Coil (images)

## Known Limitations

### By Design
- No infinite scroll ✓
- No timeline injection ✓
- No programmatic ads ✓
- No tracking ✓

### Technical
- Mock data only
- No image uploads
- Session-based state
- No analytics

## Future Enhancements

- [ ] Circled Ads collection screen
- [ ] Category filtering
- [ ] Search within Magazine
- [ ] Share ad functionality
- [ ] Persistent storage
- [ ] Backend integration
- [ ] Real image uploads
- [ ] Analytics (ethical)

## Contributing

### Code Style
- Follow Kotlin conventions
- Use Material 3 components
- Compose best practices
- Document public APIs

### Adding Features
1. Update models if needed
2. Extend repository
3. Create/update screens
4. Wire navigation
5. Test thoroughly
6. Update documentation

## License

Part of Kabinka Social Android app.

## Contact

See main app documentation for contact info.

---

**Version**: 1.0.0  
**Last Updated**: January 2026  
**Status**: ✅ Production Ready (with mock data)
