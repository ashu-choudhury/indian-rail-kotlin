package com.github.ashuchoudhury.indianrail.models

import kotlinx.serialization.Serializable

@Serializable
data class RouteStop(
    val source_stn_name: String,
    val source_stn_code: String,
    val arrive: String,
    val depart: String,
    val distance: String,
    val day: String,
    val zone: String
)

@Serializable
data class LiveStationTrain(
    val train_no: String,
    val train_name: String,
    val source_stn_name: String,
    val dstn_stn_name: String,
    val time_at: String,
    val detail: String
)
