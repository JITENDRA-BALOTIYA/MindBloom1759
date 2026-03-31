package com.example.mental_health.data.remote

import com.example.mental_health.data.model.GeminiRequest
import com.example.mental_health.data.model.GeminiResponse
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface GeminiApi {

    @POST("v1beta/models/gemini-pro:generateContent")
    suspend fun getResponse(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}
