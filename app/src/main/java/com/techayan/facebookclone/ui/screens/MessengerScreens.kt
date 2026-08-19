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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Videocam
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.techayan.facebookclone.data.Conversation
import com.techayan.facebookclone.data.FacebookState
import com.techayan.facebookclone.ui.components.UserAvatar
import com.techayan.facebookclone.ui.theme.FacebookBlue
import com.techayan.facebookclone.ui.theme.LocalFacebookColors
import com.techayan.facebookclone.ui.theme.MessengerBlue
import com.techayan.facebookclone.ui.theme.OnlineGreen

@Composable
fun MessengerScreen(
    state: FacebookState,
    onBack: () -> Unit,
    onOpenChat: (String) -> Unit,
) {
    val colors = LocalFacebookColors.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.surface)
            .statusBarsPadding(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = colors.text)
            }
            Text("Chats", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = colors.text, modifier = Modifier.weight(1f))
            RoundIcon(Icons.Filled.Edit, colors)
        }
        Box(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(colors.iconButton)
                .padding(horizontal = 14.dp, vertical = 10.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Search, contentDescription = null, tint = colors.textSecondary, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Search", color = colors.textSecondary)
            }
        }
        LazyColumn {
            items(state.conversations, key = { it.id }) { conversation ->
                ChatRow(state, conversation) { onOpenChat(conversation.id) }
            }
        }
    }
}

@Composable
private fun ChatRow(state: FacebookState, conversation: Conversation, onClick: () -> Unit) {
    val colors = LocalFacebookColors.current
    val user = state.user(conversation.userId)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        UserAvatar(user = user, size = 56.dp, showOnline = true)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                user.name,
                fontWeight = if (conversation.unread > 0) FontWeight.Bold else FontWeight.SemiBold,
                color = colors.text,
                fontSize = 16.sp,
            )
            Text(
                conversation.lastMessage.ifBlank { "Say hello" },
                color = if (conversation.unread > 0) colors.text else colors.textSecondary,
                fontWeight = if (conversation.unread > 0) FontWeight.SemiBold else FontWeight.Normal,
                fontSize = 14.sp,
                maxLines = 1,
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(conversation.time, color = colors.textSecondary, fontSize = 12.sp)
            if (conversation.unread > 0) {
                Spacer(Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(MessengerBlue),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("${conversation.unread}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ChatScreen(
    state: FacebookState,
    conversationId: String,
    onBack: () -> Unit,
    onSend: (String) -> Unit,
    onProfile: (String) -> Unit,
) {
    val colors = LocalFacebookColors.current
    val conversation = state.conversation(conversationId)
    val user = state.user(conversation.userId)
    var draft by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.surface)
            .statusBarsPadding()
            .imePadding()
            .navigationBarsPadding(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = colors.text)
            }
            UserAvatar(user = user, size = 36.dp, showOnline = true, onClick = { onProfile(user.id) })
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f).clickable { onProfile(user.id) }) {
                Text(user.name, fontWeight = FontWeight.Bold, color = colors.text)
                Text(
                    if (user.isOnline) "Active now" else "Active yesterday",
                    color = if (user.isOnline) OnlineGreen else colors.textSecondary,
                    fontSize = 12.sp,
                )
            }
            IconButton(onClick = {}) { Icon(Icons.Filled.Call, contentDescription = "Call", tint = FacebookBlue) }
            IconButton(onClick = {}) { Icon(Icons.Filled.Videocam, contentDescription = "Video", tint = FacebookBlue) }
            IconButton(onClick = { onProfile(user.id) }) { Icon(Icons.Filled.Info, contentDescription = "Info", tint = FacebookBlue) }
        }
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(colors.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(conversation.messages, key = { it.id }) { message ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (message.fromMe) Arrangement.End else Arrangement.Start,
                ) {
                    if (!message.fromMe) {
                        UserAvatar(user = user, size = 28.dp)
                        Spacer(Modifier.width(6.dp))
                    }
                    Box(
                        modifier = Modifier
                            .widthIn(max = 260.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(if (message.fromMe) MessengerBlue else colors.iconButton)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                    ) {
                        Text(
                            message.text,
                            color = if (message.fromMe) Color.White else colors.text,
                            fontSize = 15.sp,
                        )
                    }
                }
            }
        }
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(22.dp))
                    .background(colors.iconButton)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
            ) {
                if (draft.isEmpty()) {
                    Text("Aa", color = colors.textSecondary)
                }
                BasicTextField(
                    value = draft,
                    onValueChange = { draft = it },
                    textStyle = TextStyle(color = colors.text, fontSize = 16.sp),
                    cursorBrush = SolidColor(FacebookBlue),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Spacer(Modifier.width(8.dp))
            IconButton(
                onClick = {
                    onSend(draft)
                    draft = ""
                },
            ) {
                Icon(Icons.Filled.Send, contentDescription = "Send", tint = FacebookBlue)
            }
        }
    }
}

@Composable
private fun RoundIcon(icon: androidx.compose.ui.graphics.vector.ImageVector, colors: com.techayan.facebookclone.ui.theme.FacebookColors) {
    Box(
        modifier = Modifier
            .padding(end = 8.dp)
            .size(36.dp)
            .clip(CircleShape)
            .background(colors.iconButton),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = colors.text, modifier = Modifier.size(18.dp))
    }
}
