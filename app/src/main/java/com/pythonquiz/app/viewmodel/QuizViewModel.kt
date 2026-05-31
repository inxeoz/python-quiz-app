package com.pythonquiz.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pythonquiz.app.data.ProgressStore
import com.pythonquiz.app.data.Question
import com.pythonquiz.app.data.QuizLoader
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class QuizSession(
    val questions: List<Question> = emptyList(),
    val answers: Map<Int, String> = emptyMap(),
    val flagged: Set<Int> = emptySet(),
    val submitted: Boolean = false,
    val currentIndex: Int = 0
)

data class QuizUiState(
    val questions: List<Question> = emptyList(),
    val loading: Boolean = true,
    val session: QuizSession = QuizSession(),
    val currentScreen: Screen = Screen.Setup
)

enum class Screen { Setup, Quiz, Report, Browse }
enum class PracticeScope { All, NewOnly, Incomplete, Completed }

class QuizViewModel(application: Application) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(QuizUiState())
    val state: StateFlow<QuizUiState> = _state.asStateFlow()

    val progressStore = ProgressStore(application)
    val completedIds: StateFlow<Set<Int>> = progressStore.completedIds
        .stateIn(viewModelScope, SharingStarted.Lazily, emptySet())
    val seenIds: StateFlow<Set<Int>> = progressStore.seenIds
        .stateIn(viewModelScope, SharingStarted.Lazily, emptySet())
    val searchHistory: StateFlow<Set<String>> = progressStore.searchHistory
        .stateIn(viewModelScope, SharingStarted.Lazily, emptySet())

    var allQuestions: List<Question> = emptyList()
        private set

    init {
        loadQuestions()
    }

    private fun loadQuestions() {
        viewModelScope.launch {
            try {
                allQuestions = QuizLoader.loadQuestions(getApplication())
                _state.value = _state.value.copy(loading = false)
            } catch (e: Exception) {
                _state.value = _state.value.copy(loading = false)
            }
        }
    }

    fun startQuiz(
        selectedLevels: Set<Int>,
        count: Int,
        shuffle: Boolean,
        scope: PracticeScope = PracticeScope.All
    ) {
        val completed = completedIds.value
        val seen = seenIds.value
        val pool = allQuestions.filter {
            it.level in selectedLevels && when (scope) {
                PracticeScope.All -> true
                PracticeScope.NewOnly -> it.id !in seen
                PracticeScope.Incomplete -> it.id !in completed
                PracticeScope.Completed -> it.id in completed
            }
        }
        val qs = if (shuffle) pool.shuffled() else pool
        val selected = if (count >= qs.size) qs else qs.take(count)

        _state.value = _state.value.copy(
            session = QuizSession(questions = selected),
            currentScreen = Screen.Quiz
        )
    }

    fun startQuizFromQuestions(questions: List<Question>, shuffle: Boolean = false) {
        val selected = if (shuffle) questions.shuffled() else questions
        _state.value = _state.value.copy(
            session = QuizSession(questions = selected),
            currentScreen = Screen.Quiz
        )
    }

    fun selectAnswer(questionIndex: Int, option: String) {
        val s = _state.value.session
        if (s.submitted) return
        val newAnswers = s.answers + (questionIndex to option)
        _state.value = _state.value.copy(
            session = s.copy(answers = newAnswers)
        )
    }

    fun navigateQuestion(delta: Int) {
        val s = _state.value.session
        val next = (s.currentIndex + delta).coerceIn(0, s.questions.lastIndex)
        _state.value = _state.value.copy(
            session = s.copy(currentIndex = next)
        )
    }

    fun jumpToQuestion(index: Int) {
        _state.value = _state.value.copy(
            session = _state.value.session.copy(currentIndex = index)
        )
    }

    fun toggleFlag(index: Int) {
        val s = _state.value.session
        val newFlagged = if (index in s.flagged) s.flagged - index else s.flagged + index
        _state.value = _state.value.copy(
            session = s.copy(flagged = newFlagged)
        )
    }

    fun submitQuiz() {
        _state.value = _state.value.copy(
            session = _state.value.session.copy(submitted = true),
            currentScreen = Screen.Report
        )
    }

    fun goToSetup() {
        _state.value = _state.value.copy(
            session = QuizSession(),
            currentScreen = Screen.Setup
        )
    }

    fun goToBrowse() {
        _state.value = _state.value.copy(currentScreen = Screen.Browse)
    }

    fun addSearchQuery(query: String) {
        val trimmed = query.trim()
        if (trimmed.length < 2) return
        viewModelScope.launch {
            progressStore.addSearchQuery(trimmed)
        }
    }

    fun clearSearchHistory() {
        viewModelScope.launch {
            progressStore.clearSearchHistory()
        }
    }

    fun toggleCompleted(id: Int) {
        viewModelScope.launch {
            progressStore.toggleCompleted(id)
        }
    }

    fun markAsSeen(id: Int) {
        viewModelScope.launch {
            progressStore.markAsSeen(id)
        }
    }
}
