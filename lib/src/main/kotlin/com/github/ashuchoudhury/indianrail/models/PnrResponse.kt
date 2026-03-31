package com.github.ashuchoudhury.indianrail.models

import kotlinx.serialization.Serializable

@Serializable
data class PnrResponse(
    val success: Boolean,
    val time_stamp: Long,
    val data: PnrStatusData? = null,
    val error: String? = null
)

@Serializable
data class PnrStatusData(
    val Pnr: String? = null,
    val TrainNo: String? = null,
    val TrainName: String? = null,
    val Doj: String? = null,
    val BookingDate: String? = null,
    val Quota: String? = null,
    val DestinationDoj: String? = null,
    val SourceDoj: String? = null,
    val From: String? = null,
    val To: String? = null,
    val ReservationUpto: String? = null,
    val BoardingPoint: String? = null,
    val Class: String? = null,
    val ChartPrepared: Boolean = false,
    val BoardingStationName: String? = null,
    val ReservationUptoName: String? = null,
    val PassengerStatus: List<PassengerStatusData> = emptyList()
)

@Serializable
data class PassengerStatusData(
    val Number: Int = 0,
    val BookingStatus: String? = null,
    val CurrentStatus: String? = null,
    val Prediction: String? = null,
    val PredictionPercentage: String? = null
)
