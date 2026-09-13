package com.example.otakulist.ui.browse

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.otakulist.data.local.entity.AnimeEntity
import com.example.otakulist.data.model.AnimeSortOrder
import com.example.otakulist.data.model.WatchlistStatus
import com.example.otakulist.data.repository.AnimeRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.stateIn

enum class CatalogType {
    REGULAR, ADULT
}

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class BrowseViewModel(
    private val repository: AnimeRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCatalogType = MutableStateFlow(CatalogType.REGULAR)
    val selectedCatalogType: StateFlow<CatalogType> = _selectedCatalogType.asStateFlow()

    val isAdultContentEnabled: StateFlow<Boolean> = repository.settingsManager.isAdultContentEnabled

    private val _selectedGenre = MutableStateFlow("All")
    val selectedGenre: StateFlow<String> = _selectedGenre.asStateFlow()

    private val _selectedType = MutableStateFlow("All")
    val selectedType: StateFlow<String> = _selectedType.asStateFlow()

    private val _selectedStatus = MutableStateFlow("All")
    val selectedStatus: StateFlow<String> = _selectedStatus.asStateFlow()

    private val _selectedSeason = MutableStateFlow("All")
    val selectedSeason: StateFlow<String> = _selectedSeason.asStateFlow()

    private val _selectedSort = MutableStateFlow(AnimeSortOrder.TITLE_ASC)
    val selectedSort: StateFlow<AnimeSortOrder> = _selectedSort.asStateFlow()

    init {
        viewModelScope.launch {
            isAdultContentEnabled.collect { enabled ->
                if (!enabled && _selectedCatalogType.value == CatalogType.ADULT) {
                    _selectedCatalogType.value = CatalogType.REGULAR
                }
            }
        }
    }

    val availableGenres: StateFlow<List<String>> = _selectedCatalogType
        .flatMapLatest { catalogType ->
            repository.getAllGenres(isAdult = catalogType == CatalogType.ADULT)
                .map { listOf("All") + it }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = listOf("All") + DEFAULT_GENRES
        )

    val availableTypes: StateFlow<List<String>> = _selectedCatalogType
        .flatMapLatest { catalogType ->
            repository.getAllTypes(isAdult = catalogType == CatalogType.ADULT)
                .map { listOf("All") + it }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = listOf("All", "TV", "Movie", "OVA", "ONA", "Special", "TV Special", "Music")
        )

    val availableStatuses: StateFlow<List<String>> = _selectedCatalogType
        .flatMapLatest { catalogType ->
            repository.getAllStatuses(isAdult = catalogType == CatalogType.ADULT)
                .map { listOf("All") + it }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = listOf("All", "Sedang Tayang", "Selesai Tayang", "Akan Tayang")
        )

    val availableSeasons: StateFlow<List<String>> = _selectedCatalogType
        .flatMapLatest { catalogType ->
            repository.getAllSeasons(isAdult = catalogType == CatalogType.ADULT)
                .map { listOf("All") + it }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = listOf("All")
        )

    val savedWatchlistMap: StateFlow<Map<Int, WatchlistStatus>> = _selectedCatalogType
        .flatMapLatest { catalogType ->
            repository.getAllWatchlist(isAdult = catalogType == CatalogType.ADULT)
                .map { list ->
                    list.associate { it.watchlist.animeId to WatchlistStatus.fromValue(it.watchlist.status) }
                }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

    private data class FilterOptions(
        val genre: String,
        val type: String,
        val status: String,
        val season: String,
        val sort: AnimeSortOrder
    )

    private val filterState = combine(
        _selectedGenre,
        _selectedType,
        _selectedStatus,
        _selectedSeason,
        _selectedSort
    ) { genre, type, status, season, sort ->
        FilterOptions(genre, type, status, season, sort)
    }

    val animeList: StateFlow<List<AnimeEntity>> = combine(
        _searchQuery.debounce(250),
        _selectedCatalogType,
        filterState
    ) { query, catalogType, filters ->
        FilterParams(
            query = query,
            genre = filters.genre,
            type = filters.type,
            status = filters.status,
            season = filters.season,
            sort = filters.sort,
            catalogType = catalogType
        )
    }.flatMapLatest { params ->
        repository.searchAndFilterAnime(
            query = params.query,
            selectedGenre = params.genre,
            selectedType = params.type,
            selectedStatus = params.status,
            selectedSeason = params.season,
            sortOrder = params.sort,
            isAdult = params.catalogType == CatalogType.ADULT
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun onCatalogTypeSelect(type: CatalogType) {
        if (type == CatalogType.ADULT && !isAdultContentEnabled.value) return
        if (_selectedCatalogType.value != type) {
            _selectedCatalogType.value = type
            _selectedGenre.value = "All"
            _selectedType.value = "All"
            _selectedStatus.value = "All"
            _selectedSeason.value = "All"
        }
    }

    fun onQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onGenreSelect(genre: String) {
        _selectedGenre.value = genre
    }

    fun onTypeSelect(type: String) {
        _selectedType.value = type
    }

    fun onStatusSelect(status: String) {
        _selectedStatus.value = status
    }

    fun onSeasonSelect(season: String) {
        _selectedSeason.value = season
    }

    fun onSortSelect(sort: AnimeSortOrder) {
        _selectedSort.value = sort
    }

    fun clearFilters() {
        _searchQuery.value = ""
        _selectedGenre.value = "All"
        _selectedType.value = "All"
        _selectedStatus.value = "All"
        _selectedSeason.value = "All"
        _selectedSort.value = AnimeSortOrder.TITLE_ASC
    }

    fun toggleWatchlist(animeId: Int, isAdult: Boolean): Boolean {
        val isCurrentlySaved = savedWatchlistMap.value.containsKey(animeId)
        viewModelScope.launch {
            if (isCurrentlySaved) {
                repository.removeFromWatchlist(animeId, isAdult)
            } else {
                repository.saveToWatchlist(
                    animeId = animeId,
                    status = WatchlistStatus.BELUM,
                    currentEpisode = 0,
                    personalScore = 0,
                    personalNote = "",
                    isAdult = isAdult
                )
            }
        }
        return !isCurrentlySaved
    }

    private data class FilterParams(
        val query: String,
        val genre: String,
        val type: String,
        val status: String,
        val season: String,
        val sort: AnimeSortOrder,
        val catalogType: CatalogType
    )

    companion object {
        private val DEFAULT_GENRES = listOf(
            "Action", "Adventure", "Cars", "Comedy", "Dementia", "Demons", "Drama", "Ecchi",
            "Fantasy", "Game", "Gourmet", "Harem", "Historical", "Horror", "Isekai", "Josei",
            "Kids", "Magic", "Martial Arts", "Mecha", "Military", "Music", "Mystery", "Parody",
            "Police", "Psychological", "Reverse Harem", "Romance", "Samurai", "School", "Sci-Fi",
            "Seinen", "Shoujo", "Shoujo Ai", "Shounen", "Shounen Ai", "Slice of Life", "Space",
            "Sports", "Super Power", "Supernatural", "Thriller", "Vampire"
        )
    }

    class Factory(private val repository: AnimeRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(BrowseViewModel::class.java)) {
                return BrowseViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}