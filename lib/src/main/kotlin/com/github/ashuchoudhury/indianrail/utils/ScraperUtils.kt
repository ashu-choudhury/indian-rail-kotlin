package com.github.ashuchoudhury.indianrail.utils

import java.util.Calendar

object ScraperUtils {
    val USER_AGENTS = listOf(
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36",
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/118.0.0.0 Safari/537.36",
        "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36"
    )

    fun getRandomUserAgent() = USER_AGENTS.random()

    fun getDayOnDate(dd: Int, mm: Int, yyyy: Int): Int {
        val calendar = Calendar.getInstance()
        calendar.set(yyyy, mm - 1, dd)
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) // 1 = Sunday, ..., 7 = Saturday
        
        // Match the logic from Node.js scraper:
        // day = date.getDay() >= 0 && date.getDay() <= 2 ? date.getDay() + 4 : date.getDay() - 3;
        // JS getDay(): 0 = Sun, 1 = Mon, 2 = Tue, 3 = Wed, 4 = Thu, 5 = Fri, 6 = Sat
        // Kotlin DAY_OF_WEEK: 1 = Sun, 2 = Mon, 3 = Tue, 4 = Wed, 5 = Thu, 6 = Fri, 7 = Sat
        
        val jsDay = dayOfWeek - 1
        return if (jsDay in 0..2) jsDay + 4 else jsDay - 3
    }
}
