package app.kabinka.frontend.magazine

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate

/**
 * Mock repository for Magazine data
 * In production, this would connect to a real backend
 */
class MagazineRepository {
    
    private val _interactions = MutableStateFlow<Map<String, AdPersistentState>>(emptyMap())
    val interactions: StateFlow<Map<String, AdPersistentState>> = _interactions.asStateFlow()
    
    private val _submissions = MutableStateFlow<List<AdSubmission>>(emptyList())
    val submissions: StateFlow<List<AdSubmission>> = _submissions.asStateFlow()
    
    // Mock magazine issues - showing only current issue for now
    private val mockIssues = listOf(
        MagazineIssue(
            issueNumber = 1,
            title = "Creative Horizons",
            month = "January 2026",
            tagline = "Discover local artists and innovative startups",
            coverImageUrl = null,
            publishDate = LocalDate.of(2026, 1, 1),
            isCurrent = true,
            ads = getMockAdsForIssue3()
        )
    )
    
    fun getAllIssues(): List<MagazineIssue> {
        return mockIssues
    }
    
    fun getCurrentIssue(): MagazineIssue? {
        return mockIssues.firstOrNull { it.isCurrent }
    }
    
    fun getPastIssues(): List<MagazineIssue> {
        return mockIssues.filter { !it.isCurrent }.sortedByDescending { it.publishDate }
    }
    
    fun getIssueById(issueId: String): MagazineIssue? {
        return mockIssues.firstOrNull { it.id == issueId }
    }
    
    fun getAdById(adId: String): MagazineAd? {
        return mockIssues.flatMap { it.ads }.firstOrNull { it.id == adId }
    }
    
    fun getInteractionState(adId: String): AdPersistentState {
        return _interactions.value[adId] ?: AdPersistentState(adId)
    }
    
    fun markAsCircled(adId: String) {
        val current = getInteractionState(adId)
        _interactions.value = _interactions.value + (adId to current.copy(isSaved = true))
    }
    
    fun markAsCrumpled(adId: String) {
        val current = getInteractionState(adId)
        _interactions.value = _interactions.value + (adId to current.copy(isDismissed = true))
    }
    
    fun markAsTornOff(adId: String) {
        // Torn off doesn't need persistent state, it just navigates
    }
    
    fun getCircledAds(): List<MagazineAd> {
        val circledIds = _interactions.value.filter { it.value.isSaved }.keys
        return mockIssues.flatMap { it.ads }.filter { it.id in circledIds }
    }
    
    fun submitAd(submission: AdSubmission) {
        val updated = submission.copy(
            status = SubmissionStatus.SUBMITTED,
            submittedDate = LocalDate.now()
        )
        _submissions.value = _submissions.value + updated
    }
    
    fun saveDraft(submission: AdSubmission) {
        val existing = _submissions.value.find { it.id == submission.id }
        if (existing != null) {
            _submissions.value = _submissions.value.map { 
                if (it.id == submission.id) submission else it 
            }
        } else {
            _submissions.value = _submissions.value + submission
        }
    }
    
    private fun getMockAdsForIssue3(): List<MagazineAd> {
        return listOf(
            MagazineAd(
                headline = "Artisan Coffee Roasters",
                bodyCopy = "Experience freshly roasted beans from local farms. Open daily from 7am.",
                sponsorName = "Morningside Coffee Co.",
                category = AdCategory.FOOD,
                destinationUrl = "https://example.com/coffee",
                fullDescription = "Morningside Coffee Co. brings you the finest artisan coffee experience. Our beans are sourced from sustainable local farms and roasted fresh daily in small batches. Visit our downtown location to experience the perfect cup.",
                ctaText = "Visit Our Shop"
            ),
            MagazineAd(
                headline = "Gallery Opening: Digital Dreams",
                bodyCopy = "Explore the intersection of technology and art. Opening reception this Friday.",
                sponsorName = "Riverside Gallery",
                category = AdCategory.ART,
                destinationUrl = "https://example.com/gallery",
                fullDescription = "Join us for the opening reception of 'Digital Dreams' - a groundbreaking exhibition featuring digital artists from around the region. Experience immersive installations and interactive art pieces.",
                ctaText = "RSVP Now"
            ),
            MagazineAd(
                headline = "Your Neighborhood Yoga Studio",
                bodyCopy = "Find your center with classes for all levels. First class free for new members.",
                sponsorName = "Zen Flow Yoga",
                category = AdCategory.LOCAL,
                destinationUrl = "https://example.com/yoga",
                fullDescription = "Zen Flow Yoga offers a welcoming space for practitioners of all levels. From gentle flow to power yoga, our experienced instructors guide you on your wellness journey.",
                ctaText = "Book a Class"
            ),
            MagazineAd(
                headline = "New App: TaskFlow Pro",
                bodyCopy = "Organize your life with our intuitive task manager. Available now on all platforms.",
                sponsorName = "TaskFlow Technologies",
                category = AdCategory.APP,
                destinationUrl = "https://example.com/taskflow",
                fullDescription = "TaskFlow Pro is the task management app you've been waiting for. Beautiful design meets powerful features. Sync across all your devices and collaborate with your team seamlessly.",
                ctaText = "Download Free"
            ),
            MagazineAd(
                headline = "Live Music Every Weekend",
                bodyCopy = "Discover new sounds at our intimate venue. Check our calendar for upcoming shows.",
                sponsorName = "The Blue Note",
                category = AdCategory.MUSIC,
                destinationUrl = "https://example.com/bluenode",
                fullDescription = "The Blue Note presents live music every Friday and Saturday night. From jazz to indie rock, we showcase the best local and touring musicians in an intimate setting.",
                ctaText = "See Schedule"
            ),
            MagazineAd(
                headline = "Farm Fresh Produce Delivery",
                bodyCopy = "Get local, organic vegetables delivered to your door every week.",
                sponsorName = "Greenfield Farms",
                category = AdCategory.FOOD,
                destinationUrl = "https://example.com/greenfield",
                fullDescription = "Greenfield Farms connects you directly with local organic farmers. Enjoy fresh, seasonal produce delivered weekly. Support sustainable agriculture and eat healthier.",
                ctaText = "Start Subscription"
            ),
            MagazineAd(
                headline = "Photography Workshop",
                bodyCopy = "Master portrait lighting techniques. Weekend intensive with award-winning photographer.",
                sponsorName = "Aperture Studio",
                category = AdCategory.ART,
                destinationUrl = "https://example.com/aperture",
                fullDescription = "Join our intensive photography workshop led by award-winning portrait photographer Sarah Chen. Learn professional lighting techniques and editing workflows in this hands-on weekend course.",
                ctaText = "Register Now"
            ),
            MagazineAd(
                headline = "Vintage Vinyl Records",
                bodyCopy = "Rare pressings and classic albums. New arrivals weekly at our record shop.",
                sponsorName = "Spin Cycle Records",
                category = AdCategory.MUSIC,
                destinationUrl = "https://example.com/vinyl",
                fullDescription = "Spin Cycle Records is a vinyl lover's paradise. Browse our extensive collection of rare pressings, classic albums, and new releases. We also buy and trade records.",
                ctaText = "Browse Collection"
            ),
            MagazineAd(
                headline = "Outdoor Adventure Gear",
                bodyCopy = "Gear up for your next adventure. Expert advice and quality equipment.",
                sponsorName = "Summit Outfitters",
                category = AdCategory.LOCAL,
                destinationUrl = "https://example.com/summit",
                fullDescription = "Summit Outfitters has been equipping adventurers for over 20 years. From hiking to climbing to camping, we have the gear you need and the expertise to help you choose it.",
                ctaText = "Shop Gear"
            ),
            MagazineAd(
                headline = "Craft Beer Tasting Room",
                bodyCopy = "Small-batch brews crafted on-site. Tours available every Saturday.",
                sponsorName = "Ironworks Brewery",
                category = AdCategory.FOOD,
                destinationUrl = "https://example.com/ironworks",
                fullDescription = "Ironworks Brewery crafts exceptional small-batch beers in our historic downtown location. Visit our tasting room to sample our rotating selection of ales, lagers, and seasonal brews. Weekend brewery tours available.",
                ctaText = "Visit Taproom"
            )
        )
    }
    
    private fun getMockAdsForIssue2(): List<MagazineAd> {
        return listOf(
            MagazineAd(
                headline = "Holiday Market Spectacular",
                bodyCopy = "Shop local this season! Over 100 vendors featuring handmade gifts and treats.",
                sponsorName = "Winter Wonderland Market",
                category = AdCategory.EVENT,
                destinationUrl = "https://example.com/market",
                fullDescription = "The annual Winter Wonderland Market returns with over 100 local artisans and vendors. Find unique handmade gifts, delicious treats, and festive entertainment.",
                ctaText = "Get Directions"
            ),
            MagazineAd(
                headline = "Cozy Winter Fashion",
                bodyCopy = "Stay warm in style with our new collection of sustainable knitwear.",
                sponsorName = "Maple Threads",
                category = AdCategory.FASHION,
                destinationUrl = "https://example.com/maple",
                fullDescription = "Maple Threads brings you ethically-made winter essentials. Our new collection features cozy knitwear crafted from sustainable materials.",
                ctaText = "Shop Now"
            ),
            MagazineAd(
                headline = "Community Theater Production",
                bodyCopy = "A classic tale reimagined. Shows every weekend through December.",
                sponsorName = "Elmwood Players",
                category = AdCategory.CULTURE,
                destinationUrl = "https://example.com/theater",
                fullDescription = "The Elmwood Players present their winter production - a fresh take on a beloved classic. Support local theater and enjoy an unforgettable performance.",
                ctaText = "Buy Tickets"
            )
        )
    }
    
    private fun getMockAdsForIssue1(): List<MagazineAd> {
        return listOf(
            MagazineAd(
                headline = "Welcome to Magazine",
                bodyCopy = "A new way to discover local creators and projects. Swipe to explore!",
                sponsorName = "Kabinka Social",
                category = AdCategory.OTHER,
                destinationUrl = "https://example.com/about",
                fullDescription = "Welcome to the inaugural issue of Magazine! This is a curated space for local businesses, creators, and projects to share their stories.",
                ctaText = "Learn More"
            ),
            MagazineAd(
                headline = "Bookshop on Main Street",
                bodyCopy = "Discover your next favorite read. Specializing in local authors and indie presses.",
                sponsorName = "Chapter & Verse Books",
                category = AdCategory.LOCAL,
                destinationUrl = "https://example.com/books",
                fullDescription = "Chapter & Verse is your neighborhood independent bookshop. We specialize in works by local authors and small presses, with a curated selection for every reader.",
                ctaText = "Visit Store"
            )
        )
    }
    
    companion object {
        @Volatile
        private var instance: MagazineRepository? = null
        
        fun getInstance(): MagazineRepository {
            return instance ?: synchronized(this) {
                instance ?: MagazineRepository().also { instance = it }
            }
        }
    }
}
