package com.example.booknest.model

data class Book(
    val id: String,
    val title: String,
    val author: String,
    val price: Double,
    val rating: Float,
    val category: String,
    val description: String,
    val coverEmoji: String = "📚"
)

data class CartItem(
    val book: Book,
    val quantity: Int = 1
)

data class AgentChatMessage(
    val id: String,
    val sender: MessageSender,
    val text: String,
    val suggestedBookIds: List<String> = emptyList(),
    val actionSuggestion: String? = null
)

enum class MessageSender {
    USER,
    AGENT_BOB
}
