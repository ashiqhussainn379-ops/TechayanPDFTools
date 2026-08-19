package com.techayan.facebookclone.data

object SampleData {

    const val CURRENT_USER_ID = "u_ahmad"

    private val ahmad = User(
        id = CURRENT_USER_ID,
        name = "Ahmad Khan",
        firstName = "Ahmad",
        headline = "Software Engineer at Techayan",
        city = "Lahore, Pakistan",
        friendsCount = 486,
        avatarColor = 0xFF1877F2,
        coverColor = 0xFF0A3D91,
        isOnline = true,
        isCurrentUser = true,
    )

    private val people = listOf(
        ahmad,
        User("u_sara", "Sara Ali", "Sara", "UI Designer", "Karachi", 312, 0xFFE91E63, 0xFF880E4F, true),
        User("u_ali", "Ali Raza", "Ali", "Photographer", "Islamabad", 890, 0xFF00A400, 0xFF1B5E20),
        User("u_fatima", "Fatima Noor", "Fatima", "Medical Student", "Lahore", 540, 0xFF9C27B0, 0xFF4A148C, true),
        User("u_hassan", "Hassan Malik", "Hassan", "Cricket analyst", "Faisalabad", 1204, 0xFFFF6F00, 0xFFE65100),
        User("u_ayesha", "Ayesha Siddiqui", "Ayesha", "Travel blogger", "Multan", 2201, 0xFF00897B, 0xFF004D40, true),
        User("u_omar", "Omar Sheikh", "Omar", "Startup founder", "Karachi", 670, 0xFF3949AB, 0xFF1A237E),
        User("u_zainab", "Zainab Khan", "Zainab", "Teacher", "Peshawar", 198, 0xFFD81B60, 0xFF880E4F),
        User("u_usman", "Usman Tariq", "Usman", "Gym coach", "Rawalpindi", 433, 0xFF5D4037, 0xFF3E2723, true),
        User("u_hira", "Hira Qureshi", "Hira", "Chef & food creator", "Lahore", 980, 0xFFC2185B, 0xFF880E4F),
        User("u_bilal", "Bilal Ahmed", "Bilal", "Works at Jazz", "Sialkot", 155, 0xFF455A64, 0xFF263238),
        User("u_maryam", "Maryam Iqbal", "Maryam", "Architect", "Hyderabad", 276, 0xFF7B1FA2, 0xFF4A148C, true),
    )

    fun initial(idFactory: () -> String = { "id_${System.currentTimeMillis()}" }): FacebookState {
        return FacebookState(
            currentUserId = CURRENT_USER_ID,
            users = people,
            posts = posts(),
            stories = stories(),
            friendRequests = listOf(
                FriendRequest("fr1", "u_bilal", 12, "2d"),
                FriendRequest("fr2", "u_maryam", 8, "5d"),
                FriendRequest("fr3", "u_zainab", 3, "1w"),
            ),
            suggestions = listOf("u_omar", "u_usman", "u_hira"),
            marketplace = marketplace(),
            notifications = notifications(),
            conversations = conversations(),
            videos = videos(),
            idFactory = idFactory,
        )
    }

    private fun posts(): List<Post> = listOf(
        Post(
            id = "p1",
            authorId = "u_ayesha",
            time = "12m",
            text = "Hunza morning hit different ❄️ Mountains, chai, and no notifications.",
            visual = PostVisual.Photo(0xFF4FC3F7, 0xFF01579B, "Hunza Valley"),
            likes = 248,
            comments = listOf(
                Comment("c1", "u_sara", "Yaar next trip pe mujhe bhi le ke chalna!", "8m", 4),
                Comment("c2", CURRENT_USER_ID, "Scenes on another level 🔥", "4m"),
            ),
            shares = 18,
            feeling = "feeling blessed",
        ),
        Post(
            id = "p2",
            authorId = "u_ali",
            time = "1h",
            text = "New street portrait series from Anarkali. Which one should I print?",
            visual = PostVisual.Photo(0xFFFFB74D, 0xFFBF360C, "Anarkali Bazaar"),
            likes = 412,
            comments = listOf(
                Comment("c3", "u_fatima", "The lighting is insane. Print all of them.", "40m", 11),
            ),
            shares = 31,
        ),
        Post(
            id = "p3",
            authorId = CURRENT_USER_ID,
            time = "3h",
            text = "Shipped a Kotlin app today. Compose is actually fun when the UI starts coming together.",
            visual = PostVisual.Gradient(0xFF1877F2, 0xFF0A3D91, "💻", "Built with Kotlin"),
            likes = 96,
            comments = listOf(
                Comment("c4", "u_omar", "Let's ship the next one together.", "2h", 2),
            ),
            shares = 6,
            myReaction = Reaction.Like,
        ),
        Post(
            id = "p4",
            authorId = "u_hassan",
            time = "5h",
            text = "Pakistan vs anyone, I'm still watching like it's the 1992 final.",
            visual = PostVisual.Gradient(0xFF00A400, 0xFF1B5E20, "🏏", "Match day"),
            likes = 1304,
            comments = listOf(
                Comment("c5", "u_usman", "Captain's knock incoming inshallah", "4h", 22),
                Comment("c6", "u_ali", "Scoreboard anxiety is a lifestyle.", "3h"),
            ),
            shares = 88,
        ),
        Post(
            id = "p5",
            authorId = "u_hira",
            time = "Yesterday",
            text = "Beef biryani experiment: extra potatoes, extra love, extra neighbours knocking.",
            visual = PostVisual.Photo(0xFFFFCC80, 0xFFE65100, "Sunday Biryani"),
            likes = 673,
            comments = listOf(
                Comment("c7", "u_fatima", "Plate bhej do please 😋", "22h", 9),
            ),
            shares = 44,
            feeling = "feeling hungry",
        ),
        Post(
            id = "p6",
            authorId = "u_sara",
            time = "Yesterday",
            text = "Redesigned a social app header tonight. Tiny type, huge difference.",
            visual = PostVisual.Gradient(0xFFF48FB1, 0xFFAD1457, "🎨", "Design dump"),
            likes = 157,
            comments = emptyList(),
            shares = 9,
        ),
        Post(
            id = "p7",
            authorId = "u_fatima",
            time = "2d",
            text = "Ward round done. Coffee is no longer a drink, it's a personality.",
            likes = 89,
            comments = listOf(
                Comment("c8", CURRENT_USER_ID, "Proud of you, doc.", "2d", 3),
            ),
            shares = 2,
            feeling = "feeling tired",
        ),
    )

    private fun stories(): List<Story> = listOf(
        Story("s1", "u_sara", false, 0xFFE91E63, 0xFF4A148C, "Studio night"),
        Story("s2", "u_ali", false, 0xFFFFA726, 0xFFBF360C, "Golden hour"),
        Story("s3", "u_fatima", false, 0xFF7E57C2, 0xFF311B92, "Campus"),
        Story("s4", "u_ayesha", true, 0xFF4DD0E1, 0xFF006064, "North"),
        Story("s5", "u_hassan", false, 0xFF9CCC65, 0xFF33691E, "Practice"),
        Story("s6", "u_hira", false, 0xFFFF8A65, 0xFFBF360C, "Kitchen"),
    )

    private fun marketplace(): List<MarketplaceItem> = listOf(
        MarketplaceItem("m1", "iPhone 13 · 128GB", "Rs 145,000", "Lahore", 0xFF90A4AE, 0xFF37474F),
        MarketplaceItem("m2", "Honda CG 125", "Rs 185,000", "Rawalpindi", 0xFFEF5350, 0xFFB71C1C),
        MarketplaceItem("m3", "Study table + chair", "Rs 8,500", "Karachi", 0xFFA1887F, 0xFF4E342E),
        MarketplaceItem("m4", "PS5 Disc Edition", "Rs 155,000", "Islamabad", 0xFF42A5F5, 0xFF0D47A1),
        MarketplaceItem("m5", "MacBook Air M1", "Rs 175,000", "Lahore", 0xFFB0BEC5, 0xFF455A64),
        MarketplaceItem("m6", "Wooden dining set", "Rs 42,000", "Faisalabad", 0xFFD7CCC8, 0xFF5D4037),
    )

    private fun notifications(): List<NotificationItem> = listOf(
        NotificationItem("n1", "u_sara", "Sara Ali and 14 others reacted to your photo.", "2m", true, NotificationType.Like),
        NotificationItem("n2", "u_omar", "Omar Sheikh commented: \"Let's ship the next one together.\"", "1h", true, NotificationType.Comment),
        NotificationItem("n3", "u_fatima", "Fatima Noor sent you a friend suggestion.", "3h", true, NotificationType.Friend),
        NotificationItem("n4", "u_hassan", "Hassan Malik is live: Pakistan match discussion.", "5h", false, NotificationType.Group),
        NotificationItem("n5", "u_ayesha", "Ayesha Siddiqui has a birthday today. Write on her timeline.", "8h", false, NotificationType.Birthday),
        NotificationItem("n6", "u_ali", "Your memory with Ali Raza from 3 years ago.", "1d", false, NotificationType.Memory),
    )

    private fun conversations(): List<Conversation> = listOf(
        Conversation(
            id = "ch1",
            userId = "u_sara",
            lastMessage = "Yaar next trip pe mujhe bhi le ke chalna!",
            time = "8m",
            unread = 2,
            messages = listOf(
                ChatMessage("cm1", false, "Did you see Ayesha's Hunza pictures?", "20m"),
                ChatMessage("cm2", true, "Just liked them. Looks unreal.", "15m"),
                ChatMessage("cm3", false, "Yaar next trip pe mujhe bhi le ke chalna!", "8m"),
            ),
        ),
        Conversation(
            id = "ch2",
            userId = "u_omar",
            lastMessage = "You: Let's jump on a call tomorrow",
            time = "2h",
            unread = 0,
            messages = listOf(
                ChatMessage("cm4", false, "Loved the Kotlin post.", "3h"),
                ChatMessage("cm5", true, "Let's jump on a call tomorrow", "2h"),
            ),
        ),
        Conversation(
            id = "ch3",
            userId = "u_fatima",
            lastMessage = "Coffee after the shift?",
            time = "Yesterday",
            unread = 1,
            messages = listOf(
                ChatMessage("cm6", false, "Ward round was chaos today.", "Yesterday"),
                ChatMessage("cm7", false, "Coffee after the shift?", "Yesterday"),
            ),
        ),
        Conversation(
            id = "ch4",
            userId = "u_ali",
            lastMessage = "You: Print the Anarkali one",
            time = "Yesterday",
            unread = 0,
            messages = listOf(
                ChatMessage("cm8", false, "Need a second opinion on the portraits.", "Yesterday"),
                ChatMessage("cm9", true, "Print the Anarkali one", "Yesterday"),
            ),
        ),
        Conversation(
            id = "ch5",
            userId = "u_hassan",
            lastMessage = "Match at 7. Don't be late.",
            time = "2d",
            unread = 0,
            messages = listOf(
                ChatMessage("cm10", false, "Match at 7. Don't be late.", "2d"),
            ),
        ),
    )

    private fun videos(): List<WatchVideo> = listOf(
        WatchVideo("v1", "u_ayesha", "Sunrise drive to Attabad Lake", "1.2M views", "3h", 0xFF4FC3F7, 0xFF01579B),
        WatchVideo("v2", "u_hassan", "Best Pakistan knocks of the decade", "860K views", "8h", 0xFF66BB6A, 0xFF1B5E20),
        WatchVideo("v3", "u_hira", "Street food in Lahore at 2am", "540K views", "1d", 0xFFFFB74D, 0xFFE65100),
        WatchVideo("v4", "u_ali", "How I edit portraits on my phone", "210K views", "2d", 0xFF90CAF9, 0xFF1565C0),
    )
}
