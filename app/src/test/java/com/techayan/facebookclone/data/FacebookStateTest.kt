package com.techayan.facebookclone.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FacebookStateTest {

    private var nextId = 0
    private lateinit var state: FacebookState

    @Before
    fun setUp() {
        nextId = 0
        state = SampleData.initial { "gen_${++nextId}" }
    }

    @Test
    fun likeIncreasesCountAndUnlikeRestoresIt() {
        val post = state.post("p1")
        val liked = state.toggleLike("p1")
        assertEquals(Reaction.Like, liked.post("p1").myReaction)
        assertEquals(post.likes + 1, liked.post("p1").likes)

        val unliked = liked.toggleLike("p1")
        assertEquals(Reaction.None, unliked.post("p1").myReaction)
        assertEquals(post.likes, unliked.post("p1").likes)
    }

    @Test
    fun switchingReactionKeepsCount() {
        val liked = state.toggleLike("p1")
        val loved = liked.setReaction("p1", Reaction.Love)
        assertEquals(Reaction.Love, loved.post("p1").myReaction)
        assertEquals(liked.post("p1").likes, loved.post("p1").likes)
    }

    @Test
    fun addCommentIgnoresBlankAndAppendsText() {
        val unchanged = state.addComment("p1", "   ")
        assertEquals(state.post("p1").comments.size, unchanged.post("p1").comments.size)

        val commented = state.addComment("p1", "Looks amazing")
        val comments = commented.post("p1").comments
        assertEquals("Looks amazing", comments.last().text)
        assertEquals(state.currentUserId, comments.last().userId)
        assertEquals("Just now", comments.last().time)
    }

    @Test
    fun createPostAddsToTopOfFeed() {
        val next = state.createPost("Hello Facebook", "feeling happy", null)
        assertEquals("Hello Facebook", next.posts.first().text)
        assertEquals(state.currentUserId, next.posts.first().authorId)
        assertEquals("feeling happy", next.posts.first().feeling)
        assertEquals(state.posts.size + 1, next.posts.size)
    }

    @Test
    fun createPostIgnoresEmpty() {
        assertEquals(state.posts.size, state.createPost("  ", null, null).posts.size)
    }

    @Test
    fun acceptFriendRequestUpdatesCountsAndRemovesRequest() {
        val before = state.currentUser().friendsCount
        val next = state.acceptRequest("fr1")
        assertTrue(next.friendRequests.none { it.id == "fr1" })
        assertEquals(before + 1, next.currentUser().friendsCount)
        assertEquals(state.user("u_bilal").friendsCount + 1, next.user("u_bilal").friendsCount)
        assertTrue(next.notifications.first().text.contains("Bilal Ahmed"))
    }

    @Test
    fun sendMessageAppendsToConversation() {
        val next = state.sendMessage("ch2", "See you at 5")
        val conversation = next.conversation("ch2")
        assertEquals("See you at 5", conversation.lastMessage)
        assertTrue(conversation.messages.last().fromMe)
        assertEquals(0, conversation.unread)
    }

    @Test
    fun searchPeopleFiltersByName() {
        val hits = state.searchPeople("sara")
        assertEquals(1, hits.size)
        assertEquals("Sara Ali", hits.first().name)
        assertFalse(state.searchPeople("").any { it.isCurrentUser })
    }

    @Test
    fun shareAndSavePost() {
        val shared = state.sharePost("p2")
        assertEquals(state.post("p2").shares + 1, shared.post("p2").shares)
        val saved = shared.toggleSave("p2")
        assertTrue("p2" in saved.savedPostIds)
        assertFalse("p2" in saved.toggleSave("p2").savedPostIds)
    }
}
