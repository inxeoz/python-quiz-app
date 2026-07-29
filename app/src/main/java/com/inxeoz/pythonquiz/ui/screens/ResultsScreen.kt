package com.inxeoz.pythonquiz.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inxeoz.pythonquiz.ui.theme.LocalQuizColors
import com.inxeoz.pythonquiz.ui.theme.QuizColors
import com.inxeoz.pythonquiz.viewmodel.QuizSession
import com.inxeoz.pythonquiz.viewmodel.QuizViewModel
import com.inxeoz.pythonquiz.data.QuizLoader


@Composable
fun ResultsScreen(vm: QuizViewModel, isDark: Boolean, onThemeToggle: () -> Unit) {
    val colors = LocalQuizColors.current
    val state by vm.state.collectAsState()
    val session = state.session

    val total = session.questions.size
    val correct = session.questions.count { session.answers[it.id] == it.answer }
    val pct = if (total > 0) (correct * 100) / total else 0

    val dominantLevel = remember(session.questions) {
        val levels = session.questions.map { it.level }
        if (levels.isEmpty()) 2
        else levels.groupingBy { it }.eachCount().maxByOrNull { it.value }?.key ?: 2
    }

    val levelLabel = remember(dominantLevel) { QuizLoader.levelName(dominantLevel) }

    val resultMessage = remember(pct) {
        when {
            pct >= 90 -> "Outstanding performance!"
            pct >= 70 -> "Great job!"
            pct >= 50 -> "Good effort!"
            pct >= 30 -> "Keep practicing!"
            else -> "Don't give up!"
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg)
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 430.dp)
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Spacer(modifier = Modifier.size(40.dp))
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    val badgeColor = Color(QuizLoader.levelColorValue(dominantLevel))
                    Box(
                        modifier = Modifier
                            .background(
                                color = badgeColor,
                                shape = RoundedCornerShape(999.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = levelLabel,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.66.sp
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

            Spacer(modifier = Modifier.height(36.dp))

            Text(
                text = "Quiz Completed!",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = colors.text,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = resultMessage,
                fontSize = 16.sp,
                color = colors.textMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(40.dp))

            ScoreCircle(
                pct = pct,
                correct = correct,
                total = total,
                colors = colors,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(40.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatCard(
                    value = "${pct}%",
                    label = "Accuracy",
                    colors = colors,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    value = "$total",
                    label = "Questions",
                    colors = colors,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            PressScaleButton(
                text = "Back to Home",
                onClick = { vm.goToSetup() },
                colors = colors,
                isPrimary = true,
                modifier = Modifier.fillMaxWidth()
            )

        }
    }
}

@Composable
private fun ScoreCircle(
    pct: Int,
    correct: Int,
    total: Int,
    colors: QuizColors,
    modifier: Modifier = Modifier
) {
    val animatedSweep by animateFloatAsState(
        targetValue = (pct / 100f) * 360f,
        animationSpec = tween(durationMillis = 800),
        label = "sweep"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(200.dp)
    ) {
        Canvas(modifier = Modifier.size(200.dp)) {
            val strokeWidth = 12.dp.toPx()
            drawArc(
                color = colors.border,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            if (animatedSweep > 0f) {
                drawArc(
                    color = colors.accent,
                    startAngle = -90f,
                    sweepAngle = animatedSweep,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$correct",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = colors.text,
                lineHeight = 48.sp
            )
            Text(
                text = "out of $total",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.textMuted
            )
        }
    }
}

@Composable
private fun StatCard(
    value: String,
    label: String,
    colors: QuizColors,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .shadow(4.dp, RoundedCornerShape(16.dp))
            .background(colors.surface, RoundedCornerShape(16.dp))
            .border(1.dp, colors.border, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = colors.text
        )
        Text(
            text = label,
            fontSize = 14.sp,
            color = colors.textMuted
        )
    }
}

@Composable
private fun PressScaleButton(
    text: String,
    onClick: () -> Unit,
    colors: QuizColors,
    isPrimary: Boolean,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        label = "buttonPress"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .background(
                color = if (isPrimary) colors.accent else colors.surface,
                shape = RoundedCornerShape(16.dp)
            )
            .then(
                if (!isPrimary) {
                    Modifier.border(1.dp, colors.border, RoundedCornerShape(16.dp))
                } else {
                    Modifier
                }
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 18.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isPrimary) Color.White else colors.text,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
    }
}
