package com.example.teumteum.data.remote.friend.search

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import com.example.teumteum.data.remote.friend.dto.FriendSearchResponse

interface FriendSearchRetrofitInterface {
    @GET("/api/users/search")
    fun searchUserByNickname(@Query("keyword") nickname: String): Call<FriendSearchResponse>

}
