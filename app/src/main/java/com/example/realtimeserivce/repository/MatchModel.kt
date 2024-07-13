package com.example.realtimeserivce.repository

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.util.Log
import com.example.realtimeserivce.ency.EncyResponse
import com.example.realtimeserivce.ency.EncyService
import com.example.realtimeserivce.ency.NaverInformation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Response

class MatchModel {
    // 단어가 존재하는지 확인하는 메서드 model로 이전
    // coroutine lib를 활용해서 retrofit 작업이 끝난 후에 반환 값을 return 받을 수 있도록 변경
    suspend fun checkWord(word: String): String {
        return withContext(Dispatchers.IO) {
            val ency = EncyService.encyInterface.getResult(
                clientId = NaverInformation.CLIENT_ID,
                clientSecret = NaverInformation.CLIENT_SECRET,
                query = word
            )
            val result = ency.items[0].trimResults
            val isMatch = if (result.first.contains(word)) word else ""
            if (isMatch.isNotEmpty()) {
                return@withContext "$isMatch: ${result.second}"
            } else {
                return@withContext "failed"
            }
        }
    }
}