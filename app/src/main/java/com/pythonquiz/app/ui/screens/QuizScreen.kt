package com.pythonquiz.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.OutlinedFlag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pythonquiz.app.data.Question
import com.pythonquiz.app.ui.theme.*
import com.pythonquiz.app.viewmodel.QuizSession
import com.pythonquiz.app.viewmodel.QuizViewModel

@Composable
fun QuizScreen(vm: QuizViewModel) {
    val state by vm.state.collectAsState()
    val session = state.session
    val q = session.questions.getOrNull(session.currentIndex) ?: return

    var showOverview by remember { mutableStateOf(false) }

    LaunchedEffect(session.currentIndex, q.id) {
        vm.markAsSeen(q.id)
    }

    var lastAnswerCount by remember { mutableStateOf(0) }
    LaunchedEffect(session.answers.size) {
        val count = session.answers.size
        if (count > lastAnswerCount
            && session.currentIndex < session.questions.lastIndex
            && !session.submitted
        ) {
            lastAnswerCount = count
            kotlinx.coroutines.delay(400)
            vm.navigateQuestion(1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        Header(
            session = session,
            onBack = { vm.goToSetup() },
            onOverview = { showOverview = !showOverview }
        )
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                QuestionCard(
                    question = q,
                    index = session.currentIndex,
                    total = session.questions.size,
                    userAnswer = session.answers[session.currentIndex],
                    isFlagged = session.currentIndex in session.flagged,
                    submitted = session.submitted,
                    onSelectAnswer = { vm.selectAnswer(session.currentIndex, it) },
                    onToggleFlag = { vm.toggleFlag(session.currentIndex) }
                )
            }
        }
        BottomNav(
            session = session,
            onPrev = { vm.navigateQuestion(-1) },
            onNext = { vm.navigateQuestion(1) },
            onSubmit = { vm.submitQuiz() }
        )
    }

    if (showOverview) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable { showOverview = false },
            contentAlignment = Alignment.CenterEnd
        ) {
            OverviewPanel(
                session = session,
                onJumpTo = { idx ->
                    vm.jumpToQuestion(idx)
                    showOverview = false
                }
            )
        }
    }
}

@Composable
private fun Header(session: QuizSession, onBack: () -> Unit, onOverview: () -> Unit) {
    Column(
        modifier = Modifier
            .background(Surface)
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = TextMuted)
            }
            Spacer(Modifier.width(4.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Practice Session", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Text)
                Text("${session.questions.size} questions", fontSize = 12.sp, color = TextMuted)
            }
            IconButton(onClick = onOverview) {
                Icon(Icons.Filled.Dashboard, "Overview", tint = AccentLight)
            }
            Spacer(Modifier.width(8.dp))
            Text("${session.answers.size}/${session.questions.size}", fontSize = 13.sp, color = TextMuted, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(4.dp))
        val pct = if (session.questions.isNotEmpty()) (session.answers.size.toFloat() / session.questions.size) * 100 else 0f
        LinearProgressIndicator(
            progress = { pct / 100f },
            modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
            color = AccentLight,
            trackColor = Border
        )
    }
}

@Composable
private fun BottomNav(
    session: QuizSession,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onSubmit: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth().navigationBarsPadding(),
        color = DarkBg.copy(alpha = 0.95f),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .heightIn(min = 56.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = onPrev,
                enabled = session.currentIndex > 0,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Text)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Prev")
            }

            Spacer(Modifier.weight(1f))

            Text(
                "${session.answers.size} of ${session.questions.size} answered",
                fontSize = 12.sp, color = TextMuted
            )

            Spacer(Modifier.weight(1f))

            if (session.currentIndex < session.questions.lastIndex) {
                OutlinedButton(
                    onClick = onNext,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Text)
                ) {
                    Text("Next")
                    Spacer(Modifier.width(6.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, null, modifier = Modifier.size(18.dp))
                }
            } else if (!session.submitted) {
                Button(
                    onClick = onSubmit,
                    colors = ButtonDefaults.buttonColors(containerColor = Yellow, contentColor = DarkBg),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Submit", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun QuestionCard(
    question: Question,
    index: Int,
    total: Int,
    userAnswer: String?,
    isFlagged: Boolean,
    submitted: Boolean,
    onSelectAnswer: (String) -> Unit,
    onToggleFlag: () -> Unit
) {
    val answered = submitted || userAnswer != null
    val levelNames = mapOf(0 to "Basic", 1 to "Beginner", 2 to "Intermediate", 4 to "Advanced", 5 to "Expert")
    val levelColor = when (question.level) { 0 -> Level0; 1 -> Level1; 2 -> Level2; 4 -> Level4; else -> Level5 }

    Column {
        Card(
            colors = CardDefaults.cardColors(containerColor = Surface),
            border = BorderStroke(1.dp, Border),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Q${index + 1} of $total", fontSize = 13.sp, color = TextDim, fontWeight = FontWeight.Medium)
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(levelColor.copy(alpha = 0.15f))
                    .padding(horizontal = 10.dp, vertical = 3.dp)
            ) {
                Text("Level ${question.level} · ${levelNames[question.level]}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = levelColor)
            }
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Surface2)
                    .padding(horizontal = 10.dp, vertical = 3.dp)
            ) {
                Text(question.topic, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted, letterSpacing = 0.5.sp)
            }
            Spacer(Modifier.weight(1f))
            IconButton(onClick = onToggleFlag) {
                Icon(
                    if (isFlagged) Icons.Filled.Flag else Icons.Filled.OutlinedFlag,
                    contentDescription = "Flag",
                    tint = if (isFlagged) Yellow else TextDim
                )
            }
        }
        Spacer(Modifier.height(16.dp))

        Text(text = question.question, fontSize = 17.sp, fontWeight = FontWeight.Medium, color = Text, lineHeight = 26.sp)
        Spacer(Modifier.height(20.dp))

        val letters = listOf("A", "B", "C", "D", "E")
        question.options.forEachIndexed { i, opt ->
            val isSelected = userAnswer == opt
            val isCorrectOption = opt == question.answer
            val isPickedWrong = isSelected && !isCorrectOption

            val showFeedback = answered

            val bgColor = when {
                showFeedback && isCorrectOption && isSelected -> Correct.copy(alpha = 0.18f)
                showFeedback && isCorrectOption && !isSelected -> Correct.copy(alpha = 0.08f)
                showFeedback && isPickedWrong -> Wrong.copy(alpha = 0.18f)
                isSelected -> Accent.copy(alpha = 0.15f)
                else -> Surface
            }
            val borderColor = when {
                showFeedback && isCorrectOption && isSelected -> Correct
                showFeedback && isCorrectOption && !isSelected -> Correct.copy(alpha = 0.4f)
                showFeedback && isPickedWrong -> Wrong
                isSelected -> AccentLight
                else -> Border
            }
            val markerColor = when {
                showFeedback && isCorrectOption && isSelected -> Correct
                showFeedback && isCorrectOption && !isSelected -> Correct.copy(alpha = 0.5f)
                showFeedback && isPickedWrong -> Wrong
                isSelected -> Accent
                else -> Border
            }

            val shape = RoundedCornerShape(12.dp)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp)
                    .clip(shape)
                    .background(bgColor)
                    .border(1.5.dp, borderColor, shape)
                    .clickable(enabled = !submitted) { onSelectAnswer(opt) }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(50))
                        .background(markerColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        letters.getOrElse(i) { "$i" },
                        fontSize = 13.sp, fontWeight = FontWeight.Bold,
                        color = if (showFeedback && (isCorrectOption || isPickedWrong) || (isSelected && !showFeedback)) Color.White else TextMuted
                    )
                }
                Spacer(Modifier.width(16.dp))
                Text(text = opt, fontSize = 16.sp, color = Text, lineHeight = 22.sp, modifier = Modifier.weight(1f))
                if (showFeedback && isCorrectOption) {
                    Icon(Icons.Filled.CheckCircle, "Correct", tint = Correct, modifier = Modifier.size(24.dp))
                } else if (showFeedback && isPickedWrong) {
                    Icon(Icons.Filled.Close, "Wrong", tint = Wrong, modifier = Modifier.size(24.dp))
                }
            }
        }

        if (answered) {
            Spacer(Modifier.height(16.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White.copy(alpha = 0.04f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(modifier = Modifier.padding(16.dp)) {
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .fillMaxHeight()
                            .background(Accent)
                            .clip(RoundedCornerShape(2.dp))
                    )
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text("💡 Explanation:", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Text)
                        Spacer(Modifier.height(6.dp))
                        Text(text = question.explanation, fontSize = 14.sp, color = TextMuted, lineHeight = 20.sp)
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
fun OverviewPanel(session: QuizSession, onJumpTo: (Int) -> Unit) {
    Surface(
        modifier = Modifier
            .width(280.dp)
            .fillMaxHeight(),
        color = Surface,
        shadowElevation = 8.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Questions", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextMuted, modifier = Modifier.padding(bottom = 16.dp))
            val columns = 5
            val rows = (session.questions.size + columns - 1) / columns
            for (row in 0 until rows) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (col in 0 until columns) {
                        val idx = row * columns + col
                        if (idx < session.questions.size) {
                            val isAnswered = session.answers.containsKey(idx)
                            val dotColor = when {
                                idx in session.flagged -> Yellow
                                isAnswered -> Accent
                                else -> Surface2
                            }
                            val isCurrent = idx == session.currentIndex
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(dotColor.copy(alpha = if (isAnswered || idx in session.flagged) 1f else 0.5f))
                                    .then(
                                        if (isCurrent) Modifier.border(2.dp, Yellow, RoundedCornerShape(8.dp))
                                        else Modifier
                                    )
                                    .clickable { onJumpTo(idx) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text("${idx + 1}", fontSize = 13.sp, fontWeight = FontWeight.Bold,
                                    color = if (isAnswered || idx in session.flagged) Color.White else TextMuted)
                            }
                        }
                    }
                }
            }
        }
    }
}
