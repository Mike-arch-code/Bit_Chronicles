package com.bit_chronicles.model


data class ChatMessage(
    val sender: String,
    val message: String,
    val timestamp: Long
)
