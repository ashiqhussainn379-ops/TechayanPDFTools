package com.techayan.facebookclone.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.OndemandVideo
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.outlined.ChatBubble
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.OndemandVideo
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.techayan.facebookclone.data.FacebookState
import com.techayan.facebookclone.data.MainTab
import com.techayan.facebookclone.data.SampleData
import com.techayan.facebookclone.ui.screens.ChatScreen
import com.techayan.facebookclone.ui.screens.CommentsScreen
import com.techayan.facebookclone.ui.screens.CreatePostScreen
import com.techayan.facebookclone.ui.screens.FriendsScreen
import com.techayan.facebookclone.ui.screens.HomeScreen
import com.techayan.facebookclone.ui.screens.LoginScreen
import com.techayan.facebookclone.ui.screens.MarketplaceScreen
import com.techayan.facebookclone.ui.screens.MenuScreen
import com.techayan.facebookclone.ui.screens.MessengerScreen
import com.techayan.facebookclone.ui.screens.NotificationsScreen
import com.techayan.facebookclone.ui.screens.ProfileScreen
import com.techayan.facebookclone.ui.screens.SearchScreen
import com.techayan.facebookclone.ui.screens.StoryViewerScreen
import com.techayan.facebookclone.ui.screens.WatchScreen
import com.techayan.facebookclone.ui.theme.FacebookBlue
import com.techayan.facebookclone.ui.theme.FacebookTheme
import com.techayan.facebookclone.ui.theme.LocalFacebookColors
import com.techayan.facebookclone.ui.theme.LoveRed

sealed interface Route {
    data object Login : Route
    data object Main : Route
    data object Search : Route
    data object CreatePost : Route
    data object Messenger : Route
    data class Chat(val conversationId: String) : Route
    data class Comments(val postId: String) : Route
    data class Story(val storyId: String) : Route
    data class Profile(val userId: String) : Route
}

@Composable
fun FacebookApp() {
    var state by remember { mutableStateOf(SampleData.initial()) }
    var stack by remember { mutableStateOf(listOf<Route>(Route.Login)) }
    val current = stack.last()

    fun push(route: Route) {
        stack = stack + route
    }

    fun pop() {
        if (stack.size > 1) stack = stack.dropLast(1)
    }

    fun replace(route: Route) {
        stack = listOf(route)
    }

    FacebookTheme(darkTheme = state.darkTheme) {
        if (stack.size > 1) {
            BackHandler { pop() }
        }
        when (val route = current) {
            Route.Login -> LoginScreen(onLoggedIn = { replace(Route.Main) })
            Route.Main -> MainShell(
                state = state,
                onState = { state = it },
                onSearch = { push(Route.Search) },
                onCreatePost = { push(Route.CreatePost) },
                onMessenger = { push(Route.Messenger) },
                onStory = {
                    state = state.markStoryViewed(it)
                    push(Route.Story(it))
                },
                onProfile = { push(Route.Profile(it)) },
                onComments = { push(Route.Comments(it)) },
                onLogout = { replace(Route.Login) },
            )
            Route.Search -> SearchScreen(
                state = state,
                onBack = ::pop,
                onProfile = { push(Route.Profile(it)) },
            )
            Route.CreatePost -> CreatePostScreen(
                state = state,
                onBack = ::pop,
                onPost = { text, feeling, visual ->
                    state = state.createPost(text, feeling, visual)
                    pop()
                },
            )
            Route.Messenger -> MessengerScreen(
                state = state,
                onBack = ::pop,
                onOpenChat = { id ->
                    state = state.openConversation(id)
                    push(Route.Chat(id))
                },
            )
            is Route.Chat -> ChatScreen(
                state = state,
                conversationId = route.conversationId,
                onBack = ::pop,
                onSend = { state = state.sendMessage(route.conversationId, it) },
                onProfile = { push(Route.Profile(it)) },
            )
            is Route.Comments -> CommentsScreen(
                state = state,
                postId = route.postId,
                onBack = ::pop,
                onSend = { state = state.addComment(route.postId, it) },
                onProfile = { push(Route.Profile(it)) },
            )
            is Route.Story -> StoryViewerScreen(
                state = state,
                storyId = route.storyId,
                onBack = ::pop,
                onProfile = { push(Route.Profile(it)) },
            )
            is Route.Profile -> ProfileScreen(
                state = state,
                userId = route.userId,
                onBack = ::pop,
                onMessage = {
                    val (next, id) = state.startOrOpenChat(route.userId)
                    state = next
                    push(Route.Chat(id))
                },
                onToggleLike = { state = state.toggleLike(it) },
                onReact = { id, reaction -> state = state.setReaction(id, reaction) },
                onComments = { push(Route.Comments(it)) },
                onShare = { state = state.sharePost(it) },
                onSave = { state = state.toggleSave(it) },
            )
        }
    }
}

@Composable
private fun MainShell(
    state: FacebookState,
    onState: (FacebookState) -> Unit,
    onSearch: () -> Unit,
    onCreatePost: () -> Unit,
    onMessenger: () -> Unit,
    onStory: (String) -> Unit,
    onProfile: (String) -> Unit,
    onComments: (String) -> Unit,
    onLogout: () -> Unit,
) {
    var tab by rememberSaveable { mutableStateOf(MainTab.Home) }
    val colors = LocalFacebookColors.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding(),
    ) {
        FacebookTopBar(
            unreadMessages = state.unreadMessageCount(),
            onCreate = onCreatePost,
            onSearch = onSearch,
            onMessenger = onMessenger,
        )
        TabBar(
            selected = tab,
            unreadNotifications = state.unreadNotificationCount(),
            onSelect = { selected ->
                tab = selected
                if (selected == MainTab.Notifications) {
                    onState(state.markNotificationsRead())
                }
            },
        )
        Box(modifier = Modifier.weight(1f)) {
            when (tab) {
                MainTab.Home -> HomeScreen(
                    state = state,
                    onCreatePost = onCreatePost,
                    onStory = { onStory(it.id) },
                    onProfile = onProfile,
                    onToggleLike = { onState(state.toggleLike(it)) },
                    onReact = { id, reaction -> onState(state.setReaction(id, reaction)) },
                    onComments = onComments,
                    onShare = { onState(state.sharePost(it)) },
                    onSave = { onState(state.toggleSave(it)) },
                )
                MainTab.Friends -> FriendsScreen(
                    state = state,
                    onProfile = onProfile,
                    onAccept = { onState(state.acceptRequest(it)) },
                    onDecline = { onState(state.declineRequest(it)) },
                    onAdd = { onState(state.addSuggestedFriend(it)) },
                )
                MainTab.Watch -> WatchScreen(state, onProfile)
                MainTab.Marketplace -> MarketplaceScreen(state)
                MainTab.Notifications -> NotificationsScreen(state, onProfile)
                MainTab.Menu -> MenuScreen(
                    state = state,
                    onProfile = { onProfile(state.currentUserId) },
                    onToggleTheme = { onState(state.toggleTheme()) },
                    onLogout = onLogout,
                    onSearch = onSearch,
                    onSaved = onSearch,
                )
            }
        }
    }
}

@Composable
private fun FacebookTopBar(
    unreadMessages: Int,
    onCreate: () -> Unit,
    onSearch: () -> Unit,
    onMessenger: () -> Unit,
) {
    val colors = LocalFacebookColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surface)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "facebook",
            color = if (colors.isDark) Color.White else FacebookBlue,
            fontWeight = FontWeight.Black,
            fontSize = 28.sp,
            letterSpacing = (-0.8).sp,
            modifier = Modifier.weight(1f),
        )
        RoundAction(Icons.Filled.Add, "Create", colors.iconButton, colors.text, onCreate)
        Spacer(Modifier.width(8.dp))
        RoundAction(Icons.Filled.Search, "Search", colors.iconButton, colors.text, onSearch)
        Spacer(Modifier.width(8.dp))
        Box {
            RoundAction(Icons.Outlined.ChatBubble, "Messenger", colors.iconButton, colors.text, onMessenger)
            if (unreadMessages > 0) {
                Badge(unreadMessages, Modifier.align(Alignment.TopEnd))
            }
        }
    }
}

@Composable
private fun RoundAction(
    icon: ImageVector,
    label: String,
    background: Color,
    tint: Color,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(background)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun TabBar(
    selected: MainTab,
    unreadNotifications: Int,
    onSelect: (MainTab) -> Unit,
) {
    val colors = LocalFacebookColors.current
    Column(modifier = Modifier.background(colors.surface)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            TabIcon(MainTab.Home, selected, Icons.Filled.Home, Icons.Outlined.Home, onSelect, Modifier.weight(1f))
            TabIcon(MainTab.Friends, selected, Icons.Filled.People, Icons.Outlined.People, onSelect, Modifier.weight(1f))
            TabIcon(MainTab.Watch, selected, Icons.Filled.OndemandVideo, Icons.Outlined.OndemandVideo, onSelect, Modifier.weight(1f))
            TabIcon(MainTab.Marketplace, selected, Icons.Filled.Storefront, Icons.Outlined.Storefront, onSelect, Modifier.weight(1f))
            Box(modifier = Modifier.weight(1f)) {
                TabIcon(MainTab.Notifications, selected, Icons.Filled.Notifications, Icons.Outlined.Notifications, onSelect, Modifier.fillMaxWidth())
                if (unreadNotifications > 0) {
                    Badge(unreadNotifications, Modifier.align(Alignment.TopEnd).padding(top = 4.dp, end = 18.dp))
                }
            }
            TabIcon(MainTab.Menu, selected, Icons.Filled.Menu, Icons.Filled.Menu, onSelect, Modifier.weight(1f))
        }
        HorizontalDivider(color = colors.divider, thickness = 0.6.dp)
    }
}

@Composable
private fun TabIcon(
    tab: MainTab,
    selected: MainTab,
    selectedIcon: ImageVector,
    unselectedIcon: ImageVector,
    onSelect: (MainTab) -> Unit,
    modifier: Modifier,
) {
    val active = tab == selected
    Column(
        modifier = modifier
            .clickable { onSelect(tab) }
            .padding(top = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = if (active) selectedIcon else unselectedIcon,
            contentDescription = tab.name,
            tint = if (active) FacebookBlue else LocalFacebookColors.current.textSecondary,
            modifier = Modifier.size(26.dp),
        )
        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .background(if (active) FacebookBlue else Color.Transparent),
        )
    }
}

@Composable
private fun Badge(count: Int, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(16.dp)
            .clip(CircleShape)
            .background(LoveRed),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            if (count > 9) "9+" else "$count",
            color = Color.White,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}
