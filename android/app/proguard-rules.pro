# Room Database Protection
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao class * { *; }
-keep class * implements androidx.room.migration.Migration { *; }
-keep class com.example.otakulist.data.local.** { *; }
-keep class com.example.otakulist.data.local.entity.** { *; }
-keep class com.example.otakulist.data.local.dao.** { *; }

# Models and Serialization / Gson
-keep class com.example.otakulist.data.model.** { *; }
-keep class com.example.otakulist.data.model.WatchlistBackupModel { *; }
-keepclassmembers class com.example.otakulist.data.model.WatchlistBackupModel { *; }
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keep class com.google.gson.** { *; }

# Coil
-keep class coil.** { *; }

# Sync Models & Remote Client
-keep class com.example.otakulist.data.model.SyncManifestDto { *; }
-keep class com.example.otakulist.data.model.CatalogPatchDto { *; }
-keep class com.example.otakulist.data.model.AnimeEntityPayload { *; }
-keep class com.example.otakulist.data.remote.** { *; }
