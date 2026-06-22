package com.tili.stamina.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/** 应用级 DataStore 扩展 */
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "stamina_settings")

/**
 * DataStore Preferences 管理器。
 * 存储轻量级体力状态和背景 URI 配置。
 *
 * 对应设计文档 §3.1 数据库与存储设计
 */
class PreferencesManager(private val context: Context) {

    companion object Keys {
        val CURRENT_STAMINA = intPreferencesKey("current_stamina")
        val LAST_UPDATE_TIME = longPreferencesKey("last_update_time")
        val APP_BG_URI = stringPreferencesKey("app_bg_uri")
        val WIDGET_BG_URI = stringPreferencesKey("widget_bg_uri")
    }

    // ──── 体力数据流 ────

    /** 观察当前体力状态（实时 Flow） */
    val staminaFlow: Flow<StaminaPrefs> = context.dataStore.data.map { prefs ->
        StaminaPrefs(
            currentStamina = prefs[CURRENT_STAMINA] ?: StaminaCalculator.MAX_STAMINA,
            lastUpdateTime = prefs[LAST_UPDATE_TIME] ?: System.currentTimeMillis()
        )
    }

    /** 一次性读取体力偏好 */
    suspend fun getStaminaPrefs(): StaminaPrefs {
        val prefs = context.dataStore.data.first()
        return StaminaPrefs(
            currentStamina = prefs[CURRENT_STAMINA] ?: StaminaCalculator.MAX_STAMINA,
            lastUpdateTime = prefs[LAST_UPDATE_TIME] ?: System.currentTimeMillis()
        )
    }

    /** 保存体力值和时间戳 */
    suspend fun saveStamina(stamina: Int, savedTime: Long) {
        context.dataStore.edit { prefs ->
            prefs[CURRENT_STAMINA] = stamina
            prefs[LAST_UPDATE_TIME] = savedTime
        }
    }

    // ──── 背景 URI ────

    /** 观察 App 背景 URI */
    val appBgUriFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[APP_BG_URI] ?: ""
    }

    /** 观察小组件背景 URI */
    val widgetBgUriFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[WIDGET_BG_URI] ?: ""
    }

    /** 一次性读取 App 背景 URI */
    suspend fun getAppBgUri(): String {
        return context.dataStore.data.first()[APP_BG_URI] ?: ""
    }

    /** 一次性读取小组件背景 URI */
    suspend fun getWidgetBgUri(): String {
        return context.dataStore.data.first()[WIDGET_BG_URI] ?: ""
    }

    /** 保存 App 背景 URI */
    suspend fun saveAppBgUri(uri: String) {
        context.dataStore.edit { prefs ->
            prefs[APP_BG_URI] = uri
        }
    }

    /** 保存小组件背景 URI */
    suspend fun saveWidgetBgUri(uri: String) {
        context.dataStore.edit { prefs ->
            prefs[WIDGET_BG_URI] = uri
        }
    }
}

/**
 * DataStore 中存储的体力偏好数据类
 */
data class StaminaPrefs(
    val currentStamina: Int = StaminaCalculator.MAX_STAMINA,
    val lastUpdateTime: Long = System.currentTimeMillis()
)
