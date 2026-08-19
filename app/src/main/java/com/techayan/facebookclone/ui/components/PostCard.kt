@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)

package com.techayan.facebookclone.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.ThumbUpOffAlt
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.techayan.facebookclone.data.Post
import com.techayan.facebookclone.data.PostVisual
import com.techayan.facebookclone.data.Reaction
import com.techayan.facebookclone.data.User
import com.techayan.facebookclone.ui.theme.CareOrange
import com.techayan.facebookclone.ui.theme.LikeBlue
import com.techayan.facebookclone.ui.theme.LocalFacebookColors
import com.techayan.facebookclone.ui.theme.LoveRed

@Composable
fun PostCard(
    post: Post,
    author: User,
    saved: Boolean,
    onAuthorClick: () -> Unit,
    onToggleLike: () -> Unit,
    onReact: (Reaction) -> Unit,
    onComment: () -> Unit,
    onShare: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalFacebookColors.current
    var menu by remember { mutableStateOf(false) }
    var picker by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.surface),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            UserAvatar(user = author, size = 40.dp, onClick = onAuthorClick)
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = colors.text, fontSize = 15.sp)) {
                            append(author.name)
                        }
                        if (post.feeling != null) {
                            withStyle(SpanStyle(color = colors.textSecondary, fontWeight = FontWeight.Normal, fontSize = 15.sp)) {
                                append(" is ${post.feeling}")
                            }
                        }
                    },
                    modifier = Modifier.clickable(onClick = onAuthorClick),
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(post.time, color = colors.textSecondary, fontSize = 12.sp)
                    Text(" · ", color = colors.textSecondary, fontSize = 12.sp)
                    Icon(
                        Icons.Filled.Public,
                        contentDescription = post.privacy,
                        tint = colors.textSecondary,
                        modifier = Modifier.size(12.dp),
                    )
                }
            }
            Box {
                IconButton(onClick = { menu = true }) {
                    Icon(Icons.Filled.MoreHoriz, contentDescription = "More", tint = colors.textSecondary)
                }
                DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                    DropdownMenuItem(
                        text = { Text(if (saved) "Unsave post" else "Save post") },
                        onClick = {
                            menu = false
                            onSave()
                        },
                    )
                    DropdownMenuItem(
                        text = { Text("Hide post") },
                        onClick = { menu = false },
                    )
                }
            }
        }

        if (post.text.isNotBlank()) {
            Text(
                text = post.text,
                color = colors.text,
                fontSize = 15.sp,
                lineHeight = 21.sp,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
            )
            Spacer(Modifier.height(8.dp))
        }

        post.visual?.let { visual ->
            when (visual) {
                is PostVisual.Gradient -> {
                    GradientPhoto(
                        start = visual.start,
                        end = visual.end,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(visual.emoji, fontSize = 52.sp)
                            Spacer(Modifier.height(8.dp))
                            Text(
                                visual.caption,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 26.sp,
                            )
                        }
                    }
                }
                is PostVisual.Photo -> {
                    GradientPhoto(
                        start = visual.start,
                        end = visual.end,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp),
                    ) {
                        Text(
                            visual.label,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ReactionBadges(post.myReaction, post.likes)
            Spacer(Modifier.weight(1f))
            if (post.comments.isNotEmpty()) {
                Text(
                    "${post.comments.size} comments",
                    color = colors.textSecondary,
                    fontSize = 13.sp,
                    modifier = Modifier.clickable(onClick = onComment),
                )
                Spacer(Modifier.width(10.dp))
            }
            if (post.shares > 0) {
                Text("${post.shares} shares", color = colors.textSecondary, fontSize = 13.sp)
            }
        }

        HorizontalDivider(color = colors.divider, thickness = 0.6.dp)

        if (picker) {
            ReactionPicker(
                onPick = {
                    picker = false
                    onReact(it)
                },
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            val likeColor = reactionColor(post.myReaction)
            val likeLabel = reactionLabel(post.myReaction)
            ActionButton(
                icon = if (post.myReaction == Reaction.None) Icons.Outlined.ThumbUpOffAlt else Icons.Filled.ThumbUp,
                label = likeLabel,
                tint = likeColor ?: colors.textSecondary,
                onClick = {
                    picker = false
                    onToggleLike()
                },
                onLongClick = { picker = true },
            )
            ActionButton(
                icon = Icons.Outlined.ChatBubbleOutline,
                label = "Comment",
                tint = colors.textSecondary,
                onClick = onComment,
            )
            ActionButton(
                icon = Icons.AutoMirrored.Filled.Reply,
                label = "Share",
                tint = colors.textSecondary,
                onClick = onShare,
            )
        }
    }
}

@Composable
private fun ReactionBadges(mine: Reaction, likes: Int) {
    val colors = LocalFacebookColors.current
    Row(verticalAlignment = Alignment.CenterVertically) {
        val shown = if (mine == Reaction.None) {
            listOf(Reaction.Like, Reaction.Love)
        } else {
            listOf(mine, if (mine == Reaction.Like) Reaction.Love else Reaction.Like)
        }
        shown.distinct().forEachIndexed { index, reaction ->
            Box(
                modifier = Modifier
                    .padding(end = if (index == 0) 0.dp else 0.dp)
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(reactionBadgeColor(reaction)),
                contentAlignment = Alignment.Center,
            ) {
                Text(reactionEmoji(reaction), fontSize = 10.sp)
            }
        }
        Spacer(Modifier.width(6.dp))
        Text("$likes", color = colors.textSecondary, fontSize = 13.sp)
    }
}

@Composable
private fun ReactionPicker(onPick: (Reaction) -> Unit) {
    val colors = LocalFacebookColors.current
    Row(
        modifier = Modifier
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(colors.iconButton)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        listOf(
            Reaction.Like, Reaction.Love, Reaction.Care, Reaction.Haha,
            Reaction.Wow, Reaction.Sad, Reaction.Angry,
        ).forEach { reaction ->
            Text(
                reactionEmoji(reaction),
                fontSize = 26.sp,
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable { onPick(reaction) }
                    .padding(2.dp),
            )
        }
    }
}

@Composable
private fun ActionButton(
    icon: ImageVector,
    label: String,
    tint: Color,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(vertical = 10.dp, horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(6.dp))
        Text(label, color = tint, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
    }
}

private fun reactionEmoji(reaction: Reaction): String = when (reaction) {
    Reaction.None, Reaction.Like -> "👍"
    Reaction.Love -> "❤️"
    Reaction.Care -> "🥰"
    Reaction.Haha -> "😆"
    Reaction.Wow -> "😮"
    Reaction.Sad -> "😢"
    Reaction.Angry -> "😡"
}

private fun reactionLabel(reaction: Reaction): String = when (reaction) {
    Reaction.None -> "Like"
    Reaction.Like -> "Like"
    Reaction.Love -> "Love"
    Reaction.Care -> "Care"
    Reaction.Haha -> "Haha"
    Reaction.Wow -> "Wow"
    Reaction.Sad -> "Sad"
    Reaction.Angry -> "Angry"
}

private fun reactionColor(reaction: Reaction): Color? = when (reaction) {
    Reaction.None -> null
    Reaction.Like -> LikeBlue
    Reaction.Love -> LoveRed
    Reaction.Care -> CareOrange
    Reaction.Haha, Reaction.Wow -> CareOrange
    Reaction.Sad, Reaction.Angry -> CareOrange
}

private fun reactionBadgeColor(reaction: Reaction): Color = when (reaction) {
    Reaction.Love -> LoveRed
    Reaction.Care, Reaction.Haha, Reaction.Wow, Reaction.Sad, Reaction.Angry -> CareOrange
    else -> LikeBlue
}
