package com.inxeoz.pythonquiz.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ── Light palette (matches HTML oklch tokens) ──
val LightBg = Color(0xFFF6F9FB)
val LightSurface = Color(0xFFFFFFFF)
val LightSurface2 = Color(0xFFF0F4F6)
val LightBorder = Color(0xFFDBDFE2)
val LightText = Color(0xFF0E171E)
val LightTextMuted = Color(0xFF5A656D)
val LightTextDim = Color(0xFF89939B)
val LightAccent = Color(0xFF008A6A)
val LightAccentMuted = Color(0xFFCBEEE0)
val LightCorrect = Color(0xFF008B1D)
val LightWrong = Color(0xFFD41101)
val LightWarning = Color(0xFFAE7300)

// ── Dark palette (matches HTML oklch tokens) ──
val DarkBg = Color(0xFF050A0F)
val DarkSurface = Color(0xFF12171B)
val DarkSurface2 = Color(0xFF1B2126)
val DarkBorder = Color(0xFF25292E)
val DarkText = Color(0xFFDBDEE1)
val DarkTextMuted = Color(0xFF7C8186)
val DarkTextDim = Color(0xFF5E6469)
val DarkAccent = Color(0xFF00A079)
val DarkAccentMuted = Color(0xFF09281F)
val DarkCorrect = Color(0xFF1FBF4A)
val DarkWrong = Color(0xFFEF4444)
val DarkWarning = Color(0xFFD99A00)

@Immutable
data class QuizColors(
    val bg: Color, val surface: Color, val surface2: Color,
    val border: Color, val text: Color, val textMuted: Color, val textDim: Color,
    val accent: Color, val accentMuted: Color,
    val correct: Color, val wrong: Color, val warning: Color,
    // level badge colors (same both themes)
    val levelBasic: Color = Color(0xFF6B7280),
    val levelBeginner: Color = Color(0xFF3B82F6),
    val levelIntermediate: Color = Color(0xFF00A079),
    val levelAdvanced: Color = Color(0xFFC68615),
    val levelExpert: Color = Color(0xFFEF4444),
)

val LightQuizColors = QuizColors(
    bg = LightBg, surface = LightSurface, surface2 = LightSurface2,
    border = LightBorder, text = LightText, textMuted = LightTextMuted, textDim = LightTextDim,
    accent = LightAccent, accentMuted = LightAccentMuted,
    correct = LightCorrect, wrong = LightWrong, warning = LightWarning,
)

val DarkQuizColors = QuizColors(
    bg = DarkBg, surface = DarkSurface, surface2 = DarkSurface2,
    border = DarkBorder, text = DarkText, textMuted = DarkTextMuted, textDim = DarkTextDim,
    accent = DarkAccent, accentMuted = DarkAccentMuted,
    correct = DarkCorrect, wrong = DarkWrong, warning = DarkWarning,
)

val LocalQuizColors = staticCompositionLocalOf { LightQuizColors }
