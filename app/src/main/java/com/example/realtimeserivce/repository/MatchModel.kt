package com.example.realtimeserivce.repository

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.util.Log
import com.example.realtimeserivce.ency.EncyResponse
import com.example.realtimeserivce.ency.EncyService
import com.example.realtimeserivce.ency.NaverInformation
import retrofit2.Call
import retrofit2.Response

class MatchModel {
    // 단어가 존재하는지 확인하는 메서드 model로 이전
    fun checkWord(word: String) {
        val ency = EncyService.encyInterface.getResult(
            clientId = NaverInformation.CLIENT_ID,
            clientSecret = NaverInformation.CLIENT_SECRET,
            query = word
        )
        ency.enqueue(object: retrofit2.Callback<EncyResponse> {
            @SuppressLint("SetTextI18n")
            override fun onResponse(p0: Call<EncyResponse>, p1: Response<EncyResponse>) {
                if (p1.isSuccessful) {
                    if (p1.body()?.items != null) {
                        p1.body()?.items?.forEach { itemSame ->
                            // html태그를 전부 제거하고 title, description을 받아온다.
                            val result = itemSame.trimResults
                            // 사용자가 입력한 단어 이외의 title에 넘어온 다른 데이터를 제거하며 일치하는 문구가 없을 경우에는 빈칸으로 변경한다
                            val isMatch = if (result.first.contains(word)) word else ""
                            if (isMatch != "") {
                                // todo - result를 await을 통해 결과를 기다려 받을 수 있도록 suspend로 checkword 바꿔주기
                                val result = "$isMatch:\n${result.second}"

                            } else {
                                // todo - "failed" 결과 값을 보내야되는 부분
                            }
                        }
                    }
                }
            }
            override fun onFailure(p0: Call<EncyResponse>, p1: Throwable) {
               Log.e(TAG, "$p1")
            }
        })
    }
}