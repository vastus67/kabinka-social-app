package com.kabinka.social.data.models

import java.time.Instant
import java.time.temporal.ChronoUnit

data class Conversation(
    val id: String,
    val participants: List<User>,
    val lastMessage: DirectMessage?,
    val unread: Boolean = false
)

data class DirectMessage(
    val id: String,
    val author: User,
    val content: String,
    val createdAt: Instant
)

fun mockConversations(): List<Conversation> {
    val users = mockUsers()
    val now = Instant.now()

    return listOf(
        Conversation(
            id = "c1",
            participants = listOf(users[0]),
            lastMessage = DirectMessage(
                id = "dm1",
                author = users[0],
                content = "Hey, thanks for the trail recommendation!",
                createdAt = now.minus(30, ChronoUnit.MINUTES)
            ),
            unread = true
        ),
        Conversation(
            id = "c2",
            participants = listOf(users[1]),
            lastMessage = DirectMessage(
                id = "dm2",
                author = users[1],
                content = "Let's meet up for coffee sometime",
                createdAt = now.minus(2, ChronoUnit.HOURS)
            ),
            unread = false
        )
    )
}
