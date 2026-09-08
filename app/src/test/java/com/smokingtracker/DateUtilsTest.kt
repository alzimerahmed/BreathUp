package com.smokingtracker

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar

class DateUtilsTest {

    private fun calendarAt(year: Int, month: Int, day: Int, hour: Int = 12): Long =
        Calendar.getInstance().apply {
            clear()
            set(year, month, day, hour, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

    @Test
    fun `same instant returns zero days`() {
        val now = System.currentTimeMillis()
        assertEquals(0L, daysBetween(now, now))
    }

    @Test
    fun `same calendar day returns zero regardless of time`() {
        val morning = calendarAt(2026, Calendar.SEPTEMBER, 8, 1)
        val evening = calendarAt(2026, Calendar.SEPTEMBER, 8, 23)
        assertEquals(0L, daysBetween(morning, evening))
    }

    @Test
    fun `next calendar day returns one`() {
        val day1 = calendarAt(2026, Calendar.SEPTEMBER, 8)
        val day2 = calendarAt(2026, Calendar.SEPTEMBER, 9)
        assertEquals(1L, daysBetween(day1, day2))
    }

    @Test
    fun `crossing month boundary counts correctly`() {
        val sep30 = calendarAt(2026, Calendar.SEPTEMBER, 30)
        val oct1 = calendarAt(2026, Calendar.OCTOBER, 1)
        assertEquals(1L, daysBetween(sep30, oct1))
    }

    @Test
    fun `negative direction returns negative days`() {
        val day1 = calendarAt(2026, Calendar.SEPTEMBER, 8)
        val day2 = calendarAt(2026, Calendar.SEPTEMBER, 10)
        assertEquals(-2L, daysBetween(day2, day1))
    }
}
