package com.example.realtimeserivce.data

// user의 현재 접속상태를 표시하는 dataclass
data class CurrentStatus(
    val user: String? = null,
    val status: String? = null
)
