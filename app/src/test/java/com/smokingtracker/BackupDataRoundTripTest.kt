package com.smokingtracker

import com.google.gson.Gson
import com.smokingtracker.BackupManager.BackupData
import com.smokingtracker.BackupManager.BackupEntry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BackupDataRoundTripTest {

    private val gson = Gson()

    @Test
    fun `backup data serializes and deserializes losslessly`() {
        val original = BackupData(
            isRegistered = true,
            entries = listOf(
                BackupEntry(timestamp = 1000L, trigger = "stress", isResisted = false),
                BackupEntry(timestamp = 2000L, trigger = null, isResisted = true, cravingIntensity = 7, outcomeNote = "held on")
            ),
            appTheme = "DARK",
            unlockedAchievements = setOf("login_1", "nosmoke_1d"),
            dailyLimit = 5,
            packPrice = 12.5f,
            packSize = 20,
            currency = "EUR",
            colorPreset = "OCEAN_DEEP",
            fontPreset = "ZENITH",
            amoledTheme = true,
            customTriggers = listOf("walking"),
            disabledDefaultTriggers = setOf("alcohol")
        )

        val json = gson.toJson(original)
        val restored = gson.fromJson(json, BackupData::class.java)

        assertEquals(3, restored.version)
        assertEquals(true, restored.isRegistered)
        assertEquals(2, restored.entries?.size)
        assertEquals(BackupEntry(timestamp = 1000L, trigger = "stress", isResisted = false), restored.entries!![0])
        assertEquals(BackupEntry(timestamp = 2000L, trigger = null, isResisted = true, cravingIntensity = 7, outcomeNote = "held on"), restored.entries!![1])
        assertEquals("DARK", restored.appTheme)
        assertEquals(setOf("login_1", "nosmoke_1d"), restored.unlockedAchievements)
        assertEquals(5, restored.dailyLimit)
        assertEquals(12.5f, restored.packPrice)
        assertEquals(20, restored.packSize)
        assertEquals("EUR", restored.currency)
        assertEquals(listOf("walking"), restored.customTriggers)
        assertEquals(setOf("alcohol"), restored.disabledDefaultTriggers)
    }

    @Test
    fun `legacy backup with smokingEntries and entryTriggers parses`() {
        val legacyJson = """
            {
              "version": 2,
              "isRegistered": true,
              "smokingEntries": [1000, 2000],
              "entryTriggers": {"1000": "coffee"},
              "appTheme": "SYSTEM",
              "unlockedAchievements": ["login_1"]
            }
        """.trimIndent()

        val data = gson.fromJson(legacyJson, BackupData::class.java)

        assertNull(data.entries)
        assertEquals(listOf(1000L, 2000L), data.smokingEntries)
        assertEquals(mapOf(1000L to "coffee"), data.entryTriggers)
        // Gson bypasses Kotlin default values for missing fields -> null.
        // BackupManager.restore coalesces these nulls, so behavior stays safe.
        assertNull(data.colorPreset)
        assertNull(data.fontPreset)
        assertNull(data.packSize)
    }

    @Test
    fun `missing optional fields deserialize as null`() {
        val minimalJson = """
            {
              "isRegistered": false,
              "appTheme": "LIGHT",
              "unlockedAchievements": []
            }
        """.trimIndent()

        val data = gson.fromJson(minimalJson, BackupData::class.java)

        // Gson leaves missing fields null (defaults are bypassed);
        // BackupManager.restore applies the fallbacks instead.
        assertNull(data.dailyLimit)
        assertNull(data.packSize)
        assertNull(data.currency)
        assertNull(data.containerBorderEnabled)
        assertNull(data.notificationLowPriority)
        assertNull(data.notificationShowTimer)
        assertNull(data.notificationShowResistButton)
        assertNull(data.entries)
        assertNull(data.appLaunchDates)
        assertNull(data.disabledDefaultTriggers)
    }

    @Test
    fun `backup entry defaults apply for legacy entries`() {
        val json = """{"timestamp": 42}"""
        val entry = gson.fromJson(json, BackupEntry::class.java)
        assertEquals(42L, entry.timestamp)
        assertNull(entry.trigger)
        assertEquals(false, entry.isResisted)
    }
}
