package com.github.ashuchoudhury.indianrail.scraper

import com.github.ashuchoudhury.indianrail.models.BaseResponse
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ConfirmTktScraperTest {

    private fun createMockClient(mockResponse: String, statusCode: HttpStatusCode = HttpStatusCode.OK): HttpClient {
        val mockEngine = MockEngine { _ ->
            respond(
                content = mockResponse,
                status = statusCode,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        return HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true; isLenient = true; coerceInputValues = true })
            }
        }
    }

    private fun createErrorClient(): HttpClient {
        val mockEngine = MockEngine { _ ->
            respondError(HttpStatusCode.InternalServerError)
        }
        return HttpClient(mockEngine) {
             install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true; isLenient = true; coerceInputValues = true })
            }
        }
    }

    @Test
    fun `test getLiveStatus successful parsing`() = runTest {
        val mockJson = """
            {
              "TrainNo": "12301",
              "TrainName": "RAJDHANI",
              "DelayInMinutes": 10,
              "StationName": "NDLS"
            }
        """.trimIndent()

        val client = createMockClient(mockJson)
        val scraper = ConfirmTktScraper(client)

        val response = scraper.getLiveStatus("12301", "01-01-2026")
        assertTrue(response.success)
        assertNotNull(response.data)
        assertEquals("12301", response.data?.TrainNo)
        assertEquals(10, response.data?.DelayInMinutes)
    }

    @Test
    fun `test getLiveStatus failure json parsing`() = runTest {
        val mockJson = "invalid json"
        val client = createMockClient(mockJson)
        val scraper = ConfirmTktScraper(client)

        val response = scraper.getLiveStatus("12301", "01-01-2026")
        assertFalse(response.success)
        assertNull(response.data)
        assertNotNull(response.error)
        assertTrue(response.error!!.contains("parse"))
    }

    @Test
    fun `test getLiveStatus server error`() = runTest {
        val client = createErrorClient()
        val scraper = ConfirmTktScraper(client)

        val response = scraper.getLiveStatus("12301", "01-01-2026")
        assertFalse(response.success)
        assertNull(response.data)
        assertNotNull(response.error)
    }
}
