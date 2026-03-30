package com.github.ashuchoudhury.indianrail.scraper

import com.github.ashuchoudhury.indianrail.models.BaseResponse
import com.github.ashuchoudhury.indianrail.models.CombinedTrain
import com.github.ashuchoudhury.indianrail.models.LiveStationTrain
import com.github.ashuchoudhury.indianrail.models.RouteStop
import com.github.ashuchoudhury.indianrail.models.Train
import com.github.ashuchoudhury.indianrail.utils.ScraperUtils
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import org.jsoup.Jsoup

class ErailScraper(private val client: HttpClient) {

    private val ERAIL_BASE = "https://erail.in"

    suspend fun getTrainsBetweenStations(from: String, to: String): BaseResponse<List<CombinedTrain>> {
        val url = "$ERAIL_BASE/rail/getTrains.aspx?Station_From=$from&Station_To=$to&DataSource=0&Language=0&Cache=true"
        val responseText = client.get(url) {
            header("User-Agent", ScraperUtils.getRandomUserAgent())
        }.bodyAsText()

        return parseBetweenStations(responseText)
    }

    suspend fun getTrain(trainNo: String): BaseResponse<Train> {
        val url = "$ERAIL_BASE/rail/getTrains.aspx?TrainNo=$trainNo&DataSource=0&Language=0&Cache=true"
        val responseText = client.get(url) {
            header("User-Agent", ScraperUtils.getRandomUserAgent())
        }.bodyAsText()

        return parseCheckTrain(responseText)
    }

    suspend fun getTrainRoute(trainNo: String): BaseResponse<List<RouteStop>> {
        val trainInfo = getTrain(trainNo)
        if (!trainInfo.success) {
            return BaseResponse(false, System.currentTimeMillis(), emptyList())
        }
        val trainId = trainInfo.data.train_id ?: ""
        val url = "$ERAIL_BASE/data.aspx?Action=TRAINROUTE&Password=2012&Data1=$trainId&Data2=0&Cache=true"
        val responseText = client.get(url) {
            header("User-Agent", ScraperUtils.getRandomUserAgent())
        }.bodyAsText()

        return parseRoute(responseText)
    }

    suspend fun getStationLive(stationCode: String): BaseResponse<List<LiveStationTrain>> {
        val url = "$ERAIL_BASE/station-live/$stationCode?DataSource=0&Language=0&Cache=true"
        val responseText = client.get(url) {
            header("User-Agent", ScraperUtils.getRandomUserAgent())
        }.bodyAsText()

        return parseLiveStation(responseText)
    }

    private fun parseBetweenStations(raw: String): BaseResponse<List<CombinedTrain>> {
        val parts = raw.split("~~~~~~~~").filter { it.isNotBlank() }
        if (parts.isEmpty()) return BaseResponse(false, System.currentTimeMillis(), emptyList())

        val firstPart = parts[0].split("~")
        if (firstPart.size > 5 && firstPart[5].startsWith("No direct trains found")) {
            return BaseResponse(false, System.currentTimeMillis(), emptyList())
        }

        if (raw.contains("Please try again after some time.") ||
            raw.contains("From station not found") ||
            raw.contains("To station not found")
        ) {
            return BaseResponse(false, System.currentTimeMillis(), emptyList())
        }

        val trains = mutableListOf<CombinedTrain>()
        for (part in parts) {
            val subParts = part.split("~^")
            if (subParts.size == 2) {
                val data = subParts[1].split("~").filter { it.isNotBlank() }
                if (data.size >= 14) {
                    val train = Train(
                        train_no = data[0],
                        train_name = data[1],
                        source_stn_name = data[2],
                        source_stn_code = data[3],
                        dstn_stn_name = data[4],
                        dstn_stn_code = data[5],
                        from_stn_name = data[6],
                        from_stn_code = data[7],
                        to_stn_name = data[8],
                        to_stn_code = data[9],
                        from_time = data[10],
                        to_time = data[11],
                        travel_time = data[12],
                        running_days = data[13]
                    )
                    trains.add(CombinedTrain(train))
                }
            }
        }

        return BaseResponse(true, System.currentTimeMillis(), trains)
    }

    private fun parseCheckTrain(raw: String): BaseResponse<Train> {
        val parts = raw.split("~~~~~~~~").filter { it.isNotBlank() }
        if (parts.isEmpty() || raw.contains("Train not found")) {
            return BaseResponse(false, System.currentTimeMillis(), Train("", "", "", "", "", "", "", "", "", "", "", "", "", ""))
        }

        var data1 = parts[0].split("~").filter { it.isNotBlank() }.toMutableList()
        if (data1.size > 1 && data1[1].length > 6) {
            data1.removeAt(0)
        }

        if (data1.size < 15) {
             return BaseResponse(false, System.currentTimeMillis(), Train("", "", "", "", "", "", "", "", "", "", "", "", "", ""))
        }

        val train = Train(
            train_no = data1[1].replace("^", ""),
            train_name = data1[2],
            from_stn_name = data1[3],
            from_stn_code = data1[4],
            to_stn_name = data1[5],
            to_stn_code = data1[6],
            from_time = data1[11],
            to_time = data1[12],
            travel_time = data1[13],
            running_days = data1[14]
        )

        // Additional data from second part if available
        if (parts.size > 1) {
            val data2 = parts[1].split("~").filter { it.isNotBlank() }
            if (data2.size >= 20) {
                return BaseResponse(true, System.currentTimeMillis(), train.copy(
                    type = data2[11],
                    train_id = data2[12],
                    distance_from_to = data2[18],
                    average_speed = data2[19]
                ))
            }
        }

        return BaseResponse(true, System.currentTimeMillis(), train)
    }

    private fun parseRoute(raw: String): BaseResponse<List<RouteStop>> {
        val stationsRaw = raw.split("~^")
        val route = mutableListOf<RouteStop>()
        for (stnRaw in stationsRaw) {
            val data = stnRaw.split("~").filter { it.isNotBlank() }
            if (data.size >= 10) {
                route.add(RouteStop(
                    source_stn_name = data[2],
                    source_stn_code = data[1],
                    arrive = data[3],
                    depart = data[4],
                    distance = data[6],
                    day = data[7],
                    zone = data[9]
                ))
            }
        }
        return BaseResponse(true, System.currentTimeMillis(), route)
    }

    private fun parseLiveStation(html: String): BaseResponse<List<LiveStationTrain>> {
        val doc = Jsoup.parse(html)
        val trains = mutableListOf<LiveStationTrain>()
        val elements = doc.select(".name")
        for (el in elements) {
            val text = el.text()
            val trainNo = text.take(5)
            val trainName = text.drop(5).trim()
            val stnDiv = el.nextElementSibling()
            val stnText = stnDiv?.text() ?: ""
            val nextTd = el.parent()?.nextElementSibling()
            val timeText = nextTd?.text() ?: ""

            trains.add(LiveStationTrain(
                train_no = trainNo,
                train_name = trainName,
                source_stn_name = stnText.split("→").firstOrNull()?.trim() ?: "",
                dstn_stn_name = stnText.split("→").getOrNull(1)?.trim() ?: "",
                time_at = timeText.take(5),
                detail = timeText.drop(5)
            ))
        }
        return BaseResponse(true, System.currentTimeMillis(), trains)
    }
}
