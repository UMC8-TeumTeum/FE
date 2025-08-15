package com.example.teumteum.data.remote.alarm

import android.util.Log
import com.example.teumteum.data.remote.alarm.dto.GetAlarmListResponse
import com.example.teumteum.ui.alarm.view.AlarmListView
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

class AlarmService @Inject constructor(
    private val alarmApi: AlarmRetrofitInterface
){
    private lateinit var alarmListView: AlarmListView

    fun setAlarmListGetView(alarmListView: AlarmListView) {
        this.alarmListView = alarmListView
    }

    companion object {
        private val gson = Gson()
    }

    // 알림 리스트 조회
    fun getAlarmList(duration: String, page: Int) {

        alarmApi.getAlarmList(duration, page).enqueue(object : Callback<GetAlarmListResponse> {
            override fun onResponse(
                call: Call<GetAlarmListResponse>,
                response: Response<GetAlarmListResponse>
            ) {
                Log.d("ALARM/SUCCESS", response.toString())

                if (response.isSuccessful) {
                    val getAlarmListResponse = response.body()

                    if (getAlarmListResponse != null && getAlarmListResponse.code == "COMMON200") {
                        val alarmList = response.body()?.result?.alarmList ?: emptyList()
//                        alarmListView.onGetAlarmListSuccess(alarmList)
                    } else {
                        alarmListView.onGetAlarmListFailure(
                            getAlarmListResponse?.code ?: "UNKNOWN",
                            getAlarmListResponse?.message ?: "조회 실패"
                        )
                    }
                } else {
                    // 실패 응답 처리
                    val errorMsg = response.errorBody()?.string()
                    Log.d("ALARM/ERROR_BODY", errorMsg ?: "에러 메시지 없음")

                    try {
                        if (!errorMsg.isNullOrEmpty()) {
                            val errorResponse = gson.fromJson(errorMsg, GetAlarmListResponse::class.java)
                            val errorMessage = errorResponse.result?.toString()
                            alarmListView.onGetAlarmListFailure(errorResponse.code, errorMessage)
                        } else {
                            alarmListView.onGetAlarmListFailure("EMPTY_ERROR_BODY", "응답 본문이 없습니다.")
                        }
                    } catch (e: Exception) {
                        Log.e("ALARM/PARSE_ERROR", "JSON 파싱 실패: ${e.localizedMessage}")
                        alarmListView.onGetAlarmListFailure("PARSE_ERROR", "응답 파싱에 실패했습니다.")
                    }
                }
            }

            override fun onFailure(call: Call<GetAlarmListResponse>, t: Throwable) {
                Log.d("ALARM/FAILURE", t.message.toString())
                alarmListView.onGetAlarmListFailure("NETWORK_ERROR")
            }
        })
    }
}