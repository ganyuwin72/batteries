package com.tili.stamina.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.glance.appwidget.GlanceAppWidgetReceiver

import com.tili.stamina.data.PreferencesManager
import com.tili.stamina.data.StaminaCalculator

/**
 * 小组件 BroadcastReceiver — 系统添加小组件时由框架自动调用。
 * 继承 GlanceAppWidgetReceiver 简化 AppWidgetProvider 样板代码。
 */
class StaminaWidgetReceiver : GlanceAppWidgetReceiver() {

    override val glanceAppWidget: StaminaWidget = StaminaWidget()

    override fun onReceive(context: Context, intent: Intent) {
        // 让 Glance 框架先处理 APPWIDGET_UPDATE 等标准 action
        super.onReceive(context, intent)
    }
}

/**
 * 小组件按钮消耗体力回调。
 *
 * 当用户在小组件上点击 -20 或 -40 按钮时，
 * Glance 框架在工作线程上调用此 ActionCallback。
 */
class ConsumeCallback : androidx.glance.appwidget.action.ActionCallback {

    companion object {
        const val KEY_POINTS = "consume_points"
        private const val TAG = "ConsumeCallback"
    }

    override suspend fun onAction(
        context: Context,
        glanceId: androidx.glance.GlanceId,
        parameters: android.os.Bundle
    ) {
        val points = parameters.getInt(KEY_POINTS, 0)
        if (points <= 0) return

        try {
            // 1. 读取当前体力
            val prefsManager = PreferencesManager(context)
            val prefs = prefsManager.getStaminaPrefs()

            // 2. 刷新体力
            val now = System.currentTimeMillis()
            val result = StaminaCalculator.refresh(
                lastStamina = prefs.currentStamina,
                lastTime = prefs.lastUpdateTime,
                currentTime = now
            )

            // 3. 扣除（最低到 0）
            val finalStamina = maxOf(0, result.stamina - points)

            // 4. 持久化
            prefsManager.saveStamina(finalStamina, System.currentTimeMillis())

            // 5. 刷新小组件显示
            StaminaWidget().update(context, glanceId)

            Log.d(TAG, "Widget 消耗 $points 点体力: ${result.stamina} → $finalStamina")
        } catch (e: Exception) {
            Log.e(TAG, "小组件消耗体力失败", e)
        }
    }
}

/**
 * 处理小组件消耗体力广播的 Receiver。
 * 用作 Glance Action 的补充（当 ActionCallback 不可用时的备选方案）。
 */
class WidgetActionReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_CONSUME = "com.tili.stamina.action.CONSUME"
        const val EXTRA_POINTS = "consume_points"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_CONSUME) return

        val points = intent.getIntExtra(EXTRA_POINTS, 0)
        if (points <= 0) return

        // 在后台线程执行
        kotlinx.coroutines.runBlocking {
            try {
                val prefsManager = PreferencesManager(context)
                val prefs = prefsManager.getStaminaPrefs()

                val now = System.currentTimeMillis()
                val result = StaminaCalculator.refresh(
                    lastStamina = prefs.currentStamina,
                    lastTime = prefs.lastUpdateTime,
                    currentTime = now
                )

                val finalStamina = maxOf(0, result.stamina - points)
                prefsManager.saveStamina(finalStamina, System.currentTimeMillis())

                StaminaWidget.updateWidget(context)
            } catch (e: Exception) {
                Log.e("WidgetActionReceiver", "消耗体力失败", e)
            }
        }
    }
}
