package com.example.otakulist.data.local

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppThemeMode {
    DARK,
    LIGHT,
    SYSTEM
}

class AppSettingsManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)

    private val _isAdultContentEnabled = MutableStateFlow(
        prefs.getBoolean(KEY_IS_ADULT_ENABLED, true)
    )
    val isAdultContentEnabled: StateFlow<Boolean> = _isAdultContentEnabled.asStateFlow()

    private val _themeMode = MutableStateFlow(
        AppThemeMode.entries.find { it.name == prefs.getString(KEY_THEME_MODE, AppThemeMode.DARK.name) } ?: AppThemeMode.DARK
    )
    val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()

    private val _accentColorHex = MutableStateFlow(
        prefs.getString(KEY_ACCENT_COLOR_HEX, "#5B6EE8") ?: "#5B6EE8"
    )
    val accentColorHex: StateFlow<String> = _accentColorHex.asStateFlow()

    fun setAdultContentEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_IS_ADULT_ENABLED, enabled).apply()
        _isAdultContentEnabled.value = enabled
    }

    fun setThemeMode(mode: AppThemeMode) {
        prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
        _themeMode.value = mode
    }

    fun setAccentColorHex(hex: String) {
        prefs.edit().putString(KEY_ACCENT_COLOR_HEX, hex).apply()
        _accentColorHex.value = hex
    }

    companion object {
        private const val KEY_IS_ADULT_ENABLED = "is_adult_enabled"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_ACCENT_COLOR_HEX = "accent_color_hex"

        @Volatile
        private var instance: AppSettingsManager? = null

        fun getInstance(context: Context): AppSettingsManager {
            return instance ?: synchronized(this) {
                instance ?: AppSettingsManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
