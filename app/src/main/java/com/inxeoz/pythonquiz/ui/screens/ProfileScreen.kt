package com.inxeoz.pythonquiz.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inxeoz.pythonquiz.ui.theme.LocalQuizColors
import com.inxeoz.pythonquiz.ui.theme.QuizColors
import com.inxeoz.pythonquiz.viewmodel.QuizViewModel

@Composable
fun ProfileScreen(
    vm: QuizViewModel,
    onBack: () -> Unit,
    onFlaggedQuestionsClick: () -> Unit,
    onThemeToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalQuizColors.current
    val flaggedIds by vm.flaggedIds.collectAsState()
    val history by vm.quizHistory.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.bg)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 430.dp)
                .align(Alignment.TopCenter)
                .padding(horizontal = 24.dp)
        ) {
            // ── Header ──
            item {
                Spacer(modifier = Modifier.height(40.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Surface(
                        modifier = Modifier.size(40.dp).clip(CircleShape),
                        shape = CircleShape,
                        color = colors.surface,
                        border = BorderStroke(1.dp, colors.border),
                        onClick = onBack
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = colors.text,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Text(
                        "Profile",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.text
                    )
                    Surface(
                        modifier = Modifier.size(40.dp).clip(CircleShape),
                        shape = CircleShape,
                        color = colors.surface,
                        border = BorderStroke(1.dp, colors.border),
                        onClick = onThemeToggle
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Icon(
                                Icons.Default.LightMode,
                                "Toggle theme",
                                tint = colors.textMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }

            // ── Profile Card ──
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = colors.surface,
                    shadowElevation = 4.dp,
                    border = BorderStroke(1.dp, colors.border)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Avatar
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(colors.accentMuted),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Avatar",
                                modifier = Modifier.size(40.dp),
                                tint = colors.accent
                            )
                        }

                        // Name
                        Text(
                            text = "Alex Rivera",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.text,
                            textAlign = TextAlign.Center
                        )

                        // Email
                        Text(
                            text = "alex.rivera@example.com",
                            fontSize = 14.sp,
                            color = colors.textMuted,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Divider
                        HorizontalDivider(
                            modifier = Modifier.padding(top = 4.dp),
                            thickness = 1.dp,
                            color = colors.border
                        )

                        // Stats row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            ProfileStat(value = "${history.size}", label = "Quizzes", colors = colors)
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(36.dp)
                                    .background(colors.border)
                            )
                            ProfileStat(value = if (history.isNotEmpty()) history.map { (it.correctCount * 100) / maxOf(it.totalCount, 1) }.average().toInt().toString() + "%" else "—%", label = "Avg Score", colors = colors)
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(36.dp)
                                    .background(colors.border)
                            )
                            ProfileStat(value = "4", label = "Streak", colors = colors)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(28.dp))
            }

            // ── Flagged Link Card ──
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onFlaggedQuestionsClick
                        ),
                    shape = RoundedCornerShape(16.dp),
                    color = colors.surface,
                    shadowElevation = 4.dp,
                    border = BorderStroke(1.dp, colors.border)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Flag,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = colors.accent
                            )
                            Column {
                                Text(
                                    text = "Flagged Questions",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.text
                                )
                                Text(
                                    text = "${flaggedIds.size} questions",
                                    fontSize = 13.sp,
                                    color = colors.textMuted
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = colors.textMuted
                        )
                    }
                }
                Spacer(modifier = Modifier.height(40.dp))
            }

        }

    }
}

@Composable
private fun ProfileStat(
    value: String,
    label: String,
    colors: QuizColors
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = colors.text
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = colors.textMuted,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}
