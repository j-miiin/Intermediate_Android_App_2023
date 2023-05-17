package com.example.chapter3_today_notice

import com.google.gson.annotations.SerializedName

data class Message(
    @SerializedName("message")
    val message: String
)