package com.github.ashuchoudhury.indianrail.models

import kotlinx.serialization.Serializable

@Serializable
data class Train(
    val train_no: String,
    val train_name: String,
    val source_stn_name: String? = null,
    val source_stn_code: String? = null,
    val dstn_stn_name: String? = null,
    val dstn_stn_code: String? = null,
    val from_stn_name: String,
    val from_stn_code: String,
    val to_stn_name: String,
    val to_stn_code: String,
    val from_time: String,
    val to_time: String,
    val travel_time: String,
    val running_days: String,
    val type: String? = null,
    val train_id: String? = null,
    val distance_from_to: String? = null,
    val average_speed: String? = null
)

@Serializable
data class CombinedTrain(
    val train_base: Train
)
