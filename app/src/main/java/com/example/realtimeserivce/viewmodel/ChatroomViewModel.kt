package com.example.realtimeserivce.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.realtimeserivce.data.ChatroomId
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database

class ChatroomViewModel: ViewModel() {
    private val auth = Firebase.auth
    private val database = Firebase.database

    init {
        observeChatroom()
    }
    private val _roomId = MutableLiveData<MutableList<ChatroomId>>()
    val roomId: LiveData<MutableList<ChatroomId>> get() = _roomId

    // 현재 로그인되어있는 사용자의 uid가 포함된 채팅방이 개설되면 이에대한 변경을 감지해 livedata로 전달하는 메서드
    private fun observeChatroom() {
        database.getReference("chat_rooms").addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val chatroom = mutableListOf<ChatroomId>()
                snapshot.children.forEach {
                    if (it.key?.contains(auth.uid!!)!!) {
                        val chatroomId = it.key!!
                        val myRoomId = chatroomId.replace(auth.uid!!, "")
                        if (myRoomId != "") {
                            chatroom.add(ChatroomId(chatroomId, myRoomId))
                        } else {
                            chatroom.add(ChatroomId(chatroomId, auth.uid!!))
                        }
                    }
                }
                _roomId.postValue(chatroom)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}