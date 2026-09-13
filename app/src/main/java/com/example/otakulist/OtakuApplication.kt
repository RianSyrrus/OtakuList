package com.example.otakulist

import android.app.Application
import com.example.otakulist.data.local.AppDatabase
import com.example.otakulist.data.repository.AnimeRepository

import com.example.otakulist.data.local.AppSettingsManager

class OtakuApplication : Application() {
    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }
    val settingsManager: AppSettingsManager by lazy { AppSettingsManager.getInstance(this) }
    val repository: AnimeRepository by lazy {
        AnimeRepository(
            database = database,
            animeDao = database.animeDao(),
            watchlistDao = database.watchlistDao(),
            adultAnimeDao = database.adultAnimeDao(),
            watchlistAdultDao = database.watchlistAdultDao(),
            settingsManager = settingsManager
        )
    }
}
