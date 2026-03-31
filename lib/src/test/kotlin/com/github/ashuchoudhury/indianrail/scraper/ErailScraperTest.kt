package com.github.ashuchoudhury.indianrail.scraper

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ErailScraperTest {

    private fun createMockClient(mockResponse: String): HttpClient {
        val mockEngine = MockEngine { _ ->
            respond(
                content = mockResponse,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "text/html")
            )
        }
        return HttpClient(mockEngine)
    }

    @Test
    fun `test getTrain parsing with valid structure`() = runTest {
        val rawHtml = "12301~^12301~RAJDHANI EXPRES~NEW DELHI~NDLS~HOWRAH JN~HWH~00:00~00:00~19:00~19:00~16:55~10:00~20:00~1111111~~~~~~~~~2~HWH RAJDHANI~1~2~0~2~0~3~8~10~1452~85"
        val client = createMockClient(rawHtml)
        val scraper = ErailScraper(client)

        val response = scraper.getTrain("12301")
        assertTrue(response.success)
        assertNotNull(response.data)
        assertEquals("12301", response.data?.train_no)
        assertEquals("RAJDHANI EXPRES", response.data?.train_name)
        assertEquals("NEW DELHI", response.data?.from_stn_name)
    }

    @Test
    fun `test getTrain parsing with invalid structure returns failure`() = runTest {
        val rawHtml = "Train not found"
        val client = createMockClient(rawHtml)
        val scraper = ErailScraper(client)

        val response = scraper.getTrain("99999")
        assertFalse(response.success)
        assertNull(response.data)
        assertNotNull(response.error)
        assertEquals("Train not found", response.error)
    }
}
