package com.example.matchingapp

data class Profile(
    val userid: String,         // 프로필 ID
    val name: String,       // 이름
    val isMentor: Int,  // 멘토 여부
    val major: String,      // 전공
    val intro: String     // 자기소개
)
