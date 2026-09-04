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
    private val repository: AnimeRepository
) : ViewModel() {

    val anime: StateFlow<AnimeEntity?> = repository.getAnimeById(animeId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val watchlistEntry: StateFlow<UserWatchlistEntity?> = repository.getWatchlistEntry(animeId)
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
                personalNote = ""
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
                personalNote = current.personalNote
            )
        }
    }

    fun toggleStatus() {
        viewModelScope.launch {
            repository.toggleWatchlistStatus(animeId)
        }
    }


    fun updateScore(score: Int) {
        viewModelScope.launch {
            val current = watchlistEntry.value ?: return@launch
            val newScore = if (current.personalScore == score) 0 else score
            repository.saveToWatchlist(
                animeId = animeId,
                status = WatchlistStatus.fromValue(current.status),
                currentEpisode = current.currentEpisode,
                personalScore = newScore,
                personalNote = current.personalNote
            )
        }
    }

    fun updateNote(note: String) {
        viewModelScope.launch {
            val current = watchlistEntry.value ?: return@launch
            repository.saveToWatchlist(
                animeId = animeId,
                status = WatchlistStatus.fromValue(current.status),
                currentEpisode = current.currentEpisode,
                personalScore = current.personalScore,
                personalNote = note
            )
        }
    }

    fun removeFromWatchlist() {
        viewModelScope.launch {
            repository.removeFromWatchlist(animeId)
        }
    }

    class Factory(
        private val animeId: Int,
        private val repository: AnimeRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(DetailViewModel::class.java)) {
                return DetailViewModel(animeId, repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
