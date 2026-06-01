package com.pythonquiz.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pythonquiz.app.data.SavedQuizAttempt
import com.pythonquiz.app.data.QuizLoader
import com.pythonquiz.app.ui.theme.Accent
import com.pythonquiz.app.ui.theme.AccentLight
import com.pythonquiz.app.ui.theme.Border
import com.pythonquiz.app.ui.theme.Correct
import com.pythonquiz.app.ui.theme.DarkBg
import com.pythonquiz.app.ui.theme.Level0
import com.pythonquiz.app.ui.theme.Level1
import com.pythonquiz.app.ui.theme.Level2
import com.pythonquiz.app.ui.theme.Level4
import com.pythonquiz.app.ui.theme.Level5
import com.pythonquiz.app.ui.theme.Surface
import com.pythonquiz.app.ui.theme.Surface2
import com.pythonquiz.app.ui.theme.Text
import com.pythonquiz.app.ui.theme.TextDim
import com.pythonquiz.app.ui.theme.TextMuted
import com.pythonquiz.app.ui.theme.Warning
import com.pythonquiz.app.viewmodel.PracticeScope
import com.pythonquiz.app.viewmodel.QuizViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SetupScreen(
    vm: QuizViewModel,
    onStart: (Set<Int>, Int, Boolean, PracticeScope, Boolean, Boolean, Int) -> Unit,
    onBrowse: () -> Unit
) {
    val state by vm.state.collectAsState()
    val allQuestions = vm.allQuestions
    val completedIds by vm.completedIds.collectAsState()
    val seenIds by vm.seenIds.collectAsState()
    val flaggedIds by vm.flaggedIds.collectAsState()
    val dueReviewIds by vm.dueReviewIds.collectAsState()
    val quizHistory by vm.quizHistory.collectAsState()

    var selectedLevels by remember { mutableStateOf(setOf(0, 1, 2, 4, 5)) }
    var questionCount by remember { mutableStateOf(25) }
    var useAllCount by remember { mutableStateOf(false) }
    var shuffle by remember { mutableStateOf(true) }
    var shuffleOptions by remember { mutableStateOf(true) }
    var timedMode by remember { mutableStateOf(false) }
    var timeLimitMinutes by remember { mutableStateOf(20) }
    var scope by remember { mutableStateOf(PracticeScope.All) }

    val levelOptions = QuizLoader.levelNames.toSortedMap().toList()
    val scopedQuestions = remember(allQuestions, completedIds, seenIds, flaggedIds, dueReviewIds, selectedLevels, scope) {
        allQuestions.filter {
            it.level in selectedLevels && when (scope) {
                PracticeScope.All -> true
                PracticeScope.NewOnly -> it.id !in seenIds
                PracticeScope.Incomplete -> it.id !in completedIds
                PracticeScope.Completed -> it.id in completedIds
                PracticeScope.Flagged -> it.id in flaggedIds
                PracticeScope.ReviewDue -> it.id in dueReviewIds
            }
        }
    }
    val poolSize = scopedQuestions.size
    val actualCount = if (useAllCount) poolSize else questionCount.coerceAtMost(poolSize)
    val completionPct = if (allQuestions.isNotEmpty()) completedIds.size * 100 / allQuestions.size else 0
    val seenPct = if (allQuestions.isNotEmpty()) seenIds.size * 100 / allQuestions.size else 0

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
                .widthIn(max = 760.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Spacer(Modifier.height(8.dp))
            HeaderBlock(
                total = allQuestions.size,
                completed = completedIds.size,
                seen = seenIds.size,
                completionPct = completionPct,
                seenPct = seenPct,
                flagged = flaggedIds.size,
                reviewDue = dueReviewIds.size
            )

            if (state.hasSavedSession) {
                SectionCard(title = "Continue") {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Resume unfinished session", color = Text, fontWeight = FontWeight.SemiBold)
                            Text(
                                "${state.session.answers.size}/${state.session.questions.size} answered",
                                color = TextDim,
                                fontSize = 12.sp
                            )
                        }
                        Button(
                            onClick = { vm.resumeSavedSession() },
                            colors = ButtonDefaults.buttonColors(containerColor = Accent),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Resume", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            FlaggedHomeSection(
                flaggedCount = flaggedIds.size,
                onViewFlagged = { vm.goToFlaggedBrowse() },
                onRetryFlagged = { vm.retryFlaggedQuestions() }
            )

            QuizHistorySection(
                attempts = quizHistory,
                onRetry = { vm.retryQuizAttempt(it) },
                onClear = { vm.clearQuizHistory() }
            )

            SectionCard(title = "Build a Session") {
                Text("Difficulty", color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                ScrollRow {
                    levelOptions.forEach { (level, name) ->
                        val selected = level in selectedLevels
                        LevelChip(
                            text = "Lv$level",
                            subtext = name,
                            selected = selected,
                            color = levelColor(level),
                            onClick = {
                                selectedLevels = if (selected && selectedLevels.size > 1) selectedLevels - level else selectedLevels + level
                            }
                        )
                    }
                }

                Spacer(Modifier.height(18.dp))
                Text("Study focus", color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                ScrollRow {
                    ScopeChip("All", PracticeScope.All, scope) { scope = it }
                    ScopeChip("New", PracticeScope.NewOnly, scope) { scope = it }
                    ScopeChip("Incomplete", PracticeScope.Incomplete, scope) { scope = it }
                    ScopeChip("Completed", PracticeScope.Completed, scope) { scope = it }
                    ScopeChip("Flagged (${flaggedIds.size})", PracticeScope.Flagged, scope) { scope = it }
                    ScopeChip("Review (${dueReviewIds.size})", PracticeScope.ReviewDue, scope) { scope = it }
                }

                Spacer(Modifier.height(18.dp))
                Text("Question count", color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                ScrollRow {
                    listOf(10, 25, 50, 100, 250).forEach { count ->
                        CountChip(
                            text = "$count",
                            selected = !useAllCount && questionCount == count,
                            onClick = {
                                questionCount = count
                                useAllCount = false
                            }
                        )
                    }
                    CountChip("All", useAllCount) { useAllCount = true }
                }

                Spacer(Modifier.height(18.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Surface2)
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Shuffle, null, tint = AccentLight, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Shuffle questions", color = Text, fontWeight = FontWeight.SemiBold)
                        Text("Randomizes the question order for each session", color = TextDim, fontSize = 12.sp)
                    }
                    Switch(
                        checked = shuffle,
                        onCheckedChange = { shuffle = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Text, checkedTrackColor = Accent)
                    )
                }

                Spacer(Modifier.height(10.dp))
                OptionRow(
                    icon = { Icon(Icons.Filled.Shuffle, null, tint = Warning, modifier = Modifier.size(20.dp)) },
                    title = "Shuffle answer options",
                    subtitle = "Option order is saved for this session",
                    checked = shuffleOptions,
                    onCheckedChange = { shuffleOptions = it }
                )

                Spacer(Modifier.height(10.dp))
                OptionRow(
                    icon = { Icon(Icons.Filled.Timer, null, tint = Correct, modifier = Modifier.size(20.dp)) },
                    title = "Timed mode",
                    subtitle = "$timeLimitMinutes minutes",
                    checked = timedMode,
                    onCheckedChange = { timedMode = it }
                )
                if (timedMode) {
                    Spacer(Modifier.height(8.dp))
                    ScrollRow {
                        listOf(10, 20, 30, 45, 60).forEach { minutes ->
                            CountChip(
                                text = "${minutes}m",
                                selected = timeLimitMinutes == minutes,
                                onClick = { timeLimitMinutes = minutes }
                            )
                        }
                    }
                }
            }

            SectionCard(title = "Ready") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MiniMetric("$actualCount", "selected", AccentLight, Modifier.weight(1f))
                    MiniMetric("$poolSize", "available", TextMuted, Modifier.weight(1f))
                    MiniMetric("${selectedLevels.size}", "levels", Warning, Modifier.weight(1f))
                }
                Spacer(Modifier.height(14.dp))
                Button(
                    onClick = {
                        onStart(
                            selectedLevels,
                            if (useAllCount) poolSize else questionCount,
                            shuffle,
                            scope,
                            shuffleOptions,
                            timedMode,
                            timeLimitMinutes
                        )
                    },
                    enabled = poolSize > 0,
                    colors = ButtonDefaults.buttonColors(containerColor = Accent, disabledContainerColor = Surface2),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Filled.PlayArrow, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Start Practice", fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(10.dp))
                OutlinedButton(
                    onClick = onBrowse,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Border),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Text)
                ) {
                    Icon(Icons.Filled.Search, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Browse Question Bank", fontWeight = FontWeight.SemiBold)
                }
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun HeaderBlock(
    total: Int,
    completed: Int,
    seen: Int,
    completionPct: Int,
    seenPct: Int,
    flagged: Int,
    reviewDue: Int
) {
    SectionCard(title = "Dashboard") {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Accent),
                contentAlignment = Alignment.Center
            ) {
                Text("Py", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Python Quiz", style = MaterialTheme.typography.headlineLarge, color = Text)
                Text("Offline exam practice with saved progress, history, and review queues.", color = TextMuted)
            }
            OfflineBadge()
        }
        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            StatCard("Questions", "$total", Icons.Outlined.RadioButtonUnchecked, TextMuted, Modifier.weight(1f))
            StatCard("Completed", "$completed", Icons.Filled.CheckCircle, Correct, Modifier.weight(1f))
            StatCard("Seen", "$seen", Icons.Filled.Visibility, AccentLight, Modifier.weight(1f))
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            MiniMetric("$flagged", "flagged", Warning, Modifier.weight(1f))
            MiniMetric("$reviewDue", "review due", AccentLight, Modifier.weight(1f))
            MiniMetric("Offline", "mode", Correct, Modifier.weight(1f))
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            ProgressCard("Mastery", completionPct, Correct, Modifier.weight(1f))
            ProgressCard("Coverage", seenPct, AccentLight, Modifier.weight(1f))
        }
    }
}

@Composable
private fun OfflineBadge() {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Correct.copy(alpha = 0.14f))
            .border(1.dp, Correct.copy(alpha = 0.45f), RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Lock, null, tint = Correct, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(6.dp))
            Text("Offline", color = Correct, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun FlaggedHomeSection(
    flaggedCount: Int,
    onViewFlagged: () -> Unit,
    onRetryFlagged: () -> Unit
) {
    SectionCard(title = "Flagged Questions") {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Flag, null, tint = Warning, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("$flaggedCount flagged", color = Text, fontWeight = FontWeight.SemiBold)
                Text("Review saved flags from any session.", color = TextDim, fontSize = 12.sp)
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = onViewFlagged,
                enabled = flaggedCount > 0,
                modifier = Modifier.weight(1f).height(48.dp),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Border),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Text)
            ) {
                Icon(Icons.Filled.Search, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Open")
            }
            Button(
                onClick = onRetryFlagged,
                enabled = flaggedCount > 0,
                modifier = Modifier.weight(1f).height(48.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Warning, disabledContainerColor = Surface2)
            ) {
                Icon(Icons.Filled.Refresh, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Re-quiz", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun QuizHistorySection(
    attempts: List<SavedQuizAttempt>,
    onRetry: (SavedQuizAttempt) -> Unit,
    onClear: () -> Unit
) {
    if (attempts.isEmpty()) return

    val totalQuestions = attempts.sumOf { it.totalCount }
    val totalCorrect = attempts.sumOf { it.correctCount }
    val overallPct = if (totalQuestions > 0) totalCorrect * 100 / totalQuestions else 0
    val bestPct = attempts.maxOfOrNull { if (it.totalCount > 0) it.correctCount * 100 / it.totalCount else 0 } ?: 0

    SectionCard(title = "Quiz History") {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.History, null, tint = AccentLight, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("${attempts.size} completed quizzes", color = Text, fontWeight = FontWeight.SemiBold)
                Text("Overall $overallPct% · Best $bestPct%", color = TextDim, fontSize = 12.sp)
            }
            IconButton(onClick = onClear) {
                Icon(Icons.Filled.DeleteSweep, "Clear history", tint = TextDim)
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            MiniMetric("$overallPct%", "overall", Correct, Modifier.weight(1f))
            MiniMetric("$bestPct%", "best", AccentLight, Modifier.weight(1f))
            MiniMetric("$totalQuestions", "answered", Warning, Modifier.weight(1f))
        }
        Spacer(Modifier.height(12.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            attempts.forEach { attempt ->
                AttemptRow(attempt = attempt, onRetry = { onRetry(attempt) })
            }
        }
    }
}

@Composable
private fun AttemptRow(attempt: SavedQuizAttempt, onRetry: () -> Unit) {
    val pct = if (attempt.totalCount > 0) attempt.correctCount * 100 / attempt.totalCount else 0
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Surface2)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text("${attempt.correctCount}/${attempt.totalCount} · $pct%", color = Text, fontWeight = FontWeight.Bold)
            Text(formatAttemptDate(attempt.completedAtMillis), color = TextDim, fontSize = 12.sp)
        }
        if (attempt.timedMode) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Correct.copy(alpha = 0.14f))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text("${attempt.timeLimitMinutes}m", color = Correct, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(8.dp))
        }
        OutlinedButton(
            onClick = onRetry,
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, Border),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Text)
        ) {
            Icon(Icons.Filled.PlayArrow, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text("Re-quiz")
        }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Surface),
        border = BorderStroke(1.dp, Border),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = Text)
            Spacer(Modifier.height(14.dp))
            content()
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Surface2)
            .padding(12.dp)
    ) {
        Icon(icon, null, tint = color, modifier = Modifier.size(18.dp))
        Spacer(Modifier.height(8.dp))
        Text(value, color = Text, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(label, color = TextMuted, fontSize = 12.sp)
    }
}

@Composable
private fun ProgressCard(label: String, pct: Int, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Surface2)
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, color = TextMuted, fontSize = 12.sp, modifier = Modifier.weight(1f))
            Text("$pct%", color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(8.dp))
        androidx.compose.material3.LinearProgressIndicator(
            progress = { pct / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = Border
        )
    }
}

@Composable
private fun MiniMetric(value: String, label: String, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Surface2)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, color = color, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
        Text(label, color = TextMuted, fontSize = 12.sp)
    }
}

@Composable
private fun OptionRow(
    icon: @Composable () -> Unit,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Surface2)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon()
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = Text, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = TextDim, fontSize = 12.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = Text, checkedTrackColor = Accent)
        )
    }
}

@Composable
fun ScrollRow(content: @Composable RowScope.() -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        content = content
    )
}

@Composable
fun LevelChip(text: String, subtext: String, selected: Boolean, color: Color, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(112.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) color.copy(alpha = 0.16f) else Surface2)
            .border(1.dp, if (selected) color else Border, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Text(text, fontWeight = FontWeight.Bold, color = if (selected) color else Text)
        Text(subtext, fontSize = 12.sp, color = TextMuted)
    }
}

@Composable
private fun ScopeChip(label: String, value: PracticeScope, selected: PracticeScope, onClick: (PracticeScope) -> Unit) {
    FilterChip(
        selected = value == selected,
        onClick = { onClick(value) },
        label = { Text(label) },
        shape = RoundedCornerShape(8.dp),
        colors = FilterChipDefaults.filterChipColors(
            containerColor = Surface2,
            labelColor = TextMuted,
            selectedContainerColor = Accent.copy(alpha = 0.18f),
            selectedLabelColor = AccentLight
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = value == selected,
            borderColor = Border,
            selectedBorderColor = Accent
        )
    )
}

@Composable
fun CountChip(text: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) Accent.copy(alpha = 0.18f) else Surface2)
            .border(1.dp, if (selected) Accent else Border, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Text(text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = if (selected) AccentLight else Text)
    }
}

private fun levelColor(level: Int): Color = Color(QuizLoader.levelColorValue(level))

private fun formatAttemptDate(millis: Long): String {
    return SimpleDateFormat("dd MMM, h:mm a", Locale.getDefault()).format(Date(millis))
}
