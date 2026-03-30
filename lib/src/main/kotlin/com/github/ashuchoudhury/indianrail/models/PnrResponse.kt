package com.github.ashuchoudhury.indianrail.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class PnrResponse(
    val success: Boolean,
    val time_stamp: Long,
    val data: JsonElement
)
