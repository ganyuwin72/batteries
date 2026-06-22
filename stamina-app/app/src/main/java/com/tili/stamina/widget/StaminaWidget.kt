package com.tili.stamina.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.Action
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.background
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.defaultWeight
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import com.tili.stamina.R
import com.tili.stamina.data.PreferencesManager
import com.tili.stamina.data.StaminaCalculator
import com.tili.stamina.data.WidgetState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 体力桌面小组件 — 基于 Jetpack Glance 实现。
 *
 * 布局对应 HTML 原型中的 "桌面小组件模拟" 区域。
 * - 显示当前体力值 + 进度条
 * - 显示倒计时信息
 * - 提供 -20, -40 两个快捷消耗按钮
 * - 支持用户自定义背景图片
 */
class StaminaWidget : GlanceAppWidget() {

    companion object {
        /** 小组件专用 action: 消耗体力 */
        const val ACTION_CONSUME = "com.tili.stamina.action.CONSUME"
        const val EXTRA_POINTS = "consume_points"

        /** 便捷方法：刷新所有小组件实例 */
        suspend fun updateWidget(context: Context) {
            val manager = GlanceAppWidgetManager(context)
            val glanceIds = manager.getGlanceIds(StaminaWidget::class.java)
            glanceIds.forEach { glanceId ->
                StaminaWidget().update(context, glanceId)
            }
        }
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // 从 DataStore 读取体力状态
        val state = loadWidgetState(context)

        provideContent {
            GlanceTheme {
                WidgetContent(state = state)
            }
        }
    }

    /** 加载小组件所需数据 */
    private suspend fun loadWidgetState(context: Context): WidgetState {
        return withContext(Dispatchers.IO) {
            val prefsManager = PreferencesManager(context)
            val prefs = prefsManager.getStaminaPrefs()
            val now = System.currentTimeMillis()

            val result = StaminaCalculator.refresh(
                lastStamina = prefs.currentStamina,
                lastTime = prefs.lastUpdateTime,
                currentTime = now
            )

            val secondsUntilNext = StaminaCalculator.secondsUntilNextPoint(
                result.stamina, result.savedTime, now
            )
            val secondsToFull = StaminaCalculator.estimatedSecondsToFull(
                result.stamina, secondsUntilNext
            )
            val widgetBg = prefsManager.getWidgetBgUri()

            WidgetState(
                currentStamina = result.stamina,
                maxStamina = StaminaCalculator.MAX_STAMINA,
                isFull = result.stamina >= StaminaCalculator.MAX_STAMINA,
                secondsUntilNextPoint = secondsUntilNext,
                secondsToFull = secondsToFull,
                appBgUri = "",
                widgetBgUri = widgetBg
            )
        }
    }
}

/**
 * 小组件 Compose 布局。
 * 对应 HTML 原型中的 widgetCard。
 */
@Composable
fun WidgetContent(state: WidgetState) {
    val context = LocalContext.current
    val progress = (state.currentStamina.toFloat() / state.maxStamina).coerceIn(0f, 1f)

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.background)
            .cornerRadius(24.dp)
    ) {
        // ── 背景图片 ──
        if (state.widgetBgUri.isNotEmpty()) {
            try {
                val bitmap = loadWidgetBitmap(context, state.widgetBgUri)
                if (bitmap != null) {
                    Image(
                        provider = ImageProvider(bitmap),
                        contentDescription = null,
                        modifier = GlanceModifier.fillMaxSize()
                    )
                }
            } catch (_: Exception) {
                // 加载失败则显示默认背景
            }
        }

        // ── 半透明遮罩层 ──
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.45f))
        )

        // ── 前景内容 ──
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // 顶部：标题 + 体力数字
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Top
            ) {
                // 左侧：图标 + 标题 + 倒计时
                Column(modifier = GlanceModifier.defaultWeight()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = GlanceModifier
                                .size(24.dp)
                                .background(Color(0xFF10B981))
                                .cornerRadius(6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "⚡",
                                style = TextStyle(fontSize = 12.sp)
                            )
                        }

                        Spacer(modifier = GlanceModifier.width(8.dp))

                        Column {
                            Text(
                                text = "当前剩余体力",
                                style = TextStyle(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                            Text(
                                text = if (state.isFull) {
                                    "已满"
                                } else {
                                    "+1点 / ${formatCountdown(state.secondsUntilNextPoint)}"
                                },
                                style = TextStyle(
                                    fontSize = 10.sp,
                                    color = if (state.isFull)
                                        Color(0xFF34D399)
                                    else
                                        Color.White.copy(alpha = 0.7f)
                                )
                            )
                        }
                    }
                }

                // 右侧：体力数字
                Column(horizontalAlignment = Alignment.End) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = state.currentStamina.toString(),
                            style = TextStyle(
                                fontSize = 30.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        )
                        Text(
                            text = "/${state.maxStamina}",
                            style = TextStyle(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.6f)
                            )
                        )
                    }
                }
            }

            Spacer(modifier = GlanceModifier.height(8.dp))

            // ── 进度条（使用 Row + weight 实现 Glance 兼容的百分比宽度）──
            Row(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .cornerRadius(4.dp)
            ) {
                // 已填充部分（翠绿色）
                if (state.currentStamina > 0) {
                    Box(
                        modifier = GlanceModifier
                            .defaultWeight(state.currentStamina.toFloat())
                            .height(8.dp)
                            .background(Color(0xFF34D399))
                    )
                }
                // 未填充部分（半透明白色）
                if (state.maxStamina - state.currentStamina > 0) {
                    Box(
                        modifier = GlanceModifier
                            .defaultWeight((state.maxStamina - state.currentStamina).toFloat())
                            .height(8.dp)
                            .background(Color.White.copy(alpha = 0.15f))
                    )
                }
            }

            Spacer(modifier = GlanceModifier.height(8.dp))

            // ── 底部：快捷消耗按钮 ──
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "快捷消耗:",
                    style = TextStyle(
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                )

                Spacer(modifier = GlanceModifier.width(8.dp))

                // -20 按钮
                Box(
                    modifier = GlanceModifier
                        .background(Color.White.copy(alpha = 0.15f))
                        .cornerRadius(8.dp)
                        .clickable(
                            actionRunCallback<ConsumeCallback>(
                                actionParametersOf(ConsumeCallback.KEY_POINTS to 20)
                            )
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "-20",
                        style = TextStyle(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    )
                }

                Spacer(modifier = GlanceModifier.width(6.dp))

                // -40 按钮
                Box(
                    modifier = GlanceModifier
                        .background(Color.White.copy(alpha = 0.15f))
                        .cornerRadius(8.dp)
                        .clickable(
                            actionRunCallback<ConsumeCallback>(
                                actionParametersOf(ConsumeCallback.KEY_POINTS to 40)
                            )
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "-40",
                        style = TextStyle(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    )
                }
            }
        }
    }
}

/** 从 URI 加载小组件用的 Bitmap（已压缩） */
private fun loadWidgetBitmap(context: Context, uriString: String): Bitmap? {
    return try {
        val uri = Uri.parse(uriString)
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val options = BitmapFactory.Options().apply {
            // 小组件图片最大尺寸限制在 400x400 以内
            inJustDecodeBounds = true
            BitmapFactory.decodeStream(inputStream, null, this)
            inputStream.close()

            val scaleFactor = maxOf(
                (outWidth / 400).coerceAtLeast(1),
                (outHeight / 400).coerceAtLeast(1)
            )
            inSampleSize = scaleFactor
            inJustDecodeBounds = false
        }

        val stream = context.contentResolver.openInputStream(uri) ?: return null
        val bitmap = BitmapFactory.decodeStream(stream, null, options)
        stream.close()
        bitmap
    } catch (_: Exception) {
        null
    }
}

/** 格式化倒计时 */
private fun formatCountdown(totalSeconds: Long): String {
    val sec = totalSeconds.coerceAtLeast(0)
    val m = sec / 60
    val s = sec % 60
    return "${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}"
}
