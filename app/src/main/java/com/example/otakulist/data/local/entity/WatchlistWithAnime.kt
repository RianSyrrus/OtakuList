package com.example.otakulist.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

data class WatchlistWithAnime(
    @Embedded
    val watchlist: UserWatchlistEntity,
    @Relation(
        parentColumn = "anime_id",
        entityColumn = "id"
    )
    val anime: AnimeEntity
)
