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
            coverImageUrl = "https://i.imgur.com/nXV4i21.jpeg",
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
                headline = "Self-Hosted AI Bridge",
                heroImageUrl = "https://assetstorev1-prd-cdn.unity3d.com/key-image/62acb6a6-00bf-4276-a08d-090a0a1e019c.webp", // Add your image path here
                bodyCopy = "Plug and play Unity integration for self hosted AI. Connect to LlamaCPP, Ollama, and Stable Diffusion for both development workflows and in game runtime use.",
                sponsorName = "Velesio AI",
                category = AdCategory.APP,
                destinationUrl = "https://assetstore.unity.com/packages/tools/generative-ai/personaforge-self-hosted-ai-bridge-335152",
                fullDescription = "Velesio PersonaForge is a self hosted AI bridge for Unity that makes it easy to wire your project to local inference. It is designed to work with the Velesio AI Server Docker Compose stack (including LlamaCPP and Ollama for text generation and Stable Diffusion via Automatic1111 for image generation), and it includes simple example scenes demonstrating inference plus an RPG character generator and chat minigame style demo.",
                ctaText = "View on Unity Asset Store"
            ),
            MagazineAd(
                headline = "Premium Party Halls in Sofia",
                heroImageUrl = "https://zalicontrast.com/wp-content/uploads/2023/11/294042479_3218130801734194_5995780853301563883_n.jpg", // Add your image path here
                bodyCopy = "Zali Contrast offers stylish, fully equipped party halls for birthdays, anniversaries, corporate events, and private celebrations in Sofia. Choose the perfect space to host your next event with sound, lighting and amenities included.",
                sponsorName = "Zali Contrast",
                category = AdCategory.EVENT,
                destinationUrl = "https://zalicontrast.com/",
                fullDescription = "Zali Contrast provides a portfolio of versatile party halls for rent across Sofia. With over 8 years of experience hosting celebrations, their venues accommodate a variety of events — from birthdays and anniversaries to corporate gatherings. Each hall comes with professional sound systems, Wi-Fi, lighting, refreshment areas, and optional catering, entertainment and staff support. Available locations include Contrast Original, Contrast 2, Contrast Yin Yang, Vitosha and North halls, varying in capacity and style to fit your event needs.",
                ctaText = "Book a Party Hall"
            ),
            MagazineAd(
                headline = "Bespoke Interior Design & Project Realization",
                heroImageUrl = "https://studiodbdesign.com/wp-content/uploads/2025/09/10021.2.jpg", // Add your image path here
                bodyCopy = "Studio DB Design is a team of young, ambitious professionals offering bespoke interior design and full project realization for residential and commercial spaces, with projects that stand the test of time.",
                sponsorName = "Studio DB Design",
                category = AdCategory.ART,
                destinationUrl = "https://studiodbdesign.com/",
                fullDescription = "Studio DB Design is a modern interior design studio based in Plovdiv, Bulgaria, specializing in customized residential and commercial interior design and execution. Their approach emphasizes creating timeless, client-centric designs that reflect each client’s individuality rather than following fleeting trends. The studio manages projects from concept through realization and delivers tailored solutions with attention to detail and long-lasting aesthetic value.",
                ctaText = "Explore Our Projects"
            ),
            MagazineAd(
                headline = "Professional Web Design & Development",
                heroImageUrl = "https://scontent.fpdv1-1.fna.fbcdn.net/v/t39.30808-6/568731319_1323898099747003_5429283809062995099_n.png?_nc_cat=106&ccb=1-7&_nc_sid=cc71e4&_nc_ohc=vhwcXYCRW4UQ7kNvwGqjI1H&_nc_oc=AdnqWBiCD58m2aRn0Wj2ImBBe1p5C3l8y6mgLdqd15WzX87dlGDxzLE8CSLjlWc4tSE&_nc_zt=23&_nc_ht=scontent.fpdv1-1.fna&_nc_gid=JB30zT1wY4vLARTSvv1B2g&oh=00_Afo2bHz5oMobXBZGJvefJdq6qvGQlRVR-Z4N6zThc-nZsA&oe=69754F71", // Add your image path here
                bodyCopy = "Web Control – M creates modern, high-performance websites and online stores with a focus on responsive design and long-term business value.",
                sponsorName = "Web Control – M",
                category = AdCategory.APP,
                destinationUrl = "https://webcontrolm.com/",
                fullDescription = "Web Control – M is a web design and development agency specializing in professional website creation that combines modern aesthetics, performance optimization, and ease of maintenance. Their services include bespoke website builds, SEO optimization, online store development, and digital consulting to help businesses improve their online presence and achieve measurable results. Clients consistently praise the team for quality, timeliness, and clear communication throughout the project lifecycle.",
                ctaText = "Get a Custom Website Quote"
            ),
            MagazineAd(
                headline = "Streetwear Fits & Graphic Tees",
                heroImageUrl = "https://i.imgur.com/yoYTjlR.jpeg", // Add your image path here
                bodyCopy = "Boli Me Huq drops bold unisex tees and crewnecks with city-flavored prints and statement graphics — made for daily rotation and standout looks.",
                sponsorName = "Boli Me Huq",
                category = AdCategory.FASHION,
                destinationUrl = "https://bolimehuq.com/",
                fullDescription = "Boli Me Huq is a streetwear shop offering a wide range of unisex graphic tees and crewneck sweatshirts with attitude-driven prints and designs that speak to the local scene. The collection features a mix of drop-style pieces, from ‘Bulgaria Friends’ and ‘V-Lev’ tees to bold crewnecks perfect for layering and everyday wear. With rotating designs and sale pricing across categories, Boli Me Huq is about making street style accessible and expressive for everyday outfits.",
                ctaText = "Shop the Collection"
            ),
            MagazineAd(
                headline = "Streetwear From the Local Scene",
                heroImageUrl = "https://neizdurjala.com/cdn/shop/files/IMG_0243.jpg", // Add your image path here
                bodyCopy = "Neizdurjala delivers raw, local streetwear — tees, hoodies and crewnecks built around attitude, identity and everyday wear, not trends.",
                sponsorName = "Neizdurjala",
                category = AdCategory.FASHION,
                destinationUrl = "https://neizdurjala.com/",
                fullDescription = "Neizdurjala is a local streetwear brand rooted in everyday city life and underground culture. The collection focuses on clean silhouettes, bold statements and wearable pieces designed for daily rotation — from tees and hoodies to zip-ups and long sleeves. No seasonal hype, no overbranding — just clothes that reflect mood, presence and a distinctly local edge.",
                ctaText = "Shop the Drop"
            )
        )
    }
    
    private fun getMockAdsForIssue2(): List<MagazineAd> {
        return listOf(
            MagazineAd(
                headline = "Holiday Market Spectacular",
                heroImageUrl = null, // Add your image path here
                bodyCopy = "Shop local this season! Over 100 vendors featuring handmade gifts and treats.",
                sponsorName = "Winter Wonderland Market",
                category = AdCategory.EVENT,
                destinationUrl = "https://example.com/market",
                fullDescription = "The annual Winter Wonderland Market returns with over 100 local artisans and vendors. Find unique handmade gifts, delicious treats, and festive entertainment.",
                ctaText = "Get Directions"
            ),
            MagazineAd(
                headline = "Cozy Winter Fashion",
                heroImageUrl = null, // Add your image path here
                bodyCopy = "Stay warm in style with our new collection of sustainable knitwear.",
                sponsorName = "Maple Threads",
                category = AdCategory.FASHION,
                destinationUrl = "https://example.com/maple",
                fullDescription = "Maple Threads brings you ethically-made winter essentials. Our new collection features cozy knitwear crafted from sustainable materials.",
                ctaText = "Shop Now"
            ),
            MagazineAd(
                headline = "Community Theater Production",
                heroImageUrl = null, // Add your image path here
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
                heroImageUrl = null, // Add your image path here
                bodyCopy = "A new way to discover local creators and projects. Swipe to explore!",
                sponsorName = "Kabinka Social",
                category = AdCategory.OTHER,
                destinationUrl = "https://example.com/about",
                fullDescription = "Welcome to the inaugural issue of Magazine! This is a curated space for local businesses, creators, and projects to share their stories.",
                ctaText = "Learn More"
            ),
            MagazineAd(
                headline = "Bookshop on Main Street",
                heroImageUrl = null, // Add your image path here
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
