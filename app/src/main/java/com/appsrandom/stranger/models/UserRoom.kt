package com.appsrandom.stranger.models

import androidx.annotation.Keep

@Keep
data class UserRoom (
    val incoming: String = "",
    val createdBy: String = "",
    val isAvailable: Boolean = true,
    val status: String = "0"
)