package com.example.otakulist

import android.app.Application
import com.example.otakulist.data.local.AppDatabase
import com.example.otakulist.data.repository.AnimeRepository

class OtakuApplication : Application() {
    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }
    val repository: AnimeRepository by lazy { AnimeRepository(database.animeDao(), database.watchlistDao()) }
}
