package com.example.otakulist.ui.watchlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.otakulist.data.local.entity.WatchlistWithAnime
import com.example.otakulist.data.model.WatchlistStatus
import com.example.otakulist.data.repository.AnimeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SeasonGroup(
    val season: String,
    val items: List<WatchlistWithAnime>
)

class WatchlistViewModel(
    private val repository: AnimeRepository
) : ViewModel() {

    private val _selectedSeason = MutableStateFlow("Semua")
    val selectedSeason: StateFlow<String> = _selectedSeason.asStateFlow()

    private val _statusFilter = MutableStateFlow("Semua") // "Semua", "SUDAH", "BELUM"
    val statusFilter: StateFlow<String> = _statusFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _expandedSeasons = MutableStateFlow<Set<String>>(emptySet())
    val expandedSeasons: StateFlow<Set<String>> = _expandedSeasons.asStateFlow()

    val allWatchlist = repository.getAllWatchlist()

    // Available seasons present in the user's watchlist
    val availableSeasons: StateFlow<List<String>> = allWatchlist
        .map { list ->
            val seasons = list.mapNotNull { it.anime.season?.takeIf { s -> s.isNotBlank() } }.distinct()
            val seasonOrder = mapOf("Winter" to 1, "Spring" to 2, "Summer" to 3, "Fall" to 4)
            val sorted = seasons.sortedWith { a, b ->
                val partsA = a.split(" ")
                val partsB = b.split(" ")
                val yearA = partsA.getOrNull(1)?.toIntOrNull() ?: 0
                val yearB = partsB.getOrNull(1)?.toIntOrNull() ?: 0
                if (yearA != yearB) {
                    yearB.compareTo(yearA)
                } else {
                    val orderA = seasonOrder[partsA.getOrNull(0)] ?: 0
                    val orderB = seasonOrder[partsB.getOrNull(0)] ?: 0
                    orderB.compareTo(orderA)
                }
            }
            listOf("Semua") + sorted
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = listOf("Semua")
        )

    val groupedWatchlist: StateFlow<List<SeasonGroup>> = combine(
        allWatchlist,
        _selectedSeason,
        _statusFilter,
        _searchQuery
    ) { list, seasonFilter, status, query ->
        val trimmedQuery = query.trim()
        val filtered = list.filter { item ->
            val matchesSearch = if (trimmedQuery.isBlank()) {
                true
            } else {
                item.anime.title.contains(trimmedQuery, ignoreCase = true) ||
                    (item.anime.japaneseTitle?.contains(trimmedQuery, ignoreCase = true) == true) ||
                    (item.anime.englishTitle?.contains(trimmedQuery, ignoreCase = true) == true) ||
                    (item.anime.genres?.contains(trimmedQuery, ignoreCase = true) == true)
            }
            val matchesSeason = (seasonFilter == "Semua" || (item.anime.season ?: "Lainnya") == seasonFilter)
            val isSudah = item.watchlist.status == WatchlistStatus.SUDAH.value || item.watchlist.status == "COMPLETED"
            val matchesStatus = when (status) {
                "SUDAH" -> isSudah
                "BELUM" -> !isSudah
                else -> true
            }
            matchesSearch && matchesSeason && matchesStatus
        }

        val seasonOrder = mapOf("Winter" to 1, "Spring" to 2, "Summer" to 3, "Fall" to 4)
        val grouped = filtered.groupBy { it.anime.season?.takeIf { s -> s.isNotBlank() } ?: "Lainnya" }

        grouped.map { (seasonName, items) ->
            SeasonGroup(
                season = seasonName,
                items = items.sortedBy { it.anime.title }
            )
        }.sortedWith { a, b ->
            if (a.season == "Lainnya") 1
            else if (b.season == "Lainnya") -1
            else {
                val partsA = a.season.split(" ")
                val partsB = b.season.split(" ")
                val yearA = partsA.getOrNull(1)?.toIntOrNull() ?: 0
                val yearB = partsB.getOrNull(1)?.toIntOrNull() ?: 0
                if (yearA != yearB) {
                    yearB.compareTo(yearA)
                } else {
                    val orderA = seasonOrder[partsA.getOrNull(0)] ?: 0
                    val orderB = seasonOrder[partsB.getOrNull(0)] ?: 0
                    orderB.compareTo(orderA)
                }
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val totalCount: StateFlow<Int> = allWatchlist
        .map { it.size }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    val sudahCount: StateFlow<Int> = allWatchlist
        .map { list -> list.count { it.watchlist.status == WatchlistStatus.SUDAH.value || it.watchlist.status == "COMPLETED" } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    val belumCount: StateFlow<Int> = allWatchlist
        .map { list -> list.count { it.watchlist.status != WatchlistStatus.SUDAH.value && it.watchlist.status != "COMPLETED" } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    fun selectSeason(season: String) {
        _selectedSeason.value = season
        if (season != "Semua") {
            _expandedSeasons.value = _expandedSeasons.value + season
        }
    }

    fun selectStatusFilter(status: String) {
        _statusFilter.value = status
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun toggleSeasonExpand(season: String) {
        val current = _expandedSeasons.value
        _expandedSeasons.value = if (current.contains(season)) {
            current - season
        } else {
            current + season
        }
    }

    fun toggleAllCollapse() {
        val allCurrentSeasons = groupedWatchlist.value.map { it.season }.toSet()
        if (_expandedSeasons.value.containsAll(allCurrentSeasons)) {
            _expandedSeasons.value = emptySet()
        } else {
            _expandedSeasons.value = allCurrentSeasons
        }
    }

    fun toggleWatchlistStatus(animeId: Int) {
        viewModelScope.launch {
            repository.toggleWatchlistStatus(animeId)
        }
    }

    fun removeFromWatchlist(animeId: Int) {
        viewModelScope.launch {
            repository.removeFromWatchlist(animeId)
        }
    }

    class Factory(private val repository: AnimeRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(WatchlistViewModel::class.java)) {
                return WatchlistViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
