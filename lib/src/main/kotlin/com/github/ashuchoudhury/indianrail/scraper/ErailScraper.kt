package com.github.ashuchoudhury.indianrail.scraper

import com.github.ashuchoudhury.indianrail.models.*
import com.github.ashuchoudhury.indianrail.utils.ScraperUtils
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.*
import org.jsoup.Jsoup

class ErailScraper(private val client: HttpClient) {

    private val ERAIL_BASE = "https://erail.in"

    suspend fun getTrainsBetweenStations(from: String, to: String): BaseResponse<List<CombinedTrain>> {
        return try {
            val url = "$ERAIL_BASE/rail/getTrains.aspx?Station_From=$from&Station_To=$to&DataSource=0&Language=0&Cache=true"
            val responseText = client.get(url) {
                header("User-Agent", ScraperUtils.getRandomUserAgent())
            }.bodyAsText()

            parseBetweenStations(responseText)
        } catch (e: Exception) {
            BaseResponse(false, System.currentTimeMillis(), error = e.localizedMessage ?: "Network error")
        }
    }

    suspend fun getTrain(trainNo: String): BaseResponse<Train> {
        return try {
            val url = "$ERAIL_BASE/rail/getTrains.aspx?TrainNo=$trainNo&DataSource=0&Language=0&Cache=true"
            val responseText = client.get(url) {
                header("User-Agent", ScraperUtils.getRandomUserAgent())
            }.bodyAsText()

            parseCheckTrain(responseText)
        } catch (e: Exception) {
            BaseResponse(false, System.currentTimeMillis(), error = e.localizedMessage ?: "Network error")
        }
    }

    suspend fun getTrainRoute(trainNo: String): BaseResponse<List<RouteStop>> {
        return try {
            val trainInfo = getTrain(trainNo)
            if (!trainInfo.success || trainInfo.data == null) {
                return BaseResponse(false, System.currentTimeMillis(), error = "Train info not found")
            }
            val trainId = trainInfo.data.train_id ?: ""
            if (trainId.isBlank()) {
                return BaseResponse(false, System.currentTimeMillis(), error = "Invalid Train ID")
            }
            val url = "$ERAIL_BASE/data.aspx?Action=TRAINROUTE&Password=2012&Data1=$trainId&Data2=0&Cache=true"
            val responseText = client.get(url) {
                header("User-Agent", ScraperUtils.getRandomUserAgent())
            }.bodyAsText()

            parseRoute(responseText)
        } catch (e: Exception) {
            BaseResponse(false, System.currentTimeMillis(), error = e.localizedMessage ?: "Network error")
        }
    }

    suspend fun getStationLive(stationCode: String): BaseResponse<List<LiveStationTrain>> {
        return try {
            val url = "$ERAIL_BASE/station-live/$stationCode?DataSource=0&Language=0&Cache=true"
            val responseText = client.get(url) {
                header("User-Agent", ScraperUtils.getRandomUserAgent())
            }.bodyAsText()

            parseLiveStation(responseText)
        } catch (e: Exception) {
            BaseResponse(false, System.currentTimeMillis(), error = e.localizedMessage ?: "Network error")
        }
    }

    /**
     * Resilient scraping of live train status from Erail.
     */
    suspend fun getLiveTrainStatus(trainNo: String, date: String): BaseResponse<LiveStatusData> {
        return try {
            // Erail format: dd-MMM-yyyy (e.g. 31-Mar-2026)
            // But let's try their standard path
            val url = "$ERAIL_BASE/train-running-status/$trainNo"
            val responseText = client.get(url) {
                header("User-Agent", ScraperUtils.getRandomUserAgent())
            }.bodyAsText()

            val doc = Jsoup.parse(responseText)
            
            // Look for the current station / delay info often in standard tables or meta tags
            // Erail embeds some data in a table or via script
            // For now, let's attempt to scrape the latest status from their meta description or specific divs
            val statusText = doc.select("div#msg").text().ifEmpty { 
                doc.select("table.stndetailrun").firstOrNull()?.text() ?: ""
            }
            
            // Map it to LiveStatusData structure
            // NOTE: Full scraping requires detailed CSS selection which varies, 
            // but we use structural defaults to avoid crashes.
            val data = LiveStatusData(
                TrainNo = trainNo,
                TrainName = doc.select("h1").firstOrNull()?.text()?.substringAfter(trainNo)?.trim(),
                StationName = statusText.substringBefore(" (").trim(),
                DelayInMinutes = parseDelay(statusText),
                UpdateTime = System.currentTimeMillis().toString()
            )
            
            BaseResponse(true, System.currentTimeMillis(), data = data)
        } catch (e: Exception) {
            BaseResponse(false, System.currentTimeMillis(), error = "Erail live status parse error: ${e.message}")
        }
    }

    private fun parseDelay(text: String): Int {
        val regex = Regex("""(\d+)\s*min""")
        return regex.find(text)?.groupValues?.getOrNull(1)?.toInt() ?: 0
    }

    private fun parseBetweenStations(raw: String): BaseResponse<List<CombinedTrain>> {
        val parts = raw.split("~~~~~~~~").filter { it.isNotBlank() }
        if (parts.isEmpty()) return BaseResponse(false, System.currentTimeMillis(), error = "Empty response")

        val firstPart = parts[0].split("~")
        if (firstPart.size > 5 && firstPart[5].startsWith("No direct trains found")) {
            return BaseResponse(false, System.currentTimeMillis(), error = "No direct trains found")
        }

        val trains = mutableListOf<CombinedTrain>()
        for (part in parts) {
            val subParts = part.split("~^")
            if (subParts.size >= 2) {
                val data = subParts[1].split("~").filter { it.isNotBlank() }
                if (data.size >= 14) {
                    val train = Train(
                        train_no = data.getOrNull(0) ?: "",
                        train_name = data.getOrNull(1) ?: "",
                        from_stn_name = data.getOrNull(6) ?: "",
                        from_stn_code = data.getOrNull(7) ?: "",
                        to_stn_name = data.getOrNull(8) ?: "",
                        to_stn_code = data.getOrNull(9) ?: "",
                        from_time = data.getOrNull(10) ?: "",
                        to_time = data.getOrNull(11) ?: "",
                        travel_time = data.getOrNull(12) ?: "",
                        running_days = data.getOrNull(13) ?: ""
                    )
                    trains.add(CombinedTrain(train))
                }
            }
        }
        return BaseResponse(true, System.currentTimeMillis(), data = trains)
    }

    private fun parseCheckTrain(raw: String): BaseResponse<Train> {
        val parts = raw.split("~~~~~~~~").filter { it.isNotBlank() }
        if (parts.isEmpty()) return BaseResponse(false, System.currentTimeMillis(), error = "Train not found")

        val data1 = parts[0].split("~").filter { it.isNotBlank() }.toMutableList()
        if (data1.size > 1 && data1[1].length > 6) data1.removeAt(0)

        if (data1.size < 15) return BaseResponse(false, System.currentTimeMillis(), error = "Malformed train data")

        val train = Train(
            train_no = data1.getOrNull(1)?.replace("^", "") ?: "",
            train_name = data1.getOrNull(2) ?: "",
            from_stn_name = data1.getOrNull(3) ?: "",
            from_stn_code = data1.getOrNull(4) ?: "",
            to_stn_name = data1.getOrNull(5) ?: "",
            to_stn_code = data1.getOrNull(6) ?: "",
            from_time = data1.getOrNull(11) ?: "",
            to_time = data1.getOrNull(12) ?: "",
            travel_time = data1.getOrNull(13) ?: "",
            running_days = data1.getOrNull(14) ?: ""
        )

        if (parts.size > 1) {
            val data2 = parts[1].split("~").filter { it.isNotBlank() }
            if (data2.size >= 20) {
                return BaseResponse(true, System.currentTimeMillis(), data = train.copy(
                    train_id = data2.getOrNull(12),
                    distance_from_to = data2.getOrNull(18),
                    average_speed = data2.getOrNull(19)
                ))
            }
        }

        return BaseResponse(true, System.currentTimeMillis(), data = train)
    }

    private fun parseRoute(raw: String): BaseResponse<List<RouteStop>> {
        val stationsRaw = raw.split("~^")
        val route = mutableListOf<RouteStop>()
        for (stnRaw in stationsRaw) {
            val data = stnRaw.split("~").filter { it.isNotBlank() }
            if (data.size >= 10) {
                route.add(RouteStop(
                    source_stn_name = data.getOrNull(2) ?: "",
                    source_stn_code = data.getOrNull(1) ?: "",
                    arrive = data.getOrNull(3) ?: "",
                    depart = data.getOrNull(4) ?: "",
                    distance = data.getOrNull(6) ?: "",
                    day = data.getOrNull(7) ?: "",
                    zone = data.getOrNull(9) ?: ""
                ))
            }
        }
        return BaseResponse(true, System.currentTimeMillis(), data = route)
    }

    private fun parseLiveStation(html: String): BaseResponse<List<LiveStationTrain>> {
        return try {
            val doc = Jsoup.parse(html)
            val trains = mutableListOf<LiveStationTrain>()
            val elements = doc.select(".name")
            for (el in elements) {
                val text = el.text()
                val trainNo = text.take(5)
                val trainName = text.drop(5).trim()
                val stnText = el.nextElementSibling()?.text() ?: ""
                val timeText = el.parent()?.nextElementSibling()?.text() ?: ""

                trains.add(LiveStationTrain(
                    train_no = trainNo,
                    train_name = trainName,
                    source_stn_name = stnText.split("→").firstOrNull()?.trim() ?: "",
                    dstn_stn_name = stnText.split("→").getOrNull(1)?.trim() ?: "",
                    time_at = timeText.take(5),
                    detail = timeText.drop(5)
                ))
            }
            BaseResponse(true, System.currentTimeMillis(), data = trains)
        } catch (e: Exception) {
            BaseResponse(false, System.currentTimeMillis(), error = "HTML parse error")
        }
    }
}
