package com.example.realtimeserivce.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.realtimeserivce.data.ChatroomId
import com.example.realtimeserivce.data.CurrentStatus
import com.example.realtimeserivce.data.MatchResult
import com.example.realtimeserivce.data.MatchWord
import com.example.realtimeserivce.data.Message
import com.example.realtimeserivce.repository.MatchModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class MatchViewModel: ViewModel() {
    private val model = MatchModel()
    private val auth = Firebase.auth
    private val database = Firebase.database.reference

    private val _word = MutableLiveData<MutableList<MatchWord>>()
    val word: LiveData<MutableList<MatchWord>> get() = _word

    private val _playerStatus = MutableLiveData<MutableList<CurrentStatus>>()
    val playerStatus: LiveData<MutableList<CurrentStatus>> get() = _playerStatus

    init {
        getPlayerStatus()
    }

    // todo - 승리, 패배를 현재 날짜를 기준으로 기록해주는 메서드
    fun writeResults(result: MatchResult) {
        database.child("match_records").child(getCurrentDay()).push().setValue(result)
    }
    private fun getCurrentDay(): String {
        val currentDate = Date()
        val currentZone = TimeZone.getTimeZone("Asia/Seoul")
        val dayFormat = SimpleDateFormat("yyyy년 MM월 dd일", Locale.KOREA)

        dayFormat.timeZone = currentZone
        val dayValue = dayFormat.format(currentDate)

        return dayValue
    }

    // 단어 Api 필터를 통과한 Word를 database로 전송하는 메서드
    fun sendFilterWord(word: String, matchRoomId: String) {
        val matchWord = MatchWord(
            sender = auth.uid!!,
            value = word
        )

        database.child("match_rooms").child(matchRoomId).child("match_words").push().setValue(matchWord)
    }

    // match_words database에 해당하는 data 변경 사항이 감지되면 snapshot을 통해 livedata로 전달하는 메서드
    fun wordListChange(matchRoomId: String) {
        database.child("match_rooms").child(matchRoomId).child("match_words")
            .addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val wordList = mutableListOf<MatchWord>()
                    snapshot.children.forEach {
                        val word = it.getValue(MatchWord::class.java)!!
                        wordList.add(word)
                    }
                    _word.postValue(wordList)
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

    // 단어가 api를 거쳐 필터링 되어 실패 성공 여부에 따라 비동기로 값을 반환
    suspend fun checkWord(word: String): String {
        return withContext(Dispatchers.IO){
            model.checkWord(word)
        }
    }
}