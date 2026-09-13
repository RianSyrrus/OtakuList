package com.example.otakulist.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.otakulist.data.repository.AnimeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class SettingsViewModel(
    private val repository: AnimeRepository
) : ViewModel() {

    val isAdultContentEnabled: StateFlow<Boolean> = repository.settingsManager.isAdultContentEnabled
    val themeMode = repository.settingsManager.themeMode
    val accentColorHex = repository.settingsManager.accentColorHex

    fun setThemeMode(mode: com.example.otakulist.data.local.AppThemeMode) {
        repository.settingsManager.setThemeMode(mode)
    }

    fun setAccentColorHex(hex: String) {
        repository.settingsManager.setAccentColorHex(hex)
    }

    val regularWatchlistCount: StateFlow<Int> = repository.getAllWatchlist(isAdult = false)
        .map { it.size }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    val adultWatchlistCount: StateFlow<Int> = repository.getAllWatchlist(isAdult = true)
        .map { it.size }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    private val _exportJson = MutableStateFlow<String?>(null)
    val exportJson: StateFlow<String?> = _exportJson.asStateFlow()

    private val _importMessage = MutableStateFlow<String?>(null)
    val importMessage: StateFlow<String?> = _importMessage.asStateFlow()

    private val _actionFeedback = MutableStateFlow<String?>(null)
    val actionFeedback: StateFlow<String?> = _actionFeedback.asStateFlow()

    fun setAdultContentEnabled(enabled: Boolean) {
        repository.settingsManager.setAdultContentEnabled(enabled)
    }

    fun clearAllWatchlist() {
        viewModelScope.launch {
            repository.clearWatchlist()
            _actionFeedback.value = "Seluruh entri watchlist berhasil dikosongkan."
        }
    }

    fun dismissFeedback() {
        _actionFeedback.value = null
    }

    fun exportWatchlist() {
        viewModelScope.launch {
            val json = repository.exportWatchlistJson()
            _exportJson.value = json
        }
    }

    fun dismissExportDialog() {
        _exportJson.value = null
    }

    fun importWatchlist(json: String) {
        viewModelScope.launch {
            val result = repository.importWatchlistJson(json)
            if (result.isSuccess) {
                _importMessage.value = "Berhasil memulihkan ${result.getOrNull()} entri watchlist."
            } else {
                _importMessage.value = "Gagal memulihkan data: Format JSON tidak valid."
            }
        }
    }

    fun dismissImportMessage() {
        _importMessage.value = null
    }

    class Factory(private val repository: AnimeRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
                return SettingsViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
