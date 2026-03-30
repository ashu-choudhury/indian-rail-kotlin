package com.github.ashuchoudhury.indianrail.scraper

import com.github.ashuchoudhury.indianrail.models.PnrResponse
import com.github.ashuchoudhury.indianrail.utils.ScraperUtils
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

class ConfirmTktScraper(private val client: HttpClient) {

    private val PNR_URL = "https://www.confirmtkt.com/pnr-status/"
    private val LIVE_STATUS_URL = "https://www.confirmtkt.com/itapi/LiveStatus"
    private val SEAT_AVAILABILITY_URL = "https://www.confirmtkt.com/itapi/SeatAvailability"
    private val SCHEDULE_URL = "https://www.confirmtkt.com/itapi/Schedule"

    suspend fun getPnrStatus(pnr: String): PnrResponse {
        val url = "$PNR_URL$pnr"
        val responseText = client.get(url) {
            header("User-Agent", ScraperUtils.getRandomUserAgent())
        }.bodyAsText()

        return parsePnrStatus(responseText)
    }

    suspend fun getLiveStatus(trainNo: String, date: String): JsonElement {
        val url = "$LIVE_STATUS_URL?TrainNo=$trainNo&Date=$date"
        val responseText = client.get(url) {
            header("User-Agent", ScraperUtils.getRandomUserAgent())
        }.bodyAsText()
        return Json.parseToJsonElement(responseText)
    }

    suspend fun getSeatAvailability(
        trainNo: String, from: String, to: String,
        quota: String = "GN", cls: String = "SL", date: String
    ): JsonElement {
        val url = "$SEAT_AVAILABILITY_URL?TrainNo=$trainNo&From=$from&To=$to&Quota=$quota&Class=$cls&JourneyDate=$date"
        val responseText = client.get(url) {
            header("User-Agent", ScraperUtils.getRandomUserAgent())
        }.bodyAsText()
        return Json.parseToJsonElement(responseText)
    }

    suspend fun getSchedule(trainNo: String): JsonElement {
        val url = "$SCHEDULE_URL?TrainNo=$trainNo"
        val responseText = client.get(url) {
            header("User-Agent", ScraperUtils.getRandomUserAgent())
        }.bodyAsText()
        return Json.parseToJsonElement(responseText)
    }

    private fun parsePnrStatus(html: String): PnrResponse {
        val regex = Regex("""data\s*=\s*(\{.*?;)""")
        val match = regex.find(html)
        if (match != null) {
            val jsonStr = match.groupValues[1].removeSuffix(";")
            return try {
                val jsonElement = Json.parseToJsonElement(jsonStr)
                PnrResponse(true, System.currentTimeMillis(), jsonElement)
            } catch (e: Exception) {
                PnrResponse(false, System.currentTimeMillis(), Json.parseToJsonElement("{}"))
            }
        }
        return PnrResponse(false, System.currentTimeMillis(), Json.parseToJsonElement("{}"))
    }
}
