package com.github.ashuchoudhury.indianrail.utils

import java.time.LocalDate

object ScraperUtils {
    val USER_AGENTS = listOf(
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36",
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/118.0.0.0 Safari/537.36",
        "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36"
    )

    fun getRandomUserAgent() = USER_AGENTS.random()

    @JvmStatic
    fun getDayOnDate(dd: Int, mm: Int, yyyy: Int): Int {
        return try {
            val localDate = LocalDate.of(yyyy, mm, dd)
            val dayOfWeek = localDate.dayOfWeek.value // 1 (Monday) to 7 (Sunday)
            
            // Map DayOfWeek to JS day (0 = Sunday, 1 = Monday)
            val jsDay = if (dayOfWeek == 7) 0 else dayOfWeek
            
            // Indian Rail website maps running days based on a custom index where:
            // Wed=0, Thu=1, Fri=2, Sat=3, Sun=4, Mon=5, Tue=6
            if (jsDay in 0..2) jsDay + 4 else jsDay - 3
        } catch (e: Exception) {
            -1
        }
    }
}
