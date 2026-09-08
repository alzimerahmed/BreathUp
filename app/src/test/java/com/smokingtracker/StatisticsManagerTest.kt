package com.smokingtracker

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.concurrent.TimeUnit

class StatisticsManagerTest {

    private val manager = StatisticsManager()

    private fun todayAt(hour: Int, minute: Int = 0): Long =
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

    private fun daysAgoAt(daysAgo: Int, hour: Int = 10): Long =
        todayAt(hour) - TimeUnit.DAYS.toMillis(daysAgo.toLong())

    @Test
    fun `empty entries return zeroed stats`() {
        val stats = manager.calculateStats(emptyList())
        assertEquals(0, stats.totalCount)
        assertEquals(0, stats.maxPerDay)
        assertEquals(0, stats.minPerDay)
        assertEquals(0f, stats.avgPerDay)
        assertNull(stats.trackingSince)
        assertEquals(0, stats.longestStreakDays)
        assertEquals(0, stats.totalTrackingDays)
    }

    @Test
    fun `single day entries produce max and min equal to count`() {
        val now = System.currentTimeMillis()
        val entries = listOf(now, now - 1, now - 2)
        val stats = manager.calculateStats(entries)
        assertEquals(3, stats.totalCount)
        assertEquals(3, stats.maxPerDay)
        assertEquals(3, stats.minPerDay)
        assertEquals(now - 2, stats.trackingSince)
    }

    @Test
    fun `days without entries make min per day zero`() {
        val today = todayAt(10)
        val threeDaysAgo = daysAgoAt(3)
        val stats = manager.calculateStats(listOf(today, threeDaysAgo))
        assertEquals(1, stats.maxPerDay)
        assertEquals(0, stats.minPerDay)
        assertTrue(stats.totalTrackingDays >= 4)
    }

    @Test
    fun `unsorted entries are handled`() {
        val now = System.currentTimeMillis()
        val twoHoursAgo = now - TimeUnit.HOURS.toMillis(2)
        val stats = manager.calculateStats(listOf(now, twoHoursAgo))
        assertEquals(twoHoursAgo, stats.trackingSince)
        assertEquals(2, stats.totalCount)
    }

    @Test
    fun `current smoke free streak is zero when last entry is today`() {
        assertEquals(0, manager.currentSmokeFreeStreakDays(listOf(todayAt(8))))
    }

    @Test
    fun `current smoke free streak counts days since last entry`() {
        val entries = listOf(daysAgoAt(3), daysAgoAt(5))
        assertEquals(3, manager.currentSmokeFreeStreakDays(entries))
    }

    @Test
    fun `current smoke free streak is zero for empty entries`() {
        assertEquals(0, manager.currentSmokeFreeStreakDays(emptyList()))
    }

    @Test
    fun `weekly count only includes entries in the reference week`() {
        val date = Calendar.getInstance()
        val inWeek = date.timeInMillis - TimeUnit.HOURS.toMillis(1)
        val outOfWeek = date.timeInMillis - TimeUnit.DAYS.toMillis(14)
        val count = manager.getWeeklyCount(listOf(inWeek, outOfWeek), date)
        assertEquals(1, count)
    }

    @Test
    fun `monthly count only includes entries in the reference month`() {
        val date = Calendar.getInstance()
        val inMonth = date.timeInMillis - TimeUnit.HOURS.toMillis(1)
        val outOfMonth = date.timeInMillis - TimeUnit.DAYS.toMillis(40)
        val count = manager.getMonthlyCount(listOf(inMonth, outOfMonth), date)
        assertEquals(1, count)
    }

    @Test
    fun `daily data has 24 buckets and counts the right hour`() {
        val date = Calendar.getInstance().apply { timeInMillis = todayAt(15) }
        val data = manager.generateDailyData(listOf(todayAt(15), todayAt(15, 30), todayAt(9)), date)
        assertEquals(24, data.size)
        assertEquals(2, data[15])
        assertEquals(1, data[9])
        assertEquals(0, data[0])
    }

    @Test
    fun `weekly data has 7 buckets`() {
        val date = Calendar.getInstance()
        val data = manager.generateWeeklyData(listOf(date.timeInMillis), date)
        assertEquals(7, data.size)
        assertEquals(1, data.sum())
    }

    @Test
    fun `yearly data has 12 buckets`() {
        val date = Calendar.getInstance()
        val data = manager.generateYearlyData(listOf(date.timeInMillis), date)
        assertEquals(12, data.size)
        assertEquals(1, data.sum())
    }

    @Test
    fun `monthly data returns 4 weekly chunks`() {
        val date = Calendar.getInstance()
        val data = manager.generateMonthlyData(listOf(date.timeInMillis), date)
        assertEquals(4, data.size)
        assertEquals(1, data.sum())
    }

    @Test
    fun `historical baseline computes cigarettes and money`() {
        val start = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(10)
        val stats = manager.calculateHistoricalBaseline(
            startDate = start,
            dailyAvg = 20,
            packPrice = 100f,
            packSize = 20,
            rankedTriggers = emptyList()
        )
        assertEquals(10, stats.totalDays)
        assertEquals(200, stats.totalCigarettes)
        assertEquals(1000.0, stats.totalMoneySpent, 0.01)
    }

    @Test
    fun `historical baseline returns zero for invalid input`() {
        val now = System.currentTimeMillis()
        assertEquals(
            StatisticsManager.HistoricalBaselineStats(0, 0.0, 0, emptyMap()),
            manager.calculateHistoricalBaseline(0L, 20, 100f, 20, emptyList())
        )
        assertEquals(
            StatisticsManager.HistoricalBaselineStats(0, 0.0, 0, emptyMap()),
            manager.calculateHistoricalBaseline(now + 1, 20, 100f, 20, emptyList())
        )
        assertEquals(
            StatisticsManager.HistoricalBaselineStats(0, 0.0, 0, emptyMap()),
            manager.calculateHistoricalBaseline(now - 1000, 0, 100f, 20, emptyList())
        )
    }

    @Test
    fun `historical baseline trigger counts sum to total cigarettes`() {
        val start = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(5)
        val stats = manager.calculateHistoricalBaseline(
            startDate = start,
            dailyAvg = 10,
            packPrice = 50f,
            packSize = 20,
            rankedTriggers = listOf("stress", "coffee", "alcohol")
        )
        assertEquals(50, stats.totalCigarettes)
        assertEquals(
            stats.totalCigarettes,
            stats.estimatedTriggerCounts.values.sum()
        )
        assertTrue(stats.estimatedTriggerCounts["stress"]!! >= stats.estimatedTriggerCounts["coffee"]!!)
        assertTrue(stats.estimatedTriggerCounts["coffee"]!! >= stats.estimatedTriggerCounts["alcohol"]!!)
    }

    @Test
    fun `weekly comparison detects decrease`() {
        val ref = Calendar.getInstance()
        val thisWeek = ref.timeInMillis - TimeUnit.HOURS.toMillis(1)
        val lastWeek = ref.timeInMillis - TimeUnit.DAYS.toMillis(7)
        val result = manager.calculateWeeklyComparison(
            entries = listOf(thisWeek, lastWeek, lastWeek, lastWeek),
            referenceDate = ref
        )
        assertEquals(1, result.thisWeekCount)
        assertEquals(3, result.lastWeekCount)
        assertEquals(-2, result.difference)
        assertEquals(StatisticsManager.ComparisonTrend.DECREASED, result.trend)
        assertEquals(67, result.percentChange)
    }

    @Test
    fun `weekly comparison detects increase`() {
        val ref = Calendar.getInstance()
        val thisWeek1 = ref.timeInMillis - TimeUnit.HOURS.toMillis(1)
        val thisWeek2 = ref.timeInMillis - TimeUnit.HOURS.toMillis(2)
        val lastWeek = ref.timeInMillis - TimeUnit.DAYS.toMillis(7)
        val result = manager.calculateWeeklyComparison(
            entries = listOf(thisWeek1, thisWeek2, lastWeek),
            referenceDate = ref
        )
        assertEquals(2, result.thisWeekCount)
        assertEquals(1, result.lastWeekCount)
        assertEquals(StatisticsManager.ComparisonTrend.INCREASED, result.trend)
    }

    @Test
    fun `weekly comparison with empty previous week reports 100 percent`() {
        val ref = Calendar.getInstance()
        val result = manager.calculateWeeklyComparison(
            entries = listOf(ref.timeInMillis),
            referenceDate = ref
        )
        assertEquals(1, result.thisWeekCount)
        assertEquals(0, result.lastWeekCount)
        assertEquals(100, result.percentChange)
        assertEquals(StatisticsManager.ComparisonTrend.INCREASED, result.trend)
    }

    @Test
    fun `hourly distribution finds peak hour and period`() {
        val entries = listOf(todayAt(14), todayAt(14, 20), todayAt(14, 40), todayAt(3))
        val result = manager.calculateHourlyDistribution(entries)
        assertEquals(14, result.peakHour)
        assertEquals(3, result.peakHourCount)
        assertEquals(75, result.peakPeriodPercent)
        assertEquals(R.string.peak_period_afternoon, result.peakPeriodNameResId)
    }
}
