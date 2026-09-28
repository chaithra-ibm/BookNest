package com.example.booknest.data

import com.example.booknest.model.Book

object BookRepository {
    val sampleBooks = listOf(
        Book(
            id = "1",
            title = "Building Intelligent AI Agents",
            author = "Dr. Elena Vance",
            price = 34.99,
            rating = 4.9f,
            category = "Technology",
            description = "A practical guide to designing multi-agent workflows, autonomous reasoning, and tool calling with enterprise AI platforms.",
            coverEmoji = "🤖"
        ),
        Book(
            id = "2",
            title = "Cloud Native Architecture with IBM Cloud",
            author = "Marcus Sterling",
            price = 42.50,
            rating = 4.8f,
            category = "Technology",
            description = "Master microservices, Red Hat OpenShift, Kubernetes, and enterprise-grade resilience in production environments.",
            coverEmoji = "☁️"
        ),
        Book(
            id = "3",
            title = "The Quantum Mind",
            author = "Sarah Jenkins",
            price = 28.00,
            rating = 4.7f,
            category = "Science",
            description = "Explore foundational quantum computing principles, qubit entanglement, and the next frontier in computational algorithms.",
            coverEmoji = "⚛️"
        ),
        Book(
            id = "4",
            title = "Kotlin & Jetpack Compose in Action",
            author = "Alex Rivera",
            price = 39.95,
            rating = 4.9f,
            category = "Development",
            description = "Step-by-step modern Android UI development using declarative Jetpack Compose, state management, and Material 3 design.",
            coverEmoji = "📱"
        ),
        Book(
            id = "5",
            title = "Data Governance & Responsible AI",
            author = "Priya Sharma",
            price = 31.25,
            rating = 4.6f,
            category = "AI & Ethics",
            description = "Frameworks for compliance, model lineage, ethical AI guardrails, and secure data orchestration in enterprise environments.",
            coverEmoji = "🛡️"
        ),
        Book(
            id = "6",
            title = "Mastering Design Patterns & Clean Code",
            author = "David K. Wilson",
            price = 29.99,
            rating = 4.8f,
            category = "Development",
            description = "Deep dive into solid architectural design patterns, refactoring techniques, and robust testing paradigms.",
            coverEmoji = "📐"
        )
    )

    fun getRecommendationsForQuery(query: String): List<Book> {
        val lower = query.lowercase()
        return sampleBooks.filter {
            it.title.lowercase().contains(lower) ||
            it.category.lowercase().contains(lower) ||
            it.description.lowercase().contains(lower) ||
            it.author.lowercase().contains(lower)
        }.ifEmpty {
            sampleBooks.take(2)
        }
    }
}
