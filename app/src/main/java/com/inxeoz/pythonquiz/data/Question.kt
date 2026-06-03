package com.inxeoz.pythonquiz.data

import kotlinx.serialization.Serializable

@Serializable
data class Question(
    val id: Int,
    val level: Int,
    val topic: String,
    val question: String,
    val options: List<String>,
    val answer: String,
    val explanation: String,
    val category: String = ""   // assigned by QuizLoader from source file
)
