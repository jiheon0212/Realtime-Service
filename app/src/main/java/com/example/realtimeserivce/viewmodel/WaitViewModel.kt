package com.example.realtimeserivce.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.realtimeserivce.data.CurrentStatus
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

    private val _playerStatus = MutableLiveData<MutableList<CurrentStatus>>()
    val playerStatus: LiveData<MutableList<CurrentStatus>> get() = _playerStatus
    init {
        observeMatchRoom()
        getPlayerStatus()
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

    // user의 대기열 접속 상태를 변경하는 메서드
    fun setReadyStatus(isOnline: Boolean) {
        if (auth.uid != null) {
            val userRef = database.child("player_list/${auth.uid}")
            userRef.setValue(if (isOnline) CurrentStatus(auth.uid!!, "online") else CurrentStatus(auth.uid!!, "offline"))
        } else {
            return
        }
    }

    // 전체 player들의 대기 상태를 가져오는 메서드
    private fun getPlayerStatus() {
        database.child("player_list").addValueEventListener(object: ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val statusList = mutableListOf<CurrentStatus>()
                dataSnapshot.children.forEach { snapshot ->
                    val data = snapshot.getValue(CurrentStatus::class.java)!!
                    statusList.add(data)
                }
                _playerStatus.postValue(statusList)
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }
}