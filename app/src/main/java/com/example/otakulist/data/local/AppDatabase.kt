package com.example.otakulist.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.otakulist.data.local.dao.AnimeDao
import com.example.otakulist.data.local.dao.WatchlistDao
import com.example.otakulist.data.local.entity.AnimeEntity
import com.example.otakulist.data.local.entity.UserWatchlistEntity

@Database(
    entities = [
        AnimeEntity::class,
        UserWatchlistEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun animeDao(): AnimeDao
    abstract fun watchlistDao(): WatchlistDao

    companion object {
        private const val DATABASE_NAME = "anime_catalog.db"
        private const val ASSET_DATABASE_PATH = "databases/anime_catalog.db"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context.applicationContext).also { INSTANCE = it }
            }
        }

        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                DATABASE_NAME
            )
                .createFromAsset(ASSET_DATABASE_PATH)
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()
        }
    }
}
