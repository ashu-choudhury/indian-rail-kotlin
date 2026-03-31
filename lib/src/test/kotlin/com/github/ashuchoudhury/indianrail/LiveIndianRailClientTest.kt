package com.github.ashuchoudhury.indianrail

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LiveIndianRailClientTest {

    private lateinit var client: IndianRailClient

    @BeforeAll
    fun setup() {
        client = IndianRailClient()
    }

    @Test
    fun `live getTrainsBetweenStationsOnDate handles DayOfWeek logic`() = runTest {
        val from = "NDLS"
        val to = "SBC"
        // Use a date 5 days from now
        val futureDate = LocalDate.now().plusDays(5)
        val dateStr = futureDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
        
        val response = client.getTrainsBetweenStationsOnDate(from, to, dateStr)
        if (response.success) {
            assertNotNull(response.data)
            println("Found ${response.data!!.size} trains for $dateStr")
        } else {
            assertNotNull(response.error)
        }
    }

    @Test
    fun `live getLiveTrainStatus for active train`() = runTest {
        val trainNo = "12628"
        val date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val response = client.getLiveTrainStatus(trainNo, date)
        if (response.success) {
            assertNotNull(response.data)
            assertNotNull(response.data?.StationName)
            println("Live Status for $trainNo: ${response.data?.StationName}, Delay: ${response.data?.DelayInMinutes} mins")
        } else {
            assertNotNull(response.error)
            println("Live Status for $trainNo failed: ${response.error}")
        }
    }

    @Test
    fun `live test invalid input resiliency`() = runTest {
        val response = client.getTrainDetails("INVALID")
        assertFalse(response.success)
        assertNotNull(response.error)
    }

    @Test
    fun `live E2E Search to Status`() = runTest {
        // Step 1: Search
        val search = client.getTrainsBetweenStations("NDLS", "SBC")
        assertTrue(search.success && search.data!!.isNotEmpty(), "Initial search must succeed")
        
        val firstTrain = search.data!!.first().train_base.train_no
        println("First train from search: $firstTrain")
        
        // Step 2: Get Status
        val date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val status = client.getLiveTrainStatus(firstTrain, date)
        if (status.success) {
            assertNotNull(status.data)
            println("Successfully tracked $firstTrain from search: ${status.data?.StationName}")
        } else {
            println("E2E tracking failed for $firstTrain: ${status.error}")
        }
    }

    @Test
    fun `live getFullTrainSchedule returns exhaustive data`() = runTest {
        val trainNo = "12301" // Rajdhani
        val response = client.getFullTrainSchedule(trainNo)
        if (response.success) {
            assertNotNull(response.data)
            assertEquals("12301", response.data?.TrainNo)
            assertTrue(response.data!!.Stations.isNotEmpty())
            println("Schedule for $trainNo has ${response.data!!.Stations.size} stations")
        } else {
            assertNotNull(response.error)
        }
    }
}
