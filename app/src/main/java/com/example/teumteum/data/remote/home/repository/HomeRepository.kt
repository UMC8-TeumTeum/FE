package com.example.teumteum.data.remote.home.repository

import android.util.Log
import com.example.teumteum.data.remote.home.model.GetCalendarResponse
import com.example.teumteum.data.remote.home.model.ScheduleResult
import com.example.teumteum.data.remote.home.model.TeumTimeResponse
import com.example.teumteum.data.remote.home.service.HomeService
import com.example.teumteum.data.remote.todo.model.GetTodoResult
import com.example.teumteum.utils.handleApiResponse
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HomeRepository @Inject constructor(
    private val homeService: HomeService
){
    suspend fun getTodaySchedule(date: String): Result<List<ScheduleResult>> = runCatching {
        val response = homeService.getTodaySchedule(date)
        Log.d("HomeSchedule", "response = ${response.body()}")
        handleApiResponse(response)
    }

    suspend fun getTeumTime(): Result<TeumTimeResponse> = runCatching {
        val response = homeService.getTeumTime()
        Log.d("HomeSchedule", "response = ${response.body()}")
        handleApiResponse(response)
    }

    suspend fun getCalendar(startDate: String, endDate: String): Result<GetCalendarResponse> {
        return try {
            val response = homeService.getCalendar(startDate, endDate)
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
