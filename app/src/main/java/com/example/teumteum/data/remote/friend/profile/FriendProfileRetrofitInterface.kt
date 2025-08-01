package com.example.teumteum.data.remote.friend.profile

import com.example.teumteum.data.remote.friend.dto.FriendProfileResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface FriendProfileRetrofitInterface {

    @GET("/api/friends/{userId}/profile")
    fun getFriendProfile(@Path("userId") userId: Int): Call<FriendProfileResponse>

}