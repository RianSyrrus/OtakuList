package com.example.otakulist.data.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class WatchlistBackupModel(
    @SerializedName("animeId", alternate = ["a"])
    val animeId: Int,

    @SerializedName("status", alternate = ["b"])
    val status: String,

    @SerializedName("currentEpisode", alternate = ["c"])
    val currentEpisode: Int = 0,

    @SerializedName("personalScore", alternate = ["d"])
    val personalScore: Int = 0,

    @SerializedName("personalNote", alternate = ["e"])
    val personalNote: String = "",

    @SerializedName("updatedAt", alternate = ["f"])
    val updatedAt: Long = System.currentTimeMillis(),

    @SerializedName("isAdult", alternate = ["g"])
    val isAdult: Boolean = false
)
