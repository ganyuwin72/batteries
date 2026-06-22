package com.tili.stamina.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tili.stamina.ui.theme.Emerald400
import com.tili.stamina.ui.theme.Emerald600
import com.tili.stamina.ui.theme.White08
import com.tili.stamina.ui.theme.White60
import com.tili.stamina.ui.theme.White90

/**
 * 体力环组件 — 带渐变色动画的圆形进度条。
 * 对应 HTML 原型中的 SVG 环形体力槽。
 *
 * @param currentStamina 当前体力值
 * @param maxStamina 最大体力值 (默认 200)
 * @param modifier Modifier
 * @param strokeWidth 环形粗细
 * @param size 组件尺寸
 */
@Composable
fun StaminaRing(
    currentStamina: Int,
    maxStamina: Int = 200,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 7.dp,
    size: Dp = 224.dp
) {
    val progress = (currentStamina.toFloat() / maxStamina).coerceIn(0f, 1f)

    // 动画过渡
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 500),
        label = "stamina_progress"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.aspectRatio(1f)
    ) {
        // 环形进度条
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasSize = this.size.minDimension
            val stroke = strokeWidth.toPx()
            val topLeft = Offset(
                (this.size.width - canvasSize) / 2 + stroke / 2,
                (this.size.height - canvasSize) / 2 + stroke / 2
            )
            val arcSize = Size(canvasSize - stroke, canvasSize - stroke)

            // 背景环
            drawArc(
                color = Color.White.copy(alpha = 0.08f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )

            // 前景渐变环
            val gradientBrush = Brush.sweepGradient(
                colors = listOf(Emerald400, Emerald600, Emerald400)
            )
            drawArc(
                brush = gradientBrush,
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }

        // 中央数字
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = currentStamina.toString(),
                fontSize = 48.sp,
                fontWeight = FontWeight.Black,
                color = White90,
                letterSpacing = (-1.5).sp
            )
            Text(
                text = "MAX $maxStamina",
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = White60,
                letterSpacing = 3.sp
            )
        }
    }
}
