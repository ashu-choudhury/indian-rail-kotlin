package com.github.ashuchoudhury.indianrail.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LiveStatusData(
    @SerialName("TrainNo") val TrainNo: String? = null,
    @SerialName("train_name") val TrainName: String? = null,
    @SerialName("status") val RequiredPosition: String? = null,
    val RunDate: String? = null,
    val StationName: String? = null,
    val StationCode: String? = null,
    val DelayInMinutes: Int = 0,
    val HasArrived: Boolean = false,
    val HasDeparted: Boolean = false,
    val IsTerminated: Boolean = false,
    val DistanceFromSource: String? = null,
    @SerialName("new_last_updated") val UpdateTime: String? = null,
    @SerialName("data") val Stations: List<LiveStationStatus> = emptyList()
)

@Serializable
data class LiveStationStatus(
    @SerialName("station_name") val StationName: String? = null,
    val StationCode: String? = null,
    @SerialName("arrival") val ArrivalTime: String? = null,
    @SerialName("departure") val DepartureTime: String? = null,
    @SerialName("delay") val DelayInMinutes: Int = 0,
    @SerialName("is_current_station") val IsCurrentStation: Boolean = false,
    val ScheduleArrival: String? = null,
    val ScheduleDeparture: String? = null,
    val Distance: String? = null,
    val DayCount: Int = 0
)

@Serializable
data class SeatAvailabilityData(
    val TrainNo: String? = null,
    val TrainName: String? = null,
    val From: String? = null,
    val To: String? = null,
    val Quota: String? = null,
    val Class: String? = null,
    val RemainingSeats: String? = null,
    val Availability: List<SeatAvailabilityDay> = emptyList()
)

@Serializable
data class SeatAvailabilityDay(
    val Date: String? = null,
    val Status: String? = null,
    val Fare: String? = null
)

@Serializable
data class TrainScheduleData(
    val TrainNo: String? = null,
    val TrainName: String? = null,
    val Source: String? = null,
    val Destination: String? = null,
    val Stations: List<ScheduleStation> = emptyList()
)

@Serializable
data class ScheduleStation(
    val StationCode: String? = null,
    val StationName: String? = null,
    val ArrivalTime: String? = null,
    val DepartureTime: String? = null,
    val Distance: String? = null,
    val DayCount: String? = null,
    val StopTimeInMin: String? = null,
    val PlatformRange: String? = null
)
