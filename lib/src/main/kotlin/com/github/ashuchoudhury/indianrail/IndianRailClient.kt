package com.github.ashuchoudhury.indianrail

import com.github.ashuchoudhury.indianrail.models.BaseResponse
import com.github.ashuchoudhury.indianrail.models.CombinedTrain
import com.github.ashuchoudhury.indianrail.models.LiveStationTrain
import com.github.ashuchoudhury.indianrail.models.PnrResponse
import com.github.ashuchoudhury.indianrail.models.RouteStop
import com.github.ashuchoudhury.indianrail.models.Train
import com.github.ashuchoudhury.indianrail.scraper.ConfirmTktScraper
import com.github.ashuchoudhury.indianrail.scraper.ErailScraper
import com.github.ashuchoudhury.indianrail.utils.ScraperUtils
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class IndianRailClient(private val client: HttpClient = createDefaultClient()) {

    private val erailScraper = ErailScraper(client)
    private val pnrScraper = ConfirmTktScraper(client)

    /**
     * Get trains between two stations.
     * @param from Station code (e.g., "NDLS")
     * @param to Station code (e.g., "BCT")
     */
    suspend fun getTrainsBetweenStations(from: String, to: String): BaseResponse<List<CombinedTrain>> {
        return erailScraper.getTrainsBetweenStations(from, to)
    }

    /**
     * Get trains between two stations on a specific date.
     * @param from Station code
     * @param to Station code
     * @param date Date in "DD-MM-YYYY" format
     */
    suspend fun getTrainsBetweenStationsOnDate(from: String, to: String, date: String): BaseResponse<List<CombinedTrain>> {
        val result = erailScraper.getTrainsBetweenStations(from, to)
        if (!result.success) return result

        val dateParts = date.split("-")
        if (dateParts.size != 3) return BaseResponse(false, System.currentTimeMillis(), emptyList())

        val day = ScraperUtils.getDayOnDate(dateParts[0].toInt(), dateParts[1].toInt(), dateParts[2].toInt())
        val filtered = result.data.filter {
            it.train_base.running_days.getOrNull(day) == '1'
        }

        return BaseResponse(true, System.currentTimeMillis(), filtered)
    }

    /**
     * Get details of a single train.
     */
    suspend fun getTrainDetails(trainNo: String): BaseResponse<Train> {
        return erailScraper.getTrain(trainNo)
    }

    /**
     * Get the route (stops) of a train.
     */
    suspend fun getTrainRoute(trainNo: String): BaseResponse<List<RouteStop>> {
        return erailScraper.getTrainRoute(trainNo)
    }

    /**
     * Get live station status (trains arriving/departing).
     */
    suspend fun getLiveStation(stationCode: String): BaseResponse<List<LiveStationTrain>> {
        return erailScraper.getStationLive(stationCode)
    }

    /**
     * Get PNR status.
     */
    suspend fun getPnrStatus(pnr: String): PnrResponse {
        return pnrScraper.getPnrStatus(pnr)
    }

    /**
     * Get live train status (exactly where it is).
     * @param date The date the train started its journey in "YYYY-MM-DD" format.
     */
    suspend fun getLiveTrainStatus(trainNo: String, date: String): BaseResponse<kotlinx.serialization.json.JsonElement> {
        val data = pnrScraper.getLiveStatus(trainNo, date)
        return BaseResponse(true, System.currentTimeMillis(), data)
    }

    /**
     * Get seat availability and fare.
     */
    suspend fun getSeatAvailability(
        trainNo: String, from: String, to: String,
        date: String, quota: String = "GN", cls: String = "SL"
    ): BaseResponse<kotlinx.serialization.json.JsonElement> {
        val data = pnrScraper.getSeatAvailability(trainNo, from, to, quota, cls, date)
        return BaseResponse(true, System.currentTimeMillis(), data)
    }

    /**
     * Get full detailed schedule of a train.
     */
    suspend fun getFullTrainSchedule(trainNo: String): BaseResponse<kotlinx.serialization.json.JsonElement> {
        val data = pnrScraper.getSchedule(trainNo)
        return BaseResponse(true, System.currentTimeMillis(), data)
    }

    fun close() {
        client.close()
    }

    companion object {
        fun createDefaultClient(): HttpClient {
            return HttpClient(OkHttp) {
                install(ContentNegotiation) {
                    json(Json {
                        ignoreUnknownKeys = true
                        prettyPrint = true
                        isLenient = true
                    })
                }
            }
        }
    }
}
