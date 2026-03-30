package com.github.ashuchoudhury.indianrail.models

import kotlinx.serialization.Serializable

@Serializable
data class BaseResponse<T>(
    val success: Boolean,
    val time_stamp: Long,
    val data: T
)
