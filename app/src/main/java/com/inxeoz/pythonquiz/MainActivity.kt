package com.inxeoz.pythonquiz

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.inxeoz.pythonquiz.ui.screens.QuizScreen
import com.inxeoz.pythonquiz.ui.screens.ResultsScreen
import com.inxeoz.pythonquiz.ui.screens.WelcomeScreen
import com.inxeoz.pythonquiz.ui.theme.LocalQuizColors
import com.inxeoz.pythonquiz.ui.theme.PythonQuizTheme
import com.inxeoz.pythonquiz.viewmodel.QuizViewModel
import com.inxeoz.pythonquiz.viewmodel.Screen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val systemDark = isSystemInDarkTheme()
            var forceDark by remember { mutableStateOf(systemDark) }

            PythonQuizTheme(darkTheme = forceDark) {
                val vm: QuizViewModel = viewModel()
                val state by vm.state.collectAsState()
                val onThemeToggle = { forceDark = !forceDark }

                QuizApp(
                    vm = vm,
                    viewModelState = state,
                    isDark = forceDark,
                    onThemeToggle = onThemeToggle
                )
            }
        }
    }
}

@Composable
private fun QuizApp(
    vm: QuizViewModel,
    viewModelState: com.inxeoz.pythonquiz.viewmodel.QuizUiState,
    isDark: Boolean,
    onThemeToggle: () -> Unit
) {
    val colors = LocalQuizColors.current

    Box(modifier = Modifier.fillMaxSize().background(colors.bg)) {
        when {
            viewModelState.loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = colors.accent
                )
            }
            viewModelState.currentScreen == Screen.Quiz -> QuizScreen(vm = vm, isDark = isDark, onThemeToggle = onThemeToggle)
            viewModelState.currentScreen == Screen.Report -> ResultsScreen(vm = vm, isDark = isDark, onThemeToggle = onThemeToggle)
            else -> WelcomeScreen(vm = vm, isDark = isDark, onThemeToggle = onThemeToggle)
        }
    }
}
