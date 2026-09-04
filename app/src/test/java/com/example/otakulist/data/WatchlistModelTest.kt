package com.example.otakulist.data

import com.example.otakulist.data.model.AnimeSortOrder
import com.example.otakulist.data.model.WatchlistStatus
import com.example.otakulist.data.repository.WatchlistBackupModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class WatchlistModelTest {

    @Test
    fun testWatchlistStatusFromValue() {
        assertEquals(WatchlistStatus.WATCHING, WatchlistStatus.fromValue("WATCHING"))
        assertEquals(WatchlistStatus.WATCHING, WatchlistStatus.fromValue("Watching"))
        assertEquals(WatchlistStatus.PLAN_TO_WATCH, WatchlistStatus.fromValue("PLAN_TO_WATCH"))
        assertEquals(WatchlistStatus.COMPLETED, WatchlistStatus.fromValue("COMPLETED"))
        assertEquals(WatchlistStatus.DROPPED, WatchlistStatus.fromValue("DROPPED"))
        // fallback
        assertEquals(WatchlistStatus.WATCHING, WatchlistStatus.fromValue("UNKNOWN"))
    }

    @Test
    fun testSortOrderEnum() {
        assertEquals(3, AnimeSortOrder.entries.size)
        assertEquals("Skor Tertinggi", AnimeSortOrder.SCORE_DESC.displayName)
    }

    @Test
    fun testBackupJsonSerialization() {
        val gson = Gson()
        val originalList = listOf(
            WatchlistBackupModel(
                animeId = 1,
                status = WatchlistStatus.WATCHING.value,
                currentEpisode = 5,
                personalScore = 9,
                personalNote = "Great anime",
                updatedAt = 123456789L
            ),
            WatchlistBackupModel(
                animeId = 6,
                status = WatchlistStatus.COMPLETED.value,
                currentEpisode = 12,
                personalScore = 10,
                personalNote = "Masterpiece",
                updatedAt = 987654321L
            )
        )

        val json = gson.toJson(originalList)
        assertNotNull(json)

        val type = object : TypeToken<List<WatchlistBackupModel>>() {}.type
        val restoredList: List<WatchlistBackupModel> = gson.fromJson(json, type)

        assertEquals(2, restoredList.size)
        assertEquals(1, restoredList[0].animeId)
        assertEquals("WATCHING", restoredList[0].status)
        assertEquals(5, restoredList[0].currentEpisode)
        assertEquals(9, restoredList[0].personalScore)
        assertEquals("Great anime", restoredList[0].personalNote)
    }
}
