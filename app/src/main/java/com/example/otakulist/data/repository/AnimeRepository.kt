package com.example.otakulist.data.repository

import androidx.sqlite.db.SimpleSQLiteQuery
import com.example.otakulist.data.local.dao.AnimeDao
import com.example.otakulist.data.local.dao.WatchlistDao
import com.example.otakulist.data.local.entity.AnimeEntity
import com.example.otakulist.data.local.entity.UserWatchlistEntity
import com.example.otakulist.data.local.entity.WatchlistWithAnime
import com.example.otakulist.data.model.AnimeSortOrder
import com.example.otakulist.data.model.WatchlistStatus
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

data class WatchlistBackupModel(
    val animeId: Int,
    val status: String,
    val currentEpisode: Int,
    val personalScore: Int,
    val personalNote: String,
    val updatedAt: Long
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
    private val gson: Gson = Gson()
) {

    fun getAllAnime(): Flow<List<AnimeEntity>> = animeDao.getAllAnime()

    fun getAnimeById(id: Int): Flow<AnimeEntity?> = animeDao.getAnimeById(id)

    suspend fun getAnimeByIdSync(id: Int): AnimeEntity? = animeDao.getAnimeByIdSync(id)

    fun searchAndFilterAnime(
        query: String = "",
        selectedGenre: String? = null,
        selectedType: String? = null,
        selectedStatus: String? = null,
        selectedSeason: String? = null,
        sortOrder: AnimeSortOrder = AnimeSortOrder.TITLE_ASC
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

        val sql = "SELECT * FROM anime $whereClause $orderClause"
        val sqLiteQuery = SimpleSQLiteQuery(sql, bindArgs.toTypedArray())
        return animeDao.queryAnime(sqLiteQuery)
    }

    fun getAllSeasons(): Flow<List<String>> = animeDao.getAllSeasons().map { rawSeasons ->
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

    fun getAllGenres(): Flow<List<String>> = animeDao.getAllRawGenres().map { rawList ->
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

    fun getAllTypes(): Flow<List<String>> = animeDao.getAllTypes()

    fun getAllStatuses(): Flow<List<String>> = animeDao.getAllStatuses()

    fun getWatchlistByStatus(status: WatchlistStatus): Flow<List<WatchlistWithAnime>> {
        return watchlistDao.getWatchlistByStatus(status.value)
    }

    fun getWatchlistCount(status: WatchlistStatus): Flow<Int> {
        return watchlistDao.getCountByStatus(status.value)
    }

    fun getWatchlistEntry(animeId: Int): Flow<UserWatchlistEntity?> {
        return watchlistDao.getWatchlistEntry(animeId)
    }

    suspend fun getWatchlistEntrySync(animeId: Int): UserWatchlistEntity? {
        return watchlistDao.getWatchlistEntrySync(animeId)
    }

    suspend fun saveToWatchlist(
        animeId: Int,
        status: WatchlistStatus,
        currentEpisode: Int = 0,
        personalScore: Int = 0,
        personalNote: String = ""
    ) {
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

    suspend fun incrementEpisode(animeId: Int): Boolean {
        val entry = watchlistDao.getWatchlistEntrySync(animeId) ?: return false
        val anime = animeDao.getAnimeByIdSync(animeId)
        val maxEpisodes = anime?.episodes?.toIntOrNull()

        val nextEp = entry.currentEpisode + 1
        if (maxEpisodes != null && nextEp > maxEpisodes) {
            return false
        }

        val newStatus = if (maxEpisodes != null && nextEp >= maxEpisodes) {
            WatchlistStatus.COMPLETED.value
        } else {
            entry.status
        }

        watchlistDao.upsertWatchlist(
            entry.copy(
                currentEpisode = nextEp,
                status = newStatus,
                updatedAt = System.currentTimeMillis()
            )
        )
        return true
    }

    fun getAllWatchlist(): Flow<List<WatchlistWithAnime>> {
        return watchlistDao.getAllWatchlist()
    }

    suspend fun removeFromWatchlist(animeId: Int) {
        watchlistDao.removeFromWatchlist(animeId)
    }

    suspend fun toggleWatchlistStatus(animeId: Int) {
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
            saveToWatchlist(animeId = animeId, status = WatchlistStatus.BELUM)
        }
    }

    fun getStats(): Flow<StatsModel> = watchlistDao.getAllWatchlist().map { allEntries ->
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
    }

    suspend fun exportWatchlistJson(): String {
        val all = watchlistDao.getAllWatchlistSync()
        val models = all.map {
            WatchlistBackupModel(
                animeId = it.watchlist.animeId,
                status = it.watchlist.status,
                currentEpisode = it.watchlist.currentEpisode,
                personalScore = it.watchlist.personalScore,
                personalNote = it.watchlist.personalNote,
                updatedAt = it.watchlist.updatedAt
            )
        }
        return gson.toJson(models)
    }

    suspend fun importWatchlistJson(json: String): Result<Int> {
        return runCatching {
            val type = object : TypeToken<List<WatchlistBackupModel>>() {}.type
            val list: List<WatchlistBackupModel> = gson.fromJson(json, type)
            var count = 0
            for (item in list) {
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
                count++
            }
            count
        }
    }
}
