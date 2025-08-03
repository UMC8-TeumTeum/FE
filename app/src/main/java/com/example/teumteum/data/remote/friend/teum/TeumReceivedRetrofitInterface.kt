package com.example.teumteum.data.remote.friend.teum

import com.example.teumteum.data.remote.friend.dto.TeumReceivedResponse
import retrofit2.Call
import retrofit2.http.GET

interface TeumReceivedRetrofitInterface {

    @GET("/api/teums/request/received")
    fun getReceivedTeumRequests(): Call<TeumReceivedResponse>
}
