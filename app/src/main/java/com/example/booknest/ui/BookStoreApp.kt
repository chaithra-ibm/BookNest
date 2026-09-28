package com.example.booknest.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.booknest.data.BookRepository
import com.example.booknest.model.AgentChatMessage
import com.example.booknest.model.Book
import com.example.booknest.model.CartItem
import com.example.booknest.model.MessageSender
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookStoreApp() {
    var selectedTab by remember { mutableIntStateOf(0) }
    var cartItems by remember { mutableStateOf<List<CartItem>>(emptyList()) }
    var selectedCategory by remember { mutableStateOf("All") }
    var isAgentSheetOpen by remember { mutableStateOf(false) }

    // Chat messages for IBM BOB agent
    var chatMessages by remember {
        mutableStateOf(
            listOf(
                AgentChatMessage(
                    id = UUID.randomUUID().toString(),
                    sender = MessageSender.AGENT_BOB,
                    text = "Hello! I am your AI Book Assistant powered by IBM BOB agentic workflows. How can I help you find your next great read?",
                    suggestedBookIds = listOf("1", "2")
                )
            )
        )
    }

    val categories = listOf("All", "Technology", "Science", "Development", "AI & Ethics")
    val books = BookRepository.sampleBooks.filter {
        selectedCategory == "All" || it.category == selectedCategory
    }

    val addToCart = { book: Book ->
        val existing = cartItems.find { it.book.id == book.id }
        if (existing != null) {
            cartItems = cartItems.map {
                if (it.book.id == book.id) it.copy(quantity = it.quantity + 1) else it
            }
        } else {
            cartItems = cartItems + CartItem(book = book, quantity = 1)
        }
    }

    val removeFromCart = { bookId: String ->
        cartItems = cartItems.filter { it.book.id != bookId }
    }

    val updateCartQuantity = { bookId: String, delta: Int ->
        cartItems = cartItems.mapNotNull {
            if (it.book.id == bookId) {
                val newQty = it.quantity + delta
                if (newQty > 0) it.copy(quantity = newQty) else null
            } else it
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "BookNest",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Text(
                                text = "IBM BOB Powered",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Text("📖", fontSize = 20.sp) },
                    label = { Text("Catalog") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = {
                        BadgedBox(badge = {
                            if (cartItems.isNotEmpty()) {
                                Badge {
                                    Text(cartItems.sumOf { it.quantity }.toString())
                                }
                            }
                        }) {
                            Text("🛒", fontSize = 20.sp)
                        }
                    },
                    label = { Text("Cart") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = {
                        selectedTab = 2
                        isAgentSheetOpen = true
                    },
                    icon = { Text("🤖", fontSize = 20.sp) },
                    label = { Text("Bob AI") }
                )
            }
        },
        floatingActionButton = {
            if (selectedTab != 2) {
                FloatingActionButton(
                    onClick = { isAgentSheetOpen = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🤖", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Ask BOB", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> CatalogScreen(
                    categories = categories,
                    selectedCategory = selectedCategory,
                    onCategorySelect = { selectedCategory = it },
                    books = books,
                    onAddToCart = addToCart
                )
                1 -> CartScreen(
                    cartItems = cartItems,
                    onUpdateQuantity = updateCartQuantity,
                    onRemove = removeFromCart,
                    onCheckout = {
                        cartItems = emptyList()
                    }
                )
                2 -> AgentChatScreen(
                    messages = chatMessages,
                    onSendMessage = { query ->
                        val userMsg = AgentChatMessage(
                            id = UUID.randomUUID().toString(),
                            sender = MessageSender.USER,
                            text = query
                        )
                        val recommended = BookRepository.getRecommendationsForQuery(query)
                        val agentResponse = AgentChatMessage(
                            id = UUID.randomUUID().toString(),
                            sender = MessageSender.AGENT_BOB,
                            text = "IBM BOB analyzed your query \"$query\" using semantic book matching. Here are tailored recommendations:",
                            suggestedBookIds = recommended.map { it.id }
                        )
                        chatMessages = chatMessages + listOf(userMsg, agentResponse)
                    },
                    onAddToCart = addToCart
                )
            }

            if (isAgentSheetOpen && selectedTab != 2) {
                ModalBottomSheet(
                    onDismissRequest = { isAgentSheetOpen = false },
                    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                ) {
                    Box(modifier = Modifier.fillMaxHeight(0.85f)) {
                        AgentChatScreen(
                            messages = chatMessages,
                            onSendMessage = { query ->
                                val userMsg = AgentChatMessage(
                                    id = UUID.randomUUID().toString(),
                                    sender = MessageSender.USER,
                                    text = query
                                )
                                val recommended = BookRepository.getRecommendationsForQuery(query)
                                val agentResponse = AgentChatMessage(
                                    id = UUID.randomUUID().toString(),
                                    sender = MessageSender.AGENT_BOB,
                                    text = "IBM BOB agent matched the following titles for you:",
                                    suggestedBookIds = recommended.map { it.id }
                                )
                                chatMessages = chatMessages + listOf(userMsg, agentResponse)
                            },
                            onAddToCart = addToCart
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CatalogScreen(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelect: (String) -> Unit,
    books: List<Book>,
    onAddToCart: (Book) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "✨ Intelligent Recommendations",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Driven by IBM BOB agentic reasoning to match books to your learning path and preferences.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                    )
                }
            }
        }

        item {
            Text(
                text = "Categories",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { category ->
                    FilterChip(
                        selected = category == selectedCategory,
                        onClick = { onCategorySelect(category) },
                        label = { Text(category) }
                    )
                }
            }
        }

        item {
            Text(
                text = "Featured Books (${books.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        items(books, key = { it.id }) { book ->
            BookItemCard(book = book, onAddToCart = { onAddToCart(book) })
        }
    }
}

@Composable
fun BookItemCard(
    book: Book,
    onAddToCart: () -> Unit
) {
    var addedFeedback by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(72.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = book.coverEmoji, fontSize = 36.sp)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = book.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "by ${book.author}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("⭐ ${book.rating}", style = MaterialTheme.typography.labelSmall)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "$${String.format("%.2f", book.price)}",
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = {
                    onAddToCart()
                    addedFeedback = true
                },
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(if (addedFeedback) "Added ✓" else "Add")
            }
        }
    }
}

@Composable
fun CartScreen(
    cartItems: List<CartItem>,
    onUpdateQuantity: (String, Int) -> Unit,
    onRemove: (String) -> Unit,
    onCheckout: () -> Unit
) {
    val total = cartItems.sumOf { it.book.price * it.quantity }
    var orderPlaced by remember { mutableStateOf(false) }

    if (orderPlaced) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("🎉", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Order Placed Successfully!",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "IBM BOB agent has verified and routed your fulfillment request.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { orderPlaced = false }) {
                        Text("Continue Shopping")
                    }
                }
            }
        }
        return
    }

    if (cartItems.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🛒", fontSize = 54.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Your Cart is Empty",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Ask IBM BOB or browse catalog to add books.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Shopping Cart (${cartItems.size} items)",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(cartItems, key = { it.book.id }) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(item.book.coverEmoji, fontSize = 28.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                item.book.title,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                "$${String.format("%.2f", item.book.price)} each",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { onUpdateQuantity(item.book.id, -1) }) {
                                Text("-", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            }
                            Text("${item.quantity}", fontWeight = FontWeight.Bold)
                            IconButton(onClick = { onUpdateQuantity(item.book.id, 1) }) {
                                Text("+", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Total Amount:", style = MaterialTheme.typography.titleMedium)
            Text(
                "$${String.format("%.2f", total)}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                orderPlaced = true
                onCheckout()
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Proceed to Agentic Checkout", modifier = Modifier.padding(8.dp))
        }
    }
}

@Composable
fun AgentChatScreen(
    messages: List<AgentChatMessage>,
    onSendMessage: (String) -> Unit,
    onAddToCart: (Book) -> Unit
) {
    var inputText by remember { mutableStateOf("") }
    val quickPrompts = listOf(
        "Recommend AI and Machine Learning books",
        "Best books on Cloud and Architecture",
        "Android development with Jetpack Compose",
        "Books on AI Ethics & Governance"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Agent Header Banner
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("🤖", fontSize = 18.sp)
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("IBM BOB Assistant", fontWeight = FontWeight.Bold)
                    Text("Agentic Book Recommendation & Discovery Engine", style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Quick prompts
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            items(quickPrompts) { prompt ->
                SuggestionChip(
                    onClick = {
                        onSendMessage(prompt)
                    },
                    label = { Text(prompt, style = MaterialTheme.typography.labelSmall) }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Message List
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                ChatBubble(message = msg, onAddToCart = onAddToCart)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Input row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = { Text("Ask BOB for book advice...") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                singleLine = true
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    if (inputText.isNotBlank()) {
                        val text = inputText
                        inputText = ""
                        onSendMessage(text)
                    }
                },
                shape = CircleShape,
                modifier = Modifier.size(50.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text("➔", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ChatBubble(
    message: AgentChatMessage,
    onAddToCart: (Book) -> Unit
) {
    val isUser = message.sender == MessageSender.USER
    val alignment = if (isUser) Alignment.End else Alignment.Start
    val bgColor = if (isUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
    val textColor = if (isUser) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 2.dp,
                bottomEnd = if (isUser) 2.dp else 16.dp
            ),
            color = bgColor,
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = if (isUser) "You" else "IBM BOB Agent",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = textColor.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor
                )
            }
        }

        if (message.suggestedBookIds.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            val recommendedBooks = BookRepository.sampleBooks.filter {
                message.suggestedBookIds.contains(it.id)
            }
            Column(
                modifier = Modifier.padding(start = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                recommendedBooks.forEach { book ->
                    Card(
                        modifier = Modifier.widthIn(max = 280.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(book.coverEmoji, fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    book.title,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    "$${String.format("%.2f", book.price)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            FilledTonalButton(
                                onClick = { onAddToCart(book) },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text("+ Cart", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        }
    }
}
