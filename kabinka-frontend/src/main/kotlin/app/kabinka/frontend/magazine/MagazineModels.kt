package app.kabinka.frontend.magazine

import java.time.LocalDate
import java.util.UUID

/**
 * Represents a Magazine issue containing curated advertisements
 */
data class MagazineIssue(
    val id: String = UUID.randomUUID().toString(),
    val issueNumber: Int,
    val title: String,
    val month: String,
    val tagline: String,
    val coverImageUrl: String? = null,
    val publishDate: LocalDate,
    val ads: List<MagazineAd> = emptyList(),
    val isCurrent: Boolean = false
)

/**
 * Category for magazine advertisements
 */
enum class AdCategory(val displayName: String) {
    APP("App"),
    ART("Art"),
    LOCAL("Local"),
    EVENT("Event"),
    MUSIC("Music"),
    FASHION("Fashion"),
    FOOD("Food & Drink"),
    CULTURE("Culture"),
    OTHER("Other")
}

/**
 * Represents a single advertisement in a Magazine issue
 */
data class MagazineAd(
    val id: String = UUID.randomUUID().toString(),
    val headline: String,
    val heroImageUrl: String? = null,
    val bodyCopy: String,
    val sponsorName: String,
    val category: AdCategory = AdCategory.OTHER,
    val destinationUrl: String? = null,
    val fullDescription: String? = null,
    val additionalImages: List<String> = emptyList(),
    val ctaText: String = "Learn More"
)

/**
 * State machine for ad card interactions
 */
sealed class AdInteractionState {
    object Idle : AdInteractionState()
    data class Dragging(val offsetX: Float) : AdInteractionState()
    data class Circling(val progress: Float) : AdInteractionState()
    object Circled : AdInteractionState()
    data class Crumpling(val progress: Float) : AdInteractionState()
    object Crumpled : AdInteractionState()
}

/**
 * Persistent state for an ad
 */
data class AdPersistentState(
    val adId: String,
    val isSaved: Boolean = false,
    val isDismissed: Boolean = false
)

/**
 * Submission status for ad submissions
 */
enum class SubmissionStatus {
    DRAFT,
    SUBMITTED,
    UNDER_REVIEW,
    APPROVED,
    REJECTED
}

/**
 * Represents a user's submission for a magazine ad
 */
data class AdSubmission(
    val id: String = UUID.randomUUID().toString(),
    val brandName: String,
    val headline: String,
    val description: String,
    val imageUrl: String? = null,
    val destinationUrl: String,
    val category: AdCategory,
    val contactEmail: String,
    val status: SubmissionStatus = SubmissionStatus.DRAFT,
    val submittedDate: LocalDate? = null,
    val notes: String? = null
)
