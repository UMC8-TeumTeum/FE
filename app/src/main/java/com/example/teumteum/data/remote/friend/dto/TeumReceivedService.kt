package com.example.teumteum.data.remote.friend.dto

import android.util.Log
import com.example.teumteum.data.remote.friend.teum.TeumReceivedRetrofitInterface
import com.example.teumteum.ui.friend.view.TeumReceivedView
import com.example.teumteum.utils.getRetrofitWithToken
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TeumReceivedService {

    private lateinit var teumReceivedView: TeumReceivedView

    fun setTeumReceivedView(view: TeumReceivedView) {
        this.teumReceivedView = view
    }

    fun getReceivedTeumRequests() {
        val api = getRetrofitWithToken().create(TeumReceivedRetrofitInterface::class.java)
        api.getReceivedTeumRequests().enqueue(object : Callback<TeumReceivedResponse> {
            override fun onResponse(
                call: Call<TeumReceivedResponse>,
                response: Response<TeumReceivedResponse>
            ) {
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.isSuccess == true) {
                        teumReceivedView.onTeumReceivedSuccess(body.result?.content ?: emptyList())
                    } else {
                        teumReceivedView.onTeumReceivedFailure(
                            body?.code ?: "UNKNOWN",
                            body?.message ?: "응답 파싱 실패"
                        )
                    }
                } else {
                    teumReceivedView.onTeumReceivedFailure(
                        "HTTP_${response.code()}",
                        response.errorBody()?.string() ?: "서버 응답 오류"
                    )
                }
            }

            override fun onFailure(call: Call<TeumReceivedResponse>, t: Throwable) {
                teumReceivedView.onTeumReceivedFailure(
                    "NETWORK_ERROR",
                    t.localizedMessage ?: "알 수 없는 네트워크 오류"
                )
            }
        })
    }
}
