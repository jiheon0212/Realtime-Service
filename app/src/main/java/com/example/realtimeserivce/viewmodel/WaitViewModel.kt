package com.example.realtimeserivce.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database

class WaitViewModel: ViewModel() {
    private val database = Firebase.database.reference
    private val auth = Firebase.auth

    private val _matchRoomIds = MutableLiveData<MutableList<String>>()
    val matchRoomIds: LiveData<MutableList<String>> get() = _matchRoomIds
    init {
        observeMatchRoom()
    }

    // 현재 로그인되어있는 사용자의 uid가 포함된 매치Id가 생성되면 이에대한 변경을 감지해 livedata로 전달하는 메서드
    private fun observeMatchRoom() {
        database.child("match_rooms").addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val matchIds = mutableListOf<String>()
                snapshot.children.forEach {
                    if (it.key?.contains(auth.uid!!)!!) {
                        val matchId = it.key!!
                        matchIds.add(matchId)
                    }
                }
                _matchRoomIds.postValue(matchIds)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}