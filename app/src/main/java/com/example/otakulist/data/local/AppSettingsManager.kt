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

    private val _syncServerHost = MutableStateFlow(
        prefs.getString(KEY_SYNC_SERVER_HOST, "192.168.1.15:8080") ?: "192.168.1.15:8080"
    )
    val syncServerHost: StateFlow<String> = _syncServerHost.asStateFlow()

    private val _catalogVersion = MutableStateFlow(
        prefs.getString(KEY_CATALOG_VERSION, "1.0.0") ?: "1.0.0"
    )
    val catalogVersion: StateFlow<String> = _catalogVersion.asStateFlow()

    private val _lastSyncTimestamp = MutableStateFlow(
        prefs.getLong(KEY_LAST_SYNC_TIMESTAMP, 0L)
    )
    val lastSyncTimestamp: StateFlow<Long> = _lastSyncTimestamp.asStateFlow()

    fun setSyncServerHost(host: String) {
        val sanitized = host.trim()
        prefs.edit().putString(KEY_SYNC_SERVER_HOST, sanitized).apply()
        _syncServerHost.value = sanitized
    }

    fun setCatalogVersion(version: String) {
        val sanitized = version.trim()
        prefs.edit().putString(KEY_CATALOG_VERSION, sanitized).apply()
        _catalogVersion.value = sanitized
    }

    fun setLastSyncTimestamp(timestamp: Long) {
        prefs.edit().putLong(KEY_LAST_SYNC_TIMESTAMP, timestamp).apply()
        _lastSyncTimestamp.value = timestamp
    }

    companion object {
        private const val KEY_IS_ADULT_ENABLED = "is_adult_enabled"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_ACCENT_COLOR_HEX = "accent_color_hex"
        private const val KEY_SYNC_SERVER_HOST = "sync_server_host"
        private const val KEY_CATALOG_VERSION = "catalog_version"
        private const val KEY_LAST_SYNC_TIMESTAMP = "last_sync_timestamp"

        @Volatile
        private var instance: AppSettingsManager? = null

        fun getInstance(context: Context): AppSettingsManager {
            return instance ?: synchronized(this) {
                instance ?: AppSettingsManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
