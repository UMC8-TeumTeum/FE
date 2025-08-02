package com.example.teumteum.data.remote.activity

import android.util.Log
import com.example.teumteum.data.remote.activity.dto.ActivityAiRequest
import com.example.teumteum.data.remote.activity.dto.ActivityAiResponse
import com.example.teumteum.data.remote.activity.dto.ActivityWishRequest
import com.example.teumteum.data.remote.activity.dto.ActivityWishResponse
import com.example.teumteum.data.remote.activity.dto.FillAiRequest
import com.example.teumteum.data.remote.activity.dto.FillAiResponse
import com.example.teumteum.ui.activity.view.ActivityAiView
import com.example.teumteum.ui.activity.view.ActivityWishView
import com.example.teumteum.ui.activity.view.FillAiView
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

class ActivityService @Inject constructor(
    private val activityApi: ActivityRetrofitInterface
){
    private lateinit var activityWishView: ActivityWishView
    private lateinit var activityAiView: ActivityAiView
    private lateinit var aiFillView: FillAiView

    fun setActivityWishView(activityWishView: ActivityWishView) {
        this.activityWishView = activityWishView
    }

    fun setActivityAiView(activityAiView: ActivityAiView) {
        this.activityAiView = activityAiView
    }

    fun setAiFillView(aiFillView: FillAiView) {
        this.aiFillView = aiFillView
    }

    companion object {
        private val gson = Gson()
    }

    // 채움활동 입력하여 위시리스트 불러오기
    fun activityWish(request: ActivityWishRequest) {

        activityApi.activityWish(request).enqueue(object : Callback<ActivityWishResponse> {
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

        activityApi.activityAi(request).enqueue(object : Callback<ActivityAiResponse> {
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

    // ai 컨텐츠 빈틈 채우기(투두 등록)
    fun fillAi(aiContentId: Long, request: FillAiRequest) {

        activityApi.fillAi(aiContentId, request).enqueue(object : Callback<FillAiResponse> {
            override fun onResponse(
                call: Call<FillAiResponse>,
                response: Response<FillAiResponse>
            ) {
                Log.d("FILL/SUCCESS", response.toString())

                if (response.isSuccessful) {
                    val fillResponse = response.body()

                    if (fillResponse != null && fillResponse.code == "HOME20011") {
                        aiFillView.onFillAiSuccess(fillResponse.code)
                    } else {
                        aiFillView.onFillAiFailure(fillResponse?.code ?: "UNKNOWN")
                    }
                } else {
                    // 실패 응답 처리
                    val errorMsg = response.errorBody()?.string()
                    Log.d("FILL/ERROR_BODY", errorMsg ?: "에러 메시지 없음")

                    // gson으로 실패 응답 파싱
                    try {
                        if (!errorMsg.isNullOrEmpty()) {
                            val errorResponse = gson.fromJson(errorMsg, FillAiResponse::class.java)
                            aiFillView.onFillAiFailure(errorResponse.code)
                        } else {
                            aiFillView.onFillAiFailure("EMPTY_ERROR_BODY")
                        }
                    } catch (e: Exception) { // JSON 파싱 실패 시
                        Log.e("FILL/PARSE_ERROR", "JSON 파싱 실패: ${e.localizedMessage}")
                        aiFillView.onFillAiFailure("PARSE_ERROR")
                    }
                }
            }

            override fun onFailure(call: Call<FillAiResponse>, t: Throwable) {
                Log.d("FILL/FAILURE", t.message.toString())
                aiFillView.onFillAiFailure("NETWORK_ERROR")
            }
        })
    }
}