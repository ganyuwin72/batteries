package com.tili.stamina.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tili.stamina.ui.theme.Amber400
import com.tili.stamina.ui.theme.Black30
import com.tili.stamina.ui.theme.Emerald400
import com.tili.stamina.ui.theme.White10
import com.tili.stamina.ui.theme.White60
import com.tili.stamina.ui.theme.White90
import com.tili.stamina.ui.theme.Zinc800
import com.tili.stamina.ui.theme.Zinc900

/**
 * 体力恢复状态面板 — 显示距离下一点回复倒计时和预计充满时间。
 * 对应 HTML 原型中的 glass-panel 恢复状态区。
 *
 * @param isFull 体力是否已满
 * @param secondsUntilNext 距离下一点回复的秒数
 * @param secondsToFull 预计充满还需要多少秒
 */
@Composable
fun RecoveryPanel(
    isFull: Boolean,
    secondsUntilNext: Long,
    secondsToFull: Long,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = Black30.copy(alpha = 0.75f),
                shape = RoundedCornerShape(16.dp)
            )
            .background(
                color = White10,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (isFull) {
            Text(
                text = "体力已满，处于巅峰状态",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Emerald400
            )
        } else {
            Text(
                text = "体力正在恢复中…",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Amber400
            )

            // 下一点倒计时
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "距离下一点回复：",
                    fontSize = 11.sp,
                    color = White60
                )
                Text(
                    text = formatCountdown(secondsUntilNext),
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = White90,
                    modifier = Modifier
                        .background(Zinc800, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            // 预计充满
            Text(
                text = "预计充满还需要：${formatHoursMinutes(secondsToFull)}",
                fontSize = 11.sp,
                color = White60.copy(alpha = 0.7f)
            )
        }
    }
}

/** 格式化秒数为 mm:ss */
fun formatCountdown(totalSeconds: Long): String {
    val sec = totalSeconds.coerceAtLeast(0)
    val m = sec / 60
    val s = sec % 60
    return "${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}"
}

/** 格式化秒数为 XX小时XX分钟 */
fun formatHoursMinutes(totalSeconds: Long): String {
    val sec = totalSeconds.coerceAtLeast(0)
    val totalMin = (sec + 59) / 60 // 向上取整分钟
    val h = totalMin / 60
    val m = totalMin % 60
    return "${h.toString().padStart(2, '0')}小时${m.toString().padStart(2, '0')}分钟"
}
