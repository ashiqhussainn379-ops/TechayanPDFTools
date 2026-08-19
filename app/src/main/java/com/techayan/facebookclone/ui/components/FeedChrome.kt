package com.techayan.facebookclone.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.techayan.facebookclone.data.FacebookState
import com.techayan.facebookclone.data.Story
import com.techayan.facebookclone.data.User
import com.techayan.facebookclone.ui.theme.CreateGreen
import com.techayan.facebookclone.ui.theme.FacebookBlue
import com.techayan.facebookclone.ui.theme.LocalFacebookColors
import com.techayan.facebookclone.ui.theme.LoveRed

@Composable
fun ComposerCard(
    user: User,
    onComposerClick: () -> Unit,
    onPhotoClick: () -> Unit,
) {
    val colors = LocalFacebookColors.current
    Column(modifier = Modifier.background(colors.surface)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            UserAvatar(user = user, size = 40.dp)
            Spacer(Modifier.width(10.dp))
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(colors.iconButton)
                    .clickable(onClick = onComposerClick)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
            ) {
                Text("What's on your mind, ${user.firstName}?", color = colors.textSecondary, fontSize = 16.sp)
            }
        }
        HorizontalDivider(color = colors.divider, thickness = 0.4.dp)
        Row(modifier = Modifier.fillMaxWidth()) {
            ComposerAction(
                modifier = Modifier.weight(1f),
                iconTint = LoveRed,
                icon = Icons.Filled.LiveTv,
                label = "Live",
                onClick = onComposerClick,
            )
            ComposerAction(
                modifier = Modifier.weight(1f),
                iconTint = CreateGreen,
                icon = Icons.Filled.PhotoLibrary,
                label = "Photo",
                onClick = onPhotoClick,
            )
            ComposerAction(
                modifier = Modifier.weight(1f),
                iconTint = FacebookBlue,
                icon = Icons.Filled.Videocam,
                label = "Reel",
                onClick = onComposerClick,
            )
        }
    }
}

@Composable
private fun ComposerAction(
    modifier: Modifier,
    iconTint: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    val colors = LocalFacebookColors.current
    Row(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(icon, contentDescription = label, tint = iconTint, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(6.dp))
        Text(label, color = colors.text, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
    }
}

@Composable
fun StoriesRow(
    currentUser: User,
    stories: List<Story>,
    state: FacebookState,
    onCreateStory: () -> Unit,
    onStoryClick: (Story) -> Unit,
) {
    val colors = LocalFacebookColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surface)
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 10.dp, vertical = 10.dp),
    ) {
        CreateStoryCard(currentUser, onCreateStory)
        stories.forEach { story ->
            Spacer(Modifier.width(8.dp))
            StoryCard(story, state.user(story.userId)) { onStoryClick(story) }
        }
    }
}

@Composable
private fun CreateStoryCard(user: User, onClick: () -> Unit) {
    val colors = LocalFacebookColors.current
    Column(
        modifier = Modifier
            .width(112.dp)
            .height(200.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(colors.iconButton)
            .clickable(onClick = onClick),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .background(Color(user.avatarColor)),
            contentAlignment = Alignment.BottomCenter,
        ) {
            Text(user.initials, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 28.sp)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 0.dp)
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(FacebookBlue)
                    .border(3.dp, colors.surface, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Create story", tint = Color.White, modifier = Modifier.size(20.dp))
            }
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp),
            contentAlignment = Alignment.TopCenter,
        ) {
            Text("Create story", color = colors.text, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
        }
    }
}

@Composable
private fun StoryCard(story: Story, user: User, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .width(112.dp)
            .height(200.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
    ) {
        GradientPhoto(story.start, story.end, Modifier.fillMaxSize()) {
            Text(story.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
        Box(
            modifier = Modifier
                .padding(8.dp)
                .align(Alignment.TopStart),
        ) {
            UserAvatar(user = user, size = 36.dp, ring = !story.viewed)
        }
        Text(
            user.firstName,
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(8.dp),
        )
    }
}
