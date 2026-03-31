package com.github.ashuchoudhury.indianrail

import com.github.ashuchoudhury.indianrail.models.*
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
     */
    suspend fun getTrainsBetweenStations(from: String, to: String): BaseResponse<List<CombinedTrain>> {
        return erailScraper.getTrainsBetweenStations(from, to)
    }

    /**
     * Get trains between two stations on a specific date.
     */
    suspend fun getTrainsBetweenStationsOnDate(from: String, to: String, date: String): BaseResponse<List<CombinedTrain>> {
        val result = erailScraper.getTrainsBetweenStations(from, to)
        if (!result.success || result.data == null) return result

        val dateParts = date.split("-")
        if (dateParts.size != 3) return BaseResponse(false, System.currentTimeMillis(), error = "Invalid date format. Expected DD-MM-YYYY")

        val day = ScraperUtils.getDayOnDate(dateParts[0].toInt(), dateParts[1].toInt(), dateParts[2].toInt())
        if (day == -1) return BaseResponse(false, System.currentTimeMillis(), error = "Failed to parse date")
        
        val filtered = result.data.filter {
            it.train_base.running_days.getOrNull(day) == '1'
        }

        return BaseResponse(true, System.currentTimeMillis(), data = filtered)
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
     * Uses ConfirmTkt as the primary source for PNR.
     */
    suspend fun getPnrStatus(pnr: String): PnrResponse {
        return pnrScraper.getPnrStatus(pnr)
    }

    /**
     * Get live train status.
     * Pivoted to Erail for production reliability.
     */
    suspend fun getLiveTrainStatus(trainNo: String, date: String): BaseResponse<LiveStatusData> {
        return erailScraper.getLiveTrainStatus(trainNo, date)
    }

    /**
     * Get seat availability and fare.
     * Currently still uses ConfirmTkt (Note: prone to 404s, fallback logic expected).
     */
    suspend fun getSeatAvailability(
        trainNo: String, from: String, to: String,
        date: String, quota: String = "GN", cls: String = "SL"
    ): BaseResponse<SeatAvailabilityData> {
        return pnrScraper.getSeatAvailability(trainNo, from, to, quota, cls, date)
    }

    /**
     * Get full detailed schedule of a train.
     * Mapping to Erail route data for resilience.
     */
    suspend fun getFullTrainSchedule(trainNo: String): BaseResponse<TrainScheduleData> {
        val routeResponse = erailScraper.getTrainRoute(trainNo)
        if (!routeResponse.success || routeResponse.data == null) {
            return BaseResponse(false, routeResponse.time_stamp, error = routeResponse.error)
        }
        
        val scheduleData = TrainScheduleData(
            TrainNo = trainNo,
            Stations = routeResponse.data.map { stop ->
                ScheduleStation(
                    StationCode = stop.source_stn_code,
                    StationName = stop.source_stn_name,
                    ArrivalTime = stop.arrive,
                    DepartureTime = stop.depart,
                    Distance = stop.distance,
                    DayCount = stop.day
                )
            }
        )
        return BaseResponse(true, routeResponse.time_stamp, data = scheduleData)
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
                        coerceInputValues = true
                    })
                }
            }
        }
    }
}
