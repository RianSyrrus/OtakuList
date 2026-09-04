package com.example.otakulist.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.otakulist.data.local.dao.AnimeDao
import com.example.otakulist.data.local.dao.WatchlistDao
import com.example.otakulist.data.local.entity.AnimeEntity
import com.example.otakulist.data.local.entity.UserWatchlistEntity
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import java.io.File
import java.io.FileOutputStream

@Database(
    entities = [
        AnimeEntity::class,
        UserWatchlistEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun animeDao(): AnimeDao
    abstract fun watchlistDao(): WatchlistDao

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
                .addMigrations(MIGRATION_1_2)
                .fallbackToDestructiveMigration(dropAllTables = true)
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onOpen(db: SupportSQLiteDatabase) {
                        super.onOpen(db)
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
                        syncCatalogIfOutdated(context, db)
                    }
                })
                .build()
        }

        private fun syncCatalogIfOutdated(context: Context, db: SupportSQLiteDatabase) {
            try {
                var needsSync = false
                val cursor = db.query("SELECT COUNT(*) FROM anime WHERE related_anime IS NOT NULL AND related_anime != ''")
                if (cursor.moveToFirst()) {
                    if (cursor.getInt(0) == 0) {
                        needsSync = true
                    }
                } else {
                    needsSync = true
                }
                cursor.close()

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
                        db.execSQL("DELETE FROM anime")
                        db.execSQL("""
                            INSERT INTO anime (
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
                        db.execSQL("DETACH DATABASE asset_db")
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
