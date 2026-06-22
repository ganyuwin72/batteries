package com.tili.stamina.data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

/**
 * 体力数据仓库 —— 组合 Calculator 和 PreferencesManager，
 * 对外暴露统一的业务接口。
 *
 * 核心流程：
 * 1. 读取体力 → 总是先调用 refresh() 确保数据最新
 * 2. 扣除体力 → 先 refresh，再 consume，最后 save
 */
class StaminaRepository(context: Context) {

    private val prefsManager = PreferencesManager(context)

    // ──── 读取（已自动刷新）────

    /**
     * 获取刷新后的当前体力值（一次性）。
     * 每次调用都会基于时间戳重新计算。
     */
    suspend fun getRefreshedStamina(): RefreshResult {
        val prefs = prefsManager.getStaminaPrefs()
        val result = StaminaCalculator.refresh(
            lastStamina = prefs.currentStamina,
            lastTime = prefs.lastUpdateTime
        )
        // 如果计算后有变化，持久化
        if (result.stamina != prefs.currentStamina || result.savedTime != prefs.lastUpdateTime) {
            prefsManager.saveStamina(result.stamina, result.savedTime)
        }
        return result
    }

    /**
     * 获取背景 URI（不刷新体力）
     */
    suspend fun getAppBgUri(): String = prefsManager.getAppBgUri()
    suspend fun getWidgetBgUri(): String = prefsManager.getWidgetBgUri()

    // ──── Flow 流 ────

    /** 观察体力偏好原始数据 */
    val staminaPrefsFlow: Flow<StaminaPrefs> = prefsManager.staminaFlow

    /** 观察 App 背景 URI */
    val appBgUriFlow: Flow<String> = prefsManager.appBgUriFlow

    /** 观察小组件背景 URI */
    val widgetBgUriFlow: Flow<String> = prefsManager.widgetBgUriFlow

    // ──── 扣除体力 ────

    /**
     * 扣除指定点数的体力。
     * 内部自动先刷新再扣除，最后持久化。
     *
     * @param points 要扣除的点数
     * @return 扣除后的体力值
     */
    suspend fun consumeStamina(points: Int): Int {
        // 1. 先刷新体力
        val refreshed = getRefreshedStamina()

        // 2. 扣除
        val finalStamina = StaminaCalculator.consume(refreshed.stamina, points)

        // 3. 保存，时间戳更新为当前时间（重新开始 8 分钟周期）
        prefsManager.saveStamina(finalStamina, System.currentTimeMillis())

        return finalStamina
    }

    // ──── 保存背景 ────

    suspend fun saveAppBgUri(uri: String) = prefsManager.saveAppBgUri(uri)
    suspend fun saveWidgetBgUri(uri: String) = prefsManager.saveWidgetBgUri(uri)

    // ──── 小组件专用 ────

    /**
     * 供小组件快速读取：获取刷新后的体力值和倒计时信息。
     * 不需要异步 Flow，直接返回计算结果。
     */
    suspend fun getWidgetState(): WidgetState {
        val prefs = prefsManager.getStaminaPrefs()
        val now = System.currentTimeMillis()
        val result = StaminaCalculator.refresh(prefs.currentStamina, prefs.lastUpdateTime, now)

        val secondsUntilNext = StaminaCalculator.secondsUntilNextPoint(
            result.stamina, result.savedTime, now
        )
        val secondsToFull = StaminaCalculator.estimatedSecondsToFull(result.stamina, secondsUntilNext)

        val appBg = prefsManager.getAppBgUri()
        val widgetBg = prefsManager.getWidgetBgUri()

        return WidgetState(
            currentStamina = result.stamina,
            maxStamina = StaminaCalculator.MAX_STAMINA,
            isFull = result.stamina >= StaminaCalculator.MAX_STAMINA,
            secondsUntilNextPoint = secondsUntilNext,
            secondsToFull = secondsToFull,
            appBgUri = appBg,
            widgetBgUri = widgetBg
        )
    }

    /**
     * 小组件消耗体力（同步版本，在 BroadcastReceiver 中调用）。
     * 注意：此方法应在协程中调用。
     */
    suspend fun consumeStaminaForWidget(points: Int): WidgetState {
        consumeStamina(points)
        return getWidgetState()
    }
}

/**
 * 小组件展示状态
 */
data class WidgetState(
    val currentStamina: Int,
    val maxStamina: Int,
    val isFull: Boolean,
    val secondsUntilNextPoint: Long,
    val secondsToFull: Long,
    val appBgUri: String,
    val widgetBgUri: String
)
