package com.pythonquiz.app.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "quiz_progress")

class ProgressStore(private val context: Context) {

    private val completedIdsKey = stringSetPreferencesKey("completed_ids")
    private val seenIdsKey = stringSetPreferencesKey("seen_ids")
    private val searchHistoryKey = stringSetPreferencesKey("search_history")

    val completedIds: Flow<Set<Int>> = context.dataStore.data.map { prefs ->
        (prefs[completedIdsKey] ?: emptySet()).mapNotNull { it.toIntOrNull() }.toSet()
    }

    val seenIds: Flow<Set<Int>> = context.dataStore.data.map { prefs ->
        (prefs[seenIdsKey] ?: emptySet()).mapNotNull { it.toIntOrNull() }.toSet()
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

    suspend fun isCompleted(id: Int): Boolean {
        return completedIds.first().contains(id)
    }

    val searchHistory: Flow<Set<String>> = context.dataStore.data.map { prefs ->
        prefs[searchHistoryKey] ?: emptySet()
    }

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

    suspend fun getCompletedIds(): Set<Int> {
        return completedIds.first()
    }

    suspend fun getSeenIds(): Set<Int> {
        return seenIds.first()
    }
}
