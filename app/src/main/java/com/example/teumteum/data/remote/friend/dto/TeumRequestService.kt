package com.example.teumteum.data.remote.friend.dto

import com.example.teumteum.data.remote.friend.teum.TeumRetrofitInterface
import com.example.teumteum.ui.friend.view.TeumRequestView
import com.example.teumteum.utils.getRetrofitWithToken
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

//추후에 필요 틈 요청하기 api 연결 시
class TeumRequestService {

    private lateinit var teumRequestView: TeumRequestView

    fun setTeumRequestView(teumRequestView: TeumRequestView) {
        this.teumRequestView = teumRequestView
    }

    fun sendTeumRequest(request: TeumRequest) {
        val api = getRetrofitWithToken().create(TeumRetrofitInterface::class.java)
        val call = api.sendTeumRequest(request)

        call.enqueue(object : Callback<TeumResponse> {
            override fun onResponse(call: Call<TeumResponse>, response: Response<TeumResponse>) {
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.isSuccess == true && body.result != null) {
                        teumRequestView.onTeumRequestSuccess(body.result.id) //  수정: userId → id
                    } else {
                        teumRequestView.onTeumRequestFailure(
                            body?.code ?: "UNKNOWN",
                            body?.message ?: "에러 메시지 없음"
                        )
                    }
                } else {
                    teumRequestView.onTeumRequestFailure(
                        "HTTP_${response.code()}",
                        response.errorBody()?.string() ?: "서버 응답 오류"
                    )
                }
            }

            override fun onFailure(call: Call<TeumResponse>, t: Throwable) {
                teumRequestView.onTeumRequestFailure(
                    "NETWORK_ERROR",
                    t.localizedMessage ?: "알 수 없는 네트워크 오류"
                )
            }
        })
    }
}
