package com.tili.stamina.data

import kotlin.math.floor
import kotlin.math.min

/**
 * 体力恢复核心算法 — 基于时间戳差值的纯函数计算。
 *
 * 核心规则：
 * - 最大体力值 = 200
 * - 每 8 分钟自动回复 1 点
 * - 避免后台常驻定时器：所有计算基于 (当前时间 - 上次更新时间) 的差值
 *
 * 对应设计文档 §4.1 体力恢复核心算法
 */
object StaminaCalculator {

    /** 体力最大值 */
    const val MAX_STAMINA = 200

    /** 每回复 1 点体力所需毫秒数 (8 分钟) */
    const val RECOVERY_INTERVAL_MS: Long = 8 * 60 * 1000L

    /**
     * 计算刷新后的体力值及应保存的时间戳。
     *
     * @param lastStamina 上次保存的体力值
     * @param lastTime 上次保存时间戳 (毫秒)
     * @param currentTime 当前时间戳 (毫秒)，通常为 System.currentTimeMillis()
     * @return RefreshResult 包含新的体力值和应保存的时间戳
     */
    fun refresh(
        lastStamina: Int,
        lastTime: Long,
        currentTime: Long = System.currentTimeMillis()
    ): RefreshResult {
        // 如果原本就是满的，体力不变，只更新时间戳
        if (lastStamina >= MAX_STAMINA) {
            return RefreshResult(
                stamina = MAX_STAMINA,
                savedTime = currentTime
            )
        }

        // 计算时间差，得出应恢复的点数
        val timeDiff = currentTime - lastTime
        val recoveredPoints = (timeDiff / RECOVERY_INTERVAL_MS).toInt()

        return if (recoveredPoints > 0) {
            val newStamina = min(MAX_STAMINA, lastStamina + recoveredPoints)
            // 关键：新的时间戳不能直接设为当前时间，
            // 需扣除"不满 8 分钟的余数"，保证计时精准
            val remainingMillis = timeDiff % RECOVERY_INTERVAL_MS
            val newSavedTime = currentTime - remainingMillis

            RefreshResult(
                stamina = newStamina,
                savedTime = newSavedTime
            )
        } else {
            // 不足 8 分钟，体力不变，时间戳不变
            RefreshResult(
                stamina = lastStamina,
                savedTime = lastTime
            )
        }
    }

    /**
     * 计算扣除体力后的结果。
     * 调用方应先执行 [refresh] 确保体力是最新值。
     *
     * @param currentStamina 当前（已刷新）体力值
     * @param consumePoints 要扣除的点数
     * @return 扣除后的体力值（最低为 0）
     */
    fun consume(currentStamina: Int, consumePoints: Int): Int {
        return maxOf(0, currentStamina - consumePoints)
    }

    /**
     * 计算距离下一点回复剩余的秒数。
     *
     * @param lastStamina 上次保存的体力值
     * @param lastSavedTime 上次保存的时间戳
     * @param currentTime 当前时间戳
     * @return 剩余秒数；若已满则返回 RECOVERY_INTERVAL_MS/1000
     */
    fun secondsUntilNextPoint(
        lastStamina: Int,
        lastSavedTime: Long,
        currentTime: Long = System.currentTimeMillis()
    ): Long {
        if (lastStamina >= MAX_STAMINA) {
            return RECOVERY_INTERVAL_MS / 1000L
        }

        val elapsed = currentTime - lastSavedTime
        val remainingMillis = RECOVERY_INTERVAL_MS - (elapsed % RECOVERY_INTERVAL_MS)
        return remainingMillis / 1000L
    }

    /**
     * 估算从当前体力恢复到满值所需总秒数。
     *
     * @param currentStamina 当前体力值
     * @param secondsUntilNext 距离下一点回复的秒数
     * @return 预计充满所需秒数
     */
    fun estimatedSecondsToFull(
        currentStamina: Int,
        secondsUntilNext: Long
    ): Long {
        if (currentStamina >= MAX_STAMINA) return 0L
        val missingPoints = MAX_STAMINA - currentStamina
        return (missingPoints - 1) * (RECOVERY_INTERVAL_MS / 1000L) + secondsUntilNext
    }
}

/**
 * 体力刷新计算结果
 * @param stamina 计算后的体力值
 * @param savedTime 应保存的时间戳（已扣除不满周期的余数）
 */
data class RefreshResult(
    val stamina: Int,
    val savedTime: Long
)
