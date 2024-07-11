package com.example.realtimeserivce.repository

import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.database

class FirebaseAuthModel {
    private val auth = Firebase.auth
    private val database = Firebase.database
}