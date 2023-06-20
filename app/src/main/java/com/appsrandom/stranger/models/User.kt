package com.appsrandom.stranger.models

import androidx.annotation.Keep

@Keep
data class User (
    val uid: String = "",
    val name: String = "",
    val profilePic: String = "",
    val coins: Long = 0L
)