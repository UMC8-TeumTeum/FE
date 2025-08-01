package com.example.teumteum.data.remote.activity

import android.util.Log
import com.example.teumteum.data.remote.activity.dto.ActivityAiRequest
import com.example.teumteum.data.remote.activity.dto.ActivityAiResponse
import com.example.teumteum.data.remote.activity.dto.ActivityWishRequest
import com.example.teumteum.data.remote.activity.dto.ActivityWishResponse
import com.example.teumteum.ui.activity.view.ActivityAiView
import com.example.teumteum.ui.activity.view.ActivityWishView
import com.example.teumteum.utils.getRetrofitWithToken
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ActivityService {
    private lateinit var activityWishView: ActivityWishView
    private lateinit var activityAiView: ActivityAiView

    fun setActivityWishView(activityWishView: ActivityWishView) {
        this.activityWishView = activityWishView
    }

    fun setActivityAiView(activityAiView: ActivityAiView) {
        this.activityAiView = activityAiView
    }

    companion object {
        private val gson = Gson()
    }

    // 채움활동 입력하여 위시리스트 불러오기
    fun activityWish(request: ActivityWishRequest) {

        val activityService = getRetrofitWithToken().create(ActivityRetrofitInterface::class.java)

        activityService.activityWish(request).enqueue(object : Callback<ActivityWishResponse> {
            override fun onResponse(
                call: Call<ActivityWishResponse>,
                response: Response<ActivityWishResponse>
            ) {
                Log.d("ACTIVITY/SUCCESS", response.toString())

                if (response.isSuccessful) {
                    val activityWishResponse = response.body()

                    if (activityWishResponse != null && activityWishResponse.code == "ACTIVITY2001") {
                        val wishes = activityWishResponse.wishes ?: emptyList()
                        activityWishView.onGetActivityWishSuccess(activityWishResponse.code, wishes)
                    } else {
                        activityWishView.onGetActivityWishFailure(activityWishResponse?.code ?: "UNKNOWN")
                    }
                } else {
                    // 실패 응답 처리
                    val errorMsg = response.errorBody()?.string()
                    Log.d("ACTIVITY/ERROR_BODY", errorMsg ?: "에러 메시지 없음")

                    // gson으로 실패 응답 파싱
                    try {
                        if (!errorMsg.isNullOrEmpty()) {
                            val errorResponse = gson.fromJson(errorMsg, ActivityWishResponse::class.java)
                            activityWishView.onGetActivityWishFailure(errorResponse.code)
                        } else {
                            activityWishView.onGetActivityWishFailure("EMPTY_ERROR_BODY")
                        }
                    } catch (e: Exception) { // JSON 파싱 실패 시
                        Log.e("ACTIVITY/PARSE_ERROR", "JSON 파싱 실패: ${e.localizedMessage}")
                        activityWishView.onGetActivityWishFailure("PARSE_ERROR")
                    }
                }
            }

            override fun onFailure(call: Call<ActivityWishResponse>, t: Throwable) {
                Log.d("ACTIVITY/FAILURE", t.message.toString())
                activityWishView.onGetActivityWishFailure("NETWORK_ERROR")
            }
        })
    }

    // 채움활동 입력하여 ai 추천 컨텐츠 불러오기
    fun activityAi(request: ActivityAiRequest) {

        val activityService = getRetrofitWithToken().create(ActivityRetrofitInterface::class.java)

        activityService.activityAi(request).enqueue(object : Callback<ActivityAiResponse> {
            override fun onResponse(
                call: Call<ActivityAiResponse>,
                response: Response<ActivityAiResponse>
            ) {
                Log.d("ACTIVITY/SUCCESS", response.toString())

                if (response.isSuccessful) {
                    val activityAiResponse = response.body()

                    if (activityAiResponse != null && activityAiResponse.code == "ACTIVITY2001") {
                        val aiContents = activityAiResponse.aiContents ?: emptyList()
                        activityAiView.onGetActivityAiSuccess(activityAiResponse.code, aiContents)
                    } else {
                        activityAiView.onGetActivityAiFailure(activityAiResponse?.code ?: "UNKNOWN")
                    }
                } else {
                    // 실패 응답 처리
                    val errorMsg = response.errorBody()?.string()
                    Log.d("ACTIVITY/ERROR_BODY", errorMsg ?: "에러 메시지 없음")

                    // gson으로 실패 응답 파싱
                    try {
                        if (!errorMsg.isNullOrEmpty()) {
                            val errorResponse = gson.fromJson(errorMsg, ActivityAiResponse::class.java)
                            activityAiView.onGetActivityAiFailure(errorResponse.code)
                        } else {
                            activityAiView.onGetActivityAiFailure("EMPTY_ERROR_BODY")
                        }
                    } catch (e: Exception) { // JSON 파싱 실패 시
                        Log.e("ACTIVITY/PARSE_ERROR", "JSON 파싱 실패: ${e.localizedMessage}")
                        activityAiView.onGetActivityAiFailure("PARSE_ERROR")
                    }
                }
            }

            override fun onFailure(call: Call<ActivityAiResponse>, t: Throwable) {
                Log.d("ACTIVITY/FAILURE", t.message.toString())
                activityAiView.onGetActivityAiFailure("NETWORK_ERROR")
            }
        })
    }
}