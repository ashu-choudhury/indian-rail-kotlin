package com.github.ashuchoudhury.indianrail.scraper

import com.github.ashuchoudhury.indianrail.models.LiveStatusData
import com.github.ashuchoudhury.indianrail.models.PnrResponse
import com.github.ashuchoudhury.indianrail.models.PnrStatusData
import com.github.ashuchoudhury.indianrail.models.SeatAvailabilityData
import com.github.ashuchoudhury.indianrail.models.TrainScheduleData
import com.github.ashuchoudhury.indianrail.models.BaseResponse
import com.github.ashuchoudhury.indianrail.utils.ScraperUtils
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.json.Json

class ConfirmTktScraper(private val client: HttpClient) {

    private val PNR_URL = "https://www.confirmtkt.com/pnr-status/"
    private val LIVE_STATUS_URL = "https://www.confirmtkt.com/itapi/LiveStatus"
    private val SEAT_AVAILABILITY_URL = "https://www.confirmtkt.com/itapi/SeatAvailability"
    private val SCHEDULE_URL = "https://www.confirmtkt.com/itapi/Schedule"

    private val jsonFormatter = Json { ignoreUnknownKeys = true; isLenient = true; coerceInputValues = true }

    suspend fun getPnrStatus(pnr: String): PnrResponse {
        return try {
            val url = "$PNR_URL$pnr"
            val responseText = client.get(url) {
                header("User-Agent", ScraperUtils.getRandomUserAgent())
                header("Referer", "https://www.confirmtkt.com/pnr-status")
            }.bodyAsText()

            parsePnrStatus(responseText)
        } catch (e: Exception) {
            PnrResponse(false, System.currentTimeMillis(), error = e.localizedMessage ?: "Network error")
        }
    }

    suspend fun getLiveStatus(trainNo: String, date: String): BaseResponse<LiveStatusData> {
        return try {
            val url = "$LIVE_STATUS_URL?TrainNo=$trainNo&Date=$date"
            val responseText = client.get(url) {
                header("User-Agent", ScraperUtils.getRandomUserAgent())
                header("Referer", "https://www.confirmtkt.com/train-running-status")
            }.bodyAsText()
            val parsed = jsonFormatter.decodeFromString<LiveStatusData>(responseText)
            BaseResponse(true, System.currentTimeMillis(), data = parsed)
        } catch (e: Exception) {
            BaseResponse(false, System.currentTimeMillis(), error = "Failed to parse Live Status JSON: ${e.message}")
        }
    }

    suspend fun getSeatAvailability(
        trainNo: String, from: String, to: String,
        quota: String = "GN", cls: String = "SL", date: String
    ): BaseResponse<SeatAvailabilityData> {
        return try {
            val url = "$SEAT_AVAILABILITY_URL?TrainNo=$trainNo&From=$from&To=$to&Quota=$quota&Class=$cls&JourneyDate=$date"
            val responseText = client.get(url) {
                header("User-Agent", ScraperUtils.getRandomUserAgent())
                header("Referer", "https://www.confirmtkt.com/seat-availability")
            }.bodyAsText()
            val parsed = jsonFormatter.decodeFromString<SeatAvailabilityData>(responseText)
            BaseResponse(true, System.currentTimeMillis(), data = parsed)
        } catch (e: Exception) {
             BaseResponse(false, System.currentTimeMillis(), error = "Failed to parse Seat Availability JSON: ${e.message}")
        }
    }

    suspend fun getSchedule(trainNo: String): BaseResponse<TrainScheduleData> {
        return try {
            val url = "$SCHEDULE_URL?TrainNo=$trainNo"
            val responseText = client.get(url) {
                header("User-Agent", ScraperUtils.getRandomUserAgent())
                header("Referer", "https://www.confirmtkt.com/train-schedule")
            }.bodyAsText()
            val parsed = jsonFormatter.decodeFromString<TrainScheduleData>(responseText)
            BaseResponse(true, System.currentTimeMillis(), data = parsed)
        } catch (e: Exception) {
            BaseResponse(false, System.currentTimeMillis(), error = "Failed to parse Schedule JSON: ${e.message}")
        }
    }

    private fun parsePnrStatus(html: String): PnrResponse {
        val regex = Regex("""data\s*=\s*(\{.*?;)""")
        val match = regex.find(html)
        if (match != null) {
            val jsonStr = match.groupValues[1].removeSuffix(";")
            return try {
                val parsed = jsonFormatter.decodeFromString<PnrStatusData>(jsonStr)
                PnrResponse(true, System.currentTimeMillis(), data = parsed)
            } catch (e: Exception) {
                PnrResponse(false, System.currentTimeMillis(), error = "JSON parsing error: ${e.message}")
            }
        }
        return PnrResponse(false, System.currentTimeMillis(), error = "PNR data not found in HTML")
    }
}
