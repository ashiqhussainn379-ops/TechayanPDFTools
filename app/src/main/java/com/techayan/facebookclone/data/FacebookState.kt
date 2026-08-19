package com.techayan.facebookclone.data

data class FacebookState(
    val currentUserId: String,
    val users: List<User>,
    val posts: List<Post>,
    val stories: List<Story>,
    val friendRequests: List<FriendRequest>,
    val suggestions: List<String>,
    val marketplace: List<MarketplaceItem>,
    val notifications: List<NotificationItem>,
    val conversations: List<Conversation>,
    val videos: List<WatchVideo>,
    val savedPostIds: Set<String> = emptySet(),
    val darkTheme: Boolean = false,
    val idFactory: () -> String = { "id_${System.currentTimeMillis()}" },
) {
    fun user(id: String): User = users.first { it.id == id }

    fun currentUser(): User = user(currentUserId)

    fun post(id: String): Post = posts.first { it.id == id }

    fun conversation(id: String): Conversation = conversations.first { it.id == id }

    fun unreadNotificationCount(): Int = notifications.count { it.unread }

    fun unreadMessageCount(): Int = conversations.sumOf { it.unread }

    fun postsBy(userId: String): List<Post> = posts.filter { it.authorId == userId }

    fun toggleTheme(): FacebookState = copy(darkTheme = !darkTheme)

    fun toggleLike(postId: String): FacebookState = setReaction(
        postId,
        if (post(postId).myReaction == Reaction.Like) Reaction.None else Reaction.Like,
    )

    fun setReaction(postId: String, reaction: Reaction): FacebookState = copy(
        posts = posts.map { post ->
            if (post.id != postId) {
                post
            } else {
                val wasNone = post.myReaction == Reaction.None
                val goingNone = reaction == Reaction.None
                val likes = when {
                    wasNone && !goingNone -> post.likes + 1
                    !wasNone && goingNone -> (post.likes - 1).coerceAtLeast(0)
                    else -> post.likes
                }
                post.copy(myReaction = reaction, likes = likes)
            }
        },
    )

    fun addComment(postId: String, text: String): FacebookState {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return this
        val comment = Comment(
            id = idFactory(),
            userId = currentUserId,
            text = trimmed,
            time = "Just now",
        )
        return copy(
            posts = posts.map { post ->
                if (post.id == postId) post.copy(comments = post.comments + comment) else post
            },
        )
    }

    fun sharePost(postId: String): FacebookState = copy(
        posts = posts.map { post ->
            if (post.id == postId) post.copy(shares = post.shares + 1) else post
        },
    )

    fun toggleSave(postId: String): FacebookState = copy(
        savedPostIds = if (postId in savedPostIds) savedPostIds - postId else savedPostIds + postId,
    )

    fun createPost(text: String, feeling: String?, visual: PostVisual?): FacebookState {
        val trimmed = text.trim()
        if (trimmed.isEmpty() && visual == null) return this
        val post = Post(
            id = idFactory(),
            authorId = currentUserId,
            time = "Just now",
            text = trimmed,
            visual = visual,
            likes = 0,
            comments = emptyList(),
            shares = 0,
            feeling = feeling,
        )
        return copy(posts = listOf(post) + posts)
    }

    fun markStoryViewed(storyId: String): FacebookState = copy(
        stories = stories.map { story ->
            if (story.id == storyId) story.copy(viewed = true) else story
        },
    )

    fun acceptRequest(requestId: String): FacebookState {
        val request = friendRequests.find { it.id == requestId } ?: return this
        val friend = users.find { it.id == request.userId } ?: return this
        return copy(
            users = users.map { person ->
                when (person.id) {
                    currentUserId, request.userId -> person.copy(friendsCount = person.friendsCount + 1)
                    else -> person
                }
            },
            friendRequests = friendRequests.filter { it.id != requestId },
            suggestions = suggestions.filter { it != request.userId },
            notifications = listOf(
                NotificationItem(
                    id = idFactory(),
                    userId = request.userId,
                    text = "You are now friends with ${friend.name}.",
                    time = "Just now",
                    unread = true,
                    type = NotificationType.Friend,
                ),
            ) + notifications,
        )
    }

    fun declineRequest(requestId: String): FacebookState = copy(
        friendRequests = friendRequests.filter { it.id != requestId },
    )

    fun addSuggestedFriend(userId: String): FacebookState {
        if (userId !in suggestions) return this
        val person = users.find { it.id == userId } ?: return this
        return copy(
            suggestions = suggestions.filter { it != userId },
            notifications = listOf(
                NotificationItem(
                    id = idFactory(),
                    userId = userId,
                    text = "Friend request sent to ${person.name}.",
                    time = "Just now",
                    unread = true,
                    type = NotificationType.Friend,
                ),
            ) + notifications,
        )
    }

    fun markNotificationsRead(): FacebookState = copy(
        notifications = notifications.map { it.copy(unread = false) },
    )

    fun sendMessage(conversationId: String, text: String): FacebookState {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return this
        val message = ChatMessage(
            id = idFactory(),
            fromMe = true,
            text = trimmed,
            time = "Just now",
        )
        return copy(
            conversations = conversations.map { conversation ->
                if (conversation.id != conversationId) {
                    conversation
                } else {
                    conversation.copy(
                        messages = conversation.messages + message,
                        lastMessage = trimmed,
                        time = "Just now",
                        unread = 0,
                    )
                }
            },
        )
    }

    fun openConversation(conversationId: String): FacebookState = copy(
        conversations = conversations.map { conversation ->
            if (conversation.id == conversationId) conversation.copy(unread = 0) else conversation
        },
    )

    fun conversationWith(userId: String): Conversation? =
        conversations.find { it.userId == userId }

    fun startOrOpenChat(userId: String): Pair<FacebookState, String> {
        conversationWith(userId)?.let { existing ->
            return openConversation(existing.id) to existing.id
        }
        val id = idFactory()
        val conversation = Conversation(
            id = id,
            userId = userId,
            lastMessage = "",
            time = "Just now",
            unread = 0,
            messages = emptyList(),
        )
        return copy(conversations = listOf(conversation) + conversations) to id
    }

    fun searchPeople(query: String): List<User> {
        val needle = query.trim()
        if (needle.isEmpty()) return users.filterNot { it.isCurrentUser }
        return users.filter {
            !it.isCurrentUser && it.name.contains(needle, ignoreCase = true)
        }
    }
}
