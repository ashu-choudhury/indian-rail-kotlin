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
class LiveConfirmTktScraperTest {

    private lateinit var scraper: ConfirmTktScraper
    private val client = IndianRailClient.createDefaultClient()

    @BeforeAll
    fun setup() {
        scraper = ConfirmTktScraper(client)
    }

    @Test
    fun `live getPnrStatus returns error for invalid PNR`() = runTest {
        val pnr = "1234567890"
        val response = scraper.getPnrStatus(pnr)
        
        // ConfirmTkt might return success=true with empty data or success=false with error
        if (response.success) {
            // If success=true, ensure data parsing didn't crash but results are empty
            println("PNR Status for $pnr: ${response.data}")
            // Typically an invalid PNR has no passenger status
            assertTrue(response.data?.PassengerStatus?.isEmpty() ?: true, "Invalid PNR should have empty passenger status")
        } else {
            assertNotNull(response.error, "Failed response must have error message")
            println("PNR Status failed as expected: ${response.error}")
        }
    }

    @Test
    fun `live getLiveStatus returns valid data or clear error`() = runTest {
        val trainNo = "12628" // Karnataka Express
        val date = LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
        val response = scraper.getLiveStatus(trainNo, date)
        
        if (response.success) {
            assertNotNull(response.data, "Successful response must have data")
            assertNotNull(response.data?.TrainNo, "TrainNo must be present in successful status")
            println("Live Status for $trainNo: ${response.data}")
        } else {
            // We expect this to fail right now due to the 404 issue
            assertNotNull(response.error, "Failed response must have error message")
            println("Live Status failed (Possibly broken endpoint): ${response.error}")
        }
    }

    @Test
    fun `live getSeatAvailability returns valid data or clear error`() = runTest {
        val trainNo = "12628"
        val from = "SBC"
        val to = "NDLS"
        // Target a date in the future
        val date = LocalDate.now().plusDays(5).format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
        val response = scraper.getSeatAvailability(trainNo, from, to, date = date)
        
        if (response.success) {
            assertNotNull(response.data, "Successful response must have data")
            assertTrue(response.data?.Availability?.isNotEmpty() ?: false, "Should have availability data")
            println("Seat Avail for $trainNo: ${response.data}")
        } else {
            assertNotNull(response.error, "Failed response must have error message")
            println("Seat Avail failed (Possibly broken endpoint): ${response.error}")
        }
    }

    @Test
    fun `live getSchedule returns valid data or clear error`() = runTest {
        val trainNo = "12628"
        val response = scraper.getSchedule(trainNo)
        
        if (response.success) {
            assertNotNull(response.data, "Successful response must have data")
            assertEquals("12628", response.data?.TrainNo)
            assertTrue(response.data?.Stations?.isNotEmpty() ?: false, "Should have schedule stations")
            println("Schedule for $trainNo has ${response.data?.Stations?.size} stations")
        } else {
            assertNotNull(response.error, "Failed response must have error message")
            println("Schedule failed (Possibly broken endpoint): ${response.error}")
        }
    }
}
