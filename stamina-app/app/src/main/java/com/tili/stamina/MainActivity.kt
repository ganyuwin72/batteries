package com.tili.stamina

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.tili.stamina.ui.screen.MainScreen
import com.tili.stamina.ui.screen.MainViewModel
import com.tili.stamina.ui.screen.ThemeScreen
import com.tili.stamina.ui.theme.StaminaTheme

/**
 * 主 Activity — 单 Activity 架构。
 *
 * 使用 Compose 状态管理导航，不引入 Navigation 库以保持轻量。
 * 两个界面：
 * - MainScreen: 体力主界面
 * - ThemeScreen: 主题背景设置
 */
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            StaminaTheme {
                StaminaApp(viewModel = viewModel)
            }
        }
    }
}

/**
 * 简单的界面导航枚举
 */
private enum class Screen {
    MAIN, THEME
}

@Composable
private fun StaminaApp(viewModel: MainViewModel) {
    var currentScreen by remember { mutableStateOf(Screen.MAIN) }

    Crossfade(targetState = currentScreen, label = "screen_transition") { screen ->
        when (screen) {
            Screen.MAIN -> {
                MainScreen(
                    viewModel = viewModel,
                    onNavigateToTheme = { currentScreen = Screen.THEME }
                )
            }
            Screen.THEME -> {
                ThemeScreen(
                    viewModel = viewModel,
                    onNavigateBack = { currentScreen = Screen.MAIN }
                )
            }
        }
    }
}
