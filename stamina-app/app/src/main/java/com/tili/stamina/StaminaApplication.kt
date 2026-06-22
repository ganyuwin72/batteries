package com.tili.stamina

import android.app.Application

/**
 * 自定义 Application 类 — 应用级初始化入口。
 */
class StaminaApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // 预留：未来可在此初始化 WorkManager、日志框架等
    }
}
