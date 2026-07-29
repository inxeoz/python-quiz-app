package com.inxeoz.pythonquiz.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
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
import com.inxeoz.pythonquiz.data.QuizLoader
import com.inxeoz.pythonquiz.ui.theme.LocalQuizColors
import com.inxeoz.pythonquiz.ui.theme.QuizColors
import com.inxeoz.pythonquiz.viewmodel.QuizViewModel

@Composable
fun WelcomeScreen(
    vm: QuizViewModel,
    isDark: Boolean,
    onThemeToggle: () -> Unit,
) {
    val colors = LocalQuizColors.current
    var selectedLevels by remember { mutableStateOf(setOf(0, 1, 2, 4, 5)) }
    var visitedMode by remember { mutableStateOf(false) }
    val state by vm.state.collectAsState()
    val visitedCount by vm.seenIds.collectAsState()
    val totalCount = remember(state.questions, selectedLevels, visitedMode, visitedCount) {
        if (visitedMode) vm.countForLevels(emptySet(), visitedOnly = true)
        else vm.countForLevels(selectedLevels)
    }

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize().background(colors.bg),
        contentAlignment = Alignment.TopCenter,
    ) {
        val contentWidth = if (maxWidth > 430.dp) 430.dp else maxWidth
        Column(modifier = Modifier.width(contentWidth).fillMaxHeight()) {
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(start = 24.dp, end = 24.dp, top = 40.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Python Quiz",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.56).sp,
                            lineHeight = 30.sp,
                            color = colors.text,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Select difficulty levels to start",
                            fontSize = 15.sp,
                            color = colors.textMuted,
                        )
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

                Spacer(Modifier.height(28.dp))

                SectionTitle("Difficulty Level", colors)
                Spacer(Modifier.height(12.dp))
                LevelChips(
                    levels = QuizLoader.levelNames,
                    selectedLevels = selectedLevels,
                    visitedMode = visitedMode,
                    onLevelToggle = { level ->
                        selectedLevels = if (level in selectedLevels)
                            selectedLevels - level else selectedLevels + level
                    },
                    onVisitedToggle = {
                        visitedMode = !visitedMode
                    },
                    colors = colors,
                )

                Spacer(Modifier.height(24.dp))

                if (totalCount > 0) {
                    SectionTitle("$totalCount questions available", colors)
                } else {
                    SectionTitle("No questions available", colors)
                }

                Spacer(Modifier.height(16.dp))

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (totalCount > 0) colors.accent else colors.surface,
                    border = BorderStroke(1.dp, if (totalCount > 0) colors.accent else colors.border),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = totalCount > 0) {
                            if (visitedMode) vm.startQuizVisited()
                            else vm.startQuizForLevels(selectedLevels)
                        },
                ) {
                    Text(
                        "Start Quiz",
                        modifier = Modifier.padding(16.dp),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (totalCount > 0) Color.White else colors.textMuted,
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String, colors: QuizColors) {
    Text(
        title.uppercase(),
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.7.sp,
        color = colors.textMuted,
    )
}

@Composable
private fun LevelChips(
    levels: Map<Int, String>,
    selectedLevels: Set<Int>,
    visitedMode: Boolean,
    onLevelToggle: (Int) -> Unit,
    onVisitedToggle: () -> Unit,
    colors: QuizColors,
) {
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        val visitedSelected = visitedMode
        Surface(
            shape = RoundedCornerShape(999.dp),
            color = if (visitedSelected) colors.warning else colors.surface,
            border = BorderStroke(1.dp, if (visitedSelected) colors.warning else colors.border),
            modifier = Modifier.clip(RoundedCornerShape(999.dp)),
        ) {
            Text(
                "Visited",
                modifier = Modifier.clickable { onVisitedToggle() }.padding(horizontal = 18.dp, vertical = 8.dp),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (visitedSelected) Color.White else colors.textMuted,
            )
        }

        levels.entries.forEach { (level, name) ->
            val selected = level in selectedLevels && !visitedMode
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = when {
                    visitedMode -> colors.surface
                    selected -> colors.accent
                    else -> colors.surface
                },
                border = BorderStroke(
                    1.dp,
                    when {
                        visitedMode -> colors.border
                        selected -> colors.accent
                        else -> colors.border
                    }
                ),
                modifier = Modifier.clip(RoundedCornerShape(999.dp)),
            ) {
                Text(
                    name,
                    modifier = Modifier.clickable(enabled = !visitedMode) { onLevelToggle(level) }
                        .padding(horizontal = 18.dp, vertical = 8.dp),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = when {
                        visitedMode -> colors.textDim
                        selected -> Color.White
                        else -> colors.textMuted
                    },
                )
            }
        }
    }
}
