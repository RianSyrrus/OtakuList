package com.example.otakulist.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "anime")
data class AnimeEntity(
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
) {
    @androidx.room.Ignore
    var isAdult: Boolean = false

    @androidx.room.Ignore
    constructor(
        id: Int,
        title: String,
        japaneseTitle: String? = null,
        englishTitle: String? = null,
        slug: String? = null,
        type: String? = null,
        status: String? = null,
        season: String? = null,
        episodes: String? = null,
        duration: String? = null,
        rating: String? = null,
        score: String? = null,
        genres: String? = null,
        studios: String? = null,
        synopsis: String? = null,
        coverUrl: String? = null,
        coverFile: String? = null,
        otakUrl: String? = null,
        createdAt: String? = null,
        relatedAnime: String? = null,
        isAdult: Boolean
    ) : this(
        id, title, japaneseTitle, englishTitle, slug, type, status, season,
        episodes, duration, rating, score, genres, studios, synopsis,
        coverUrl, coverFile, otakUrl, createdAt, relatedAnime
    ) {
        this.isAdult = isAdult
    }
}

data class RelatedAnimeItem(
    val id: Int,
    val title: String,
    val type: String? = null
)

fun AnimeEntity.getParsedRelatedAnime(): List<RelatedAnimeItem> {
    if (relatedAnime.isNullOrBlank() || relatedAnime == "[]") return emptyList()
    return try {
        val listType = object : com.google.gson.reflect.TypeToken<List<RelatedAnimeItem>>() {}.type
        com.google.gson.Gson().fromJson(relatedAnime, listType) ?: emptyList()
    } catch (_: Exception) {
        emptyList()
    }
}

fun AnimeEntity.getPrimaryStudio(): String? {
    if (studios.isNullOrBlank()) return null
    return studios
        .split(",")
        .map { it.trim() }
        .firstOrNull { it.isNotBlank() && it != "-" }
}

fun AnimeEntity.getProducers(): String? {
    if (studios.isNullOrBlank()) return null
    val parts = studios
        .split(",")
        .map { it.trim() }
        .filter { it.isNotBlank() && it != "-" }
    if (parts.size <= 1) return null
    return parts.drop(1).joinToString(", ")
}

