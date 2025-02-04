package com.example.matchingapp

data class ChatMessage(
    val senderId: String,
    val receiverId: String,
    val message: String
)