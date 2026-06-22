package com.tili.stamina.ui.screen

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tili.stamina.data.StaminaCalculator
import com.tili.stamina.data.StaminaRepository
import com.tili.stamina.widget.StaminaWidget
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * 主界面 ViewModel — 管理体力状态、倒计时、背景设置。
 *
 * 核心设计：
 * - 体力值基于时间戳差值实时计算（非定时器累加）
 * - 每秒 tick 仅用于更新倒计时显示
 * - 界面关闭时自动停止 tick，节省资源
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = StaminaRepository(application)

    private val _uiState = MutableStateFlow(StaminaUiState())
    val uiState: StateFlow<StaminaUiState> = _uiState.asStateFlow()

    private var tickJob: Job? = null

    init {
        // 启动：刷新体力 + 加载背景
        viewModelScope.launch {
            refreshStamina()
            loadBackgrounds()
            startTicking()
        }
    }

    // ──── 体力刷新 ────

    /**
     * 基于时间戳重新计算体力值。
     * 每次打开 App / 返回前台时调用。
     */
    fun refreshStamina() {
        viewModelScope.launch {
            val result = repository.getRefreshedStamina()
            val now = System.currentTimeMillis()

            val secondsUntilNext = if (result.stamina >= StaminaCalculator.MAX_STAMINA) {
                StaminaCalculator.RECOVERY_INTERVAL_MS / 1000L
            } else {
                val elapsed = now - result.savedTime
                val remainingMs = StaminaCalculator.RECOVERY_INTERVAL_MS -
                        (elapsed % StaminaCalculator.RECOVERY_INTERVAL_MS)
                remainingMs / 1000L
            }

            val secondsToFull = StaminaCalculator.estimatedSecondsToFull(
                result.stamina, secondsUntilNext
            )

            _uiState.update {
                it.copy(
                    currentStamina = result.stamina,
                    maxStamina = StaminaCalculator.MAX_STAMINA,
                    isFull = result.stamina >= StaminaCalculator.MAX_STAMINA,
                    secondsUntilNextPoint = secondsUntilNext,
                    secondsToFull = secondsToFull
                )
            }
        }
    }

    // ──── 倒计时 tick ────

    /** 每秒更新一次倒计时和预计充满时间 */
    private suspend fun startTicking() {
        tickJob?.cancel()
        tickJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                tickCountdown()
            }
        }
    }

    private fun tickCountdown() {
        _uiState.update { state ->
            if (state.isFull) return@update state

            val newSecNext = (state.secondsUntilNextPoint - 1).coerceAtLeast(0)
            val newSecFull = (state.secondsToFull - 1).coerceAtLeast(0)

            // 检查是否该回复 1 点了
            if (newSecNext == 0L && state.currentStamina < state.maxStamina) {
                val newStamina = (state.currentStamina + 1).coerceAtMost(state.maxStamina)
                val nowFull = newStamina >= state.maxStamina

                // 触发持久化保存
                viewModelScope.launch {
                    repository.getRefreshedStamina() // 重新同步一次
                }

                state.copy(
                    currentStamina = newStamina,
                    isFull = nowFull,
                    secondsUntilNextPoint = if (nowFull) {
                        StaminaCalculator.RECOVERY_INTERVAL_MS / 1000L
                    } else {
                        StaminaCalculator.RECOVERY_INTERVAL_MS / 1000L
                    },
                    secondsToFull = StaminaCalculator.estimatedSecondsToFull(newStamina, 0L)
                )
            } else {
                state.copy(
                    secondsUntilNextPoint = newSecNext,
                    secondsToFull = newSecFull
                )
            }
        }
    }

    /** 停止倒计时（界面不可见时） */
    fun stopTicking() {
        tickJob?.cancel()
    }

    /** 恢复倒计时（界面恢复可见时） */
    fun resumeTicking() {
        if (tickJob?.isActive != true) {
            viewModelScope.launch {
                refreshStamina()
                startTicking()
            }
        }
    }

    // ──── 体力扣除 ────

    /**
     * 扣除体力。
     * 若体力已空则拒绝操作。
     */
    fun consumeStamina(points: Int, actionName: String) {
        viewModelScope.launch {
            // 先刷新
            val refreshed = repository.getRefreshedStamina()

            if (refreshed.stamina <= 0) {
                _uiState.update {
                    it.copy(toastMessage = "体力已经透支！请先休息恢复体力。")
                }
                return@launch
            }

            val newStamina = repository.consumeStamina(points)
            val now = System.currentTimeMillis()

            _uiState.update {
                it.copy(
                    currentStamina = newStamina,
                    isFull = newStamina >= StaminaCalculator.MAX_STAMINA,
                    secondsUntilNextPoint = StaminaCalculator.RECOVERY_INTERVAL_MS / 1000L,
                    secondsToFull = StaminaCalculator.estimatedSecondsToFull(
                        newStamina,
                        StaminaCalculator.RECOVERY_INTERVAL_MS / 1000L
                    ),
                    toastMessage = "⚡ 记录成功: [$actionName] 消耗 $points 点体力"
                )
            }

            // 刷新小组件
            refreshWidget()
        }
    }

    // ──── 背景设置 ────

    fun setAppBackground(uri: String, presetIndex: Int) {
        viewModelScope.launch {
            repository.saveAppBgUri(uri)
            _uiState.update {
                it.copy(appBgUri = uri, appPresetIndex = presetIndex)
            }
        }
    }

    fun setWidgetBackground(uri: String, presetIndex: Int) {
        viewModelScope.launch {
            repository.saveWidgetBgUri(uri)
            _uiState.update {
                it.copy(widgetBgUri = uri, widgetPresetIndex = presetIndex)
            }
            refreshWidget()
        }
    }

    private suspend fun loadBackgrounds() {
        val appBg = repository.getAppBgUri()
        val widgetBg = repository.getWidgetBgUri()
        _uiState.update {
            it.copy(
                appBgUri = appBg,
                widgetBgUri = widgetBg,
                appPresetIndex = if (appBg.isEmpty()) 0 else -1,
                widgetPresetIndex = if (widgetBg.isEmpty()) 0 else -1
            )
        }
    }

    /** 清除 Toast */
    fun clearToast() {
        _uiState.update { it.copy(toastMessage = null) }
    }

    /** 刷新桌面小组件 */
    private fun refreshWidget() {
        try {
            StaminaWidget.updateWidget(getApplication())
        } catch (_: Exception) {
            // 小组件可能未添加
        }
    }

    // ──── 资源清理 ────

    override fun onCleared() {
        super.onCleared()
        tickJob?.cancel()
    }
}

/**
 * 主界面 UI 状态
 */
data class StaminaUiState(
    val currentStamina: Int = StaminaCalculator.MAX_STAMINA,
    val maxStamina: Int = StaminaCalculator.MAX_STAMINA,
    val isFull: Boolean = true,
    val secondsUntilNextPoint: Long = StaminaCalculator.RECOVERY_INTERVAL_MS / 1000L,
    val secondsToFull: Long = 0L,
    val appBgUri: String = "",
    val widgetBgUri: String = "",
    val appPresetIndex: Int = 0,
    val widgetPresetIndex: Int = 0,
    val toastMessage: String? = null
)
