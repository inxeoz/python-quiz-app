package com.pythonquiz.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pythonquiz.app.ui.screens.BrowseScreen
import com.pythonquiz.app.ui.screens.QuizScreen
import com.pythonquiz.app.ui.screens.ReportScreen
import com.pythonquiz.app.ui.screens.SetupScreen
import com.pythonquiz.app.ui.theme.DarkBg
import com.pythonquiz.app.ui.theme.PythonQuizTheme
import com.pythonquiz.app.viewmodel.QuizViewModel
import com.pythonquiz.app.viewmodel.Screen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PythonQuizTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.systemBars),
                    color = DarkBg
                ) {
                    val vm: QuizViewModel = viewModel()
                    val state by vm.state.collectAsState()

                    when (state.currentScreen) {
                        Screen.Setup -> SetupScreen(
                            vm = vm,
                            onStart = { levels, count, shuffle, scope ->
                                vm.startQuiz(levels, count, shuffle, scope)
                            },
                            onBrowse = { vm.goToBrowse() }
                        )
                        Screen.Quiz -> QuizScreen(vm = vm)
                        Screen.Report -> ReportScreen(vm = vm)
                        Screen.Browse -> BrowseScreen(vm = vm)
                    }
                }
            }
        }
    }
}
