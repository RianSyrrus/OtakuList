package com.example.otakulist.data.repository

import androidx.sqlite.db.SimpleSQLiteQuery
import com.example.otakulist.data.local.AppSettingsManager
import com.example.otakulist.data.local.dao.AdultAnimeDao
import com.example.otakulist.data.local.dao.AnimeDao
import com.example.otakulist.data.local.dao.WatchlistAdultDao
import com.example.otakulist.data.local.dao.WatchlistDao
import com.example.otakulist.data.local.entity.AdultAnimeEntity
import com.example.otakulist.data.local.entity.AnimeEntity
import com.example.otakulist.data.local.entity.UserWatchlistAdultEntity
import com.example.otakulist.data.local.entity.UserWatchlistEntity
import com.example.otakulist.data.local.entity.WatchlistWithAnime
import com.example.otakulist.data.local.entity.toAnimeEntity
import com.example.otakulist.data.model.AnimeSortOrder
import com.example.otakulist.data.model.WatchlistStatus
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

data class WatchlistBackupModel(
    val animeId: Int,
    val status: String,
    val currentEpisode: Int,
    val personalScore: Int,
    val personalNote: String,
    val updatedAt: Long,
    val isAdult: Boolean = false
)

data class StatsModel(
    val totalWatchedTitles: Int = 0,
    val totalEpisodesWatched: Int = 0,
    val totalHoursWatched: Int = 0,
    val sudahCount: Int = 0,
    val belumCount: Int = 0,
    val totalWatchlistCount: Int = 0,
    val watchingCount: Int = 0,
    val planToWatchCount: Int = 0,
    val completedCount: Int = 0,
    val droppedCount: Int = 0,
    val averageScore: Double = 0.0,
    val topGenres: List<Pair<String, Int>> = emptyList(),
    val favoriteStudio: String = "-",
    val favoriteStudioCount: Int = 0,
    val busiestYear: String = "-",
    val busiestYearCount: Int = 0
)

class AnimeRepository(
    private val animeDao: AnimeDao,
    private val watchlistDao: WatchlistDao,
    private val adultAnimeDao: AdultAnimeDao,
    private val watchlistAdultDao: WatchlistAdultDao,
    val settingsManager: AppSettingsManager,
    private val gson: Gson = Gson()
) {

    fun getAllAnime(isAdult: Boolean = false): Flow<List<AnimeEntity>> = 
        if (isAdult) adultAnimeDao.getAllAdultAnime().map { list -> list.map { it.toAnimeEntity() } }
        else animeDao.getAllAnime()

    suspend fun resolveIsAdult(animeId: Int, hintIsAdult: Boolean = false): Boolean {
        return if (hintIsAdult) {
            if (adultAnimeDao.getAdultAnimeByIdSync(animeId) != null) true
            else if (animeDao.getAnimeByIdSync(animeId) != null) false
            else true
        } else {
            if (animeDao.getAnimeByIdSync(animeId) != null) false
            else if (adultAnimeDao.getAdultAnimeByIdSync(animeId) != null) true
            else false
        }
    }

    fun getAnimeById(id: Int, isAdult: Boolean = false): Flow<AnimeEntity?> = flow {
        val targetIsAdult = resolveIsAdult(id, isAdult)
        val sourceFlow = if (targetIsAdult) {
            adultAnimeDao.getAdultAnimeById(id).map { it?.toAnimeEntity() }
        } else {
            animeDao.getAnimeById(id)
        }
        emitAll(sourceFlow)
    }.flowOn(Dispatchers.IO)

    suspend fun getAnimeByIdSync(id: Int, isAdult: Boolean = false): AnimeEntity? {
        val targetIsAdult = resolveIsAdult(id, isAdult)
        return if (targetIsAdult) {
            adultAnimeDao.getAdultAnimeByIdSync(id)?.toAnimeEntity()
        } else {
            animeDao.getAnimeByIdSync(id)
        }
    }

    fun searchAndFilterAnime(
        query: String = "",
        selectedGenre: String? = null,
        selectedType: String? = null,
        selectedStatus: String? = null,
        selectedSeason: String? = null,
        sortOrder: AnimeSortOrder = AnimeSortOrder.TITLE_ASC,
        isAdult: Boolean = false
    ): Flow<List<AnimeEntity>> {
        val conditions = mutableListOf<String>()
        val bindArgs = mutableListOf<Any>()

        if (query.isNotBlank()) {
            conditions.add("(title LIKE ? OR english_title LIKE ? OR japanese_title LIKE ?)")
            val wildcard = "%${query.trim()}%"
            bindArgs.add(wildcard)
            bindArgs.add(wildcard)
            bindArgs.add(wildcard)
        }

        if (!selectedGenre.isNullOrBlank() && selectedGenre != "All") {
            conditions.add("genres LIKE ?")
            bindArgs.add("%$selectedGenre%")
        }

        if (!selectedType.isNullOrBlank() && selectedType != "All") {
            conditions.add("type = ?")
            bindArgs.add(selectedType)
        }

        if (!selectedStatus.isNullOrBlank() && selectedStatus != "All") {
            conditions.add("status = ?")
            bindArgs.add(selectedStatus)
        }

        if (!selectedSeason.isNullOrBlank() && selectedSeason != "All") {
            conditions.add("season = ?")
            bindArgs.add(selectedSeason)
        }

        val whereClause = if (conditions.isNotEmpty()) {
            "WHERE " + conditions.joinToString(" AND ")
        } else {
            ""
        }

        val orderClause = when (sortOrder) {
            AnimeSortOrder.SCORE_DESC -> "ORDER BY CASE WHEN score IS NULL OR score = '' OR score = '?' THEN 0 ELSE CAST(score AS REAL) END DESC, id ASC"
            AnimeSortOrder.YEAR_DESC -> "ORDER BY CASE WHEN season IS NULL OR season = '' THEN 0 ELSE 1 END DESC, id DESC"
            AnimeSortOrder.TITLE_ASC -> "ORDER BY title COLLATE NOCASE ASC"
        }

        val tableName = if (isAdult) "adult_anime" else "anime"
        val sql = "SELECT * FROM $tableName $whereClause $orderClause"
        val sqLiteQuery = SimpleSQLiteQuery(sql, bindArgs.toTypedArray())
        return if (isAdult) {
            adultAnimeDao.queryAdultAnime(sqLiteQuery).map { list -> list.map { it.toAnimeEntity() } }
        } else {
            animeDao.queryAnime(sqLiteQuery)
        }
    }

    fun getAllSeasons(isAdult: Boolean = false): Flow<List<String>> = 
        (if (isAdult) adultAnimeDao.getAllSeasons() else animeDao.getAllSeasons()).map { rawSeasons ->
            val seasonOrder = mapOf("Winter" to 1, "Spring" to 2, "Summer" to 3, "Fall" to 4)
            rawSeasons.sortedWith { a, b ->
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
        }

    fun getAllGenres(isAdult: Boolean = false): Flow<List<String>> = 
        (if (isAdult) adultAnimeDao.getAllRawGenres() else animeDao.getAllRawGenres()).map { rawList ->
            val genreSet = sortedSetOf<String>()
            for (raw in rawList) {
                for (g in raw.split(",")) {
                    val trimmed = g.trim()
                    if (trimmed.isNotEmpty()) {
                        genreSet.add(trimmed)
                    }
                }
            }
            genreSet.toList()
        }

    fun getAllTypes(isAdult: Boolean = false): Flow<List<String>> = 
        if (isAdult) adultAnimeDao.getAllTypes() else animeDao.getAllTypes()

    fun getAllStatuses(isAdult: Boolean = false): Flow<List<String>> = 
        if (isAdult) adultAnimeDao.getAllStatuses() else animeDao.getAllStatuses()

    fun getWatchlistByStatus(status: WatchlistStatus, isAdult: Boolean = false): Flow<List<WatchlistWithAnime>> {
        return if (isAdult) {
            watchlistAdultDao.getWatchlistByStatus(status.value).map { list -> list.map { it.toWatchlistWithAnime() } }
        } else {
            watchlistDao.getWatchlistByStatus(status.value)
        }
    }

    fun getWatchlistCount(status: WatchlistStatus, isAdult: Boolean = false): Flow<Int> {
        return if (isAdult) watchlistAdultDao.getCountByStatus(status.value)
        else watchlistDao.getCountByStatus(status.value)
    }

    fun getWatchlistEntry(animeId: Int, isAdult: Boolean = false): Flow<UserWatchlistEntity?> = flow {
        val targetIsAdult = resolveIsAdult(animeId, isAdult)
        val sourceFlow = if (targetIsAdult) {
            watchlistAdultDao.getWatchlistEntry(animeId).map { adultEntry ->
                adultEntry?.let {
                    UserWatchlistEntity(
                        animeId = it.animeId,
                        status = it.status,
                        currentEpisode = it.currentEpisode,
                        personalScore = it.personalScore,
                        personalNote = it.personalNote,
                        updatedAt = it.updatedAt
                    )
                }
            }
        } else {
            watchlistDao.getWatchlistEntry(animeId)
        }
        emitAll(sourceFlow)
    }.flowOn(Dispatchers.IO)

    suspend fun getWatchlistEntrySync(animeId: Int, isAdult: Boolean = false): UserWatchlistEntity? {
        val targetIsAdult = resolveIsAdult(animeId, isAdult)
        return if (targetIsAdult) {
            watchlistAdultDao.getWatchlistEntrySync(animeId)?.let {
                UserWatchlistEntity(
                    animeId = it.animeId,
                    status = it.status,
                    currentEpisode = it.currentEpisode,
                    personalScore = it.personalScore,
                    personalNote = it.personalNote,
                    updatedAt = it.updatedAt
                )
            }
        } else {
            watchlistDao.getWatchlistEntrySync(animeId)
        }
    }

    suspend fun saveToWatchlist(
        animeId: Int,
        status: WatchlistStatus,
        currentEpisode: Int = 0,
        personalScore: Int = 0,
        personalNote: String = "",
        isAdult: Boolean = false
    ) {
        val targetIsAdult = resolveIsAdult(animeId, isAdult)
        try {
            if (targetIsAdult) {
                val entry = UserWatchlistAdultEntity(
                    animeId = animeId,
                    status = status.value,
                    currentEpisode = currentEpisode,
                    personalScore = personalScore,
                    personalNote = personalNote,
                    updatedAt = System.currentTimeMillis()
                )
                watchlistAdultDao.upsertWatchlist(entry)
            } else {
                val entry = UserWatchlistEntity(
                    animeId = animeId,
                    status = status.value,
                    currentEpisode = currentEpisode,
                    personalScore = personalScore,
                    personalNote = personalNote,
                    updatedAt = System.currentTimeMillis()
                )
                watchlistDao.upsertWatchlist(entry)
            }
        } catch (e: Exception) {
            android.util.Log.e("AnimeRepository", "Error saving to watchlist: animeId=$animeId, targetIsAdult=$targetIsAdult", e)
        }
    }

    fun getAllWatchlist(isAdult: Boolean = false): Flow<List<WatchlistWithAnime>> {
        return if (isAdult) {
            watchlistAdultDao.getAllWatchlist().map { list -> list.map { it.toWatchlistWithAnime() } }
        } else {
            watchlistDao.getAllWatchlist()
        }
    }

    suspend fun removeFromWatchlist(animeId: Int, isAdult: Boolean = false) {
        watchlistAdultDao.removeFromWatchlist(animeId)
        watchlistDao.removeFromWatchlist(animeId)
    }

    suspend fun toggleWatchlistStatus(animeId: Int, isAdult: Boolean = false) {
        val targetIsAdult = resolveIsAdult(animeId, isAdult)
        if (targetIsAdult) {
            val entry = watchlistAdultDao.getWatchlistEntrySync(animeId)
            if (entry != null) {
                val nextStatus = if (entry.status == WatchlistStatus.SUDAH.value || entry.status == "COMPLETED") {
                    WatchlistStatus.BELUM.value
                } else {
                    WatchlistStatus.SUDAH.value
                }
                watchlistAdultDao.upsertWatchlist(
                    entry.copy(
                        status = nextStatus,
                        updatedAt = System.currentTimeMillis()
                    )
                )
            } else {
                saveToWatchlist(animeId = animeId, status = WatchlistStatus.BELUM, isAdult = true)
            }
        } else {
            val entry = watchlistDao.getWatchlistEntrySync(animeId)
            if (entry != null) {
                val nextStatus = if (entry.status == WatchlistStatus.SUDAH.value || entry.status == "COMPLETED") {
                    WatchlistStatus.BELUM.value
                } else {
                    WatchlistStatus.SUDAH.value
                }
                watchlistDao.upsertWatchlist(
                    entry.copy(
                        status = nextStatus,
                        updatedAt = System.currentTimeMillis()
                    )
                )
            } else {
                saveToWatchlist(animeId = animeId, status = WatchlistStatus.BELUM, isAdult = false)
            }
        }
    }

    fun getStats(): Flow<StatsModel> = combine(
        watchlistDao.getAllWatchlist(),
        watchlistAdultDao.getAllWatchlist(),
        settingsManager.isAdultContentEnabled
    ) { regularEntries, adultEntries, isAdultEnabled ->
        val allEntries = if (isAdultEnabled) {
            regularEntries + adultEntries.map { it.toWatchlistWithAnime() }
        } else {
            regularEntries
        }
        var sudahCount = 0
        var belumCount = 0
        var watchingCount = 0
        var planCount = 0
        var completedCount = 0
        var droppedCount = 0
        var totalEpisodes = 0
        val genreCounts = mutableMapOf<String, Int>()
        val studioCounts = mutableMapOf<String, Int>()
        val yearCounts = mutableMapOf<String, Int>()

        for (item in allEntries) {
            val status = item.watchlist.status
            if (status == WatchlistStatus.SUDAH.value || status == "COMPLETED") {
                sudahCount++
                completedCount++
            } else {
                belumCount++
                when (status) {
                    WatchlistStatus.WATCHING.value -> watchingCount++
                    WatchlistStatus.PLAN_TO_WATCH.value -> planCount++
                    WatchlistStatus.DROPPED.value -> droppedCount++
                    else -> belumCount++ // already counted
                }
            }

            totalEpisodes += item.watchlist.currentEpisode

            val genres = item.anime.genres?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()
            for (genre in genres) {
                genreCounts[genre] = (genreCounts[genre] ?: 0) + 1
            }

            if (!item.anime.studios.isNullOrBlank()) {
                val studios = item.anime.studios.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                for (s in studios) {
                    studioCounts[s] = (studioCounts[s] ?: 0) + 1
                }
            }

            if (!item.anime.season.isNullOrBlank()) {
                val yearMatch = Regex("""\b(19\d\d|20\d\d)\b""").find(item.anime.season)
                if (yearMatch != null) {
                    val y = yearMatch.value
                    yearCounts[y] = (yearCounts[y] ?: 0) + 1
                }
            }
        }

        // Estimasi 24 menit per episode -> total jam
        val totalHours = (totalEpisodes * 24) / 60
        val topGenres = genreCounts.toList().sortedByDescending { it.second }.take(5)
        val topStudioEntry = studioCounts.maxByOrNull { it.value }
        val topYearEntry = yearCounts.maxByOrNull { it.value }

        StatsModel(
            totalWatchedTitles = sudahCount,
            totalEpisodesWatched = totalEpisodes,
            totalHoursWatched = totalHours,
            sudahCount = sudahCount,
            belumCount = belumCount,
            totalWatchlistCount = allEntries.size,
            watchingCount = watchingCount,
            planToWatchCount = planCount,
            completedCount = completedCount,
            droppedCount = droppedCount,
            averageScore = 0.0,
            topGenres = topGenres,
            favoriteStudio = topStudioEntry?.key ?: "-",
            favoriteStudioCount = topStudioEntry?.value ?: 0,
            busiestYear = topYearEntry?.key ?: "-",
            busiestYearCount = topYearEntry?.value ?: 0
        )
    }.flowOn(Dispatchers.Default)

    suspend fun exportWatchlistJson(): String {
        val regularList = watchlistDao.getAllWatchlistSync().map {
            WatchlistBackupModel(
                animeId = it.watchlist.animeId,
                status = it.watchlist.status,
                currentEpisode = it.watchlist.currentEpisode,
                personalScore = it.watchlist.personalScore,
                personalNote = it.watchlist.personalNote,
                updatedAt = it.watchlist.updatedAt,
                isAdult = false
            )
        }
        val adultList = watchlistAdultDao.getAllWatchlistSync().map {
            WatchlistBackupModel(
                animeId = it.watchlist.animeId,
                status = it.watchlist.status,
                currentEpisode = it.watchlist.currentEpisode,
                personalScore = it.watchlist.personalScore,
                personalNote = it.watchlist.personalNote,
                updatedAt = it.watchlist.updatedAt,
                isAdult = true
            )
        }
        return gson.toJson(regularList + adultList)
    }

    suspend fun importWatchlistJson(json: String): Result<Int> {
        return runCatching {
            val type = object : TypeToken<List<WatchlistBackupModel>>() {}.type
            val list: List<WatchlistBackupModel> = gson.fromJson(json, type)
            var count = 0
            for (item in list) {
                val targetIsAdult = resolveIsAdult(item.animeId, item.isAdult)
                if (targetIsAdult) {
                    watchlistAdultDao.upsertWatchlist(
                        UserWatchlistAdultEntity(
                            animeId = item.animeId,
                            status = item.status,
                            currentEpisode = item.currentEpisode,
                            personalScore = item.personalScore,
                            personalNote = item.personalNote,
                            updatedAt = item.updatedAt
                        )
                    )
                } else {
                    watchlistDao.upsertWatchlist(
                        UserWatchlistEntity(
                            animeId = item.animeId,
                            status = item.status,
                            currentEpisode = item.currentEpisode,
                            personalScore = item.personalScore,
                            personalNote = item.personalNote,
                            updatedAt = item.updatedAt
                        )
                    )
                }
                count++
            }
            count
        }
    }

    suspend fun clearWatchlist(isAdult: Boolean? = null) {
        when (isAdult) {
            true -> watchlistAdultDao.clearAll()
            false -> watchlistDao.clearAll()
            null -> {
                watchlistDao.clearAll()
                watchlistAdultDao.clearAll()
            }
        }
    }
}
