package com.example.realtimeserivce.data

// user들 간 송수신하는 message의 dataclass
data class Message(
    val name: String? = null,
    val message: String? = null,
    val timestamp: String? = null
)
