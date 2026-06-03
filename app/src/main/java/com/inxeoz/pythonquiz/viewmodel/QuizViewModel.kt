package com.inxeoz.pythonquiz.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.inxeoz.pythonquiz.data.ProgressStore
import com.inxeoz.pythonquiz.data.Question
import com.inxeoz.pythonquiz.data.QuizLoader
import com.inxeoz.pythonquiz.data.SavedQuizAttempt
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
    val timedMode: Boolean = false,
    val timeLimitMinutes: Int = 0,
    val remainingSeconds: Int = 0,
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

enum class Screen { Setup, Quiz, Report, Browse, FlaggedBrowse }
enum class PracticeScope { All, NewOnly, Incomplete, Completed, Flagged, ReviewDue }

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
    val flaggedIds: StateFlow<Set<Int>> = progressStore.flaggedIds
        .stateIn(viewModelScope, SharingStarted.Lazily, emptySet())
    val dueReviewIds: StateFlow<Set<Int>> = progressStore.dueReviewIds
        .stateIn(viewModelScope, SharingStarted.Lazily, emptySet())
    val quizHistory: StateFlow<List<SavedQuizAttempt>> = progressStore.quizHistory
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    var allQuestions: List<Question> = emptyList()
        private set

    private var autoAdvanceJob: Job? = null

    val categories: List<QuizLoader.CategoryInfo>
        get() = QuizLoader.getCategories(allQuestions)

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

    fun startQuiz(
        selectedLevels: Set<Int>,
        count: Int,
        shuffle: Boolean,
        scope: PracticeScope = PracticeScope.All,
        shuffleOptions: Boolean = true,
        timedMode: Boolean = false,
        timeLimitMinutes: Int = 20
    ) {
        val completed = completedIds.value
        val seen = seenIds.value
        val flagged = flaggedIds.value
        val dueReview = dueReviewIds.value
        val pool = allQuestions.filter {
            it.level in selectedLevels && when (scope) {
                PracticeScope.All -> true
                PracticeScope.NewOnly -> it.id !in seen
                PracticeScope.Incomplete -> it.id !in completed
                PracticeScope.Completed -> it.id in completed
                PracticeScope.Flagged -> it.id in flagged
                PracticeScope.ReviewDue -> it.id in dueReview
            }
        }
        val qs = if (shuffle) pool.shuffled() else pool
        val selected = if (count >= qs.size) qs else qs.take(count)
        val optionOrders = selected.associate { q ->
            q.id to if (shuffleOptions) q.options.shuffled() else q.options
        }
        val session = QuizSession(
            questions = selected,
            optionOrders = optionOrders,
            timedMode = timedMode,
            timeLimitMinutes = if (timedMode) timeLimitMinutes else 0,
            remainingSeconds = if (timedMode) timeLimitMinutes * 60 else 0
        )
        _state.value = _state.value.copy(
            session = session,
            currentScreen = Screen.Quiz,
            hasSavedSession = selected.isNotEmpty()
        )
        persistSession(session)
    }

    fun startQuizFromQuestions(questions: List<Question>, shuffle: Boolean = false, shuffleOptions: Boolean = true) {
        val selected = if (shuffle) questions.shuffled() else questions
        val session = QuizSession(
            questions = selected,
            optionOrders = selected.associate { q -> q.id to if (shuffleOptions) q.options.shuffled() else q.options }
        )
        _state.value = _state.value.copy(
            session = session,
            currentScreen = Screen.Quiz,
            hasSavedSession = selected.isNotEmpty()
        )
        persistSession(session)
    }

    fun startQuizForCategory(category: String, selectedLevels: Set<Int>) {
        val pool = QuizLoader.questionsForCategory(allQuestions, category, selectedLevels)
        if (pool.isEmpty()) return
        val qs = pool.shuffled()
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

    fun countForCategory(category: String, levels: Set<Int>): Int =
        QuizLoader.countForCategoryLevels(allQuestions, category, levels)

    fun selectAnswer(questionId: Int, option: String) {
        val s = _state.value.session
        if (s.submitted || questionId in s.revealedAnswers) return
        updateSession(s.copy(
            answers = s.answers + (questionId to option),
            revealedAnswers = s.revealedAnswers + questionId
        ))
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

    fun jumpToQuestion(index: Int) {
        cancelAutoAdvance()
        val s = _state.value.session
        if (index !in s.questions.indices) return
        updateSession(s.copy(currentIndex = index))
    }

    fun toggleFlag(questionId: Int) {
        viewModelScope.launch {
            progressStore.toggleFlagged(questionId)
        }
    }

    fun clearAllFlagged() {
        viewModelScope.launch {
            val ids = flaggedIds.value.toList()
            ids.forEach { progressStore.toggleFlagged(it) }
        }
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
            progressStore.addQuizAttempt(
                SavedQuizAttempt(
                    id = System.currentTimeMillis(),
                    completedAtMillis = System.currentTimeMillis(),
                    questionIds = session.questions.map { it.id },
                    answers = session.answers,
                    correctCount = correctIds.size,
                    totalCount = session.questions.size,
                    timedMode = session.timedMode,
                    timeLimitMinutes = session.timeLimitMinutes
                )
            )
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

    fun goToBrowse() {
        _state.value = _state.value.copy(currentScreen = Screen.Browse)
    }

    fun goToFlaggedBrowse() {
        _state.value = _state.value.copy(currentScreen = Screen.FlaggedBrowse)
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

    fun tickTimer() {
        val s = _state.value.session
        if (!s.timedMode || s.submitted || s.remainingSeconds <= 0) return
        val updated = s.copy(remainingSeconds = s.remainingSeconds - 1)
        updateSession(updated)
        if (updated.remainingSeconds == 0) {
            submitQuiz()
        }
    }

    fun resumeSavedSession() {
        viewModelScope.launch {
            progressStore.getActiveSession()?.toSession(allQuestions)?.let { session ->
                _state.value = _state.value.copy(
                    session = session,
                    currentScreen = Screen.Quiz,
                    hasSavedSession = true
                )
            }
        }
    }

    fun retryQuizAttempt(attempt: SavedQuizAttempt) {
        val questions = questionsByIds(attempt.questionIds)
        startQuizFromQuestions(questions, shuffle = false, shuffleOptions = true)
    }

    fun retryFlaggedQuestions() {
        val questions = questionsByIds(flaggedIds.value.toList())
        startQuizFromQuestions(questions, shuffle = false, shuffleOptions = true)
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

    fun shareScoreSummary(): String {
        val session = _state.value.session
        val total = session.questions.size
        val correct = session.questions.count { q -> session.answers[q.id] == q.answer }
        val pct = if (total > 0) correct * 100 / total else 0
        return "Python Quiz score: $correct/$total ($pct%)."
    }

    fun clearQuizHistory() {
        viewModelScope.launch {
            progressStore.clearQuizHistory()
        }
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

    private fun questionsByIds(ids: List<Int>): List<Question> {
        val byId = allQuestions.associateBy { it.id }
        return ids.mapNotNull { byId[it] }
    }

    private fun QuizSession.toSaved(): SavedQuizSession = SavedQuizSession(
        questionIds = questions.map { it.id },
        answers = answers,
        currentIndex = currentIndex,
        submitted = submitted,
        optionOrders = optionOrders,
        timedMode = timedMode,
        timeLimitMinutes = timeLimitMinutes,
        remainingSeconds = remainingSeconds
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
            currentIndex = currentIndex.coerceIn(0, restoredQuestions.lastIndex),
            timedMode = timedMode,
            timeLimitMinutes = timeLimitMinutes,
            remainingSeconds = remainingSeconds
        )
    }
}
