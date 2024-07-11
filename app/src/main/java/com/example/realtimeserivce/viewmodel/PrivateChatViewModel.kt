package com.example.realtimeserivce.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.realtimeserivce.data.ChatroomId
import com.example.realtimeserivce.data.Message
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import org.checkerframework.checker.units.qual.C
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class PrivateChatViewModel: ViewModel() {
    private val auth = Firebase.auth
    private val database = Firebase.database.reference

    private val _message = MutableLiveData<MutableList<Message>>()
    val message: LiveData<MutableList<Message>> get() = _message

    // message dataclass 객체를 생성해 database에 저장해주는 메서드
    fun sendMessage(message: String, chatroomId: String) {
        val messageValue = Message(
            name = auth.uid!!,
            message = message,
            timestamp = getCurrentTime()
        )

        database.child("chat_rooms").child(chatroomId).child("messages").push().setValue(messageValue)
    }

    // message database에 해당 ref data 변경 사항이 감지되면 snapshot을 통해 livedata로 전달하는 메서드
    fun messageListChange(chatroomId: String) {
        database.child("chat_rooms").child(chatroomId).child("messages")
            .addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val messageList = mutableListOf<Message>()
                    snapshot.children.forEach {
                        val message = it.getValue(Message::class.java)!!
                        messageList.add(message)
                    }
                    _message.postValue(messageList)
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    // 현재 우리니라 시간을 오전/오후로 구분하여 전달해주는 메서드
    private fun getCurrentTime(): String {
        val currentDate = Date()
        val currentZone = TimeZone.getTimeZone("Asia/Seoul")
        val timeFormat = SimpleDateFormat("a hh:mm", Locale.KOREA)
        timeFormat.timeZone = currentZone
        val timeValue = timeFormat.format(currentDate)

        return timeValue
    }
}