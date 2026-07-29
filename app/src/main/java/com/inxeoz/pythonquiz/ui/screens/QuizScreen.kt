package com.inxeoz.pythonquiz.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inxeoz.pythonquiz.data.QuizLoader
import com.inxeoz.pythonquiz.ui.theme.LocalQuizColors
import com.inxeoz.pythonquiz.ui.theme.QuizColors
import com.inxeoz.pythonquiz.viewmodel.QuizViewModel

@Composable
fun QuizScreen(vm: QuizViewModel, isDark: Boolean, onThemeToggle: () -> Unit) {
    val state by vm.state.collectAsState()
    val colors = LocalQuizColors.current
    val session = state.session
    val questions = session.questions

    BackHandler {
        vm.goToSetup()
    }

    if (questions.isEmpty()) return

    val currentIndex = session.currentIndex.coerceIn(0, questions.lastIndex)
    val question = questions[currentIndex]
    val options = session.optionOrders[question.id] ?: question.options
    val selectedAnswer = session.answers[question.id]
    val isRevealed = question.id in session.revealedAnswers
    val isSubmitted = session.submitted
    val anyRevealedOrSubmitted = isRevealed || isSubmitted
    val progress = if (questions.isNotEmpty()) (currentIndex + 1).toFloat() / questions.size else 0f

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 430.dp)
                .align(Alignment.TopCenter)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            QuizHeader(
                topic = question.topic,
                level = question.level,
                colors = colors,
                isDark = isDark,
                onBack = { vm.goToSetup() },
                onThemeToggle = onThemeToggle
            )

            Spacer(modifier = Modifier.height(8.dp))

            ProgressSection(
                current = currentIndex + 1,
                total = questions.size,
                progress = progress,
                colors = colors
            )

            Spacer(modifier = Modifier.height(16.dp))

            QuestionCard(
                questionIndex = currentIndex + 1,
                questionText = question.question,
                question = question,
                options = options,
                isSubmitted = anyRevealedOrSubmitted,
                colors = colors
            )

            Spacer(modifier = Modifier.height(24.dp))

            OptionsList(
                options = options,
                selectedAnswer = selectedAnswer,
                correctAnswer = if (anyRevealedOrSubmitted) question.answer else null,
                isSubmitted = anyRevealedOrSubmitted,
                colors = colors,
                onSelect = { vm.selectAnswer(question.id, it) }
            )

            if (anyRevealedOrSubmitted) {
                Spacer(modifier = Modifier.height(16.dp))
                ExplanationCard(
                    explanation = question.explanation,
                    isCorrect = selectedAnswer == question.answer,
                    colors = colors
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            val isFirst = currentIndex == 0
            val isLast = currentIndex >= questions.lastIndex
            QuizFooter(
                isFirst = isFirst,
                isLast = isLast,
                isRevealed = isRevealed,
                colors = colors,
                onPrevious = { vm.navigateQuestion(-1) },
                onSkip = {
                    if (isLast) vm.submitQuiz()
                    else vm.navigateQuestion(1)
                },
                onNext = {
                    if (isLast) vm.submitQuiz()
                    else vm.navigateQuestion(1)
                }
            )

        }

    }
}

@Composable
private fun QuizHeader(
    topic: String,
    level: Int,
    colors: QuizColors,
    isDark: Boolean,
    onBack: () -> Unit,
    onThemeToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .size(40.dp)
                .background(colors.surface, CircleShape)
                .border(1.dp, colors.border, CircleShape)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = colors.text,
                modifier = Modifier.size(18.dp)
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = topic,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.text,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            val badgeColor = Color(QuizLoader.levelColorValue(level))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(badgeColor)
                    .padding(horizontal = 10.dp, vertical = 3.dp)
            ) {
                Text(
                    text = QuizLoader.levelName(level).uppercase(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        IconButton(
            onClick = onThemeToggle,
            modifier = Modifier
                .size(40.dp)
                .background(colors.surface, CircleShape)
                .border(1.dp, colors.border, CircleShape)
        ) {
            Icon(
                if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                contentDescription = "Toggle theme",
                tint = colors.textMuted,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun ProgressSection(
    current: Int,
    total: Int,
    progress: Float,
    colors: QuizColors
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 400),
        label = "progress"
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Question $current of $total",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.text
            )
            Text(
                text = "${(progress * 100).toInt()}%",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.text
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(colors.border)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .fillMaxSize()
                    .clip(RoundedCornerShape(4.dp))
                    .background(colors.accent)
            )
        }
    }
}

@Composable
private fun QuestionCard(
    questionIndex: Int,
    questionText: String,
    question: com.inxeoz.pythonquiz.data.Question,
    options: List<String>,
    isSubmitted: Boolean,
    colors: QuizColors
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(24.dp))
            .background(colors.surface, RoundedCornerShape(24.dp))
            .border(1.dp, colors.border, RoundedCornerShape(24.dp))
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = "Q$questionIndex",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textMuted
            )
            IconButton(
                onClick = {
                    val text = buildString {
                        appendLine(question.question)
                        options.forEachIndexed { index, option ->
                            appendLine("${QuizLoader.optionLabel(index)}. $option")
                        }
                    }
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("Question", text)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Default.ContentCopy,
                    contentDescription = "Copy question",
                    tint = colors.textMuted,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = questionText,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = colors.text,
            lineHeight = 30.8.sp
        )
    }
}

@Composable
private fun OptionsList(
    options: List<String>,
    selectedAnswer: String?,
    correctAnswer: String?,
    isSubmitted: Boolean,
    colors: QuizColors,
    onSelect: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        options.forEachIndexed { index, option ->
            val label = QuizLoader.optionLabel(index)
            val isSelected = option == selectedAnswer
            val isCorrect = correctAnswer != null && option == correctAnswer
            val isWrong = isSubmitted && isSelected && option != correctAnswer

            val borderColor by animateColorAsState(
                targetValue = when {
                    isCorrect -> colors.correct
                    isWrong -> colors.wrong
                    isSelected -> colors.accent
                    else -> colors.border
                },
                animationSpec = tween(200),
                label = "border"
            )

            val bgColor by animateColorAsState(
                targetValue = when {
                    isCorrect -> colors.correct.copy(alpha = 0.08f)
                    isWrong -> colors.wrong.copy(alpha = 0.08f)
                    isSelected -> colors.accentMuted
                    else -> Color.Transparent
                },
                animationSpec = tween(200),
                label = "bg"
            )

            val labelBg = when {
                isSelected -> colors.accent
                else -> colors.bg
            }
            val labelBorder = when {
                isSelected -> colors.accent
                else -> colors.border
            }
            val labelTextColor = when {
                isSelected -> Color.White
                else -> colors.text
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (bgColor == Color.Transparent) colors.surface else bgColor)
                    .border(2.dp, borderColor, RoundedCornerShape(16.dp))
                    .clickable(enabled = !isSubmitted) { onSelect(option) }
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(labelBg, CircleShape)
                        .border(1.dp, labelBorder, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = labelTextColor
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = option,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.text,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun ExplanationCard(
    explanation: String,
    isCorrect: Boolean,
    colors: QuizColors
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (isCorrect) colors.correct.copy(alpha = 0.08f) else colors.wrong.copy(alpha = 0.08f))
            .border(1.dp, if (isCorrect) colors.correct else colors.wrong, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text(
            text = if (isCorrect) "Correct!" else "Incorrect",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = if (isCorrect) colors.correct else colors.wrong
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = explanation,
            fontSize = 14.sp,
            color = colors.text,
            lineHeight = 20.sp
        )
    }
}

@Composable
private fun QuizFooter(
    isFirst: Boolean,
    isLast: Boolean,
    isRevealed: Boolean,
    colors: QuizColors,
    onPrevious: () -> Unit,
    onSkip: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(if (isFirst) colors.surface2 else colors.surface)
                .border(1.dp, colors.border, RoundedCornerShape(16.dp))
                .then(if (isFirst) Modifier else Modifier.clickable { onPrevious() })
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Previous",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isFirst) colors.textDim else colors.text
            )
        }

        val isFinish = isLast && isRevealed
        val actionLabel = when {
            isFinish -> "Finish"
            isLast -> "Skip"
            isRevealed -> "Next"
            else -> "Skip"
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(if (isRevealed) colors.accent else colors.surface)
                .border(
                    1.dp,
                    if (isRevealed) colors.accent else colors.border,
                    RoundedCornerShape(16.dp)
                )
                .clickable { if (isRevealed) onNext() else onSkip() }
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = actionLabel,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isRevealed) Color.White else colors.text
            )
        }
    }
}
