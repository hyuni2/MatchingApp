package com.example.matchingapp

data class MatchRequest(
    val id: Int,              // 요청 ID
    val senderId: String,     // 보낸 사용자 ID
    val receiverId: String,   // 받은 사용자 ID
    val status: String,
    val isMentor: Int,
    val senderMajor: String,
    val receiverMajor: String
)
