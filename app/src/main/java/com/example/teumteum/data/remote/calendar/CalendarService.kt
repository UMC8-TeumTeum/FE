package com.example.teumteum.data.remote.calendar

import android.util.Log
import com.example.teumteum.data.remote.calendar.dto.CalendarResponse
import com.example.teumteum.ui.calendar.view.CalendarView
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

class CalendarService @Inject constructor(
    private val calendarApi: CalendarRetrofitInterface
){
    private lateinit var calendarView: CalendarView

    fun setCalendarView(calendarView: CalendarView) {
        this.calendarView = calendarView
    }

    companion object {
        private val gson = Gson()
    }

    fun getCalendarData(startDate: String, endDate: String) {

        calendarApi.getCalendarData(startDate, endDate).enqueue(object : Callback<CalendarResponse> {
            override fun onResponse(
                call: Call<CalendarResponse>,
                response: Response<CalendarResponse>
            ) {
                Log.d("CALENDAR/SUCCESS", response.toString())

                if (response.isSuccessful) {
                    val getCalendarResponse = response.body()

                    if (getCalendarResponse != null && getCalendarResponse.code == "HOOM20014") {
                        val calendar = getCalendarResponse.result ?: emptyList()
                        calendarView.onGetCalendarSuccess(getCalendarResponse.code, calendar)
                    } else {
                        calendarView.onGetCalendarFailure(
                            getCalendarResponse?.code ?: "UNKNOWN",
                            getCalendarResponse?.message ?: "조회 실패"
                        )
                    }
                } else {
                    // 실패 응답 처리
                    val errorMsg = response.errorBody()?.string()
                    Log.d("CALENDAR/ERROR_BODY", errorMsg ?: "에러 메시지 없음")

                    try {
                        if (!errorMsg.isNullOrEmpty()) {
                            val errorResponse = gson.fromJson(errorMsg, CalendarResponse::class.java)
                            calendarView.onGetCalendarFailure(errorResponse.code, errorResponse.message)
                        } else {
                            calendarView.onGetCalendarFailure("EMPTY_ERROR_BODY", "응답 본문이 없습니다.")
                        }
                    } catch (e: Exception) {
                        Log.e("CALENDAR/PARSE_ERROR", "JSON 파싱 실패: ${e.localizedMessage}")
                        calendarView.onGetCalendarFailure("PARSE_ERROR", "응답 파싱에 실패했습니다.")
                    }
                }
            }

            override fun onFailure(call: Call<CalendarResponse>, t: Throwable) {
                Log.d("CALENDAR/FAILURE", t.message.toString())
                calendarView.onGetCalendarFailure("NETWORK_ERROR")
            }
        })
    }
}