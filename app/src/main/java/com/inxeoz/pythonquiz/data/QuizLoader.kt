package com.inxeoz.pythonquiz.data

import android.content.Context
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString

object QuizLoader {
    private val json = Json { ignoreUnknownKeys = true }

    private val categoryFiles = mapOf(
        "python" to "python_quiz.json",
        "arts"    to "arts_quiz.json",
        "science" to "science_quiz.json",
        "tech"    to "tech_quiz.json",
        "sports"  to "sports_quiz.json"
    )

    fun loadQuestions(context: Context): List<Question> {
        val all = mutableListOf<Question>()
        for ((category, file) in categoryFiles) {
            try {
                val text = context.assets.open(file)
                    .bufferedReader().use { it.readText() }
                val questions = json.decodeFromString<List<Question>>(text)
                all.addAll(questions.map { it.copy(category = category) })
            } catch (_: Exception) {
                // file missing or malformed — skip gracefully
            }
        }
        return all
    }

    /** Categories with display name and total question count. */
    fun getCategories(allQuestions: List<Question>): List<CategoryInfo> {
        val byCategory = allQuestions.groupBy { it.category }
        return listOf(
            CategoryInfo("python", "Python", byCategory["python"]?.size ?: 0),
            CategoryInfo("arts",    "Arts",    byCategory["arts"]?.size    ?: 0),
            CategoryInfo("science", "Science", byCategory["science"]?.size ?: 0),
            CategoryInfo("tech",    "Tech",    byCategory["tech"]?.size    ?: 0),
            CategoryInfo("sports",  "Sports",  byCategory["sports"]?.size  ?: 0),
        ).filter { it.totalCount > 0 }
    }

    /** How many questions exist for [category] at [levels]. */
    fun countForCategoryLevels(allQuestions: List<Question>, category: String, levels: Set<Int>): Int {
        return allQuestions.count { it.category == category && it.level in levels }
    }

    fun questionsForCategory(allQuestions: List<Question>, category: String, levels: Set<Int>): List<Question> {
        return allQuestions.filter { it.category == category && it.level in levels }
    }

    data class CategoryInfo(
        val key: String,
        val displayName: String,
        val totalCount: Int
    )

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
