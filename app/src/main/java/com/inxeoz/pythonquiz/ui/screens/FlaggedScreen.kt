package com.inxeoz.pythonquiz.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inxeoz.pythonquiz.data.Question
import com.inxeoz.pythonquiz.data.QuizLoader
import com.inxeoz.pythonquiz.ui.theme.LocalQuizColors
import com.inxeoz.pythonquiz.viewmodel.QuizViewModel

@Composable
fun FlaggedScreen(
    vm: QuizViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalQuizColors.current
    val flaggedIds by vm.flaggedIds.collectAsState()
    val state by vm.state.collectAsState()
    val allQuestions = state.questions
    val flaggedQuestions = allQuestions.filter { it.id in flaggedIds }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.bg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 430.dp)
                .align(Alignment.TopCenter)
                .padding(horizontal = 24.dp)
        ) {
            // ── Header ──
            Spacer(modifier = Modifier.height(40.dp))
            ScreenHeader(title = "Flagged Questions", onBack = onBack)
            Spacer(modifier = Modifier.height(28.dp))

            // ── Section Title ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "All Flagged Questions",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textMuted
                )
                // Count badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(colors.border)
                        .padding(horizontal = 10.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${flaggedQuestions.size}",
                        fontSize = 12.sp,
                        color = colors.textMuted
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            if (flaggedQuestions.isEmpty()) {
                // ── Empty State ──
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Flag,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = colors.border
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No flagged questions",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.text,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Flag questions during a quiz to review them here.",
                            fontSize = 14.sp,
                            color = colors.textMuted,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                // ── Flagged List ──
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(flaggedQuestions, key = { it.id }) { question ->
                        FlaggedQuestionCard(
                            question = question,
                            onUnflag = { vm.toggleFlag(question.id) }
                        )
                    }

                    // ── Clear All Button ──
                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            OutlinedButton(
                                onClick = { vm.clearAllFlagged() },
                                shape = RoundedCornerShape(999.dp),
                                border = BorderStroke(1.dp, colors.wrong),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = "Clear all",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.wrong
                                )
                            }
                        }
                    }

                }
            }
        }

    }
}



@Composable
private fun FlaggedQuestionCard(
    question: Question,
    onUnflag: () -> Unit
) {
    val colors = LocalQuizColors.current
    val levelName = QuizLoader.levelName(question.level)

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = colors.surface,
        shadowElevation = 4.dp,
        border = BorderStroke(1.dp, colors.border)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // ── Card Header: topic/level meta ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = question.topic,
                    fontSize = 12.sp,
                    color = colors.textMuted
                )
                // Separator dot
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(colors.textMuted)
                )
                Text(
                    text = levelName,
                    fontSize = 12.sp,
                    color = colors.textMuted
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Question text
            Text(
                text = question.question,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.text,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ── Options ──
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                question.options.forEachIndexed { index, option ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = QuizLoader.optionLabel(index),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.text
                        )
                        Text(
                            text = option,
                            fontSize = 14.sp,
                            color = colors.textMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // ── Unflag Button ──
            OutlinedButton(
                onClick = onUnflag,
                shape = RoundedCornerShape(999.dp),
                border = BorderStroke(1.dp, colors.border),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Unflag",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textMuted
                )
            }
        }
    }
}
