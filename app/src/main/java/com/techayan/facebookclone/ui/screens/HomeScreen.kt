package com.techayan.facebookclone.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.techayan.facebookclone.data.FacebookState
import com.techayan.facebookclone.data.Post
import com.techayan.facebookclone.data.Reaction
import com.techayan.facebookclone.data.Story
import com.techayan.facebookclone.data.User
import com.techayan.facebookclone.ui.components.ComposerCard
import com.techayan.facebookclone.ui.components.PostCard
import com.techayan.facebookclone.ui.components.StoriesRow
import com.techayan.facebookclone.ui.theme.LocalFacebookColors

@Composable
fun HomeScreen(
    state: FacebookState,
    onCreatePost: () -> Unit,
    onStory: (Story) -> Unit,
    onProfile: (String) -> Unit,
    onToggleLike: (String) -> Unit,
    onReact: (String, Reaction) -> Unit,
    onComments: (String) -> Unit,
    onShare: (String) -> Unit,
    onSave: (String) -> Unit,
) {
    val colors = LocalFacebookColors.current
    val me = state.currentUser()
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background),
        contentPadding = PaddingValues(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            ComposerCard(
                user = me,
                onComposerClick = onCreatePost,
                onPhotoClick = onCreatePost,
            )
        }
        item {
            StoriesRow(
                currentUser = me,
                stories = state.stories,
                state = state,
                onCreateStory = onCreatePost,
                onStoryClick = onStory,
            )
        }
        items(state.posts, key = { it.id }) { post ->
            FeedPost(
                post = post,
                author = state.user(post.authorId),
                saved = post.id in state.savedPostIds,
                onAuthorClick = { onProfile(post.authorId) },
                onToggleLike = { onToggleLike(post.id) },
                onReact = { onReact(post.id, it) },
                onComment = { onComments(post.id) },
                onShare = { onShare(post.id) },
                onSave = { onSave(post.id) },
            )
        }
        item { Spacer(Modifier.height(8.dp)) }
    }
}

@Composable
private fun FeedPost(
    post: Post,
    author: User,
    saved: Boolean,
    onAuthorClick: () -> Unit,
    onToggleLike: () -> Unit,
    onReact: (Reaction) -> Unit,
    onComment: () -> Unit,
    onShare: () -> Unit,
    onSave: () -> Unit,
) {
    PostCard(
        post = post,
        author = author,
        saved = saved,
        onAuthorClick = onAuthorClick,
        onToggleLike = onToggleLike,
        onReact = onReact,
        onComment = onComment,
        onShare = onShare,
        onSave = onSave,
    )
}
