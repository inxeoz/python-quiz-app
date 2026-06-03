package com.inxeoz.pythonquiz.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inxeoz.pythonquiz.data.QuizLoader
import com.inxeoz.pythonquiz.ui.theme.LocalQuizColors
import com.inxeoz.pythonquiz.ui.theme.QuizColors
import com.inxeoz.pythonquiz.viewmodel.QuizViewModel

// ── Screen root ───────────────────────────────────────────────────────────────

@Composable
fun WelcomeScreen(
    vm: QuizViewModel,
) {
    val colors = LocalQuizColors.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedLevels by remember { mutableStateOf(setOf(0, 1, 2, 4, 5)) }

    val state by vm.state.collectAsState()
    val categories = remember(state.questions) {
        QuizLoader.getCategories(state.questions)
    }
    val categoryIcons = remember {
        mapOf(
            "python" to Icons.Default.Star,
            "arts" to Icons.Default.Star,
            "science" to Icons.Default.Science,
            "tech" to Icons.Default.Build,
            "sports" to Icons.Default.SportsSoccer,
        )
    }
    val categoryColors = remember(colors) {
        mapOf(
            "python" to colors.warning,
            "arts" to colors.levelBeginner,
            "science" to colors.accentMuted,
            "tech" to colors.accent,
            "sports" to colors.correct,
        )
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
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Let's test your knowledge",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.56).sp,
                            lineHeight = 30.sp,
                            color = colors.text,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Choose a level and category",
                            fontSize = 15.sp,
                            color = colors.textMuted,
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                SearchBar(query = searchQuery, onQueryChange = { searchQuery = it }, colors = colors)

                Spacer(Modifier.height(28.dp))

                SectionTitle("Difficulty Level", colors)
                Spacer(Modifier.height(12.dp))
                LevelChips(
                    levels = QuizLoader.levelNames,
                    selectedLevels = selectedLevels,
                    onLevelToggle = { level ->
                        selectedLevels = if (level in selectedLevels)
                            selectedLevels - level else selectedLevels + level
                    },
                    colors = colors,
                )

                Spacer(Modifier.height(24.dp))

                SectionTitle("Categories", colors)
                Spacer(Modifier.height(12.dp))
                CategoryGrid(
                    categories = categories,
                    selectedLevels = selectedLevels,
                    vm = vm,
                    categoryIcons = categoryIcons,
                    categoryColors = categoryColors,
                    colors = colors,
                )
            }
        }
    }
}

// ── Search bar ────────────────────────────────────────────────────────────────

@Composable
private fun SearchBar(query: String, onQueryChange: (String) -> Unit, colors: QuizColors) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = colors.surface,
        shadowElevation = 4.dp,
        border = BorderStroke(1.dp, colors.border),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Default.Search, null, tint = colors.textMuted, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(12.dp))
            Box(modifier = Modifier.weight(1f)) {
                if (query.isEmpty()) {
                    Text(
                        "Search categories...",
                        color = colors.textMuted,
                        fontSize = 16.sp,
                        modifier = Modifier.align(Alignment.CenterStart),
                    )
                }
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = androidx.compose.material3.MaterialTheme.typography.bodyLarge.copy(
                        color = colors.text, fontSize = 16.sp
                    ),
                    singleLine = true,
                    cursorBrush = SolidColor(colors.accent),
                )
            }
        }
    }
}

// ── Section title ─────────────────────────────────────────────────────────────

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

// ── Level chips ───────────────────────────────────────────────────────────────

@Composable
private fun LevelChips(
    levels: Map<Int, String>,
    selectedLevels: Set<Int>,
    onLevelToggle: (Int) -> Unit,
    colors: QuizColors,
) {
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        levels.entries.forEach { (level, name) ->
            val selected = level in selectedLevels
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = if (selected) colors.accent else colors.surface,
                border = BorderStroke(1.dp, if (selected) colors.accent else colors.border),
                modifier = Modifier.clip(RoundedCornerShape(999.dp)),
            ) {
                Text(
                    name,
                    modifier = Modifier.clickable { onLevelToggle(level) }.padding(horizontal = 18.dp, vertical = 8.dp),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (selected) Color.White else colors.textMuted,
                )
            }
        }
    }
}

// ── Category grid ─────────────────────────────────────────────────────────────

@Composable
private fun CategoryGrid(
    categories: List<QuizLoader.CategoryInfo>,
    selectedLevels: Set<Int>,
    vm: QuizViewModel,
    categoryIcons: Map<String, ImageVector>,
    categoryColors: Map<String, Color>,
    colors: QuizColors,
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        for (row in categories.chunked(2)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                for (cat in row) {
                    val count = remember(cat.key, selectedLevels) {
                        vm.countForCategory(cat.key, selectedLevels)
                    }
                    CategoryCard(
                        name = cat.displayName,
                        count = count,
                        icon = categoryIcons[cat.key] ?: Icons.Default.Star,
                        iconBg = categoryColors[cat.key] ?: colors.accent,
                        colors = colors,
                        onClick = { vm.startQuizForCategory(cat.key, selectedLevels) },
                        modifier = Modifier.weight(1f),
                    )
                }
                repeat(2 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

// ── Single category card ──────────────────────────────────────────────────────

@Composable
private fun CategoryCard(
    name: String,
    count: Int,
    icon: ImageVector,
    iconBg: Color,
    colors: QuizColors,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = 0.6f),
    )

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = colors.surface,
        shadowElevation = 4.dp,
        border = BorderStroke(1.dp, colors.border),
        modifier = modifier.scale(scale),
    ) {
        Column(
            modifier = Modifier
                .clickable(interactionSource, null, onClick = onClick)
                .padding(20.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconBg.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = name, tint = iconBg, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.height(12.dp))
            Text(name, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = colors.text)
            Spacer(Modifier.height(2.dp))
            Text("$count Quizzes", fontSize = 14.sp, color = colors.textMuted)
        }
    }
}
