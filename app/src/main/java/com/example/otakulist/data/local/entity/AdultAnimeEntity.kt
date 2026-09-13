package com.example.otakulist.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "adult_anime")
data class AdultAnimeEntity(
    @PrimaryKey
    val id: Int,
    val title: String,
    @ColumnInfo(name = "japanese_title")
    val japaneseTitle: String? = null,
    @ColumnInfo(name = "english_title")
    val englishTitle: String? = null,
    val slug: String? = null,
    val type: String? = null,
    val status: String? = null,
    val season: String? = null,
    val episodes: String? = null,
    val duration: String? = null,
    val rating: String? = null,
    val score: String? = null,
    val genres: String? = null,
    val studios: String? = null,
    val synopsis: String? = null,
    @ColumnInfo(name = "cover_url")
    val coverUrl: String? = null,
    @ColumnInfo(name = "cover_file")
    val coverFile: String? = null,
    @ColumnInfo(name = "otak_url")
    val otakUrl: String? = null,
    @ColumnInfo(name = "created_at")
    val createdAt: String? = null,
    @ColumnInfo(name = "related_anime")
    val relatedAnime: String? = null
)

fun AdultAnimeEntity.toAnimeEntity(): AnimeEntity {
    return AnimeEntity(
        id = id,
        title = title,
        japaneseTitle = japaneseTitle,
        englishTitle = englishTitle,
        slug = slug,
        type = type,
        status = status,
        season = season,
        episodes = episodes,
        duration = duration,
        rating = rating,
        score = score,
        genres = genres,
        studios = studios,
        synopsis = synopsis,
        coverUrl = coverUrl,
        coverFile = coverFile,
        otakUrl = otakUrl,
        createdAt = createdAt,
        relatedAnime = relatedAnime,
        isAdult = true
    )
}
