package com.smokingtracker

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Calendar

class InsightsTest {

    private fun ts(hourOfDay: Int): Long =
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hourOfDay)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

    @Test
    fun `insight is null for empty input`() {
        assertNull(StatisticsManager().calculateTriggerTimeInsight(emptyList()))
    }

    @Test
    fun `insight requires minimum sample size of 3`() {
        val data = listOf("stress" to ts(8), "stress" to ts(9))
        assertNull(StatisticsManager().calculateTriggerTimeInsight(data))
    }

    @Test
    fun `insight detects morning correlation`() {
        val data = listOf(
            "stress" to ts(7), "stress" to ts(8), "stress" to ts(9), "stress" to ts(10)
        )
        val insight = StatisticsManager().calculateTriggerTimeInsight(data)
        assertNotNull(insight)
        assertEquals("stress", insight!!.triggerKey)
        assertEquals(R.string.peak_period_morning, insight.peakPeriodResId)
        assertEquals(100, insight.percent)
        assertEquals(4, insight.sampleSize)
    }

    @Test
    fun `insight skips triggers without dominant period`() {
        val data = listOf(
            "stress" to ts(2), "stress" to ts(9), "stress" to ts(15), "stress" to ts(21)
        )
        assertNull(StatisticsManager().calculateTriggerTimeInsight(data))
    }

    @Test
    fun `insight picks trigger with largest sample among candidates`() {
        val data = listOf(
            "small" to ts(7), "small" to ts(8), "small" to ts(9),
            "big" to ts(7), "big" to ts(9), "big" to ts(10), "big" to ts(11), "big" to ts(6)
        )
        val insight = StatisticsManager().calculateTriggerTimeInsight(data)
        assertEquals("big", insight!!.triggerKey)
        assertEquals(5, insight.sampleSize)
    }

    @Test
    fun `body levels are null with no last smoke`() {
        assertNull(StatisticsManager().calculateBodyLevels(0L))
    }

    @Test
    fun `body levels are null after 72 hours`() {
        val now = System.currentTimeMillis()
        assertNull(StatisticsManager().calculateBodyLevels(now - 73L * 3_600_000, now))
    }

    @Test
    fun `body levels start at 100 percent`() {
        val now = System.currentTimeMillis()
        val levels = StatisticsManager().calculateBodyLevels(now - 1_000, now)!!
        assertEquals(100, levels.nicotinePercent)
        assertEquals(100, levels.coPercent)
    }

    @Test
    fun `nicotine halves every 2 hours`() {
        val now = System.currentTimeMillis()
        val levels = StatisticsManager().calculateBodyLevels(now - 2L * 3_600_000, now)!!
        assertEquals(50, levels.nicotinePercent)
    }

    @Test
    fun `co halves every 5 hours`() {
        val now = System.currentTimeMillis()
        val levels = StatisticsManager().calculateBodyLevels(now - 5L * 3_600_000, now)!!
        assertEquals(50, levels.coPercent)
    }

    @Test
    fun `nicotine decays faster than co`() {
        val now = System.currentTimeMillis()
        val levels = StatisticsManager().calculateBodyLevels(now - 4L * 3_600_000, now)!!
        assert(levels.nicotinePercent < levels.coPercent)
    }
}
