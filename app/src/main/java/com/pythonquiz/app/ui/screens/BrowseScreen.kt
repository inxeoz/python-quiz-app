package com.pythonquiz.app.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.OutlinedFlag
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import com.pythonquiz.app.data.QuizLoader
import com.pythonquiz.app.ui.theme.*
import com.pythonquiz.app.viewmodel.QuizViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowseScreen(vm: QuizViewModel) {
    val allQuestions = vm.allQuestions
    val completedIds by vm.completedIds.collectAsState()
    val seenIds by vm.seenIds.collectAsState()
    val searchHistory by vm.searchHistory.collectAsState()
    val flaggedIds by vm.flaggedIds.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedLevel by remember { mutableStateOf<Int?>(null) }
    var selectedTopic by remember { mutableStateOf<String?>(null) }
    var showCompletedOnly by remember { mutableStateOf(false) }
    var showIncompleteOnly by remember { mutableStateOf(false) }
    var showVisitedOnly by remember { mutableStateOf(false) }
    var showFlaggedOnly by remember { mutableStateOf(false) }

    var detailQuestion by remember { mutableStateOf<Question?>(null) }

    val filtered = remember(allQuestions, searchQuery, selectedLevel, selectedTopic, showCompletedOnly, showIncompleteOnly, showVisitedOnly, showFlaggedOnly, completedIds, seenIds, flaggedIds) {
        allQuestions.filter { q ->
            val matchesSearch = searchQuery.isBlank() ||
                    q.question.contains(searchQuery, ignoreCase = true) ||
                    q.topic.contains(searchQuery, ignoreCase = true) ||
                    q.explanation.contains(searchQuery, ignoreCase = true)
            val matchesLevel = selectedLevel == null || q.level == selectedLevel
            val matchesTopic = selectedTopic == null || q.topic == selectedTopic
            val matchesCompletion = when {
                showCompletedOnly -> q.id in completedIds
                showIncompleteOnly -> q.id !in completedIds
                else -> true
            }
            val matchesVisited = if (showVisitedOnly) q.id in seenIds else true
            val matchesFlagged = if (showFlaggedOnly) q.id in flaggedIds else true
            matchesSearch && matchesLevel && matchesTopic && matchesCompletion && matchesVisited && matchesFlagged
        }
    }

    val levelCounts = remember(allQuestions) {
        allQuestions.groupBy { it.level }.mapValues { it.value.size }
    }

    Column(modifier = Modifier.fillMaxSize().background(DarkBg)) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = DarkBg.copy(alpha = 0.92f),
            shadowElevation = 4.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = { vm.goToSetup() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = TextMuted)
                        Spacer(Modifier.width(4.dp))
                        Text("Back", color = TextMuted)
                    }
                    Spacer(Modifier.weight(1f))
                    Text(
                        "${allQuestions.size} questions · ${completedIds.size} completed · ${flaggedIds.size} flagged",
                        fontSize = 13.sp, color = TextMuted
                    )
                }

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        vm.addSearchQuery(it)
                    },
                    placeholder = { Text("Search topic, question, explanation...", color = TextDim) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Text,
                        unfocusedTextColor = Text,
                        cursorColor = Accent,
                        focusedBorderColor = Accent,
                        unfocusedBorderColor = Border,
                        focusedContainerColor = Surface,
                        unfocusedContainerColor = Surface
                    ),
                    textStyle = LocalTextStyle.current.copy(fontSize = 15.sp),
                    shape = RoundedCornerShape(10.dp),
                    leadingIcon = { Icon(Icons.Filled.Search, "Search", tint = TextDim) }
                )

                if (searchHistory.isNotEmpty() && searchQuery.isBlank()) {
                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Recent", fontSize = 12.sp, color = TextDim)
                        searchHistory.toList().takeLast(6).reversed().forEach { query ->
                            AssistChip(
                                onClick = { searchQuery = query },
                                label = { Text(query, fontSize = 12.sp) },
                                colors = AssistChipDefaults.assistChipColors(containerColor = Surface2, labelColor = TextMuted),
                                border = BorderStroke(1.dp, Border)
                            )
                        }
                        IconButton(onClick = { vm.clearSearchHistory() }, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Filled.DeleteSweep, "Clear recent searches", tint = TextDim, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }

        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Tune, null, tint = TextDim, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("${filtered.size} matching questions", color = TextMuted, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(8.dp))
            HorizontalScrollRow {
                LevelFilterChip("All", selected = selectedLevel == null) { selectedLevel = null }
                listOf(0 to "Basic", 1 to "Beginner", 2 to "Intermediate", 4 to "Advanced", 5 to "Expert").forEach { (lv, name) ->
                    LevelFilterChip(
                        "Lv$lv (${levelCounts[lv] ?: 0})",
                        selected = selectedLevel == lv,
                        onClick = { selectedLevel = if (selectedLevel == lv) null else lv }
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
            HorizontalScrollRow {
                FilterChip(
                    selected = showIncompleteOnly,
                    onClick = { showIncompleteOnly = !showIncompleteOnly; showCompletedOnly = false; showVisitedOnly = false },
                    label = { Text("Not completed", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Accent.copy(alpha = 0.15f),
                        selectedLabelColor = AccentLight
                    )
                )
                Spacer(Modifier.width(6.dp))
                FilterChip(
                    selected = showCompletedOnly,
                    onClick = { showCompletedOnly = !showCompletedOnly; showIncompleteOnly = false; showVisitedOnly = false },
                    label = { Text("Completed", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Correct.copy(alpha = 0.15f),
                        selectedLabelColor = Correct
                    )
                )
                Spacer(Modifier.width(6.dp))
                FilterChip(
                    selected = showVisitedOnly,
                    onClick = { showVisitedOnly = !showVisitedOnly; showCompletedOnly = false; showIncompleteOnly = false; showFlaggedOnly = false },
                    label = { Text("Seen (${seenIds.size})", fontSize = 12.sp) },
                    leadingIcon = {
                        Icon(
                            if (showVisitedOnly) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Level4.copy(alpha = 0.15f),
                        selectedLabelColor = Level4
                    )
                )
                Spacer(Modifier.width(6.dp))
                FilterChip(
                    selected = showFlaggedOnly,
                    onClick = { showFlaggedOnly = !showFlaggedOnly; showCompletedOnly = false; showIncompleteOnly = false; showVisitedOnly = false },
                    label = { Text("Flagged (${flaggedIds.size})", fontSize = 12.sp) },
                    leadingIcon = {
                        Icon(
                            if (showFlaggedOnly) Icons.Filled.Flag else Icons.Filled.OutlinedFlag,
                            null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Warning.copy(alpha = 0.15f),
                        selectedLabelColor = Warning
                    )
                )
                if (selectedTopic != null) {
                    Spacer(Modifier.width(6.dp))
                    AssistChip(
                        onClick = { selectedTopic = null },
                        label = { Text("Topic: $selectedTopic", fontSize = 12.sp) },
                        trailingIcon = { Icon(Icons.Filled.Close, "Clear", modifier = Modifier.size(16.dp)) }
                    )
                }
            }
        }

        if (filtered.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No questions found", color = TextDim, fontSize = 16.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (showVisitedOnly) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.Visibility, null, tint = Level4, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Visited questions", fontSize = 13.sp, color = Level4, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.weight(1f))
                            Text("${filtered.size}", fontSize = 12.sp, color = TextMuted)
                        }
                    }
                }

                items(filtered, key = { it.id }) { q ->
                    BrowseQuestionCard(
                        question = q,
                        isCompleted = q.id in completedIds,
                        isFlagged = q.id in flaggedIds,
                        onToggleCompleted = { vm.toggleCompleted(q.id) },
                        onToggleFlagged = { vm.toggleFlag(q.id) },
                        onTap = {
                            vm.markAsSeen(q.id)
                            detailQuestion = q
                        },
                        onSelectTopic = { selectedTopic = it }
                    )
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }

    if (detailQuestion != null) {
        QuestionDetailDialog(
            question = detailQuestion!!,
            isCompleted = detailQuestion!!.id in completedIds,
            onDismiss = { detailQuestion = null },
            onToggleCompleted = { vm.toggleCompleted(detailQuestion!!.id) }
        )
    }
}

@Composable
fun HorizontalScrollRow(content: @Composable RowScope.() -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        content = content
    )
}

@Composable
fun LevelFilterChip(text: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(text, fontSize = 12.sp) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = Accent.copy(alpha = 0.15f),
            selectedLabelColor = AccentLight
        )
    )
}

@Composable
fun BrowseQuestionCard(
    question: Question,
    isCompleted: Boolean,
    isFlagged: Boolean,
    onToggleCompleted: () -> Unit,
    onToggleFlagged: () -> Unit,
    onTap: () -> Unit,
    onSelectTopic: (String) -> Unit
) {
    val levelColor = Color(QuizLoader.levelColorValue(question.level))

    Card(
        modifier = Modifier.clickable(onClick = onTap),
        colors = CardDefaults.cardColors(containerColor = Surface),
        border = BorderStroke(1.dp, Border)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onToggleCompleted,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        if (isCompleted) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                        "Toggle completed",
                        tint = if (isCompleted) Correct else TextDim,
                        modifier = Modifier.size(24.dp)
                    )
                }
                IconButton(
                    onClick = onToggleFlagged,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        if (isFlagged) Icons.Filled.Flag else Icons.Filled.OutlinedFlag,
                        "Toggle flagged",
                        tint = if (isFlagged) Warning else TextDim,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(levelColor.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text("Lv${question.level}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = levelColor)
                }
                Spacer(Modifier.width(6.dp))
                TextButton(
                    onClick = { onSelectTopic(question.topic) },
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Text(question.topic, fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(6.dp))

            Text(
                question.question,
                fontSize = 15.sp,
                color = Text,
                fontWeight = FontWeight.Medium
            )

            Spacer(Modifier.height(8.dp))

            Text(
                "Answer: ${question.answer}",
                fontSize = 13.sp,
                color = if (isCompleted) Correct else TextDim
            )

            Text(
                question.explanation,
                fontSize = 12.sp,
                color = TextMuted,
                maxLines = 2
            )
        }
    }
}
