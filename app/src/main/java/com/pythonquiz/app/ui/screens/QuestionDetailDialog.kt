package com.pythonquiz.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.pythonquiz.app.data.Question
import com.pythonquiz.app.ui.theme.*

@Composable
fun QuestionDetailDialog(
    question: Question,
    isCompleted: Boolean,
    onDismiss: () -> Unit,
    onToggleCompleted: () -> Unit
) {
    val levelColor = when (question.level) { 0 -> Level0; 1 -> Level1; 2 -> Level2; 4 -> Level4; else -> Level5 }
    val levelNames = mapOf(0 to "Basic", 1 to "Beginner", 2 to "Intermediate", 4 to "Advanced", 5 to "Expert")
    val letters = listOf("A", "B", "C", "D", "E")

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            shape = RoundedCornerShape(16.dp),
            color = Surface,
            tonalElevation = 0.dp
        ) {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                // Top bar
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp, 12.dp, 16.dp, 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(levelColor.copy(alpha = 0.15f))
                                    .padding(horizontal = 10.dp, vertical = 3.dp)
                            ) {
                                Text("Level ${question.level} · ${levelNames[question.level]}",
                                    fontSize = 11.sp, fontWeight = FontWeight.Bold, color = levelColor)
                            }
                            Spacer(Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Surface2)
                                    .padding(horizontal = 10.dp, vertical = 3.dp)
                            ) {
                                Text(question.topic, fontSize = 11.sp, fontWeight = FontWeight.Bold,
                                    color = TextMuted, letterSpacing = 0.5.sp)
                            }
                        }
                    }
                    IconButton(onClick = onToggleCompleted) {
                        Icon(
                            if (isCompleted) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                            "Toggle completed",
                            tint = if (isCompleted) Correct else TextDim
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, "Close", tint = TextDim)
                    }
                }

                HorizontalDivider(color = Border, thickness = 0.5.dp)

                Column(modifier = Modifier.padding(16.dp)) {
                    Text(question.question, fontSize = 17.sp, fontWeight = FontWeight.Medium, color = Text, lineHeight = 24.sp)

                    Spacer(Modifier.height(20.dp))

                    question.options.forEachIndexed { i, opt ->
                        val isCorrectOption = opt == question.answer

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isCorrectOption) Correct.copy(alpha = 0.15f) else Surface)
                                .border(1.5.dp, if (isCorrectOption) Correct else Border, RoundedCornerShape(10.dp))
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier.size(28.dp).clip(RoundedCornerShape(50))
                                    .background(if (isCorrectOption) Correct else Border),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(letters.getOrElse(i) { "$i" }, fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCorrectOption) Color.White else TextMuted)
                            }
                            Spacer(Modifier.width(14.dp))
                            Text(opt, fontSize = 15.sp, color = Text, modifier = Modifier.weight(1f))
                            if (isCorrectOption) {
                                Icon(Icons.Filled.CheckCircle, "Correct", tint = Correct, modifier = Modifier.size(20.dp))
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color.White.copy(alpha = 0.04f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(modifier = Modifier.padding(14.dp)) {
                            Box(modifier = Modifier.width(4.dp).fillMaxHeight()
                                .background(Accent).clip(RoundedCornerShape(2.dp)))
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text("💡 Explanation:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Text)
                                Spacer(Modifier.height(4.dp))
                                Text(question.explanation, fontSize = 13.sp, color = TextMuted, lineHeight = 18.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
