package com.inxeoz.pythonquiz.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "quiz_progress")

class ProgressStore(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }
    private val completedIdsKey = stringSetPreferencesKey("completed_ids")
    private val seenIdsKey = stringSetPreferencesKey("seen_ids")
    private val searchHistoryKey = stringSetPreferencesKey("search_history")
    private val flaggedIdsKey = stringSetPreferencesKey("flagged_ids")
    private val dueReviewIdsKey = stringSetPreferencesKey("due_review_ids")
    private val activeSessionKey = stringPreferencesKey("active_session")
    private val quizHistoryKey = stringPreferencesKey("quiz_history")

    val completedIds: Flow<Set<Int>> = context.dataStore.data.map { prefs ->
        (prefs[completedIdsKey] ?: emptySet()).mapNotNull { it.toIntOrNull() }.toSet()
    }

    val seenIds: Flow<Set<Int>> = context.dataStore.data.map { prefs ->
        (prefs[seenIdsKey] ?: emptySet()).mapNotNull { it.toIntOrNull() }.toSet()
    }

    val flaggedIds: Flow<Set<Int>> = context.dataStore.data.map { prefs ->
        (prefs[flaggedIdsKey] ?: emptySet()).mapNotNull { it.toIntOrNull() }.toSet()
    }

    val dueReviewIds: Flow<Set<Int>> = context.dataStore.data.map { prefs ->
        (prefs[dueReviewIdsKey] ?: emptySet()).mapNotNull { it.toIntOrNull() }.toSet()
    }

    val quizHistory: Flow<List<SavedQuizAttempt>> = context.dataStore.data.map { prefs ->
        decodeQuizHistory(prefs[quizHistoryKey])
    }

    suspend fun toggleCompleted(id: Int) {
        context.dataStore.edit { prefs ->
            val current = prefs[completedIdsKey] ?: emptySet()
            val idStr = id.toString()
            prefs[completedIdsKey] = if (idStr in current) current - idStr else current + idStr
        }
    }

    suspend fun markAsSeen(id: Int) {
        context.dataStore.edit { prefs ->
            val current = prefs[seenIdsKey] ?: emptySet()
            prefs[seenIdsKey] = current + id.toString()
        }
    }

    suspend fun toggleFlagged(id: Int) {
        context.dataStore.edit { prefs ->
            val current = prefs[flaggedIdsKey] ?: emptySet()
            val idStr = id.toString()
            prefs[flaggedIdsKey] = if (idStr in current) current - idStr else current + idStr
        }
    }

    suspend fun isCompleted(id: Int): Boolean {
        return completedIds.first().contains(id)
    }

    val searchHistory: Flow<Set<String>> = context.dataStore.data.map { prefs ->
        prefs[searchHistoryKey] ?: emptySet()
    }

    // FIXME: Set trimming via `.drop(size - 20).toSet()` is non-deterministic —
    // HashSet iteration order is not guaranteed. Consider migrating search history
    // to a JSON-serialized list (most recent first) for deterministic eviction.
    suspend fun addSearchQuery(query: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[searchHistoryKey] ?: emptySet()
            val updated = (current + query)
            val trimmed = if (updated.size > 20) updated.drop(updated.size - 20).toSet() else updated
            prefs[searchHistoryKey] = trimmed
        }
    }

    suspend fun clearSearchHistory() {
        context.dataStore.edit { prefs ->
            prefs.remove(searchHistoryKey)
        }
    }

    suspend fun saveActiveSession(session: SavedQuizSession) {
        context.dataStore.edit { prefs ->
            prefs[activeSessionKey] = json.encodeToString(session)
        }
    }

    suspend fun getActiveSession(): SavedQuizSession? {
        val encoded = context.dataStore.data.first()[activeSessionKey] ?: return null
        return runCatching { json.decodeFromString<SavedQuizSession>(encoded) }.getOrNull()
    }

    suspend fun clearActiveSession() {
        context.dataStore.edit { prefs ->
            prefs.remove(activeSessionKey)
        }
    }

    suspend fun recordReviewResults(missedIds: Set<Int>, correctIds: Set<Int>) {
        context.dataStore.edit { prefs ->
            val current = prefs[dueReviewIdsKey] ?: emptySet()
            prefs[dueReviewIdsKey] = (current + missedIds.map { it.toString() } - correctIds.map { it.toString() }.toSet())
        }
    }

    suspend fun addQuizAttempt(attempt: SavedQuizAttempt) {
        context.dataStore.edit { prefs ->
            val current = decodeQuizHistory(prefs[quizHistoryKey])
            prefs[quizHistoryKey] = json.encodeToString((listOf(attempt) + current).take(100))
        }
    }

    suspend fun clearQuizHistory() {
        context.dataStore.edit { prefs ->
            prefs.remove(quizHistoryKey)
        }
    }

    suspend fun getCompletedIds(): Set<Int> {
        return completedIds.first()
    }

    suspend fun getSeenIds(): Set<Int> {
        return seenIds.first()
    }

    private fun decodeQuizHistory(encoded: String?): List<SavedQuizAttempt> {
        if (encoded == null) return emptyList()
        return runCatching { json.decodeFromString<List<SavedQuizAttempt>>(encoded) }.getOrDefault(emptyList())
    }
}

@Serializable
data class SavedQuizSession(
    val questionIds: List<Int> = emptyList(),
    val answers: Map<Int, String> = emptyMap(),
    val currentIndex: Int = 0,
    val submitted: Boolean = false,
    val optionOrders: Map<Int, List<String>> = emptyMap(),
    val timedMode: Boolean = false,
    val timeLimitMinutes: Int = 0,
    val remainingSeconds: Int = 0
)

@Serializable
data class SavedQuizAttempt(
    val id: Long,
    val completedAtMillis: Long,
    val questionIds: List<Int> = emptyList(),
    val answers: Map<Int, String> = emptyMap(),
    val correctCount: Int = 0,
    val totalCount: Int = 0,
    val timedMode: Boolean = false,
    val timeLimitMinutes: Int = 0
)
