package com.tili.stamina.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tili.stamina.ui.theme.Black30
import com.tili.stamina.ui.theme.Black40
import com.tili.stamina.ui.theme.Emerald400
import com.tili.stamina.ui.theme.Emerald500
import com.tili.stamina.ui.theme.White10
import com.tili.stamina.ui.theme.White60
import com.tili.stamina.ui.theme.White90
import com.tili.stamina.ui.theme.Zinc700
import com.tili.stamina.ui.theme.Zinc800

/**
 * 快捷消耗面板 — 预设按钮 + 自定义输入。
 * 对应 HTML 原型中底部的消耗操作区。
 *
 * @param onConsume 回调: (扣除点数, 操作名称)
 */
@Composable
fun ConsumePanel(
    onConsume: (Int, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var customValue by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = Black30.copy(alpha = 0.75f),
                shape = RoundedCornerShape(24.dp)
            )
            .background(
                color = White10,
                shape = RoundedCornerShape(24.dp)
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 标题栏
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "⚡ 记录当前消耗",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = White60
            )
            Text(
                text = "规整日常，拒绝拖延",
                fontSize = 10.sp,
                color = White60.copy(alpha = 0.5f)
            )
        }

        // 预设快捷按钮组（3 列网格）
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            QuickConsumeButton(
                points = 10,
                label = "冥想",
                onClick = { onConsume(10, "冥想/放松") },
                modifier = Modifier.weight(1f)
            )
            QuickConsumeButton(
                points = 30,
                label = "写作",
                onClick = { onConsume(30, "常规写作/记录") },
                modifier = Modifier.weight(1f)
            )
            QuickConsumeButton(
                points = 50,
                label = "深度工作",
                onClick = { onConsume(50, "深度心流工作") },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // 自定义扣除输入行
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = customValue,
                onValueChange = { value ->
                    // 只允许数字输入
                    if (value.isEmpty() || value.all { it.isDigit() }) {
                        customValue = value
                    }
                },
                placeholder = {
                    Text(
                        text = "自定义扣除点数…",
                        fontSize = 12.sp,
                        color = White60.copy(alpha = 0.4f)
                    )
                },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = White90,
                    unfocusedTextColor = White90,
                    focusedBorderColor = Emerald500,
                    unfocusedBorderColor = Zinc800,
                    cursorColor = Emerald500,
                    focusedContainerColor = Black40,
                    unfocusedContainerColor = Black40
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Button(
                onClick = {
                    val points = customValue.toIntOrNull()
                    if (points != null && points > 0) {
                        onConsume(points, "自定义事务")
                        customValue = ""
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Emerald500,
                    contentColor = White90
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "扣除",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * 单个快捷消耗按钮
 */
@Composable
private fun QuickConsumeButton(
    points: Int,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = Zinc800.copy(alpha = 0.8f),
            contentColor = White90
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
            Text(
                text = "-$points",
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Emerald400
            )
            Text(
                text = label,
                fontSize = 10.sp,
                color = White60
            )
        }
    }
}
