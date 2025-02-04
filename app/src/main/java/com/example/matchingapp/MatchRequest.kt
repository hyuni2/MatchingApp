package com.example.matchingapp

data class MatchRequest(
    val id: Int,              // 요청 ID
    val senderId: String,     // 보낸 사용자 ID
    val receiverId: String,   // 받은 사용자 ID
    val status: String,       // 매칭 현황
    val isMentor: Int,          // 멘토0 /멘티1
    val senderMajor: String,    // 요청보낸 사용자의 전공
    val receiverMajor: String   // 요청받은 사용자의 전공
)
