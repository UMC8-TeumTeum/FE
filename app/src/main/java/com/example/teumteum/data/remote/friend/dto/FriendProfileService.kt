package com.example.teumteum.data.remote.friend.dto

import com.example.teumteum.data.remote.friend.profile.FriendProfileRetrofitInterface
import com.example.teumteum.ui.friend.view.FriendProfileView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

class FriendProfileService @Inject constructor(
    private val friendProfileApi: FriendProfileRetrofitInterface
){

    private lateinit var friendProfileView: FriendProfileView

    fun setFriendProfileView(friendProfileView: FriendProfileView) {
        this.friendProfileView = friendProfileView
    }

    fun getFriendProfile(userId: Int) {

        friendProfileApi.getFriendProfile(userId).enqueue(object : Callback<FriendProfileResponse> {
            override fun onResponse(
                call: Call<FriendProfileResponse>,
                response: Response<FriendProfileResponse>
            ) {
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.isSuccess == true && body.result != null) {
                        friendProfileView.onFriendProfileSuccess(body.result)
                    } else {
                        friendProfileView.onFriendProfileFailure(
                            body?.code ?: "UNKNOWN",
                            body?.message ?: "에러 메시지 없음"
                        )
                    }
                } else {
                    friendProfileView.onFriendProfileFailure(
                        "HTTP_${response.code()}",
                        response.errorBody()?.string() ?: "서버 응답 오류"
                    )
                }
            }

            override fun onFailure(call: Call<FriendProfileResponse>, t: Throwable) {
                friendProfileView.onFriendProfileFailure(
                    "NETWORK_ERROR",
                    t.localizedMessage ?: "알 수 없는 네트워크 오류"
                )
            }
        })
    }
}
