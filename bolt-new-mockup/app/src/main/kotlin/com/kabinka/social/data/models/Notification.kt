package com.kabinka.social.data.models

import java.time.Instant
import java.time.temporal.ChronoUnit

enum class NotificationType {
    FOLLOW, FAVORITE, BOOST, MENTION, POLL, FOLLOW_REQUEST
}

data class Notification(
    val id: String,
    val type: NotificationType,
    val createdAt: Instant,
    val account: User,
    val status: Status? = null
)

fun mockNotifications(): List<Notification> {
    val users = mockUsers()
    val statuses = mockStatuses()
    val now = Instant.now()

    return listOf(
        Notification(
            id = "n1",
            type = NotificationType.FOLLOW,
            createdAt = now.minus(1, ChronoUnit.HOURS),
            account = users[1]
        ),
        Notification(
            id = "n2",
            type = NotificationType.FAVORITE,
            createdAt = now.minus(2, ChronoUnit.HOURS),
            account = users[2],
            status = statuses[0]
        ),
        Notification(
            id = "n3",
            type = NotificationType.BOOST,
            createdAt = now.minus(3, ChronoUnit.HOURS),
            account = users[1],
            status = statuses[0]
        ),
        Notification(
            id = "n4",
            type = NotificationType.MENTION,
            createdAt = now.minus(5, ChronoUnit.HOURS),
            account = users[0],
            status = statuses[1]
        )
    )
}
