package com.example.teumteum.data.remote.friend.teum

import com.example.teumteum.data.remote.friend.dto.TeumRequest
import com.example.teumteum.data.remote.friend.dto.TeumResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST


// 추후에 필요 틈 요청하기 api 연결 시
interface TeumRetrofitInterface {
    @POST("/api/teums/request")
    fun sendTeumRequest(@Body request: TeumRequest): Call<TeumResponse>
}
