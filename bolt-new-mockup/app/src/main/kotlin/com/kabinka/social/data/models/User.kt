package com.kabinka.social.data.models

data class User(
    val id: String,
    val username: String,
    val displayName: String,
    val avatarUrl: String? = null,
    val bio: String = "",
    val followersCount: Int = 0,
    val followingCount: Int = 0,
    val postsCount: Int = 0,
    val isFollowing: Boolean = false,
    val isFollowedBy: Boolean = false
)

fun mockUsers() = listOf(
    User(
        id = "1",
        username = "@alex",
        displayName = "Alex Nomad",
        bio = "Outdoor enthusiast, photographer, and coffee addict",
        followersCount = 1234,
        followingCount = 567,
        postsCount = 890
    ),
    User(
        id = "2",
        username = "@sam",
        displayName = "Sam Rivers",
        bio = "Nature lover, runner, adventure seeker",
        followersCount = 892,
        followingCount = 234,
        postsCount = 567
    ),
    User(
        id = "3",
        username = "@jordan",
        displayName = "Jordan Sky",
        bio = "Writer, traveler, explorer of the unknown",
        followersCount = 2341,
        followingCount = 891,
        postsCount = 1234
    )
)
