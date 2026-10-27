package com.example.otakulist.data.model

import androidx.annotation.Keep
import com.example.otakulist.data.local.entity.AdultAnimeEntity
import com.example.otakulist.data.local.entity.AnimeEntity
import com.google.gson.annotations.SerializedName

@Keep
data class SyncManifestDto(
    @SerializedName("catalog_version") val catalogVersion: String = "1.0.0",
    @SerializedName("generated_at") val generatedAt: String = "",
    @SerializedName("new_regular_count") val newRegularCount: Int = 0,
    @SerializedName("new_adult_count") val newAdultCount: Int = 0,
    @SerializedName("covers_count") val coversCount: Int = 0,
    @SerializedName("covers_size_bytes") val coversSizeBytes: Long = 0L,
    @SerializedName("checksum_sha256") val checksumSha256: String? = null
)

@Keep
data class CatalogPatchDto(
    @SerializedName("version") val version: String = "1.0.0",
    @SerializedName("regular_anime") val regularAnime: List<AnimeEntityPayload> = emptyList(),
    @SerializedName("adult_anime") val adultAnime: List<AnimeEntityPayload> = emptyList()
)

@Keep
data class AnimeEntityPayload(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String,
    @SerializedName("japanese_title") val japaneseTitle: String? = null,
    @SerializedName("english_title") val englishTitle: String? = null,
    @SerializedName("slug") val slug: String? = null,
    @SerializedName("type") val type: String? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("season") val season: String? = null,
    @SerializedName("episodes") val episodes: String? = null,
    @SerializedName("duration") val duration: String? = null,
    @SerializedName("rating") val rating: String? = null,
    @SerializedName("score") val score: String? = null,
    @SerializedName("genres") val genres: String? = null,
    @SerializedName("studios") val studios: String? = null,
    @SerializedName("synopsis") val synopsis: String? = null,
    @SerializedName("cover_url") val coverUrl: String? = null,
    @SerializedName("cover_file") val coverFile: String? = null,
    @SerializedName("otak_url") val otakUrl: String? = null,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("related_anime") val relatedAnime: String? = null
)

fun AnimeEntityPayload.toAnimeEntity(): AnimeEntity {
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
        isAdult = false
    )
}

fun AnimeEntityPayload.toAdultAnimeEntity(): AdultAnimeEntity {
    return AdultAnimeEntity(
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
        relatedAnime = relatedAnime
    )
}

data class SyncSummary(
    val catalogVersion: String,
    val regularAddedOrUpdated: Int,
    val adultAddedOrUpdated: Int,
    val coversExtracted: Int
)

sealed class SyncProgress {
    data object Connecting : SyncProgress()
    data object CheckingManifest : SyncProgress()
    data class DownloadingPatch(val stage: String) : SyncProgress()
    data class DownloadingCovers(val progressPercent: Float) : SyncProgress()
    data class ExtractingCovers(val current: Int, val total: Int) : SyncProgress()
    data object WritingDatabase : SyncProgress()
    data object Finished : SyncProgress()
}
