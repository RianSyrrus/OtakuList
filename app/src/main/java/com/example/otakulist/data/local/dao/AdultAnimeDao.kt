package com.example.otakulist.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.RawQuery
import androidx.sqlite.db.SupportSQLiteQuery
import com.example.otakulist.data.local.entity.AdultAnimeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AdultAnimeDao {

    @Query("SELECT * FROM adult_anime ORDER BY id ASC")
    fun getAllAdultAnime(): Flow<List<AdultAnimeEntity>>

    @Query("SELECT * FROM adult_anime WHERE id = :id LIMIT 1")
    fun getAdultAnimeById(id: Int): Flow<AdultAnimeEntity?>

    @Query("SELECT * FROM adult_anime WHERE id = :id LIMIT 1")
    suspend fun getAdultAnimeByIdSync(id: Int): AdultAnimeEntity?

    @Query("""
        SELECT * FROM adult_anime 
        WHERE title LIKE '%' || :query || '%' 
           OR english_title LIKE '%' || :query || '%' 
           OR japanese_title LIKE '%' || :query || '%'
        ORDER BY score DESC
    """)
    fun searchAdultAnime(query: String): Flow<List<AdultAnimeEntity>>

    @RawQuery(observedEntities = [AdultAnimeEntity::class])
    fun queryAdultAnime(query: SupportSQLiteQuery): Flow<List<AdultAnimeEntity>>

    @Query("SELECT COUNT(*) FROM adult_anime")
    suspend fun getTotalAdultAnimeCount(): Int

    @Query("SELECT DISTINCT season FROM adult_anime WHERE season IS NOT NULL AND season != '' ORDER BY season DESC")
    fun getAllSeasons(): Flow<List<String>>

    @Query("SELECT genres FROM adult_anime WHERE genres IS NOT NULL AND genres != ''")
    fun getAllRawGenres(): Flow<List<String>>

    @Query("SELECT DISTINCT type FROM adult_anime WHERE type IS NOT NULL AND type != '' AND type != '?' ORDER BY type ASC")
    fun getAllTypes(): Flow<List<String>>

    @Query("SELECT DISTINCT status FROM adult_anime WHERE status IS NOT NULL AND status != '' ORDER BY status ASC")
    fun getAllStatuses(): Flow<List<String>>

    @Query("SELECT id FROM adult_anime")
    suspend fun getAllAdultIds(): List<Int>
}
