package com.example.otakulist.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "user_watchlist",
    foreignKeys = [
        ForeignKey(
            entity = AnimeEntity::class,
            parentColumns = ["id"],
            childColumns = ["anime_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.NO_ACTION
        )
    ],
    indices = [
        Index(value = ["status"], name = "index_user_watchlist_status")
    ]
)
data class UserWatchlistEntity(
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
