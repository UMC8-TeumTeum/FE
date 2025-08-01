package com.example.teumteum.data.remote.friend.dto

import com.example.teumteum.data.remote.friend.search.FriendSearchRetrofitInterface
import com.example.teumteum.ui.friend.view.FriendSearchView
import com.example.teumteum.utils.getRetrofitWithToken
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FriendSearchService {

    private lateinit var friendSearchView: FriendSearchView

    fun setFriendSearchView(friendSearchView: FriendSearchView) {
        this.friendSearchView = friendSearchView
    }

    fun searchUser(nickname: String) {
        val friendSearchApi = getRetrofitWithToken().create(FriendSearchRetrofitInterface::class.java)
        val call = friendSearchApi.searchUserByNickname(nickname)

        call.enqueue(object : Callback<FriendSearchResponse> {
            override fun onResponse(
                call: Call<FriendSearchResponse>,
                response: Response<FriendSearchResponse>
            ) {
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.isSuccess == true && body.result != null) {
                        friendSearchView.onSearchSuccess(body.result)
                    } else {
                        friendSearchView.onSearchFailure(
                            body?.code ?: "UNKNOWN",
                            body?.message ?: "에러 메시지 없음"
                        )
                    }
                } else {
                    friendSearchView.onSearchFailure(
                        "HTTP_${response.code()}",
                        response.errorBody()?.string() ?: "서버 응답 오류"
                    )
                }
            }

            override fun onFailure(call: Call<FriendSearchResponse>, t: Throwable) {
                friendSearchView.onSearchFailure(
                    "NETWORK_ERROR",
                    t.localizedMessage ?: "알 수 없는 네트워크 오류"
                )
            }
        })
    }
}
