package com.example.otakulist.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(
    tableName = "user_watchlist_adult",
    foreignKeys = [
        ForeignKey(
            entity = AdultAnimeEntity::class,
            parentColumns = ["id"],
            childColumns = ["anime_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.NO_ACTION
        )
    ],
    indices = [
        Index(value = ["status"], name = "index_user_watchlist_adult_status")
    ]
)
data class UserWatchlistAdultEntity(
    @PrimaryKey
    @ColumnInfo(name = "anime_id")
    val animeId: Int,
    val status: String,
    @ColumnInfo(name = "current_episode", defaultValue = "0")
    val currentEpisode: Int = 0,
    @ColumnInfo(name = "personal_score", defaultValue = "0")
    val personalScore: Int = 0,
    @ColumnInfo(name = "personal_note", defaultValue = "''")
    val personalNote: String = "",
    @ColumnInfo(name = "updated_at", defaultValue = "0")
    val updatedAt: Long = System.currentTimeMillis()
)

data class WatchlistWithAdultAnime(
    @Embedded
    val watchlist: UserWatchlistAdultEntity,
    @Relation(
        parentColumn = "anime_id",
        entityColumn = "id"
    )
    val anime: AdultAnimeEntity
) {
    fun toWatchlistWithAnime(): WatchlistWithAnime {
        return WatchlistWithAnime(
            watchlist = UserWatchlistEntity(
                animeId = watchlist.animeId,
                status = watchlist.status,
                currentEpisode = watchlist.currentEpisode,
                personalScore = watchlist.personalScore,
                personalNote = watchlist.personalNote,
                updatedAt = watchlist.updatedAt
            ),
            anime = anime.toAnimeEntity()
        )
    }
}
