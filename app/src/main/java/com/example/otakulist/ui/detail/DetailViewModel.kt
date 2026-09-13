package com.example.otakulist.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.otakulist.data.local.entity.AnimeEntity
import com.example.otakulist.data.local.entity.UserWatchlistEntity
import com.example.otakulist.data.model.WatchlistStatus
import com.example.otakulist.data.repository.AnimeRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DetailViewModel(
    private val animeId: Int,
    val isAdult: Boolean = false,
    private val repository: AnimeRepository
) : ViewModel() {

    val anime: StateFlow<AnimeEntity?> = repository.getAnimeById(animeId, isAdult)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val watchlistEntry: StateFlow<UserWatchlistEntity?> = repository.getWatchlistEntry(animeId, isAdult)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val isSaved: StateFlow<Boolean> = watchlistEntry
        .map { it != null }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    fun addToWatchlist(status: WatchlistStatus) {
        viewModelScope.launch {
            repository.saveToWatchlist(
                animeId = animeId,
                status = status,
                currentEpisode = 0,
                personalScore = 0,
                personalNote = "",
                isAdult = isAdult
            )
        }
    }

    fun updateStatus(status: WatchlistStatus) {
        viewModelScope.launch {
            val current = watchlistEntry.value ?: return@launch
            repository.saveToWatchlist(
                animeId = animeId,
                status = status,
                currentEpisode = current.currentEpisode,
                personalScore = current.personalScore,
                personalNote = current.personalNote,
                isAdult = isAdult
            )
        }
    }

    fun toggleStatus() {
        viewModelScope.launch {
            repository.toggleWatchlistStatus(animeId, isAdult)
        }
    }

    fun removeFromWatchlist() {
        viewModelScope.launch {
            repository.removeFromWatchlist(animeId, isAdult)
        }
    }

    class Factory(
        private val animeId: Int,
        private val isAdult: Boolean = false,
        private val repository: AnimeRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(DetailViewModel::class.java)) {
                return DetailViewModel(animeId, isAdult, repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
