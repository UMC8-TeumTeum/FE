package com.example.teumteum.data.remote.calendar.repository

import android.util.Log
import com.example.teumteum.data.remote.calendar.model.GetCalendarResponse
import com.example.teumteum.data.remote.calendar.service.CalendarService
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CalendarRepository @Inject constructor(
    private val calendarService: CalendarService
){

    suspend fun getCalendar(startDate: String, endDate: String): Result<GetCalendarResponse> {
        return try {
            val response = calendarService.getCalendar(startDate, endDate)
            Log.d("CalendarGet", "response = ${response.body()}")

            if (response.isSuccessful) {
                val apiResponse = response.body()
                    ?: return Result.failure(Exception("서버 응답이 비어 있습니다."))

                if (apiResponse.isSuccess && apiResponse.result != null) {
                    Result.success(apiResponse.result)
                } else {
                    Result.failure(Exception(apiResponse.message))
                }
            } else {
                Result.failure(Exception("서버 오류 발생"))
            }
        } catch (e: IOException) {
            Result.failure(Exception("네트워크 연결에 실패했습니다. 인터넷을 확인하세요."))
        } catch (e: Exception) {
            Result.failure(Exception("알 수 없는 오류 발생: ${e.localizedMessage}"))
        }
    }
}