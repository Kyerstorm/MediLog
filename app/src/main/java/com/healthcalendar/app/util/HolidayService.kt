package com.healthcalendar.app.util

import android.content.Context
import com.healthcalendar.app.data.database.entities.Appointment
import com.healthcalendar.app.data.database.entities.AppointmentCategory
import com.healthcalendar.app.data.repository.AppointmentRepository
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.coroutines.runBlocking
import java.time.LocalDate as JLocalDate
import java.time.DayOfWeek
import java.time.YearMonth
import java.util.*

/**
 * Simple service to seed UK bank holidays into the app database on first run.
 *
 * This implementation seeds a precomputed set for a range of years and computes
 * movable feasts (Easter) so the list covers the next few years by algorithm.
 */
object HolidayService {
    private const val PREFS_NAME = "holiday_prefs"
    private const val PREF_KEY_SEEDED = "uk_holidays_seeded_v1"

    // Seed for current year +/- range
    private const val YEAR_RANGE = 3

    fun seedUkBankHolidaysIfNeeded(context: Context, repo: AppointmentRepository) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (prefs.getBoolean(PREF_KEY_SEEDED, false)) return

        // Run blocking here since caller may be on IO dispatcher; keep simple.
        runBlocking {
            val now = JLocalDate.now()
            val startYear = now.year - 1
            val endYear = now.year + YEAR_RANGE

            val holidays = mutableListOf<Pair<JLocalDate, String>>()

            for (y in startYear..endYear) {
                // New Year's Day (Jan 1) - if weekend, substitute following Monday
                holidays += substituteHoliday(JLocalDate.of(y, 1, 1), "New Year's Day")

                // Good Friday - 2 days before Easter
                val easter = calculateEasterSunday(y)
                holidays += Pair(easter.minusDays(2), "Good Friday")

                // Easter Monday
                holidays += Pair(easter.plusDays(1), "Easter Monday")

                // Early May bank holiday - first Monday in May
                holidays += Pair(nthWeekdayOfMonth(y, 5, DayOfWeek.MONDAY, 1), "Early May bank holiday")

                // Spring bank holiday - last Monday in May
                holidays += Pair(lastWeekdayOfMonth(y, 5, DayOfWeek.MONDAY), "Spring bank holiday")

                // Summer bank holiday - last Monday in August
                holidays += Pair(lastWeekdayOfMonth(y, 8, DayOfWeek.MONDAY), "Summer bank holiday")

                // Christmas Day (Dec 25)
                holidays += substituteHoliday(JLocalDate.of(y, 12, 25), "Christmas Day")

                // Boxing Day (Dec 26)
                holidays += substituteHoliday(JLocalDate.of(y, 12, 26), "Boxing Day")
            }

            // Insert as all-day Appointments
            for ((jdate, title) in holidays) {
                val start = LocalDateTime(jdate.year, jdate.monthValue, jdate.dayOfMonth, 0, 0)
                val end = LocalDateTime(jdate.year, jdate.monthValue, jdate.dayOfMonth, 23, 59)
                val appt = Appointment(
                    title = title,
                    description = "",
                    location = "",
                    phoneNumber = "",
                    startTime = start,
                    endTime = end,
                    isAllDay = true,
                    category = AppointmentCategory.OTHER,
                    createdAt = LocalDateTime(1970,1,1,0,0)
                )
                try {
                    repo.insertAppointment(appt)
                } catch (_: Exception) {
                    // best-effort
                }
            }

            prefs.edit().putBoolean(PREF_KEY_SEEDED, true).apply()
        }
    }

    private fun substituteHoliday(date: JLocalDate, title: String): Pair<JLocalDate, String> {
        // If weekend, move to following Monday (UK bank holiday substitution)
        val dow = date.dayOfWeek
        return when (dow) {
            DayOfWeek.SATURDAY -> Pair(date.plusDays(2), "$title (substitute)")
            DayOfWeek.SUNDAY -> Pair(date.plusDays(1), "$title (substitute)")
            else -> Pair(date, title)
        }
    }

    // Calculate Easter Sunday for a given year (Anonymous Gregorian algorithm)
    private fun calculateEasterSunday(year: Int): JLocalDate {
        val a = year % 19
        val b = year / 100
        val c = year % 100
        val d = b / 4
        val e = b % 4
        val f = (b + 8) / 25
        val g = (b - f + 1) / 3
        val h = (19 * a + b - d - g + 15) % 30
        val i = c / 4
        val k = c % 4
        val l = (32 + 2 * e + 2 * i - h - k) % 7
        val m = (a + 11 * h + 22 * l) / 451
        val month = (h + l - 7 * m + 114) / 31
        val day = ((h + l - 7 * m + 114) % 31) + 1
        return JLocalDate.of(year, month, day)
    }

    private fun nthWeekdayOfMonth(year: Int, month: Int, weekday: DayOfWeek, n: Int): JLocalDate {
        var date = JLocalDate.of(year, month, 1)
        var count = 0
        while (true) {
            if (date.dayOfWeek == weekday) {
                count++
                if (count == n) break
            }
            date = date.plusDays(1)
        }
        return date
    }

    private fun lastWeekdayOfMonth(year: Int, month: Int, weekday: DayOfWeek): JLocalDate {
        var date = JLocalDate.of(year, month, YearMonth.of(year, month).lengthOfMonth())
        while (date.dayOfWeek != weekday) {
            date = date.minusDays(1)
        }
        return date
    }

}
