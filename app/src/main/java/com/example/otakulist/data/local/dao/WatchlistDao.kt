package com.example.otakulist.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.otakulist.data.local.entity.UserWatchlistEntity
import com.example.otakulist.data.local.entity.WatchlistWithAnime
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchlistDao {

    @Transaction
    @Query("SELECT * FROM user_watchlist ORDER BY updated_at DESC")
    fun getAllWatchlist(): Flow<List<WatchlistWithAnime>>

    @Transaction
    @Query("SELECT * FROM user_watchlist WHERE status = :status ORDER BY updated_at DESC")
    fun getWatchlistByStatus(status: String): Flow<List<WatchlistWithAnime>>

    @Query("SELECT COUNT(*) FROM user_watchlist WHERE status = :status")
    fun getCountByStatus(status: String): Flow<Int>

    @Query("SELECT * FROM user_watchlist WHERE anime_id = :animeId LIMIT 1")
    fun getWatchlistEntry(animeId: Int): Flow<UserWatchlistEntity?>

    @Query("SELECT * FROM user_watchlist WHERE anime_id = :animeId LIMIT 1")
    suspend fun getWatchlistEntrySync(animeId: Int): UserWatchlistEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertWatchlist(entry: UserWatchlistEntity)

    @Query("UPDATE user_watchlist SET current_episode = :episode, updated_at = :updatedAt WHERE anime_id = :animeId")
    suspend fun updateEpisode(animeId: Int, episode: Int, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE user_watchlist SET status = :status, updated_at = :updatedAt WHERE anime_id = :animeId")
    suspend fun updateStatus(animeId: Int, status: String, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE user_watchlist SET personal_score = :score, personal_note = :note, updated_at = :updatedAt WHERE anime_id = :animeId")
    suspend fun updateScoreAndNote(animeId: Int, score: Int, note: String, updatedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM user_watchlist WHERE anime_id = :animeId")
    suspend fun removeFromWatchlist(animeId: Int)

    @Query("DELETE FROM user_watchlist")
    suspend fun clearAll()

    @Transaction
    @Query("SELECT * FROM user_watchlist ORDER BY updated_at DESC")
    suspend fun getAllWatchlistSync(): List<WatchlistWithAnime>

    @Query("SELECT SUM(current_episode) FROM user_watchlist")
    fun getTotalWatchedEpisodes(): Flow<Int?>

    @Query("SELECT COUNT(*) FROM user_watchlist WHERE status = 'COMPLETED'")
    fun getCompletedCount(): Flow<Int>
}
