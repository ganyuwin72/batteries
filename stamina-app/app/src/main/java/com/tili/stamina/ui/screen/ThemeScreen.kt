package com.tili.stamina.ui.screen

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tili.stamina.ui.component.BackgroundPicker
import com.tili.stamina.ui.theme.Emerald400
import com.tili.stamina.ui.theme.Emerald500
import com.tili.stamina.ui.theme.White60
import com.tili.stamina.ui.theme.White90
import com.tili.stamina.ui.theme.Zinc800
import com.tili.stamina.ui.theme.Zinc950

/**
 * 主题设置页面 — 更换 App 背景和小组件背景。
 * 原型对应：HTML 中的 viewTheme 区域。
 */
@Composable
fun ThemeScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    // 本地编辑状态（用于预设切换预览）
    var appCustomUri by remember { mutableStateOf<String?>(null) }
    var widgetCustomUri by remember { mutableStateOf<String?>(null) }
    var appPresetIdx by remember { mutableIntStateOf(state.appPresetIndex) }
    var widgetPresetIdx by remember { mutableIntStateOf(state.widgetPresetIndex) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Zinc950)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 48.dp, bottom = 24.dp)
    ) {
        // 顶栏：返回按钮 + 标题
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "返回",
                tint = White60,
                modifier = Modifier
                    .size(24.dp)
                    .clickable(onClick = onNavigateBack)
            )
            Spacer(modifier = Modifier.size(12.dp))
            Text(
                text = "主题与背景设置",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = White90
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        Divider(color = Zinc800)
        Spacer(modifier = Modifier.height(20.dp))

        // ── App 背景选择 ──
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Image,
                contentDescription = null,
                tint = Emerald400,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.size(6.dp))
            Text(
                text = "主应用背景图片",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = White60
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        BackgroundPicker(
            title = "",
            selectedUri = appCustomUri ?: state.appBgUri,
            selectedPresetIndex = appPresetIdx,
            onPresetSelected = { index ->
                appPresetIdx = index
                appCustomUri = null
                viewModel.setAppBackground("", index)
            },
            onCustomUriSelected = { uri ->
                appCustomUri = uri.toString()
                appPresetIdx = -1
                viewModel.setAppBackground(uri.toString(), -1)
            }
        )

        Spacer(modifier = Modifier.height(32.dp))

        // ── 小组件背景选择 ──
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Widgets,
                contentDescription = null,
                tint = Emerald400,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.size(6.dp))
            Text(
                text = "桌面小组件背景图片",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = White60
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        BackgroundPicker(
            title = "",
            selectedUri = widgetCustomUri ?: state.widgetBgUri,
            selectedPresetIndex = widgetPresetIdx,
            onPresetSelected = { index ->
                widgetPresetIdx = index
                widgetCustomUri = null
                viewModel.setWidgetBackground("", index)
            },
            onCustomUriSelected = { uri ->
                widgetCustomUri = uri.toString()
                widgetPresetIdx = -1
                viewModel.setWidgetBackground(uri.toString(), -1)
            }
        )

        Spacer(modifier = Modifier.height(32.dp))

        // 保存并返回按钮
        Button(
            onClick = onNavigateBack,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Emerald500,
                contentColor = White90
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "保存并返回",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
    }
}
