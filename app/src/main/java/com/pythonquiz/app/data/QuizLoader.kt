package com.pythonquiz.app.data

import android.content.Context
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString

object QuizLoader {
    private val json = Json { ignoreUnknownKeys = true }

    fun loadQuestions(context: Context): List<Question> {
        val text = context.assets.open("python_quiz.json")
            .bufferedReader().use { it.readText() }
        return json.decodeFromString<List<Question>>(text)
    }

    val levelNames = mapOf(
        0 to "Basic",
        1 to "Beginner",
        2 to "Intermediate",
        4 to "Advanced",
        5 to "Expert"
    )

    val levelColors = mapOf(
        0 to 0xFF6B7280.toInt(),
        1 to 0xFF3B82F6.toInt(),
        2 to 0xFF10B981.toInt(),
        4 to 0xFFF59E0B.toInt(),
        5 to 0xFFEF4444.toInt()
    )
}
