package com.example.matchingapp

data class MatchRequest(
    val id: Int,              // 요청 ID
    val senderId: String,     // 보낸 사용자 ID
    val receiverId: String,   // 받은 사용자 ID
    val status: String,
    val isMentor: Boolean // 멘토/멘티 정보 추가// 요청 상태
)
