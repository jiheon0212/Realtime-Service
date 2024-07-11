package com.example.realtimeserivce.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.realtimeserivce.data.CurrentStatus
import com.example.realtimeserivce.repository.FirebaseAuthModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.google.firebase.database.getValue
import kotlinx.coroutines.tasks.await
import java.util.Objects

class FirebaseAuthViewModel: ViewModel() {
    private val auth = Firebase.auth
    private val database = Firebase.database

    init {
        getAllUserStatus()
    }

    private val _user = MutableLiveData<FirebaseUser>()
    val user: LiveData<FirebaseUser> get() = _user

    private val _currentStatus = MutableLiveData<MutableList<CurrentStatus>>()
    val currentStatus: LiveData<MutableList<CurrentStatus>> get() = _currentStatus

    // view에서 로그인 버튼 클릭 시, auth 익명 로그인을 실행하는 메서드
    fun signInAnonymously() {
        auth.signInAnonymously().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = task.result.user!!
                _user.value = user
                setOnlineStatus(true)
            } else {
                Log.d("anonymously sign-in method has problems", "${task.exception}")
            }
        }
    }

    // 전체 user들의 접속 상태를 가져오는 메서드
    fun getAllUserStatus() {
        val allUserRef = database.reference.child("users")
        val userListListener = object: ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val statusList = mutableListOf<CurrentStatus>()
                dataSnapshot.children.forEach { snapshot ->
                    val data = snapshot.getValue(CurrentStatus::class.java)!!
                    statusList.add(data)
                }
                _currentStatus.postValue(statusList)
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        }

        allUserRef.addValueEventListener(userListListener)
    }

    // user의 대기열 접속 상태를 변경하는 메서드
    fun setOnlineStatus(isOnline: Boolean) {
        if (auth.uid != null) {
            val userRef = database.getReference("users/${auth.uid}")
            userRef.setValue(if (isOnline) CurrentStatus(auth.uid!!, "online") else CurrentStatus(auth.uid!!, "offline"))
        } else {
            return
        }
    }

    fun signOut() {
        setOnlineStatus(false)
        auth.signOut()
    }
}