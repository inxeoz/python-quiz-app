package com.inxeoz.pythonquiz

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.inxeoz.pythonquiz.ui.screens.BottomNavBar
import com.inxeoz.pythonquiz.ui.screens.BottomNavItem
import com.inxeoz.pythonquiz.ui.screens.FlaggedScreen
import com.inxeoz.pythonquiz.ui.screens.ProfileScreen
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
            var forceDark by remember { mutableStateOf<Boolean?>(null) }
            val isDark = forceDark ?: isSystemInDarkTheme()

            PythonQuizTheme(darkTheme = isDark) {
                val vm: QuizViewModel = viewModel()
                val state by vm.state.collectAsState()
                var navScreen by remember { mutableStateOf(BottomNavItem.Home) }

                QuizApp(
                    currentTab = navScreen,
                    onNavigate = { navScreen = it },
                    vm = vm,
                    viewModelState = state,
                    onThemeToggle = { forceDark = if (forceDark == true) false else true }
                )
            }
        }
    }
}

@Composable
private fun QuizApp(
    currentTab: BottomNavItem,
    onNavigate: (BottomNavItem) -> Unit,
    vm: QuizViewModel,
    viewModelState: com.inxeoz.pythonquiz.viewmodel.QuizUiState,
    onThemeToggle: () -> Unit
) {
    val colors = LocalQuizColors.current

    Column(modifier = Modifier.fillMaxSize().background(colors.bg)) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            when {
                viewModelState.currentScreen == Screen.Quiz -> QuizScreen(vm = vm)
                viewModelState.currentScreen == Screen.Report -> ResultsScreen(vm = vm)
                else -> {
                    when (currentTab) {
                        BottomNavItem.Home -> WelcomeScreen(vm = vm)
                        BottomNavItem.Flagged -> FlaggedScreen(
                            vm = vm,
                            onBack = { onNavigate(BottomNavItem.Home) }
                        )
                        BottomNavItem.Profile -> ProfileScreen(
                            vm = vm,
                            onBack = { onNavigate(BottomNavItem.Home) },
                            onFlaggedQuestionsClick = { onNavigate(BottomNavItem.Flagged) },
                            onThemeToggle = onThemeToggle
                        )
                    }
                }
            }
        }

        BottomNavBar(
            activeItem = currentTab,
            onHomeClick = {
                vm.goToSetup()
                onNavigate(BottomNavItem.Home)
            },
            onFlaggedClick = { onNavigate(BottomNavItem.Flagged) },
            onProfileClick = { onNavigate(BottomNavItem.Profile) },
            modifier = Modifier.navigationBarsPadding()
        )
    }
}
