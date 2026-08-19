package com.techayan.facebookclone.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.techayan.facebookclone.data.FacebookState
import com.techayan.facebookclone.data.PostVisual
import com.techayan.facebookclone.data.ProfileSection
import com.techayan.facebookclone.data.Reaction
import com.techayan.facebookclone.data.Story
import com.techayan.facebookclone.ui.components.GradientPhoto
import com.techayan.facebookclone.ui.components.PostCard
import com.techayan.facebookclone.ui.components.UserAvatar
import com.techayan.facebookclone.ui.theme.FacebookBlue
import com.techayan.facebookclone.ui.theme.LocalFacebookColors

@Composable
fun ProfileScreen(
    state: FacebookState,
    userId: String,
    onBack: () -> Unit,
    onMessage: () -> Unit,
    onToggleLike: (String) -> Unit,
    onReact: (String, Reaction) -> Unit,
    onComments: (String) -> Unit,
    onShare: (String) -> Unit,
    onSave: (String) -> Unit,
) {
    val colors = LocalFacebookColors.current
    val user = state.user(userId)
    var section by remember { mutableStateOf(ProfileSection.Posts) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding(),
    ) {
        item {
            Column(modifier = Modifier.background(colors.surface)) {
                Box(modifier = Modifier.height(254.dp)) {
                    GradientPhoto(
                        user.coverColor,
                        user.avatarColor,
                        Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .align(Alignment.TopCenter),
                    )
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .padding(8.dp)
                            .clip(CircleShape)
                            .background(Color(0x66000000)),
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    UserAvatar(
                        user = user,
                        size = 148.dp,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(start = 12.dp),
                    )
                }
            }
        }
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.surface)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                Text(user.name, fontWeight = FontWeight.Bold, fontSize = 28.sp, color = colors.text)
                Text("${user.friendsCount} friends", color = colors.textSecondary, fontSize = 15.sp)
                Spacer(Modifier.height(12.dp))
                Row {
                    if (user.isCurrentUser) {
                        Button(
                            onClick = {},
                            colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                        ) { Text("+ Add to story") }
                        Spacer(Modifier.width(8.dp))
                        OutlinedButton(onClick = {}, modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp)) {
                            Text("Edit profile", color = colors.text)
                        }
                    } else {
                        Button(
                            onClick = onMessage,
                            colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                        ) { Text("Message") }
                        Spacer(Modifier.width(8.dp))
                        OutlinedButton(onClick = {}, modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp)) {
                            Text("Friends", color = colors.text)
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ProfileSection.entries.forEach { tab ->
                        val selected = section == tab
                        Text(
                            tab.name,
                            color = if (selected) FacebookBlue else colors.textSecondary,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { section = tab }
                                .background(if (selected) Color(0xFFE7F3FF) else Color.Transparent)
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                        )
                    }
                }
            }
        }
        item { Spacer(Modifier.height(8.dp)) }
        when (section) {
            ProfileSection.Posts -> {
                items(state.postsBy(userId), key = { it.id }) { post ->
                    PostCard(
                        post = post,
                        author = user,
                        saved = post.id in state.savedPostIds,
                        onAuthorClick = {},
                        onToggleLike = { onToggleLike(post.id) },
                        onReact = { onReact(post.id, it) },
                        onComment = { onComments(post.id) },
                        onShare = { onShare(post.id) },
                        onSave = { onSave(post.id) },
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }
            ProfileSection.About -> {
                item {
                    Column(
                        modifier = Modifier
                            .background(colors.surface)
                            .fillMaxWidth()
                            .padding(16.dp),
                    ) {
                        Text("About", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = colors.text)
                        Spacer(Modifier.height(8.dp))
                        Text(user.headline, color = colors.text, fontSize = 15.sp)
                        Spacer(Modifier.height(6.dp))
                        Text("Lives in ${user.city}", color = colors.text, fontSize = 15.sp)
                        Spacer(Modifier.height(6.dp))
                        Text("${user.friendsCount} friends", color = colors.text, fontSize = 15.sp)
                    }
                }
            }
            ProfileSection.Friends -> {
                items(state.users.filter { it.id != userId }.take(8), key = { it.id }) { friend ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(colors.surface)
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        UserAvatar(user = friend, size = 48.dp)
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(friend.name, fontWeight = FontWeight.Bold, color = colors.text)
                            Text(friend.city, color = colors.textSecondary, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CreatePostScreen(
    state: FacebookState,
    onBack: () -> Unit,
    onPost: (String, String?, PostVisual?) -> Unit,
) {
    val colors = LocalFacebookColors.current
    val me = state.currentUser()
    var text by remember { mutableStateOf("") }
    var feeling by remember { mutableStateOf<String?>(null) }
    var visualIndex by remember { mutableIntStateOf(-1) }
    val palettes = listOf(
        PostVisual.Gradient(0xFF1877F2, 0xFF0A3D91, "✨", text.ifBlank { "New post" }),
        PostVisual.Photo(0xFFFFB74D, 0xFFE65100, "Photo"),
        PostVisual.Gradient(0xFF00A400, 0xFF1B5E20, "🌿", text.ifBlank { "Feeling green" }),
    )
    val visual = palettes.getOrNull(visualIndex)?.let { item ->
        when (item) {
            is PostVisual.Gradient -> item.copy(caption = text.ifBlank { item.caption })
            is PostVisual.Photo -> item
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.surface)
            .statusBarsPadding()
            .imePadding(),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(4.dp)) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.Close, contentDescription = "Close", tint = colors.text)
            }
            Text("Create post", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = colors.text, modifier = Modifier.weight(1f))
            Button(
                onClick = { onPost(text, feeling, visual) },
                enabled = text.isNotBlank() || visual != null,
                colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue),
                shape = RoundedCornerShape(8.dp),
            ) { Text("Post") }
        }
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            UserAvatar(user = me, size = 40.dp)
            Spacer(Modifier.width(10.dp))
            Column {
                Text(me.name, fontWeight = FontWeight.Bold, color = colors.text)
                Text(feeling ?: "Public", color = colors.textSecondary, fontSize = 12.sp)
            }
        }
        BasicTextField(
            value = text,
            onValueChange = { text = it },
            textStyle = TextStyle(color = colors.text, fontSize = 22.sp),
            cursorBrush = SolidColor(FacebookBlue),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            decorationBox = { inner ->
                Box {
                    if (text.isEmpty()) {
                        Text("What's on your mind, ${me.firstName}?", color = colors.textSecondary, fontSize = 22.sp)
                    }
                    inner()
                }
            },
        )
        visual?.let {
            when (it) {
                is PostVisual.Gradient -> GradientPhoto(it.start, it.end, Modifier.fillMaxWidth().height(140.dp).padding(16.dp).clip(RoundedCornerShape(12.dp))) {
                    Text(it.emoji + "  " + it.caption, color = Color.White, fontWeight = FontWeight.Bold)
                }
                is PostVisual.Photo -> GradientPhoto(it.start, it.end, Modifier.fillMaxWidth().height(140.dp).padding(16.dp).clip(RoundedCornerShape(12.dp))) {
                    Text(it.label, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
        Text("Add to your post", fontWeight = FontWeight.SemiBold, color = colors.text, modifier = Modifier.padding(horizontal = 16.dp))
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            listOf("Photo", "Background", "Nature").forEachIndexed { index, label ->
                FilterChip(
                    selected = visualIndex == index,
                    onClick = { visualIndex = if (visualIndex == index) -1 else index },
                    label = { Text(label) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFFE7F3FF)),
                )
            }
        }
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            listOf("feeling happy", "feeling blessed", "feeling excited").forEach { chip ->
                FilterChip(
                    selected = feeling == chip,
                    onClick = { feeling = if (feeling == chip) null else chip },
                    label = { Text(chip.removePrefix("feeling ")) },
                )
            }
        }
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
fun CommentsScreen(
    state: FacebookState,
    postId: String,
    onBack: () -> Unit,
    onSend: (String) -> Unit,
    onProfile: (String) -> Unit,
) {
    val colors = LocalFacebookColors.current
    val post = state.post(postId)
    var draft by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.surface)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding(),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = colors.text)
            }
            Text("Comments", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = colors.text)
        }
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .background(colors.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text(post.text.ifBlank { "Photo" }, color = colors.text, fontSize = 15.sp)
            }
            items(post.comments, key = { it.id }) { comment ->
                val author = state.user(comment.userId)
                Row {
                    UserAvatar(user = author, size = 36.dp, onClick = { onProfile(author.id) })
                    Spacer(Modifier.width(8.dp))
                    Column(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(colors.iconButton)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                    ) {
                        Text(author.name, fontWeight = FontWeight.Bold, color = colors.text, fontSize = 13.sp)
                        Text(comment.text, color = colors.text, fontSize = 14.sp)
                    }
                }
            }
        }
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            UserAvatar(user = state.currentUser(), size = 32.dp)
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(colors.iconButton)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
            ) {
                if (draft.isEmpty()) Text("Write a comment...", color = colors.textSecondary)
                BasicTextField(
                    value = draft,
                    onValueChange = { draft = it },
                    textStyle = TextStyle(color = colors.text, fontSize = 15.sp),
                    cursorBrush = SolidColor(FacebookBlue),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            IconButton(onClick = {
                onSend(draft)
                draft = ""
            }) {
                Icon(Icons.Filled.Send, contentDescription = "Send", tint = FacebookBlue)
            }
        }
    }
}

@Composable
fun StoryViewerScreen(
    state: FacebookState,
    storyId: String,
    onBack: () -> Unit,
    onProfile: (String) -> Unit,
) {
    val index = state.stories.indexOfFirst { it.id == storyId }.coerceAtLeast(0)
    val story = state.stories[index]
    val user = state.user(story.userId)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .statusBarsPadding()
            .clickable(onClick = onBack),
    ) {
        GradientPhoto(story.start, story.end, Modifier.fillMaxSize()) {
            Text(story.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 36.sp)
        }
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            UserAvatar(user = user, size = 40.dp, ring = true, onClick = { onProfile(user.id) })
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(user.name, color = Color.White, fontWeight = FontWeight.Bold)
                Text("Today", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
            }
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color.White)
            }
        }
        Text(
            "Tap to close",
            color = Color.White.copy(alpha = 0.7f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(24.dp),
        )
    }
}

@Composable
fun SearchScreen(
    state: FacebookState,
    onBack: () -> Unit,
    onProfile: (String) -> Unit,
) {
    val colors = LocalFacebookColors.current
    var query by remember { mutableStateOf("") }
    val results = state.searchPeople(query)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.surface)
            .statusBarsPadding(),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(8.dp)) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = colors.text)
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(colors.iconButton)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
            ) {
                if (query.isEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Search, contentDescription = null, tint = colors.textSecondary, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Search Facebook", color = colors.textSecondary)
                    }
                }
                BasicTextField(
                    value = query,
                    onValueChange = { query = it },
                    textStyle = TextStyle(color = colors.text, fontSize = 16.sp),
                    cursorBrush = SolidColor(FacebookBlue),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            }
        }
        LazyColumn {
            items(results, key = { it.id }) { user ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onProfile(user.id) }
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    UserAvatar(user = user, size = 44.dp)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(user.name, fontWeight = FontWeight.SemiBold, color = colors.text)
                        Text(user.headline, color = colors.textSecondary, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}
