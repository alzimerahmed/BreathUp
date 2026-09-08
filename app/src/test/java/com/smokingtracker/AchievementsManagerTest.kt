package com.smokingtracker

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.concurrent.TimeUnit

class AchievementsManagerTest {

    private val manager = AchievementsManager()

    private fun ctx(
        entries: List<Long> = emptyList(),
        launches: List<Long> = emptyList(),
        timeWithoutSmoking: Long = 0L,
        dailyLimit: Int = 0,
        hasCancelledWithin10s: Boolean = false,
        hasMadeBackup: Boolean = false,
        hasChangedPackPrice: Boolean = false,
        themeLangChangesToday: Int = 0,
        analyticsVisitsToday: Int = 0
    ) = AchievementContext(
        timeWithoutSmoking = timeWithoutSmoking,
        entries = entries,
        launches = launches,
        dailyLimit = dailyLimit,
        hasMadeBackup = hasMadeBackup,
        hasChangedPackPrice = hasChangedPackPrice,
        hasCancelledWithin10s = hasCancelledWithin10s,
        themeLangChangesToday = themeLangChangesToday,
        analyticsVisitsToday = analyticsVisitsToday
    )

    private fun nowMinusDays(days: Long): Long = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(days)

    @Test
    fun `achievements list has unique ids`() {
        val ids = manager.achievementsList.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun `getAchievementById returns matching achievement`() {
        assertEquals("login_1", manager.getAchievementById("login_1")?.id)
        assertEquals(null, manager.getAchievementById("does_not_exist"))
    }

    @Test
    fun `nosmoke thresholds unlock at exact boundaries`() {
        assertTrue(manager.getAchievementById("nosmoke_1d")!!.condition(ctx(timeWithoutSmoking = TimeUnit.DAYS.toMillis(1))))
        assertFalse(manager.getAchievementById("nosmoke_1d")!!.condition(ctx(timeWithoutSmoking = TimeUnit.DAYS.toMillis(1) - 1)))
        assertTrue(manager.getAchievementById("nosmoke_1w")!!.condition(ctx(timeWithoutSmoking = TimeUnit.DAYS.toMillis(7))))
        assertTrue(manager.getAchievementById("nosmoke_1m")!!.condition(ctx(timeWithoutSmoking = TimeUnit.DAYS.toMillis(30))))
    }

    @Test
    fun `login_1 unlocks with any launch`() {
        assertTrue(manager.getAchievementById("login_1")!!.condition(ctx(launches = listOf(System.currentTimeMillis()))))
        assertFalse(manager.getAchievementById("login_1")!!.condition(ctx(launches = emptyList())))
    }

    @Test
    fun `consecutive launch days unlock login achievements`() {
        val today = System.currentTimeMillis()
        val threeDays = listOf(today, nowMinusDays(1), nowMinusDays(2))
        assertTrue(manager.getAchievementById("login_3")!!.condition(ctx(launches = threeDays)))
        assertFalse(manager.getAchievementById("login_7")!!.condition(ctx(launches = threeDays)))
    }

    @Test
    fun `non-consecutive launches do not unlock streak achievements`() {
        val today = System.currentTimeMillis()
        val scattered = listOf(today, nowMinusDays(1), nowMinusDays(3), nowMinusDays(5))
        assertFalse(manager.getAchievementById("login_3")!!.condition(ctx(launches = scattered)))
    }

    @Test
    fun `secret double damage requires two entries within ten minutes`() {
        val now = System.currentTimeMillis()
        val close = listOf(now, now + 5 * 60 * 1000L)
        assertTrue(manager.getAchievementById("secret_double_damage")!!.condition(ctx(entries = close)))
        val far = listOf(now, now + TimeUnit.HOURS.toMillis(2))
        assertFalse(manager.getAchievementById("secret_double_damage")!!.condition(ctx(entries = far)))
        val single = listOf(now)
        assertFalse(manager.getAchievementById("secret_double_damage")!!.condition(ctx(entries = single)))
    }

    @Test
    fun `secret crisis requires a day exactly at the daily limit`() {
        val today = System.currentTimeMillis()
        val limit = 5
        val exact = (0 until limit).map { today + it * 60_000L }
        assertTrue(manager.getAchievementById("secret_crisis")!!.condition(ctx(entries = exact, dailyLimit = limit)))
        val under = (0 until limit - 1).map { today + it * 60_000L }
        assertFalse(manager.getAchievementById("secret_crisis")!!.condition(ctx(entries = under, dailyLimit = limit)))
        assertFalse(manager.getAchievementById("secret_crisis")!!.condition(ctx(entries = exact, dailyLimit = 0)))
    }

    @Test
    fun `secret blind eye requires five over the daily limit`() {
        val now = System.currentTimeMillis()
        val over = (0 until 10).map { now + it * 60_000L }
        assertTrue(manager.getAchievementById("secret_blind_eye")!!.condition(ctx(entries = over, dailyLimit = 5)))
        assertFalse(manager.getAchievementById("secret_blind_eye")!!.condition(ctx(entries = over, dailyLimit = 6)))
        assertFalse(manager.getAchievementById("secret_blind_eye")!!.condition(ctx(entries = over, dailyLimit = 0)))
    }

    @Test
    fun `flag based secrets unlock from context flags`() {
        assertTrue(manager.getAchievementById("secret_hesitant")!!.condition(ctx(hasCancelledWithin10s = true)))
        assertFalse(manager.getAchievementById("secret_hesitant")!!.condition(ctx()))
        assertTrue(manager.getAchievementById("secret_archivist")!!.condition(ctx(hasMadeBackup = true)))
        assertTrue(manager.getAchievementById("secret_inflation")!!.condition(ctx(hasChangedPackPrice = true)))
        assertTrue(manager.getAchievementById("secret_explorer")!!.condition(ctx(themeLangChangesToday = 3)))
        assertTrue(manager.getAchievementById("secret_analytics_collector")!!.condition(ctx(analyticsVisitsToday = 10)))
    }

    @Test
    fun `secret night owl requires an entry between 3 and 4 am`() {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 3)
            set(Calendar.MINUTE, 30)
        }
        assertTrue(manager.getAchievementById("secret_night_owl")!!.condition(ctx(entries = listOf(cal.timeInMillis))))
        val noon = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 12) }
        assertFalse(manager.getAchievementById("secret_night_owl")!!.condition(ctx(entries = listOf(noon.timeInMillis))))
    }

    @Test
    fun `secret synchronization requires midnight or noon exactly`() {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 12)
            set(Calendar.MINUTE, 0)
        }
        assertTrue(manager.getAchievementById("secret_synchronization")!!.condition(ctx(entries = listOf(cal.timeInMillis))))
        val almostNoon = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 12)
            set(Calendar.MINUTE, 1)
        }
        assertFalse(manager.getAchievementById("secret_synchronization")!!.condition(ctx(entries = listOf(almostNoon.timeInMillis))))
    }

    @Test
    fun `secret punctuality requires three evenly spaced entries`() {
        val base = System.currentTimeMillis()
        val interval = 10 * 60 * 1000L
        val punctual = listOf(0, 1, 2).map { base + it * interval }
        assertTrue(manager.getAchievementById("secret_punctuality")!!.condition(ctx(entries = punctual)))
        val irregular = listOf(base, base + interval, base + interval * 5)
        assertFalse(manager.getAchievementById("secret_punctuality")!!.condition(ctx(entries = irregular)))
        val tooFew = listOf(base, base + interval)
        assertFalse(manager.getAchievementById("secret_punctuality")!!.condition(ctx(entries = tooFew)))
    }

    @Test
    fun `calculateUnlockedAchievements returns all satisfied ids`() {
        val unlocked = manager.calculateUnlockedAchievements(
            ctx = ctx(
                entries = emptyList(),
                launches = listOf(System.currentTimeMillis()),
                timeWithoutSmoking = TimeUnit.DAYS.toMillis(2)
            )
        )
        assertTrue("login_1" in unlocked)
        assertTrue("nosmoke_1d" in unlocked)
        assertTrue("nosmoke_3d" !in unlocked)
        assertTrue("login_3" !in unlocked)
    }

    @Test
    fun `progress fraction is capped between zero and one`() {
        val now = System.currentTimeMillis()
        val entries = listOf(now - TimeUnit.HOURS.toMillis(12))
        assertEquals(0.5f, manager.progressFraction("nosmoke_1d", entries, emptyList()), 0.01f)
        assertEquals(1f, manager.progressFraction("nosmoke_1d", listOf(now - TimeUnit.DAYS.toMillis(2)), emptyList()))
        assertEquals(0f, manager.progressFraction("nosmoke_1d", emptyList(), emptyList()))
        assertEquals(1f, manager.progressFraction("login_1", emptyList(), listOf(now)), 0f)
        assertEquals(0f, manager.progressFraction("unknown_id", emptyList(), emptyList()), 0f)
    }
}
