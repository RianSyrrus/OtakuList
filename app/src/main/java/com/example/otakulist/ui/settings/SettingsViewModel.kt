package com.example.otakulist.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.otakulist.data.repository.AnimeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import com.example.otakulist.data.model.SyncManifestDto
import com.example.otakulist.data.model.SyncProgress
import com.example.otakulist.data.model.SyncSummary
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

sealed interface SyncUiState {
    data object Idle : SyncUiState
    data object Checking : SyncUiState
    data class ManifestReady(val manifest: SyncManifestDto, val isUpToDate: Boolean) : SyncUiState
    data class Syncing(val stageText: String, val progressFraction: Float? = null) : SyncUiState
    data class Success(val summary: SyncSummary) : SyncUiState
    data class Error(val errorMessage: String) : SyncUiState
}

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

    val syncServerHost: StateFlow<String> = repository.settingsManager.syncServerHost
    val catalogVersion: StateFlow<String> = repository.settingsManager.catalogVersion

    private val _syncUiState = MutableStateFlow<SyncUiState>(SyncUiState.Idle)
    val syncUiState: StateFlow<SyncUiState> = _syncUiState.asStateFlow()

    fun setSyncServerHost(host: String) {
        repository.settingsManager.setSyncServerHost(host)
    }

    fun checkServerUpdate() {
        val host = syncServerHost.value
        viewModelScope.launch {
            _syncUiState.value = SyncUiState.Checking
            val result = repository.checkLocalSyncManifest(host)
            result.fold(
                onSuccess = { manifest ->
                    val isUpToDate = manifest.catalogVersion == catalogVersion.value &&
                            manifest.newRegularCount == 0 && manifest.newAdultCount == 0
                    _syncUiState.value = SyncUiState.ManifestReady(manifest, isUpToDate)
                },
                onFailure = { error ->
                    _syncUiState.value = SyncUiState.Error(error.localizedMessage ?: "Gagal terhubung ke server PC")
                }
            )
        }
    }

    fun startSync() {
        val host = syncServerHost.value
        viewModelScope.launch {
            _syncUiState.value = SyncUiState.Syncing("Menghubungi server PC...")
            val result = repository.syncFromLocalServer(host) { progress ->
                when (progress) {
                    is SyncProgress.Connecting -> _syncUiState.value = SyncUiState.Syncing("Menghubungi server PC...")
                    is SyncProgress.CheckingManifest -> _syncUiState.value = SyncUiState.Syncing("Memeriksa indeks pembaruan...")
                    is SyncProgress.DownloadingPatch -> _syncUiState.value = SyncUiState.Syncing(progress.stage)
                    is SyncProgress.DownloadingCovers -> _syncUiState.value = SyncUiState.Syncing(
                        "Mengunduh gambar cover (${(progress.progressPercent * 100).toInt()}%)",
                        progress.progressPercent
                    )
                    is SyncProgress.ExtractingCovers -> _syncUiState.value = SyncUiState.Syncing(
                        "Mengekstrak gambar (${progress.current}/${progress.total})"
                    )
                    is SyncProgress.WritingDatabase -> _syncUiState.value = SyncUiState.Syncing("Menyimpan ke basis data...")
                    is SyncProgress.Finished -> _syncUiState.value = SyncUiState.Syncing("Sinkronisasi rampung!")
                }
            }

            result.fold(
                onSuccess = { summary ->
                    _syncUiState.value = SyncUiState.Success(summary)
                },
                onFailure = { error ->
                    _syncUiState.value = SyncUiState.Error(error.localizedMessage ?: "Terjadi kesalahan saat sinkronisasi")
                }
            )
        }
    }

    fun resetSyncState() {
        _syncUiState.value = SyncUiState.Idle
    }

    fun setAdultContentEnabled(enabled: Boolean) {
        repository.settingsManager.setAdultContentEnabled(enabled)
    }

    fun clearAllWatchlist() {
        viewModelScope.launch {
            val result = repository.clearAllWatchlist()
            if (result.isSuccess) {
                _actionFeedback.value = "Seluruh entri watchlist berhasil dikosongkan."
            } else {
                _actionFeedback.value = "Gagal mengosongkan watchlist."
            }
        }
    }

    fun dismissFeedback() {
        _actionFeedback.value = null
    }

    suspend fun getExportJsonString(): String {
        return repository.exportWatchlistJson()
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
