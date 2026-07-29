package com.inxeoz.pythonquiz.data

import android.content.Context
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString

object QuizLoader {
    private val json = Json { ignoreUnknownKeys = true }

    private const val QUIZ_FILE = "python_quiz.json"

    fun loadQuestions(context: Context): List<Question> {
        return try {
            val text = context.assets.open(QUIZ_FILE)
                .bufferedReader().use { it.readText() }
            json.decodeFromString<List<Question>>(text)
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun questionsForLevels(allQuestions: List<Question>, levels: Set<Int>): List<Question> {
        return allQuestions.filter { it.level in levels }
    }

    fun countForLevels(allQuestions: List<Question>, levels: Set<Int>): Int {
        return allQuestions.count { it.level in levels }
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

    fun levelName(level: Int): String = levelNames[level] ?: "Level $level"

    fun levelColorValue(level: Int): Int = levelColors[level] ?: levelColors.getValue(0)

    fun optionLabel(index: Int): String {
        var value = index
        val builder = StringBuilder()
        do {
            builder.insert(0, ('A'.code + (value % 26)).toChar())
            value = value / 26 - 1
        } while (value >= 0)
        return builder.toString()
    }
}
