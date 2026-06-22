package com.tili.stamina.ui.component

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.tili.stamina.ui.theme.Emerald500
import com.tili.stamina.ui.theme.White60
import com.tili.stamina.ui.theme.Zinc800
import com.tili.stamina.ui.theme.Zinc900

/**
 * 背景图片选择器 — 预设网格 + 自定义上传。
 * 对应 HTML 原型中主题设置界面的背景选择区域。
 *
 * @param title 标签文字
 * @param selectedUri 当前选中的 URI（空字符串表示默认）
 * @param onPresetSelected 预设选中回调 (preset index: 0,1,2)
 * @param onCustomUriSelected 自定义图片 URI 选中回调
 */
@Composable
fun BackgroundPicker(
    title: String,
    selectedUri: String,
    selectedPresetIndex: Int,
    onPresetSelected: (Int) -> Unit,
    onCustomUriSelected: (Uri) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // 相册选择器 — 带永久读取权限
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            // 获取永久读取权限（Android 11+ 关键步骤）
            val flags = android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
            try {
                context.contentResolver.takePersistableUriPermission(it, flags)
            } catch (_: SecurityException) {
                // 某些 URI 不支持持久化权限，仍可临时使用
            }
            onCustomUriSelected(it)
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 标题
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = White60
        )

        // 预设图片网格 (3 列)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for (index in 0..2) {
                val isSelected = selectedPresetIndex == index &&
                        selectedUri.isEmpty() // 只有未使用自定义图片时才高亮预设

                PresetThumbnail(
                    drawableRes = when (index) {
                        0 -> R.drawable.bg_preset_1
                        1 -> R.drawable.bg_preset_2
                        else -> R.drawable.bg_preset_3
                    },
                    isSelected = isSelected,
                    onClick = { onPresetSelected(index) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 上传自定义图片按钮
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Zinc900)
                .border(1.dp, Zinc800, RoundedCornerShape(12.dp))
                .clickable { galleryLauncher.launch("image/*") }
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CloudUpload,
                    contentDescription = null,
                    tint = White60,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "从相册选择自定义图片",
                    fontSize = 12.sp,
                    color = White60.copy(alpha = 0.8f)
                )
            }
        }
    }
}

/**
 * 预设缩略图
 */
@Composable
private fun PresetThumbnail(
    drawableRes: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderModifier = if (isSelected) {
        Modifier.border(2.dp, Emerald500, RoundedCornerShape(12.dp))
    } else {
        Modifier.border(1.dp, Zinc800, RoundedCornerShape(12.dp))
    }

    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(drawableRes)
            .crossfade(true)
            .build(),
        contentDescription = "背景预设",
        contentScale = ContentScale.Crop,
        modifier = modifier
            .height(64.dp)
            .clip(RoundedCornerShape(12.dp))
            .then(borderModifier)
            .clickable(onClick = onClick)
    )
}
