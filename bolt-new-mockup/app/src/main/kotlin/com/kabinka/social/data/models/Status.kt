package com.kabinka.social.data.models

import java.time.Instant
import java.time.temporal.ChronoUnit

enum class Visibility {
    PUBLIC, UNLISTED, FOLLOWERS, DIRECT
}

data class Status(
    val id: String,
    val author: User,
    val content: String,
    val createdAt: Instant,
    val visibility: Visibility = Visibility.PUBLIC,
    val repliesCount: Int = 0,
    val boostsCount: Int = 0,
    val favoritesCount: Int = 0,
    val isFavorited: Boolean = false,
    val isBoosted: Boolean = false,
    val isBookmarked: Boolean = false,
    val mediaAttachments: List<MediaAttachment> = emptyList(),
    val contentWarning: String? = null,
    val poll: Poll? = null,
    val inReplyToId: String? = null,
    val linkPreview: LinkPreview? = null
)

data class MediaAttachment(
    val id: String,
    val type: MediaType,
    val url: String,
    val previewUrl: String? = null,
    val description: String? = null
)

enum class MediaType {
    IMAGE, VIDEO, GIFV, AUDIO
}

data class Poll(
    val id: String,
    val options: List<PollOption>,
    val expiresAt: Instant,
    val votesCount: Int,
    val hasVoted: Boolean = false,
    val multiple: Boolean = false
)

data class PollOption(
    val title: String,
    val votesCount: Int
)

data class LinkPreview(
    val url: String,
    val title: String,
    val description: String?,
    val imageUrl: String?
)

fun mockStatuses(): List<Status> {
    val users = mockUsers()
    val now = Instant.now()

    return listOf(
        Status(
            id = "1",
            author = users[0],
            content = "Just finished an amazing hike in the mountains. The view was absolutely breathtaking!",
            createdAt = now.minus(2, ChronoUnit.HOURS),
            repliesCount = 12,
            boostsCount = 45,
            favoritesCount = 89,
            isFavorited = true
        ),
        Status(
            id = "2",
            author = users[1],
            content = "Morning coffee and fresh air. Nothing beats starting the day outdoors.",
            createdAt = now.minus(5, ChronoUnit.HOURS),
            repliesCount = 5,
            boostsCount = 18,
            favoritesCount = 34,
            contentWarning = "Food & Drink"
        ),
        Status(
            id = "3",
            author = users[2],
            content = "What's everyone's favorite trail around here? Looking for recommendations for this weekend.",
            createdAt = now.minus(1, ChronoUnit.DAYS),
            repliesCount = 23,
            boostsCount = 7,
            favoritesCount = 12,
            poll = Poll(
                id = "poll1",
                options = listOf(
                    PollOption("Mountain trails", 45),
                    PollOption("Forest paths", 32),
                    PollOption("Coastal routes", 28),
                    PollOption("Urban walks", 12)
                ),
                expiresAt = now.plus(2, ChronoUnit.DAYS),
                votesCount = 117,
                multiple = false
            )
        )
    )
}
