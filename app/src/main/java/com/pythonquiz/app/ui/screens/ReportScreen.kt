package com.pythonquiz.app.ui.screens

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pythonquiz.app.data.Question
import com.pythonquiz.app.data.QuizLoader
import com.pythonquiz.app.ui.theme.*
import com.pythonquiz.app.viewmodel.QuizViewModel

@Composable
fun ReportScreen(vm: QuizViewModel) {
    val state = vm.state.value
    val session = state.session
    val questions = session.questions
    val answers = session.answers
    val context = LocalContext.current

    val total = questions.size
    val correct = questions.count { q -> answers[q.id] == q.answer }
    val wrong = total - correct
    val pct = if (total > 0) (correct * 100 / total) else 0
    val grade = when {
        pct >= 90 -> "A"
        pct >= 80 -> "B"
        pct >= 70 -> "C"
        pct >= 60 -> "D"
        else -> "F"
    }
    val gradeColor = when (grade) {
        "A" -> Correct; "B" -> Level1; "C" -> Level4; else -> Wrong
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .verticalScroll(rememberScrollState())
    ) {
        TextButton(
            onClick = { vm.goToSetup() },
            modifier = Modifier.padding(8.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = TextMuted)
            Spacer(Modifier.width(4.dp))
            Text("Back to Setup", color = TextMuted)
        }

        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Quiz Report", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Text)
                    Text("$total questions · Completed now", fontSize = 14.sp, color = TextMuted)
                }
                IconButton(
                    onClick = {
                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, vm.shareScoreSummary())
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Share score"))
                    }
                ) {
                    Icon(Icons.Filled.Share, "Share score", tint = TextMuted)
                }
            }

            Spacer(Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ScoreCard(score = "$correct", label = "Correct", color = Correct, modifier = Modifier.weight(1f))
                ScoreCard(score = "$wrong", label = "Wrong", color = Wrong, modifier = Modifier.weight(1f))
                ScoreCard(
                    score = "$pct%", label = "Score", color = Text,
                    badge = "Grade: $grade", badgeColor = gradeColor,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(28.dp))

            val levelMap = mutableMapOf<Int, Pair<Int, Int>>()
            questions.forEach { q ->
                val cur = levelMap.getOrDefault(q.level, 0 to 0)
                levelMap[q.level] = (cur.first + (if (answers[q.id] == q.answer) 1 else 0)) to (cur.second + 1)
            }

            SectionTitle("By Level")
            levelMap.toSortedMap().forEach { (lv, stats) ->
                val (c, t) = stats
                val p = if (t > 0) (c * 100 / t) else 0
                LevelRow(level = lv, name = QuizLoader.levelName(lv), correct = c, total = t, pct = p, color = Color(QuizLoader.levelColorValue(lv)))
            }

            Spacer(Modifier.height(20.dp))

            val topicMap = mutableMapOf<String, Pair<Int, Int>>()
            questions.forEach { q ->
                val cur = topicMap.getOrDefault(q.topic, 0 to 0)
                topicMap[q.topic] = (cur.first + (if (answers[q.id] == q.answer) 1 else 0)) to (cur.second + 1)
            }

            SectionTitle("By Topic")
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                topicMap.toList().sortedByDescending { (_, v) -> v.second }.forEach { (topic, stats) ->
                    val (c, t) = stats
                    val p = if (t > 0) (c * 100 / t) else 0
                    TopicCard(topic = topic, correct = c, total = t, pct = p)
                }
            }

            Spacer(Modifier.height(20.dp))

            val wrongItems = questions.mapNotNull { q ->
                val ua = answers[q.id]
                if (ua != q.answer) q to ua else null
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { vm.startQuizFromQuestions(wrongItems.map { it.first }, shuffle = true) },
                    enabled = wrongItems.isNotEmpty(),
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Accent, disabledContainerColor = Surface2)
                ) {
                    Icon(Icons.Filled.Refresh, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Retry Missed", fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick = { vm.goToBrowse() },
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Border),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Text)
                ) {
                    Icon(Icons.Filled.Search, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Review Bank", fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(Modifier.height(20.dp))

            SectionTitle("Wrong Answers (${wrongItems.size})")

            if (wrongItems.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🎉 Perfect score! All answers correct.", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Correct)
                }
            } else {
                wrongItems.forEach { (q, ua) ->
                    WrongAnswerCard(q = q, userAnswer = ua)
                }
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
fun ScoreCard(score: String, label: String, color: Color, modifier: Modifier = Modifier, badge: String? = null, badgeColor: Color? = null) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Surface),
        border = BorderStroke(1.dp, Border)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(score, fontSize = 36.sp, fontWeight = FontWeight.ExtraBold, color = color)
            Spacer(Modifier.height(4.dp))
            Text(label, fontSize = 13.sp, color = TextMuted, fontWeight = FontWeight.Medium)
            if (badge != null) {
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background((badgeColor ?: color).copy(alpha = 0.15f))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(badge, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = badgeColor ?: color)
                }
            }
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextMuted, modifier = Modifier.padding(vertical = 12.dp))
}

@Composable
fun LevelRow(level: Int, name: String, correct: Int, total: Int, pct: Int, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Surface)
            .border(1.dp, Border, RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(color.copy(alpha = 0.15f))
                .padding(horizontal = 10.dp, vertical = 3.dp)
        ) {
            Text("Level $level · $name", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color)
        }
        Spacer(Modifier.width(12.dp))
        LinearProgressIndicator(
            progress = { pct / 100f },
            modifier = Modifier.weight(1f).height(8.dp).clip(RoundedCornerShape(4.dp)),
            color = color,
            trackColor = Border
        )
        Spacer(Modifier.width(12.dp))
        Text("$correct/$total ($pct%)", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = color)
    }
}

@Composable
fun TopicCard(topic: String, correct: Int, total: Int, pct: Int) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Surface),
        border = BorderStroke(1.dp, Border)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(topic, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Text)
            Spacer(Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { pct / 100f },
                modifier = Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(3.dp)),
                color = Accent,
                trackColor = Border
            )
            Spacer(Modifier.height(4.dp))
            Text("$correct/$total · $pct%", fontSize = 12.sp, color = TextMuted)
        }
    }
}

@Composable
fun WrongAnswerCard(q: Question, userAnswer: String?) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(containerColor = Surface),
        border = BorderStroke(1.dp, Border)
    ) {
        Column {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val lvlColor = when (q.level) {
                    0 -> Level0; 1 -> Level1; 2 -> Level2; 4 -> Level4; else -> Level5
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(lvlColor.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text("L${q.level}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = lvlColor)
                }
                Spacer(Modifier.width(10.dp))
                Text(q.question, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Text, modifier = Modifier.weight(1f))
                Text(if (expanded) "▼" else "▶", fontSize = 14.sp, color = TextDim)
            }
            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)) {
                    HorizontalDivider(color = Border)
                    Spacer(Modifier.height(10.dp))
                    Row {
                        Text("✕ Your answer: ", fontWeight = FontWeight.SemiBold, color = Wrong, fontSize = 13.sp)
                        Text(userAnswer ?: "Not answered", fontSize = 13.sp, color = TextMuted)
                    }
                    Spacer(Modifier.height(6.dp))
                    Row {
                        Text("✓ Correct: ", fontWeight = FontWeight.SemiBold, color = Correct, fontSize = 13.sp)
                        Text(q.answer, fontSize = 13.sp, color = Correct)
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .fillMaxHeight()
                                .background(Accent)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            q.explanation, fontSize = 13.sp, color = TextMuted,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White.copy(alpha = 0.04f))
                                .padding(12.dp)
                        )
                    }
                }
            }
        }
    }
}
