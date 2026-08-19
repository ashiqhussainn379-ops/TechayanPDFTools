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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Help
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.techayan.facebookclone.data.FacebookState
import com.techayan.facebookclone.data.FriendRequest
import com.techayan.facebookclone.data.MarketplaceItem
import com.techayan.facebookclone.data.NotificationItem
import com.techayan.facebookclone.data.NotificationType
import com.techayan.facebookclone.data.WatchVideo
import com.techayan.facebookclone.ui.components.GradientPhoto
import com.techayan.facebookclone.ui.components.UserAvatar
import com.techayan.facebookclone.ui.theme.FacebookBlue
import com.techayan.facebookclone.ui.theme.LikeBlue
import com.techayan.facebookclone.ui.theme.LocalFacebookColors
import com.techayan.facebookclone.ui.theme.LoveRed

@Composable
fun FriendsScreen(
    state: FacebookState,
    onProfile: (String) -> Unit,
    onAccept: (String) -> Unit,
    onDecline: (String) -> Unit,
    onAdd: (String) -> Unit,
) {
    val colors = LocalFacebookColors.current
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background),
        contentPadding = PaddingValues(bottom = 24.dp),
    ) {
        item {
            SectionHeader("Friends")
        }
        item {
            Text(
                "Friend requests",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = colors.text,
                modifier = Modifier
                    .background(colors.surface)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
            )
        }
        if (state.friendRequests.isEmpty()) {
            item {
                Text(
                    "No new requests",
                    color = colors.textSecondary,
                    modifier = Modifier
                        .background(colors.surface)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                )
            }
        }
        items(state.friendRequests, key = { it.id }) { request ->
            FriendRequestRow(state, request, onProfile, onAccept, onDecline)
        }
        item {
            Spacer(Modifier.height(8.dp))
            Text(
                "People you may know",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = colors.text,
                modifier = Modifier
                    .background(colors.surface)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
            )
        }
        items(state.suggestions, key = { it }) { userId ->
            SuggestionRow(state, userId, onProfile, onAdd)
        }
    }
}

@Composable
private fun FriendRequestRow(
    state: FacebookState,
    request: FriendRequest,
    onProfile: (String) -> Unit,
    onAccept: (String) -> Unit,
    onDecline: (String) -> Unit,
) {
    val colors = LocalFacebookColors.current
    val user = state.user(request.userId)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surface)
            .padding(horizontal = 16.dp, vertical = 10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            UserAvatar(user = user, size = 72.dp, onClick = { onProfile(user.id) })
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(user.name, fontWeight = FontWeight.Bold, color = colors.text, fontSize = 17.sp)
                Text("${request.mutualFriends} mutual friends", color = colors.textSecondary, fontSize = 13.sp)
                Text(request.time, color = colors.textSecondary, fontSize = 12.sp)
            }
        }
        Spacer(Modifier.height(10.dp))
        Row {
            Button(
                onClick = { onAccept(request.id) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue),
                shape = RoundedCornerShape(8.dp),
            ) { Text("Confirm") }
            Spacer(Modifier.width(8.dp))
            OutlinedButton(
                onClick = { onDecline(request.id) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
            ) { Text("Delete", color = colors.text) }
        }
    }
}

@Composable
private fun SuggestionRow(
    state: FacebookState,
    userId: String,
    onProfile: (String) -> Unit,
    onAdd: (String) -> Unit,
) {
    val colors = LocalFacebookColors.current
    val user = state.user(userId)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surface)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        UserAvatar(user = user, size = 64.dp, onClick = { onProfile(user.id) })
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(user.name, fontWeight = FontWeight.Bold, color = colors.text, fontSize = 16.sp)
            Text(user.headline, color = colors.textSecondary, fontSize = 13.sp)
        }
        Button(
            onClick = { onAdd(userId) },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE7F3FF)),
            shape = RoundedCornerShape(8.dp),
        ) {
            Text("Add friend", color = FacebookBlue, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun WatchScreen(state: FacebookState, onProfile: (String) -> Unit) {
    val colors = LocalFacebookColors.current
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(bottom = 16.dp),
    ) {
        item { SectionHeader("Watch") }
        items(state.videos, key = { it.id }) { video ->
            WatchCard(state, video, onProfile)
        }
    }
}

@Composable
private fun WatchCard(state: FacebookState, video: WatchVideo, onProfile: (String) -> Unit) {
    val colors = LocalFacebookColors.current
    val author = state.user(video.authorId)
    Column(modifier = Modifier.background(colors.surface)) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            UserAvatar(user = author, size = 40.dp, onClick = { onProfile(author.id) })
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(author.name, fontWeight = FontWeight.Bold, color = colors.text)
                Text("${video.time} · ${video.views}", color = colors.textSecondary, fontSize = 12.sp)
            }
        }
        GradientPhoto(video.start, video.end, Modifier.fillMaxWidth().height(220.dp)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("▶", color = Color.White, fontSize = 44.sp)
                Text(video.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        }
        Text(
            video.title,
            color = colors.text,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            modifier = Modifier.padding(12.dp),
        )
    }
}

@Composable
fun MarketplaceScreen(state: FacebookState) {
    val colors = LocalFacebookColors.current
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background),
        contentPadding = PaddingValues(bottom = 16.dp),
    ) {
        item { SectionHeader("Marketplace") }
        item {
            Column(modifier = Modifier.background(colors.surface).padding(12.dp)) {
                state.marketplace.chunked(2).forEach { row ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        row.forEach { item ->
                            MarketplaceTile(item, Modifier.weight(1f).padding(6.dp))
                        }
                        if (row.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun MarketplaceTile(item: MarketplaceItem, modifier: Modifier) {
    val colors = LocalFacebookColors.current
    Column(modifier = modifier) {
        GradientPhoto(
            item.start,
            item.end,
            Modifier
                .fillMaxWidth()
                .height(140.dp)
                .clip(RoundedCornerShape(8.dp)),
        )
        Spacer(Modifier.height(6.dp))
        Text(item.price, fontWeight = FontWeight.Bold, color = colors.text, fontSize = 16.sp)
        Text(item.title, color = colors.text, fontSize = 14.sp, maxLines = 1)
        Text(item.location, color = colors.textSecondary, fontSize = 12.sp)
    }
}

@Composable
fun NotificationsScreen(state: FacebookState, onProfile: (String) -> Unit) {
    val colors = LocalFacebookColors.current
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background),
    ) {
        item { SectionHeader("Notifications") }
        items(state.notifications, key = { it.id }) { item ->
            NotificationRow(state, item, onProfile)
        }
    }
}

@Composable
private fun NotificationRow(
    state: FacebookState,
    item: NotificationItem,
    onProfile: (String) -> Unit,
) {
    val colors = LocalFacebookColors.current
    val user = state.user(item.userId)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (item.unread) {
                    if (colors.isDark) Color(0xFF3A4A60) else Color(0xFFE7F3FF)
                } else {
                    colors.surface
                },
            )
            .clickable { onProfile(user.id) }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box {
            UserAvatar(user = user, size = 56.dp)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(notificationColor(item.type)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(notificationIcon(item.type), contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
            }
        }
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(item.text, color = colors.text, fontSize = 14.sp, lineHeight = 18.sp)
            Text(item.time, color = FacebookBlue, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
        }
    }
}

private fun notificationIcon(type: NotificationType): ImageVector = when (type) {
    NotificationType.Like -> Icons.Filled.ThumbUp
    NotificationType.Comment -> Icons.Filled.ChatBubble
    NotificationType.Friend, NotificationType.Group -> Icons.Filled.Group
    NotificationType.Birthday -> Icons.Filled.Cake
    NotificationType.Memory -> Icons.Filled.Photo
}

private fun notificationColor(type: NotificationType): Color = when (type) {
    NotificationType.Like -> LikeBlue
    NotificationType.Comment -> FacebookBlue
    NotificationType.Friend, NotificationType.Group -> FacebookBlue
    NotificationType.Birthday -> LoveRed
    NotificationType.Memory -> Color(0xFF7B1FA2)
}

@Composable
fun MenuScreen(
    state: FacebookState,
    onProfile: () -> Unit,
    onToggleTheme: () -> Unit,
    onLogout: () -> Unit,
    onSearch: () -> Unit,
    onSaved: () -> Unit,
) {
    val colors = LocalFacebookColors.current
    val me = state.currentUser()
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background),
        contentPadding = PaddingValues(bottom = 24.dp),
    ) {
        item { SectionHeader("Menu") }
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.surface)
                    .clickable(onClick = onProfile)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                UserAvatar(user = me, size = 56.dp)
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(me.name, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = colors.text)
                    Text("See your profile", color = colors.textSecondary, fontSize = 14.sp)
                }
            }
        }
        item { Spacer(Modifier.height(8.dp)) }
        item {
            Column(modifier = Modifier.background(colors.surface)) {
                MenuRow(Icons.Filled.PersonAdd, "Friends", "${state.friendRequests.size} requests", FacebookBlue, onProfile)
                MenuRow(Icons.Filled.Photo, "Saved", "${state.savedPostIds.size} items", Color(0xFF9C27B0), onSaved)
                MenuRow(Icons.Filled.Group, "Groups", "See your communities", FacebookBlue, onSearch)
                MenuRow(Icons.Filled.Search, "Feeds", "See the most recent posts", Color(0xFF00A400), onSearch)
            }
        }
        item { Spacer(Modifier.height(8.dp)) }
        item {
            Column(modifier = Modifier.background(colors.surface)) {
                MenuRow(
                    Icons.Outlined.DarkMode,
                    if (state.darkTheme) "Light mode" else "Dark mode",
                    "Switch appearance",
                    Color(0xFF5C6BC0),
                    onToggleTheme,
                )
                MenuRow(Icons.Outlined.Settings, "Settings & privacy", null, colors.textSecondary, onSearch)
                MenuRow(Icons.Outlined.Help, "Help & support", null, colors.textSecondary, onSearch)
                MenuRow(Icons.Outlined.Logout, "Log out", null, LoveRed, onLogout)
            }
        }
    }
}

@Composable
private fun MenuRow(
    icon: ImageVector,
    title: String,
    subtitle: String?,
    tint: Color,
    onClick: () -> Unit,
) {
    val colors = LocalFacebookColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = title, tint = tint, modifier = Modifier.size(28.dp))
        Spacer(Modifier.width(14.dp))
        Column {
            Text(title, color = colors.text, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            if (subtitle != null) {
                Text(subtitle, color = colors.textSecondary, fontSize = 13.sp)
            }
        }
    }
    HorizontalDivider(color = colors.divider, thickness = 0.4.dp)
}

@Composable
fun SectionHeader(title: String) {
    val colors = LocalFacebookColors.current
    Text(
        title,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        color = colors.text,
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surface)
            .padding(horizontal = 16.dp, vertical = 12.dp),
    )
}
