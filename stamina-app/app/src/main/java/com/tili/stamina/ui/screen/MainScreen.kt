package com.tili.stamina.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.tili.stamina.R
import com.tili.stamina.ui.component.ConsumePanel
import com.tili.stamina.ui.component.RecoveryPanel
import com.tili.stamina.ui.component.StaminaRing
import com.tili.stamina.ui.theme.Black30
import com.tili.stamina.ui.theme.Emerald400
import com.tili.stamina.ui.theme.Emerald500
import com.tili.stamina.ui.theme.White90
import com.tili.stamina.ui.theme.Zinc900
import kotlinx.coroutines.delay

/**
 * 主界面 — 体力展示与操作。
 * 原型对应：HTML 中的 viewMain 区域。
 */
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    onNavigateToTheme: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // 生命周期管理：可见时 tick，不可见时停止
    DisposableEffect(Unit) {
        viewModel.resumeTicking()
        onDispose {
            viewModel.stopTicking()
        }
    }

    // Toast 自动消失
    LaunchedEffect(state.toastMessage) {
        if (state.toastMessage != null) {
            delay(3000L)
            viewModel.clearToast()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // ── 背景层 ──
        BackgroundLayer(
            bgUri = state.appBgUri,
            presetIndex = state.appPresetIndex
        )

        // ── 前景内容 ──
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 48.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 顶部栏：同步状态 + 设置齿轮
            TopBar(onSettingsClick = onNavigateToTheme)

            Spacer(modifier = Modifier.height(24.dp))

            // 体力环
            StaminaRing(
                currentStamina = state.currentStamina,
                maxStamina = state.maxStamina,
                modifier = Modifier.size(224.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 恢复状态面板
            RecoveryPanel(
                isFull = state.isFull,
                secondsUntilNext = state.secondsUntilNextPoint,
                secondsToFull = state.secondsToFull
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 快捷消耗面板
            ConsumePanel(
                onConsume = { points, action ->
                    viewModel.consumeStamina(points, action)
                }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        // ── Toast 提示 ──
        AnimatedVisibility(
            visible = state.toastMessage != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            state.toastMessage?.let { msg ->
                Box(
                    modifier = Modifier
                        .padding(bottom = 32.dp)
                        .background(
                            Zinc900.copy(alpha = 0.95f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = msg,
                        fontSize = 12.sp,
                        color = White90
                    )
                }
            }
        }
    }
}

/**
 * 顶部状态栏
 */
@Composable
private fun TopBar(onSettingsClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 同步状态指示
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .background(Black30, RoundedCornerShape(20.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(Emerald400, CircleShape)
            )
            Spacer(modifier = Modifier.size(6.dp))
            Text(
                text = "体力同步中",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = White90.copy(alpha = 0.9f)
            )
        }

        // 设置齿轮按钮
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Black30)
                .clickable(onClick = onSettingsClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Palette,
                contentDescription = "主题设置",
                tint = White90.copy(alpha = 0.9f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

/**
 * 背景层：根据是否有自定义 URI 显示预设 drawable 或自定义图片。
 */
@Composable
private fun BackgroundLayer(bgUri: String, presetIndex: Int) {
    val context = LocalContext.current

    if (bgUri.isNotEmpty()) {
        // 用户自定义背景
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(android.net.Uri.parse(bgUri))
                .crossfade(true)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    } else {
        // 预设背景
        val presetRes = when (presetIndex) {
            0 -> R.drawable.bg_preset_1
            1 -> R.drawable.bg_preset_2
            else -> R.drawable.bg_preset_3
        }
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(presetRes)
                .crossfade(true)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }

    // 半透明黑色遮罩，保证文字可读性
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Black30)
    )
}
