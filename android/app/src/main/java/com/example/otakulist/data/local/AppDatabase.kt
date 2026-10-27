package com.example.otakulist.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.otakulist.data.local.dao.AdultAnimeDao
import com.example.otakulist.data.local.dao.AnimeDao
import com.example.otakulist.data.local.dao.WatchlistAdultDao
import com.example.otakulist.data.local.dao.WatchlistDao
import com.example.otakulist.data.local.entity.AdultAnimeEntity
import com.example.otakulist.data.local.entity.AnimeEntity
import com.example.otakulist.data.local.entity.UserWatchlistAdultEntity
import com.example.otakulist.data.local.entity.UserWatchlistEntity
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import java.io.File
import java.io.FileOutputStream

@Database(
    entities = [
        AnimeEntity::class,
        UserWatchlistEntity::class,
        AdultAnimeEntity::class,
        UserWatchlistAdultEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun animeDao(): AnimeDao
    abstract fun watchlistDao(): WatchlistDao
    abstract fun adultAnimeDao(): AdultAnimeDao
    abstract fun watchlistAdultDao(): WatchlistAdultDao

    companion object {
        private const val DATABASE_NAME = "anime_catalog.db"
        private const val ASSET_DATABASE_PATH = "databases/anime_catalog.db"

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try {
                    db.execSQL("ALTER TABLE anime ADD COLUMN related_anime TEXT")
                } catch (_: Exception) {
                }
                try {
                    db.execSQL("""
                        CREATE TABLE IF NOT EXISTS user_watchlist (
                            anime_id INTEGER NOT NULL,
                            status TEXT NOT NULL,
                            current_episode INTEGER NOT NULL DEFAULT 0,
                            personal_score INTEGER NOT NULL DEFAULT 0,
                            personal_note TEXT NOT NULL DEFAULT '',
                            updated_at INTEGER NOT NULL DEFAULT 0,
                            PRIMARY KEY(anime_id),
                            FOREIGN KEY(anime_id) REFERENCES anime(id) ON UPDATE NO ACTION ON DELETE CASCADE
                        )
                    """.trimIndent())
                    db.execSQL("CREATE INDEX IF NOT EXISTS index_user_watchlist_status ON user_watchlist (status)")
                } catch (_: Exception) {
                }
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try {
                    db.execSQL("""
                        CREATE TABLE IF NOT EXISTS adult_anime (
                            id INTEGER NOT NULL,
                            title TEXT NOT NULL,
                            japanese_title TEXT,
                            english_title TEXT,
                            slug TEXT,
                            type TEXT,
                            status TEXT,
                            season TEXT,
                            episodes TEXT,
                            duration TEXT,
                            rating TEXT,
                            score TEXT,
                            genres TEXT,
                            studios TEXT,
                            synopsis TEXT,
                            cover_url TEXT,
                            cover_file TEXT,
                            otak_url TEXT,
                            created_at TEXT,
                            related_anime TEXT,
                            PRIMARY KEY(id)
                        )
                    """.trimIndent())
                } catch (_: Exception) {
                }
                try {
                    db.execSQL("""
                        CREATE TABLE IF NOT EXISTS user_watchlist_adult (
                            anime_id INTEGER NOT NULL,
                            status TEXT NOT NULL,
                            current_episode INTEGER NOT NULL DEFAULT 0,
                            personal_score INTEGER NOT NULL DEFAULT 0,
                            personal_note TEXT NOT NULL DEFAULT '',
                            updated_at INTEGER NOT NULL DEFAULT 0,
                            PRIMARY KEY(anime_id),
                            FOREIGN KEY(anime_id) REFERENCES adult_anime(id) ON UPDATE NO ACTION ON DELETE CASCADE
                        )
                    """.trimIndent())
                    db.execSQL("CREATE INDEX IF NOT EXISTS index_user_watchlist_adult_status ON user_watchlist_adult (status)")
                } catch (_: Exception) {
                }
            }
        }

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
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                .fallbackToDestructiveMigration(dropAllTables = true)
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onOpen(db: SupportSQLiteDatabase) {
                        super.onOpen(db)
                        try {
                            db.execSQL("CREATE TABLE IF NOT EXISTS room_table_modification_log (table_id INTEGER PRIMARY KEY, invalidated INTEGER NOT NULL DEFAULT 0)")
                        } catch (_: Exception) {
                        }
                        try {
                            db.execSQL("UPDATE anime SET cover_file = replace(cover_file, '.jpg', '.webp') WHERE cover_file LIKE '%.jpg'")
                        } catch (_: Exception) {
                        }
                        try {
                            db.execSQL("""
                                CREATE TABLE IF NOT EXISTS user_watchlist (
                                    anime_id INTEGER NOT NULL,
                                    status TEXT NOT NULL,
                                    current_episode INTEGER NOT NULL DEFAULT 0,
                                    personal_score INTEGER NOT NULL DEFAULT 0,
                                    personal_note TEXT NOT NULL DEFAULT '',
                                    updated_at INTEGER NOT NULL DEFAULT 0,
                                    PRIMARY KEY(anime_id),
                                    FOREIGN KEY(anime_id) REFERENCES anime(id) ON UPDATE NO ACTION ON DELETE CASCADE
                                )
                            """.trimIndent())
                            db.execSQL("CREATE INDEX IF NOT EXISTS index_user_watchlist_status ON user_watchlist (status)")
                        } catch (_: Exception) {
                        }
                        try {
                            db.execSQL("""
                                CREATE TABLE IF NOT EXISTS adult_anime (
                                    id INTEGER NOT NULL,
                                    title TEXT NOT NULL,
                                    japanese_title TEXT,
                                    english_title TEXT,
                                    slug TEXT,
                                    type TEXT,
                                    status TEXT,
                                    season TEXT,
                                    episodes TEXT,
                                    duration TEXT,
                                    rating TEXT,
                                    score TEXT,
                                    genres TEXT,
                                    studios TEXT,
                                    synopsis TEXT,
                                    cover_url TEXT,
                                    cover_file TEXT,
                                    otak_url TEXT,
                                    created_at TEXT,
                                    related_anime TEXT,
                                    PRIMARY KEY(id)
                                )
                            """.trimIndent())
                        } catch (_: Exception) {
                        }
                        try {
                            db.execSQL("""
                                CREATE TABLE IF NOT EXISTS user_watchlist_adult (
                                    anime_id INTEGER NOT NULL,
                                    status TEXT NOT NULL,
                                    current_episode INTEGER NOT NULL DEFAULT 0,
                                    personal_score INTEGER NOT NULL DEFAULT 0,
                                    personal_note TEXT NOT NULL DEFAULT '',
                                    updated_at INTEGER NOT NULL DEFAULT 0,
                                    PRIMARY KEY(anime_id),
                                    FOREIGN KEY(anime_id) REFERENCES adult_anime(id) ON UPDATE NO ACTION ON DELETE CASCADE
                                )
                            """.trimIndent())
                            db.execSQL("CREATE INDEX IF NOT EXISTS index_user_watchlist_adult_status ON user_watchlist_adult (status)")
                        } catch (_: Exception) {
                        }
                        syncCatalogIfOutdated(context, db)
                    }
                })
                .build()
        }

        private const val CURRENT_CATALOG_SYNC_VERSION = 3

        private fun syncCatalogIfOutdated(context: Context, db: SupportSQLiteDatabase) {
            try {
                val prefs = context.getSharedPreferences("kurotrack_catalog_pref", Context.MODE_PRIVATE)
                val lastSyncedVersion = prefs.getInt("synced_catalog_version", 0)

                var needsSync = lastSyncedVersion < CURRENT_CATALOG_SYNC_VERSION

                if (!needsSync) {
                    try {
                        val cursor = db.query("SELECT COUNT(*) FROM anime WHERE related_anime IS NOT NULL AND related_anime != ''")
                        if (cursor.moveToFirst() && cursor.getInt(0) == 0) needsSync = true
                        cursor.close()
                    } catch (_: Exception) {
                        needsSync = true
                    }

                    try {
                        val adultCursor = db.query("SELECT COUNT(*) FROM adult_anime")
                        if (adultCursor.moveToFirst() && adultCursor.getInt(0) == 0) needsSync = true
                        adultCursor.close()
                    } catch (_: Exception) {
                        needsSync = true
                    }
                }

                if (needsSync) {
                    val tempFile = File(context.cacheDir, "temp_catalog_sync.db")
                    try {
                        context.assets.open(ASSET_DATABASE_PATH).use { input ->
                            FileOutputStream(tempFile).use { output ->
                                input.copyTo(output)
                            }
                        }
                        val tempPath = tempFile.absolutePath.replace("'", "''")
                        db.execSQL("ATTACH DATABASE '$tempPath' AS asset_db")
                        
                        db.execSQL("""
                            INSERT OR REPLACE INTO anime (
                                id, title, japanese_title, english_title, slug, type, status, season,
                                episodes, duration, rating, score, genres, studios, synopsis,
                                cover_url, cover_file, otak_url, created_at, related_anime
                            )
                            SELECT 
                                id, title, japanese_title, english_title, slug, type, status, season,
                                episodes, duration, rating, score, genres, studios, synopsis,
                                cover_url, cover_file, otak_url, created_at, related_anime
                            FROM asset_db.anime
                        """.trimIndent())

                        db.execSQL("""
                            INSERT OR REPLACE INTO adult_anime (
                                id, title, japanese_title, english_title, slug, type, status, season,
                                episodes, duration, rating, score, genres, studios, synopsis,
                                cover_url, cover_file, otak_url, created_at, related_anime
                            )
                            SELECT 
                                id, title, japanese_title, english_title, slug, type, status, season,
                                episodes, duration, rating, score, genres, studios, synopsis,
                                cover_url, cover_file, otak_url, created_at, related_anime
                            FROM asset_db.adult_anime
                        """.trimIndent())

                        db.execSQL("DETACH DATABASE asset_db")
                        prefs.edit().putInt("synced_catalog_version", CURRENT_CATALOG_SYNC_VERSION).apply()
                    } finally {
                        if (tempFile.exists()) {
                            tempFile.delete()
                        }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("AppDatabase", "Failed to sync catalog", e)
            }
        }
    }
}
