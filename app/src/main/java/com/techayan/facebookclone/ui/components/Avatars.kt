package com.techayan.facebookclone.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.techayan.facebookclone.data.User
import com.techayan.facebookclone.ui.theme.OnlineGreen

@Composable
fun UserAvatar(
    user: User,
    size: Dp,
    modifier: Modifier = Modifier,
    showOnline: Boolean = false,
    ring: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    Box(
        modifier = modifier
            .size(size)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        contentAlignment = Alignment.BottomEnd,
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .then(
                    if (ring) {
                        Modifier.border(2.5.dp, Color(0xFF0866FF), CircleShape)
                    } else {
                        Modifier
                    },
                )
                .background(Color(user.avatarColor)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = user.initials,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = (size.value * 0.34f).sp,
            )
        }
        if (showOnline && user.isOnline) {
            Box(
                modifier = Modifier
                    .size(size * 0.32f)
                    .clip(CircleShape)
                    .background(Color.White)
                    .padding(2.dp)
                    .clip(CircleShape)
                    .background(OnlineGreen),
            )
        }
    }
}

@Composable
fun GradientPhoto(
    start: Long,
    end: Long,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit = {},
) {
    Box(
        modifier = modifier.background(
            Brush.linearGradient(
                listOf(Color(start), Color(end)),
            ),
        ),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}
