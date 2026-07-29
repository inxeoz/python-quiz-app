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
    private val dueReviewIdsKey = stringSetPreferencesKey("due_review_ids")
    private val activeSessionKey = stringPreferencesKey("active_session")

    val completedIds: Flow<Set<Int>> = context.dataStore.data.map { prefs ->
        (prefs[completedIdsKey] ?: emptySet()).mapNotNull { it.toIntOrNull() }.toSet()
    }

    val seenIds: Flow<Set<Int>> = context.dataStore.data.map { prefs ->
        (prefs[seenIdsKey] ?: emptySet()).mapNotNull { it.toIntOrNull() }.toSet()
    }

    val dueReviewIds: Flow<Set<Int>> = context.dataStore.data.map { prefs ->
        (prefs[dueReviewIdsKey] ?: emptySet()).mapNotNull { it.toIntOrNull() }.toSet()
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

    suspend fun markAsSeen(questionId: Int) {
        context.dataStore.edit { prefs ->
            val current = prefs[seenIdsKey] ?: emptySet()
            prefs[seenIdsKey] = current + questionId.toString()
        }
    }
}

@Serializable
data class SavedQuizSession(
    val questionIds: List<Int> = emptyList(),
    val answers: Map<Int, String> = emptyMap(),
    val currentIndex: Int = 0,
    val submitted: Boolean = false,
    val optionOrders: Map<Int, List<String>> = emptyMap()
)
