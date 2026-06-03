package com.inxeoz.pythonquiz.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val LightColorScheme = lightColorScheme(
    primary = LightAccent, secondary = LightAccent,
    background = LightBg, surface = LightSurface, surfaceVariant = LightSurface2,
    onPrimary = Color.White, onSecondary = Color.White,
    onBackground = LightText, onSurface = LightText, onSurfaceVariant = LightTextMuted,
    outline = LightBorder, error = LightWrong,
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkAccent, secondary = DarkAccent,
    background = DarkBg, surface = DarkSurface, surfaceVariant = DarkSurface2,
    onPrimary = Color.White, onSecondary = Color.White,
    onBackground = DarkText, onSurface = DarkText, onSurfaceVariant = DarkTextMuted,
    outline = DarkBorder, error = DarkWrong,
)

@Composable
fun PythonQuizTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val quizColors = if (darkTheme) DarkQuizColors else LightQuizColors
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    CompositionLocalProvider(LocalQuizColors provides quizColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = MaterialTheme.typography.copy(
                headlineLarge = TextStyle(fontSize = 28.sp, lineHeight = 32.sp, fontWeight = FontWeight.Bold),
                headlineMedium = TextStyle(fontSize = 22.sp, lineHeight = 28.sp, fontWeight = FontWeight.Bold),
                titleLarge = TextStyle(fontSize = 20.sp, lineHeight = 26.sp, fontWeight = FontWeight.Bold),
                titleMedium = TextStyle(fontSize = 16.sp, lineHeight = 22.sp, fontWeight = FontWeight.SemiBold),
                bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 24.sp),
                bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 20.sp),
                labelLarge = TextStyle(fontSize = 14.sp, lineHeight = 18.sp, fontWeight = FontWeight.SemiBold),
                labelMedium = TextStyle(fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.SemiBold),
            ),
            content = content
        )
    }
}
