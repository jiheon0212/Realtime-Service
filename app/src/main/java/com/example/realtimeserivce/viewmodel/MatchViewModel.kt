package com.example.realtimeserivce.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.realtimeserivce.data.MatchResult
import com.example.realtimeserivce.data.MatchWord
import com.example.realtimeserivce.repository.MatchModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import kotlinx.coroutines.Dispatchers
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

    private val _isMatchExists = MutableLiveData<Boolean>()
    val isMatchExists: LiveData<Boolean> get() = _isMatchExists

    // 승리, 패배를 현재 날짜를 기준으로 기록해주는 메서드
    fun writeResults(loser: String, matchId: String) {
        val winner = matchId.replace(loser, "")
        val result = MatchResult(winner, loser)

        database.child("match_records").child(getCurrentDay()).push().setValue(result)
    }
    
    // 현재 날짜를 반환해주는 메서드
    private fun getCurrentDay(): String {
        val currentDate = Date()
        val currentZone = TimeZone.getTimeZone("Asia/Seoul")
        val dayFormat = SimpleDateFormat("yyyy년 MM월 dd일", Locale.KOREA)

        dayFormat.timeZone = currentZone
        val dayValue = dayFormat.format(currentDate)

        return dayValue
    }

    // 현재 match를 db에서 제거하고 플레이어들을 모두 이탈시키는 메서드
    fun destroyMatch(matchId: String) {
        database.child("match_rooms").child(matchId).removeValue()
    }
    // 현재 match가 끝났는지 db 값을 감지하는 메서드
    fun observeMatchDestroy(matchId: String) {
        database.child("match_rooms").addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var searchMatch = false
                snapshot.children.forEach { dataSnapshot ->
                    val value = dataSnapshot.key!!
                    if (value == matchId) {
                        searchMatch = true
                        _isMatchExists.postValue(true)
                        return
                    }
                }
                if (!searchMatch) {
                    _isMatchExists.postValue(false)
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
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

    // 단어가 api를 거쳐 필터링 되어 실패 성공 여부에 따라 비동기로 값을 반환
    suspend fun checkWord(word: String): String {
        return withContext(Dispatchers.IO){
            model.checkWord(word)
        }
    }

    // match를 생성하는 메서드
    fun setMatch(matchId: String) {
        database.child("match_rooms").child(matchId).setValue("started")
    }
}