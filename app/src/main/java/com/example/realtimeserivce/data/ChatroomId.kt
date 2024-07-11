package com.example.realtimeserivce.data

// 채팅방의 id를 저장하고 visibleName을 통해 채팅방 목록에서 보여질 이름을 설정하는 dataclass
data class ChatroomId(
    val roomId: String,
    val visibleName: String,
)
