package com.example.realtimeserivce.ency

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface EncyInterface {
    @GET("v1/search/encyc.json")
    fun getResult(
        @Header("X-Naver-Client-Id") clientId: String,
        @Header("X-Naver-Client-Secret") clientSecret: String,
        @Query("query") query: String,
        @Query("display") display: Int = 1,
    ): Call<EncyResponse>
}