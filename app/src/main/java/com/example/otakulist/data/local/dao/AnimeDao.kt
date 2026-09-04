package com.example.otakulist.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.RawQuery
import androidx.sqlite.db.SupportSQLiteQuery
import com.example.otakulist.data.local.entity.AnimeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AnimeDao {

    @Query("SELECT * FROM anime ORDER BY id ASC")
    fun getAllAnime(): Flow<List<AnimeEntity>>

    @Query("SELECT * FROM anime WHERE id = :id LIMIT 1")
    fun getAnimeById(id: Int): Flow<AnimeEntity?>

    @Query("SELECT * FROM anime WHERE id = :id LIMIT 1")
    suspend fun getAnimeByIdSync(id: Int): AnimeEntity?

    @Query("""
        SELECT * FROM anime 
        WHERE title LIKE '%' || :query || '%' 
           OR english_title LIKE '%' || :query || '%' 
           OR japanese_title LIKE '%' || :query || '%'
        ORDER BY score DESC
    """)
    fun searchAnime(query: String): Flow<List<AnimeEntity>>

    @RawQuery(observedEntities = [AnimeEntity::class])
    fun queryAnime(query: SupportSQLiteQuery): Flow<List<AnimeEntity>>

    @Query("SELECT COUNT(*) FROM anime")
    suspend fun getTotalAnimeCount(): Int

    @Query("SELECT DISTINCT season FROM anime WHERE season IS NOT NULL AND season != '' ORDER BY season DESC")
    fun getAllSeasons(): Flow<List<String>>

    @Query("SELECT genres FROM anime WHERE genres IS NOT NULL AND genres != ''")
    fun getAllRawGenres(): Flow<List<String>>

    @Query("SELECT DISTINCT type FROM anime WHERE type IS NOT NULL AND type != '' AND type != '?' ORDER BY type ASC")
    fun getAllTypes(): Flow<List<String>>

    @Query("SELECT DISTINCT status FROM anime WHERE status IS NOT NULL AND status != '' ORDER BY status ASC")
    fun getAllStatuses(): Flow<List<String>>
}
