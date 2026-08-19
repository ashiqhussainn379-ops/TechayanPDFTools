package com.techayan.facebookclone.data

data class User(
    val id: String,
    val name: String,
    val firstName: String,
    val headline: String,
    val city: String,
    val friendsCount: Int,
    val avatarColor: Long,
    val coverColor: Long,
    val isOnline: Boolean = false,
    val isCurrentUser: Boolean = false,
) {
    val initials: String
        get() = name.split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .joinToString("") { it.first().uppercase() }
}

enum class Reaction {
    None, Like, Love, Care, Haha, Wow, Sad, Angry
}

data class Comment(
    val id: String,
    val userId: String,
    val text: String,
    val time: String,
    val likes: Int = 0,
)

sealed class PostVisual {
    data class Gradient(
        val start: Long,
        val end: Long,
        val emoji: String,
        val caption: String,
    ) : PostVisual()

    data class Photo(
        val start: Long,
        val end: Long,
        val label: String,
    ) : PostVisual()
}

data class Post(
    val id: String,
    val authorId: String,
    val time: String,
    val privacy: String = "Public",
    val text: String,
    val visual: PostVisual? = null,
    val likes: Int,
    val comments: List<Comment> = emptyList(),
    val shares: Int,
    val myReaction: Reaction = Reaction.None,
    val feeling: String? = null,
)

data class Story(
    val id: String,
    val userId: String,
    val viewed: Boolean,
    val start: Long,
    val end: Long,
    val title: String,
)

data class FriendRequest(
    val id: String,
    val userId: String,
    val mutualFriends: Int,
    val time: String,
)

data class MarketplaceItem(
    val id: String,
    val title: String,
    val price: String,
    val location: String,
    val start: Long,
    val end: Long,
)

enum class NotificationType { Like, Comment, Friend, Birthday, Group, Memory }

data class NotificationItem(
    val id: String,
    val userId: String,
    val text: String,
    val time: String,
    val unread: Boolean,
    val type: NotificationType,
)

data class ChatMessage(
    val id: String,
    val fromMe: Boolean,
    val text: String,
    val time: String,
)

data class Conversation(
    val id: String,
    val userId: String,
    val lastMessage: String,
    val time: String,
    val unread: Int,
    val messages: List<ChatMessage>,
)

data class WatchVideo(
    val id: String,
    val authorId: String,
    val title: String,
    val views: String,
    val time: String,
    val start: Long,
    val end: Long,
)

enum class MainTab {
    Home, Friends, Watch, Marketplace, Notifications, Menu
}

enum class ProfileSection {
    Posts, About, Friends
}
