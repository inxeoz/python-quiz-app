package com.inxeoz.pythonquiz.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.inxeoz.pythonquiz.data.ProgressStore
import com.inxeoz.pythonquiz.data.Question
import com.inxeoz.pythonquiz.data.QuizLoader
import com.inxeoz.pythonquiz.data.SavedQuizSession
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class QuizSession(
    val questions: List<Question> = emptyList(),
    val answers: Map<Int, String> = emptyMap(),
    val optionOrders: Map<Int, List<String>> = emptyMap(),
    val submitted: Boolean = false,
    val currentIndex: Int = 0,
    val revealedAnswers: Set<Int> = emptySet()
)

data class QuizUiState(
    val questions: List<Question> = emptyList(),
    val loading: Boolean = true,
    val errorMessage: String? = null,
    val session: QuizSession = QuizSession(),
    val currentScreen: Screen = Screen.Setup,
    val hasSavedSession: Boolean = false
)

enum class Screen { Setup, Quiz, Report }

class QuizViewModel(application: Application) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(QuizUiState())
    val state: StateFlow<QuizUiState> = _state.asStateFlow()

    val progressStore = ProgressStore(application)
    val completedIds: StateFlow<Set<Int>> = progressStore.completedIds
        .stateIn(viewModelScope, SharingStarted.Lazily, emptySet())
    val seenIds: StateFlow<Set<Int>> = progressStore.seenIds
        .stateIn(viewModelScope, SharingStarted.Lazily, emptySet())
    val dueReviewIds: StateFlow<Set<Int>> = progressStore.dueReviewIds
        .stateIn(viewModelScope, SharingStarted.Lazily, emptySet())
    val visitCounts: StateFlow<Map<Int, Int>> = progressStore.visitCounts
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyMap())

    var allQuestions: List<Question> = emptyList()
        private set

    private var autoAdvanceJob: Job? = null

    fun countForLevels(levels: Set<Int>, visitedOnly: Boolean = false): Int {
        return if (visitedOnly && levels.isNotEmpty()) {
            val visited = seenIds.value
            allQuestions.count { it.id in visited && it.level in levels }
        } else if (visitedOnly) {
            val visited = seenIds.value
            allQuestions.count { it.id in visited }
        } else {
            QuizLoader.countForLevels(allQuestions, levels)
        }
    }

    init {
        loadQuestions()
    }

    private fun loadQuestions() {
        viewModelScope.launch {
            try {
                allQuestions = QuizLoader.loadQuestions(getApplication())
                val saved = progressStore.getActiveSession()
                val restored = saved?.toSession(allQuestions)
                _state.value = _state.value.copy(
                    questions = allQuestions,
                    loading = false,
                    session = restored ?: QuizSession(),
                    currentScreen = if (restored != null && !restored.submitted) Screen.Quiz else Screen.Setup,
                    hasSavedSession = restored != null && !restored.submitted
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    loading = false,
                    errorMessage = e.message ?: "Unable to load quiz questions"
                )
            }
        }
    }

    fun startQuizForLevels(selectedLevels: Set<Int>) {
        val pool = QuizLoader.questionsForLevels(allQuestions, selectedLevels)
        if (pool.isEmpty()) return
        val counts = visitCounts.value
        val qs = pool.sortedWith(compareBy({ counts[it.id] ?: 0 }, { it.id }))
        val session = QuizSession(
            questions = qs,
            optionOrders = qs.associate { q -> q.id to q.options.shuffled() }
        )
        _state.value = _state.value.copy(
            session = session,
            currentScreen = Screen.Quiz,
            hasSavedSession = true
        )
        persistSession(session)
    }

    fun startQuizVisited(levels: Set<Int>) {
        val visited = seenIds.value
        val pool = allQuestions.filter { it.id in visited && it.level in levels }
        if (pool.isEmpty()) return
        val counts = visitCounts.value
        val qs = pool.sortedWith(compareBy({ counts[it.id] ?: 0 }, { it.id }))
        val session = QuizSession(
            questions = qs,
            optionOrders = qs.associate { q -> q.id to q.options.shuffled() }
        )
        _state.value = _state.value.copy(
            session = session,
            currentScreen = Screen.Quiz,
            hasSavedSession = true
        )
        persistSession(session)
    }

    fun selectAnswer(questionId: Int, option: String) {
        val s = _state.value.session
        if (s.submitted || questionId in s.revealedAnswers) return
        updateSession(s.copy(
            answers = s.answers + (questionId to option),
            revealedAnswers = s.revealedAnswers + questionId
        ))
        viewModelScope.launch {
            progressStore.markAsSeen(questionId)
        }
        scheduleAutoAdvance(s, questionId)
    }

    private fun scheduleAutoAdvance(session: QuizSession, questionId: Int) {
        autoAdvanceJob?.cancel()
        val question = session.questions.find { it.id == questionId } ?: return
        val options = session.optionOrders[questionId] ?: question.options
        val totalChars = question.question.length + options.sumOf { it.length }
        val delayMs = (totalChars * 40L).coerceIn(2500L, 8000L)
        autoAdvanceJob = viewModelScope.launch {
            delay(delayMs)
            val s = _state.value.session
            if (s.submitted) return@launch
            val idx = s.currentIndex
            if (idx >= s.questions.lastIndex) submitQuiz()
            else navigateQuestion(1)
        }
    }

    private fun cancelAutoAdvance() {
        autoAdvanceJob?.cancel()
        autoAdvanceJob = null
    }

    fun navigateQuestion(delta: Int) {
        cancelAutoAdvance()
        val s = _state.value.session
        val next = (s.currentIndex + delta).coerceIn(0, s.questions.lastIndex)
        updateSession(s.copy(currentIndex = next))
    }

    fun submitQuiz() {
        cancelAutoAdvance()
        val session = _state.value.session.copy(submitted = true)
        _state.value = _state.value.copy(
            session = session,
            currentScreen = Screen.Report,
            hasSavedSession = false
        )
        viewModelScope.launch {
            val wrongIds = session.questions.filter { q -> session.answers[q.id] != q.answer }.map { it.id }.toSet()
            val correctIds = session.questions.filter { q -> session.answers[q.id] == q.answer }.map { it.id }.toSet()
            progressStore.recordReviewResults(wrongIds, correctIds)
            progressStore.clearActiveSession()
        }
    }

    fun goToSetup() {
        cancelAutoAdvance()
        _state.value = _state.value.copy(
            session = QuizSession(),
            currentScreen = Screen.Setup,
            hasSavedSession = false
        )
        viewModelScope.launch {
            progressStore.clearActiveSession()
        }
    }

    fun shareQuestionText(question: Question): String = buildString {
        appendLine(question.question)
        question.options.forEachIndexed { index, option ->
            appendLine("${QuizLoader.optionLabel(index)}. $option")
        }
        appendLine()
        appendLine("Answer: ${question.answer}")
        append("Explanation: ${question.explanation}")
    }

    private fun updateSession(session: QuizSession) {
        _state.value = _state.value.copy(session = session)
        persistSession(session)
    }

    private fun persistSession(session: QuizSession) {
        if (session.questions.isEmpty() || session.submitted) return
        viewModelScope.launch {
            progressStore.saveActiveSession(session.toSaved())
        }
    }

    private fun QuizSession.toSaved(): SavedQuizSession = SavedQuizSession(
        questionIds = questions.map { it.id },
        answers = answers,
        currentIndex = currentIndex,
        submitted = submitted,
        optionOrders = optionOrders
    )

    private fun SavedQuizSession.toSession(allQuestions: List<Question>): QuizSession? {
        val byId = allQuestions.associateBy { it.id }
        val restoredQuestions = questionIds.mapNotNull { byId[it] }
        if (restoredQuestions.isEmpty()) return null
        return QuizSession(
            questions = restoredQuestions,
            answers = answers,
            optionOrders = optionOrders.filterKeys { it in questionIds },
            submitted = submitted,
            currentIndex = currentIndex.coerceIn(0, restoredQuestions.lastIndex)
        )
    }
}
