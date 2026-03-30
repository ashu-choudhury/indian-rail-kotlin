package com.github.ashuchoudhury.indianrail.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class LiveStatus(
    val success: Boolean,
    val time_stamp: Long,
    val data: JsonElement
)

@Serializable
data class SeatAvailability(
    val success: Boolean,
    val time_stamp: Long,
    val data: JsonElement
)

@Serializable
data class TrainSchedule(
    val success: Boolean,
    val time_stamp: Long,
    val data: JsonElement
)
