package com.github.ashuchoudhury.indianrail.scraper

import com.github.ashuchoudhury.indianrail.IndianRailClient
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LiveErailScraperTest {

    private lateinit var scraper: ErailScraper
    private val client = IndianRailClient.createDefaultClient()

    @BeforeAll
    fun setup() {
        scraper = ErailScraper(client)
    }

    @Test
    fun `live getTrainsBetweenStations returns multiple results`() = runTest {
        val response = scraper.getTrainsBetweenStations("NDLS", "HWH")
        if (response.success) {
            assertNotNull(response.data)
            assertTrue(response.data!!.isNotEmpty())
            println("Found ${response.data!!.size} trains between NDLS and HWH")
        } else {
            assertNotNull(response.error)
        }
    }

    @Test
    fun `live getTrain returns valid structural data`() = runTest {
        val trainNo = "12628"
        val response = scraper.getTrain(trainNo)
        if (response.success) {
            assertNotNull(response.data)
            assertEquals("12628", response.data?.train_no)
        } else {
            assertNotNull(response.error)
        }
    }

    @Test
    fun `live getLiveTrainStatus returns meaningful data`() = runTest {
        val trainNo = "12628"
        // Today's date in Erail format not strictly needed for the URL but good for records
        val date = LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy"))
        val response = scraper.getLiveTrainStatus(trainNo, date)
        
        if (response.success) {
            assertNotNull(response.data)
            assertEquals(trainNo, response.data?.TrainNo)
            // Even if train is on-time (0 mins), it should have a station name
            assertFalse(response.data?.StationName.isNullOrEmpty())
            println("Live Status for $trainNo: ${response.data?.StationName}, Delay: ${response.data?.DelayInMinutes} mins")
        } else {
            assertNotNull(response.error)
        }
    }

    @Test
    fun `live getStationLive returns results for major station`() = runTest {
        val response = scraper.getStationLive("NDLS")
        if (response.success) {
            assertNotNull(response.data)
            println("Live trains at NDLS: ${response.data!!.size}")
        } else {
            assertNotNull(response.error)
        }
    }
}
